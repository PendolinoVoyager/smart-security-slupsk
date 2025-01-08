use hyper::header::{ACCESS_CONTROL_ALLOW_ORIGIN, CONTENT_LENGTH, CONTENT_TYPE};
/// This module handles basic response format.
use hyper::{Response, StatusCode};
use serde::{Deserialize, Serialize};
use serde_json::json;

use crate::core::context::AppContext;
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
    pub fn pack(
        ctx: &'static AppContext,
        payload: T,
        status: StatusCode,
    ) -> anyhow::Result<AppResponse> {
        let body = Self {
            status: if status.is_success() {
                "success".into()
            } else {
                "failure".into()
            },
            payload,
        };
        let body = serde_json::to_vec(&body)?;
        Response::builder()
            .header(CONTENT_TYPE, "application/json")
            .status(status)
            .header(CONTENT_LENGTH, body.len())
            .header(ACCESS_CONTROL_ALLOW_ORIGIN, &ctx.config.http.allow_origin)
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
        const ERR_MSG: &str = "internal server error";
          Response::builder()
         .header(CONTENT_TYPE, "application/json")
         .status(StatusCode::INTERNAL_SERVER_ERROR)
         .header(CONTENT_LENGTH, ERR_MSG.len())
         .header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
         .body(json!({"status": "failure", "payload": ERR_MSG}).to_string().into()).unwrap()
    };

    // 404 Not Found
    pub static ref NOT_FOUND_RESPONSE: AppResponse = {
            const ERR_MSG: &str = "resource not found";
              Response::builder()
             .header(CONTENT_TYPE, "application/json")
             .status(StatusCode::INTERNAL_SERVER_ERROR)
             .header(CONTENT_LENGTH, ERR_MSG.len())
             .header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
             .body(json!({"status": "failure", "payload": ERR_MSG}).to_string().into()).unwrap()

    };
}
