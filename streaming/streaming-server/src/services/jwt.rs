//! JWT Service Module
//!
//! This module provides functionality for handling JWT tokens, including initialization, verification, and custom user claims.
//!
//! # Overview
//! This module facilitates JWT operations by:
//! - Initializing decoding keys and validation rules for verifying JWT tokens.
//! - Defining the structure of user claims in the `UserJWTClaims` struct.
//! - Verifying JWT tokens for authenticity and validity.
//!
//! # Initialization
//! Before using any token-related functionality (e.g., [`verify_user`]), the JWT service must be initialized:
//! - Use [`init`] to load the RSA public key from the default path (`cfg/jwt_pub_key.pem`).
//! - Use [`init_with_key`] to load a custom RSA public key.
//!
//! # Important Warnings
//! - **Thread Safety**: This module makes use of `static mut` and unsafe operations to manage global decoding keys and validation rules. These are not thread-safe and can lead to undefined behavior if accessed concurrently.
//! - **Initialization Requirement**: The decoding key and validation configuration must be properly initialized before calling functions like [`verify_user`]. Failing to do so will result in definite segmentation fault.
//! - **Memory Safety**: The `Box::leak` function is used to ensure the decoding key and validation configurations persist in memory. However, this also means the memory will not be cleaned up until the program exits.
//!
//! # Usage Example
//! ```
//! use anyhow::Result;
//!
//! fn main() -> Result<()> {
//!     // Initialize with the default key
//!     jwt_service::init()?;
//!
//!     // Verify a JWT token
//!     let token = "example.jwt.token";
//!     let claims = jwt_service::verify_user(token)?;
//!
//!     println!("Token belongs to user identified with email: {}", claims.sub);
//!     Ok(())
//! }
//! ```
//!
//! # Module Details
//! - [`UserJWTClaims`]: Represents the claims structure expected in user JWTs.
//! - [`verify_user`]: Verifies the authenticity and validity of a given JWT token.

use std::ptr::null_mut;

use chrono::Utc;
use jsonwebtoken::DecodingKey;

static mut DECODING_KEY: *mut DecodingKey = null_mut();
static mut VALIDATION: *mut jsonwebtoken::Validation = null_mut();

static DEFAULT_PEM_KEY_PATH: &str = "./cfg/jwt_pub_key.pem";
/// Initializ the JWT service with default key from cfg/jwt_pub_key.pem
pub fn init() -> anyhow::Result<()> {
    let key = std::fs::read(DEFAULT_PEM_KEY_PATH)?;
    _init(&key)
}
fn _init(key: &[u8]) -> anyhow::Result<()> {
    unsafe {
        DECODING_KEY = Box::leak(Box::new(DecodingKey::from_rsa_pem(key)?));
        VALIDATION = Box::leak(Box::new(jsonwebtoken::Validation::new(
            jsonwebtoken::Algorithm::RS256,
        )));
    }
    Ok(())
}

#[derive(serde::Serialize, serde::Deserialize, Debug, Clone)]
pub struct UserJWTClaims {
    // Seconds since epoch
    pub iat: i64,
    /// Seconds since epoch
    pub exp: i64,
    /// Subject - user email
    pub sub: String,
}
impl UserJWTClaims {
    pub fn is_expired(&self) -> bool {
        let date = chrono::DateTime::from_timestamp(self.exp, 0);
        date.is_none_or(|t| t <= Utc::now())
    }
}

pub fn verify_user(token: &str) -> anyhow::Result<UserJWTClaims> {
    unsafe {
        Ok(jsonwebtoken::decode(
            token,
            DECODING_KEY.as_ref_unchecked(),
            VALIDATION.as_ref_unchecked(),
        )?
        .claims)
    }
}

#[cfg(test)]
mod tests {
    /// Using RS256 for hashing
    /// Test token signed with the corresponding private key to the test pub key
    const TEST_TOKEN: &str = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTczNjE2MDUwNCwiZXhwIjoxNzM3MDI0NTA0fQ.P3AgkyPVRZwg3Mb1EaLgPBTDG2DYEV2gTu8OqcYevJLep5edobRSNqqEFdPfgAQvOOWTx_Wp3pOlp3ZTkdybTJmmFzUx7Drh5LGOYjXc6yVVQwKJzjFdn9fIvZ5Apna0cdbEh95AxXCRYUpVFZ7Z-lN2u2wOl993-f7VBUa5bjdoWuFkkHzFI4PFFee3JuU-mDh9EBAQx-sSad8TRs2GNYy9DodlIgRo_OvE-jQcg_V3LR2cxwvzI1IsU2t20yw5Pa3Qtt6S_koz58d4vkCcQO3Y2rt3lJ2kYScUIFyMpqG-mIkGxGTlVPuIZ_5HLSyL_E3WBVzYnTr5oPdET5Vdzg";
    /// Data:
    ///  { iat: 1736160504, exp: 1737024504, sub: "admin@example.com" }
    const TEST_PUB_KEY: &[u8] = b"-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5H1dTX+toN8mi+Ooo2jq
ZWZ6Moww8W66Iw42+vr7yx5TMgSQVur1zMD+0pUIYTTlZM1hn556uYK//6z5rtIy
YM+UjdxonpRnfk5yhgOdCchoISu/Gv1y8atbT8umgiz6VwBVFMLBJO9FZFwIWrlD
b/Gmg55tpH/ZyAk0COqbehOaZB44W/EY+avpGgFvec8JEcAfpn2sYCAOF9vRweqw
l6URC7dJ0v0mXUsS1yIH+EOtzbqNZbJUTdHu2A4DrW9RAgv2JuwsECpumvV8gwbd
oYP62/t7vSo5okWnowl++WAzo0EA5xnp/1/pJLk5AeZJmyqrLqCDXJrK2SnBr8M9
OQIDAQAB
-----END PUBLIC KEY-----
";
    const TEST_SUB: &str = "admin@example.com";
    const TEST_IAT: i64 = 1736160504;
    const TEST_EXP: i64 = 1737024504;

    #[test]
    fn test_token_verify_user() {
        super::_init(TEST_PUB_KEY).unwrap();
        let res = super::verify_user(TEST_TOKEN).unwrap();

        assert_eq!(res.exp, TEST_EXP);
        assert_eq!(res.iat, TEST_IAT);
        assert_eq!(res.sub, TEST_SUB);
    }
    #[test]
    fn test_token_expiry() {
        super::_init(TEST_PUB_KEY).unwrap();

        let claims = super::verify_user(TEST_TOKEN).unwrap();
        assert!(!claims.is_expired())
    }
}