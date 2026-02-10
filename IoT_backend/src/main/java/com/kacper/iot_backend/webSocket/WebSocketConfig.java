package com.kacper.iot_backend.webSocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = Logger.getLogger(WebSocketConfig.class.getName());
    private static final String TOKEN_PARAM = "token";
    private static final String USER_KEY_ATTR = "userKey";

    private final JwtVerifier jwtVerifier;

    public WebSocketConfig(JwtVerifier jwtVerifier) {
        this.jwtVerifier = jwtVerifier;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("https://your-frontend.com")
                .addInterceptors(new JwtHandshakeInterceptor(jwtVerifier))
                .setHandshakeHandler(new JwtHandshakeHandler())
                .withSockJS();

        // Additional endpoint without SockJS for native WebSocket clients
        registry.addEndpoint("/ws")
                .setAllowedOrigins("https://your-frontend.com")
                .addInterceptors(new JwtHandshakeInterceptor(jwtVerifier))
                .setHandshakeHandler(new JwtHandshakeHandler());
    }

    // ========================
    // JwtVerifier Interface
    // ========================

    /**
     * Interface for JWT verification.
     * Implement this interface with your existing JWTService.
     *
     * Example implementation:
     * <pre>
     * {@code
     * @Component
     * public class JwtVerifierImpl implements JwtVerifier {
     *     private final JWTService jwtService;
     *
     *     public JwtVerifierImpl(JWTService jwtService) {
     *         this.jwtService = jwtService;
     *     }
     *
     *     @Override
     *     public Optional<String> validateAndExtractUsername(String token) {
     *         try {
     *             if (jwtService.isTokenExpired(token)) {
     *                 return Optional.empty();
     *             }
     *             String username = jwtService.extractUsername(token);
     *             return Optional.ofNullable(username);
     *         } catch (Exception e) {
     *             return Optional.empty();
     *         }
     *     }
     * }
     * }
     * </pre>
     */
    public interface JwtVerifier {
        /**
         * Validates the JWT token and extracts the username/userId.
         *
         * @param token the JWT token to validate
         * @return Optional containing username/userId if valid, empty if invalid
         */
        Optional<String> validateAndExtractUsername(String token);
    }

    // ========================
    // Custom HandshakeInterceptor
    // ========================

    /**
     * Intercepts WebSocket handshake to validate JWT token from query parameter.
     * Stores extracted user identity in session attributes for later use.
     */
    private static class JwtHandshakeInterceptor implements HandshakeInterceptor {

        private final JwtVerifier jwtVerifier;

        public JwtHandshakeInterceptor(JwtVerifier jwtVerifier) {
            this.jwtVerifier = jwtVerifier;
        }

        @Override
        public boolean beforeHandshake(
                ServerHttpRequest request,
                ServerHttpResponse response,
                WebSocketHandler wsHandler,
                Map<String, Object> attributes
        ) {
            String token = extractTokenFromRequest(request);

            if (token == null || token.isBlank()) {
                logger.warning("WebSocket handshake rejected: Missing token");
                return false;
            }

            Optional<String> usernameOpt = jwtVerifier.validateAndExtractUsername(token);

            if (usernameOpt.isEmpty()) {
                logger.warning("WebSocket handshake rejected: Invalid or expired token");
                return false;
            }

            String userKey = usernameOpt.get();
            attributes.put(USER_KEY_ATTR, userKey);
            logger.info("WebSocket handshake successful for user: " + userKey);

            return true;
        }

        @Override
        public void afterHandshake(
                ServerHttpRequest request,
                ServerHttpResponse response,
                WebSocketHandler wsHandler,
                Exception exception
        ) {
            // No post-handshake processing needed
        }

        private String extractTokenFromRequest(ServerHttpRequest request) {
            if (request instanceof ServletServerHttpRequest servletRequest) {
                return servletRequest.getServletRequest().getParameter(TOKEN_PARAM);
            }

            // Fallback: parse from URI query string
            String query = request.getURI().getQuery();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] keyValue = param.split("=", 2);
                    if (keyValue.length == 2 && TOKEN_PARAM.equals(keyValue[0])) {
                        return keyValue[1];
                    }
                }
            }
            return null;
        }
    }

    // ========================
    // Custom HandshakeHandler
    // ========================

    /**
     * Custom handshake handler that creates a Principal from the validated JWT identity.
     * This enables user-specific messaging via convertAndSendToUser().
     */
    private static class JwtHandshakeHandler extends DefaultHandshakeHandler {

        @Override
        protected Principal determineUser(
                ServerHttpRequest request,
                WebSocketHandler wsHandler,
                Map<String, Object> attributes
        ) {
            String userKey = (String) attributes.get(USER_KEY_ATTR);

            if (userKey == null) {
                Logger.getLogger(JwtHandshakeHandler.class.getName())
                        .warning("No user key found in handshake attributes");
                return null;
            }

            return new StompPrincipal(userKey);
        }
    }

    // ========================
    // StompPrincipal Implementation
    // ========================

    /**
     * Simple Principal implementation for STOMP WebSocket sessions.
     * The name is used as the user identifier for message routing.
     */
    private static class StompPrincipal implements Principal {

        private final String name;

        public StompPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "StompPrincipal{name='" + name + "'}";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StompPrincipal that = (StompPrincipal) o;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
