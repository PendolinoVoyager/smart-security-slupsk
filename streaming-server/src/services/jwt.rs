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

use jsonwebtoken::DecodingKey;
use std::ptr::null_mut;

static mut DECODING_KEY: *mut DecodingKey = null_mut();
static mut VALIDATION: *mut jsonwebtoken::Validation = null_mut();

static DEFAULT_PEM_KEY_PATH: &str = "./cfg/jwt_pub_key.pem";

/// Initialie the JWT service with default key from cfg/jwt_pub_key.pem
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
    /// Subject - user email
    pub sub: String,
    // Seconds since epoch
    pub iat: i64,
    /// Seconds since epoch
    pub exp: i64,
}

/// Claims for device access tokens
#[derive(serde::Serialize, serde::Deserialize, Debug, Clone)]
pub struct DeviceJWTClaims {
    /// Subject - user email
    pub sub: String,
    /// device owner user id
    pub user_id: i64,
    /// is device - completely redundant by the way
    #[serde(rename = "isDevice")]
    pub is_device: bool,
    /// device uuid from the main database
    #[serde(rename = "deviceUuid")]
    pub device_uuid: String,
    // Seconds since epoch
    pub iat: i64,
    /// Seconds since epoch
    pub exp: i64,
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

pub fn verify_device(token: &str) -> anyhow::Result<DeviceJWTClaims> {
    unsafe {
        Ok(jsonwebtoken::decode(
            token,
            DECODING_KEY.as_ref_unchecked(),
            VALIDATION.as_ref_unchecked(),
        )?
        .claims)
    }
}
/// Trim the bearer part sand return the token.
pub fn parse_authorization_header(auth_header_value: &str) -> Option<&str> {
    auth_header_value.strip_prefix("Bearer ")
}

/// Extract raw JWT token string from the request
pub fn extract_token<T>(req: &hyper::Request<T>) -> Option<&str> {
    req.headers()
        .get(hyper::header::AUTHORIZATION)
        .and_then(|hv| hv.to_str().ok())
        .and_then(|raw_header| crate::services::jwt::parse_authorization_header(raw_header))
}
/// These tests don't work because the jwt crate doesn't decode expired tokens
/// and the test tokens are expired.
/// The tests are here for reference and to help in development.
/// Just generate a new token and change the consts.
#[cfg(test)]
#[allow(unused)]
mod tests {

    /// Using RS256 for hashing
    /// Test token signed with the corresponding private key to the test pub key
    const TEST_TOKEN_USER: &str = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlhdCI6MTczNjM0MDkzMSwiZXhwIjoxNzM3MjA0OTMxfQ.mpKdw5IwOQKIHbWAWKpgQ9eIVmdJdVMxgzGpiyNqqkKBBj7ZcbuwbydSaNLwEcG0LacYF_POt-IE2P5N9WFQUhC_w3TkPA75peZZCsjAV5HB5E7Ef_0_aMfsY1qQtECJR3A2A6i7eLhLWmB-iWMwVNcsN7rbEYA3E4XlIHTEgicqE3bccUTyzHgRkVWburmIbgKut9PrVuCOd6w7i9MDIa5EbtpM-FZVqr3s-I40dCtQKDbu-tzNcHYYLGsxra7QI3VLtXM6WVUTsTLQMiIPXQte2HrPMz46uh5VpBa1O-qcCLQX18oIdywpVwiAAbQ7mPfFePMJDuuYTZa31aaBBw";
    const TEST_TOKEN_DEVICE: &str = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlzRGV2aWNlIjp0cnVlLCJkZXZpY2VVdWlkIjoiRGVmYXVsdF9VVUlEIiwiaWF0IjoxNzM4MDAxOTI2fQ.vG6uiRnPWKnKZLi5mVLZNuFhKq8hUb1r0kqf-qjsazGyyvRdIMEM67yDYtdJ9gehUXtcuaObW6XO0AaiJPkz5Mjk_fdyRPOUvw-6FEjanV4r2jFLh79dSt2dFGcD0IOuEsS5j9WJvR9e_hUB7YbykpF17YgNvPnBwDsErT-P9NrOj9Hp8u3xIvlzs4RJ4ypwYi6rQleQwiXFwpS5qdXg1M8lZM6CJNSNdEzew1ab8Y25F3Ynd_VxoWjfj2kElinWh7H28NP8FJpibHqKo5CbYvSeLwR6tFP6scnGQR-zUZ063D3jA1fk5VLNHaYciaWTdCkHxZug7pxz6lvQFIemCA";

    /// Data:
    ///  { iat: 1736340931, exp: 1737204931, sub: "admin@example.com" }
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
    const TEST_IAT: i64 = 1736340931;
    const TEST_EXP: i64 = 1737204931;

    #[test]
    fn test_token_verify_user() {
        //     super::_init(TEST_PUB_KEY).unwrap();
        //     let res = super::verify_user(TEST_TOKEN_USER).unwrap();

        //     assert_eq!(res.exp, TEST_EXP);
        //     assert_eq!(res.iat, TEST_IAT);
        //     assert_eq!(res.sub, TEST_SUB);
    }
    #[test]
    fn test_token_expiry() {
        super::_init(TEST_PUB_KEY).unwrap();

        let claims = super::verify_user(TEST_TOKEN_USER);
        assert!(claims.is_err_and(|e| {
            e.downcast::<jsonwebtoken::errors::Error>().unwrap().kind()
                == &jsonwebtoken::errors::ErrorKind::ExpiredSignature
        }));
    }
    #[test]
    fn test_token_verify_device() {
        super::_init(TEST_PUB_KEY).unwrap();
        let res = super::verify_device(TEST_TOKEN_DEVICE).unwrap();
        eprintln!("{res:#?}");
    }
}
