use hyper::header::{
    ACCESS_CONTROL_ALLOW_ORIGIN, CONNECTION, CONTENT_LENGTH, CONTENT_TYPE, HeaderValue,
};
/// This module handles basic response format.
use hyper::{Response, StatusCode};
use serde::{Deserialize, Serialize};
use serde_json::json;

use crate::core::context::AppContext;
use crate::core::http::AppResponse;

pub mod handlers;
pub mod router;

/// Add common headers to the response.
/// Includes CORS headers.
pub fn add_common_headers(res: &mut AppResponse, ctx: &'static AppContext) {
    let headers = res.headers_mut();
    headers.insert(
        ACCESS_CONTROL_ALLOW_ORIGIN,
        ctx.config.http.allow_origin.parse().unwrap(),
    );
    headers.insert(
        CONNECTION,
        HeaderValue::from_str("close").expect("connection close header failed to create"),
    );
}

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
            .header(CONNECTION, "close")
            .header(ACCESS_CONTROL_ALLOW_ORIGIN, &ctx.config.http.allow_origin)
            .body(body.into())
            .map_err(|e| {
                tracing::error!("unexpected response build failure: {e}");
                anyhow::Error::msg("failed to create a response")
            })
    }
}

lazy_static::lazy_static! {
    static ref MSG_404: String = {
        json!({"status": "failure", "payload": "resource not found"}).to_string()
    };
    static ref MSG_500: String = {
        json!({"status": "failure", "payload": "internal_server_error"}).to_string()
    };

    // 500 Internal Server Error
    pub static ref INTERNAL_SERVER_ERROR_RESPONSE: AppResponse = {
          Response::builder()
         .header(CONTENT_TYPE, "application/json")
         .status(StatusCode::INTERNAL_SERVER_ERROR)
         .header(CONTENT_LENGTH, MSG_500.len())
         .header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
         .body(MSG_500.clone().into()).unwrap()
    };

    // 404 Not Found
    pub static ref NOT_FOUND_RESPONSE: AppResponse = {
        Response::builder()
        .header(CONTENT_TYPE, "application/json")
        .status(StatusCode::NOT_FOUND)
        .header(CONTENT_LENGTH, MSG_404.len())
        .header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
        .body(MSG_404.clone().into()).unwrap()
    };
}
