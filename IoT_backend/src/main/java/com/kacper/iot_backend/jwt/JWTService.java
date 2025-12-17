package com.kacper.iot_backend.jwt;

import com.kacper.iot_backend.device.Device;
import com.kacper.iot_backend.exception.NotDeviceTokenException;
import com.kacper.iot_backend.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;
import java.util.logging.Logger;

@Service
public class JWTService
{

//    private final SecretKey secretKey;
    private final PrivateKey privateKey;
    private static final long EXPIRATION_TIME = 864000000;
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7 * 2;
    private final static Logger logger = Logger.getLogger(JWTService.class.getName());

    /**
     * keyBytes - is decoded from secretString
     */
    public JWTService(@Value("${app.secretKey}")Resource privateKeyResource) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        logger.info("Creating JWTService " + privateKeyResource.getURI());
        byte[] keyBytes = Files.readAllBytes(Path.of(privateKeyResource.getURI()));
        keyBytes = parsePEMFile(keyBytes);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        privateKey = keyFactory.generatePrivate(spec);
    }

    private byte[] parsePEMFile(byte[] keyBytes) {
        String keyString = new String(keyBytes, StandardCharsets.UTF_8);
        keyString = keyString.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        return Base64.getDecoder().decode(keyString);
    }

    /**
     * @param userDetails user details
     * @return generated token with subject, issuedAt, expiration and signed with secret key
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(privateKey)
                .compact();
    }

    public String generateDeviceAccessToken(User deviceOwner, Device device) {
        return Jwts.builder()
                .setSubject(deviceOwner.getUsername())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .claim("user_id", deviceOwner.getId())
                .claim("isDevice", true)
                .claim("deviceUuid", device.getUuid())
                .claim("deviceId", device.getId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(privateKey)
                .compact();
    }

    public boolean isDeviceToken(String token) {
        var claims = extractAllClaims(token);
        return claims.get("isDevice", Boolean.class) != null;
    }

    /**
     * @param userDetails user details
     * @return generated refresh token with claims, subject, issuedAt, expiration and signed with secret key
     */
    public String generateDeviceRefreshToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .claim("isRefresh", true)
                .signWith(privateKey)
                .compact();
    }

    public boolean isRefreshTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);

            Boolean isRefreshToken = claims.get("isRefresh", Boolean.class);
            if (isRefreshToken == null || !isRefreshToken) {
                logger.info("\n\n isRefreshToken is null or false\n\n");
                return false;
            }

            return !isTokenExpired(token);
        } catch (Exception e) {
            logger.info("\n\n isRefreshTokenValid exception\n\n");
            return false;
        }
    }


    private <T> T extractClaim(String token, Function<Claims, T> claimsTFunction) {
        logger.info("\nExtracting claim from token: " + token);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(privateKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claimsTFunction.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(privateKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * @param token token
     * @return extracted username from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * @param token token
     * @return extracted expiration date from token
     */
    public boolean isTokenExpired(String token) {
        Claims claims = extractAllClaims(token);

        // Device token is now permanent so... you know... I had to add this
        Boolean isDeviceToken = claims.get("isDevice", Boolean.class);
        if (Boolean.TRUE.equals(isDeviceToken)) {
            return false;
        }

        return claims.getExpiration().before(new Date());
    }

    public String extractDeviceUUID(String token) {
        Claims claims = extractAllClaims(token);

        Boolean isDevice = claims.get("isDevice", Boolean.class);
        if (isDevice == null || !isDevice) {
            throw new NotDeviceTokenException("Token is not a device token");
        }

        String deviceUuid = claims.get("deviceUuid", String.class);
        if (deviceUuid == null) {
            throw new NotDeviceTokenException("Device UUID is missing in the token");
        }

        return deviceUuid;
    }

    /**
     * @param token token
     * @param userDetails user details
     * @return true if token is valid, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
}