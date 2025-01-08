use hyper::header::{ACCESS_CONTROL_ALLOW_ORIGIN, CONTENT_LENGTH, CONTENT_TYPE};
/// This module handles basic response format.
use hyper::{Response, StatusCode};
use serde::{Deserialize, Serialize};

use crate::core::http::AppResponse;

pub mod handlers;
pub mod router;

/// The one and only JSON API response format for the app. All responses should be packed with it, so the API is consistent.
/// The format looks as follows for now:
/// ``` ignore
/// {
///     "status": "error|success",
///     "payload": {...}
/// }
/// ```
///
#[derive(Debug, Serialize, Deserialize, Clone)]
struct JSONAppResponse<T: Serialize> {
    status: String,
    payload: T,
}
impl<T: Serialize> JSONAppResponse<T> {
    /// Pack the resulting response into the app-wide standarized format.
    pub fn pack(payload: T, status: StatusCode) -> anyhow::Result<AppResponse> {
        let body = Self {
            status: if status.is_success() {
                "success".into()
            } else {
                "error".into()
            },
            payload,
        };
        let body = serde_json::to_vec(&body)?;
        Response::builder()
            .header(CONTENT_TYPE, "application/json")
            .status(status)
            .header(CONTENT_LENGTH, body.len())
            .header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
            .body(body.into())
            .map_err(|e| {
                tracing::error!("unexpected response build failure: {e}");
                anyhow::Error::msg("failed to create a response")
            })
    }
}

lazy_static::lazy_static! {
    // 500 Internal Server Error
    pub static ref INTERNAL_SERVER_ERROR_RESPONSE: AppResponse = {
         JSONAppResponse::pack("Internal server error", StatusCode::INTERNAL_SERVER_ERROR).unwrap()
    };

    // 404 Not Found
    pub static ref NOT_FOUND_RESPONSE: AppResponse = {
        JSONAppResponse::pack("Resource not found", StatusCode::NOT_FOUND).unwrap()

    };
}
