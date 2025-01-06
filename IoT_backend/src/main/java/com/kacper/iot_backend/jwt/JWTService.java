package com.kacper.iot_backend.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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
import java.util.HashMap;
import java.util.function.Function;
import java.util.logging.Logger;

@Service
public class JWTService
{

//    private final SecretKey secretKey;
    private final PrivateKey privateKey;
    private static final long EXPIRATION_TIME = 864000000;
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
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(privateKey)
                .compact();
    }

    /**
     * @param claims claims
     * @param userDetails user details
     * @return generated refresh token with claims, subject, issuedAt, expiration and signed with secret key
     */
    public String generateRefreshToken(HashMap<String, Object> claims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() +EXPIRATION_TIME))
                .signWith(privateKey)
                .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsTFunction) {
        return claimsTFunction.apply(Jwts.parser().verifyWith((SecretKey) privateKey).build().parseSignedClaims(token).getPayload());
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
        return extractClaim(token, Claims::getExpiration).before(new Date());
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