//package com.kacper.iot_backend.auth;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.kacper.iot_backend.config.SecurityConfig;
//import com.kacper.iot_backend.jwt.JWTService;
//import com.kacper.iot_backend.user.CustomUserDetailsService;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.http.MediaType;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@Import(SecurityConfig.class)
//@WebMvcTest(AuthController.class)
//class AuthControllerTest
//{
//    private static final Logger LOGGER = LoggerFactory.getLogger(AuthControllerTest.class);
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private AuthService authService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private JWTService jwtService;
//
//    @MockBean
//    private CustomUserDetailsService customUserDetailsService;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Test
//    void shouldRegisterUser() throws Exception {
//        LOGGER.info("Started test shouldRegisterUser");
//        AuthRegistrationRequest request = new AuthRegistrationRequest(
//                "John",
//                "Doe",
//                "john.doe@example.com",
//                passwordEncoder.encode("password")
//        );
//
//        LOGGER.debug("Mocked Request: {}", request);
//
//        AuthRegistrationResponse response = new AuthRegistrationResponse(
//                "john.doe@example.com",
//                "John",
//                "Doe",
//                "USER"
//        );
//
//        LOGGER.debug("Mocked Response: {}", response);
//
//        when(authService.register(any(AuthRegistrationRequest.class))).thenReturn(response);
//
//        LOGGER.info("Starting API test /api/v1/auth/register");
//        mockMvc.perform(post("/api/v1/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.name").value("John"))
//                .andExpect(jsonPath("$.lastName").value("Doe"))
//                .andExpect(jsonPath("$.role").value("USER"))
//                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
//
//
//        Mockito.verify(authService, Mockito.times(1)).register(any(AuthRegistrationRequest.class));
//        LOGGER.info("Finished test shouldRegisterUser");
//    }
//
//    @Test
//    void shouldReturnBadRequestForShortPassword() throws Exception {
//        LOGGER.info("Started test shouldReturnBadRequestForShortPassword");
//
//        AuthRegistrationRequest request = new AuthRegistrationRequest(
//                "John",
//                "Doe",
//                "john.doe@example.com",
//                "123"
//        );
//
//        LOGGER.debug("Mocked Request with short password: {}", request);
//
//        when(authService.register(any(AuthRegistrationRequest.class))).thenThrow(new IllegalArgumentException("Password is too short"));
//
//        LOGGER.info("Starting API test /api/v1/auth/register with short password");
//        mockMvc.perform(post("/api/v1/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//
//        LOGGER.info("Finished test shouldReturnBadRequestForShortPassword");
//    }
//
//    @Test
//    void shouldReturnBadRequestForNullFields() throws Exception {
//        LOGGER.info("Started test shouldReturnBadRequestForNullFields");
//
//        AuthRegistrationRequest request = new AuthRegistrationRequest(
//                null,
//                null,
//                null,
//                null
//        );
//
//        LOGGER.debug("Mocked Request with null fields: {}", request);
//
//        LOGGER.info("Starting API test /api/v1/auth/register with null fields");
//        mockMvc.perform(post("/api/v1/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//
//        LOGGER.info("Finished test shouldReturnBadRequestForNullFields");
//    }
//
//    @Test
//    void shouldReturnBadRequestForInvalidEmail() throws Exception {
//        LOGGER.info("Started test shouldReturnBadRequestForInvalidEmail");
//
//        AuthRegistrationRequest request = new AuthRegistrationRequest(
//                "John",
//                "Doe",
//                "invalid-email",
//                "Password.123"
//        );
//
//        LOGGER.debug("Mocked Request with invalid email: {}", request);
//
//        LOGGER.info("Starting API test /api/v1/auth/register with invalid email");
//        mockMvc.perform(post("/api/v1/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//
//        LOGGER.info("Finished test shouldReturnBadRequestForInvalidEmail");
//    }
//
//    @Test
//    void shouldLoginUser() throws Exception {
//        LOGGER.info("Started test shouldLoginUser");
//
//        AuthLoginRequest request = new AuthLoginRequest(
//                "john.doe@example.com",
//                "Password.123"
//        );
//
//        LOGGER.debug("Mocked AuthLoginRequest: {}", request);
//
//        AuthLoginResponse response = AuthLoginResponse.builder()
//                .token("mocked-jwt-token")
//                .email("john.doe@example.com")
//                .role("USER")
//                .build();
//
//        LOGGER.debug("Mocked AuthLoginResponse: {}", response);
//
//        when(authService.login(any(AuthLoginRequest.class))).thenReturn(response);
//
//        LOGGER.info("Starting API test /api/v1/auth/login");
//        mockMvc.perform(post("/api/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
//
//        Mockito.verify(authService, Mockito.times(1)).login(any(AuthLoginRequest.class));
//        LOGGER.info("Finished test shouldLoginUser");
//    }
//
//    @Test
//    void shouldReturnBadRequestForNullLoginFields() throws Exception {
//        LOGGER.info("Started test shouldReturnBadRequestForNullLoginFields");
//
//        AuthLoginRequest request = new AuthLoginRequest(
//                null,
//                null
//        );
//
//        LOGGER.debug("Mocked AuthLoginRequest with null fields: {}", request);
//
//        LOGGER.info("Starting API test /api/v1/auth/login with null fields");
//        mockMvc.perform(post("/api/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//
//        LOGGER.info("Finished test shouldReturnBadRequestForNullLoginFields");
//    }
//
//    @Test
//    void shouldReturnBadRequestForNotValidEmail() throws Exception {
//        LOGGER.info("Started test shouldReturnBadRequestForNotValidEmail");
//        AuthLoginRequest request = new AuthLoginRequest(
//                "invalid-mail",
//                "Password.123"
//        );
//
//        LOGGER.debug("Mocked AuthLoginRequest with invalid-mail field: {}", request);
//        mockMvc.perform(post("/api/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//
//        LOGGER.info("Finished test shouldReturnBadRequestForNotValidEmail");
//    }
//
//
//
//
//}
