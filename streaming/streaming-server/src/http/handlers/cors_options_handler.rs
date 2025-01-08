//! This handler is responsible for handling preflight CORS requests
//! In short, any options request like this:
//! OPTIONS /resource/foo
//! Access-Control-Request-Method: DELETE
//! Access-Control-Request-Headers: x-requested-with
//! Origin: https://foo.bar.org
//!
//! Will be responded with
//! HTTP/1.1 204 No Content
//! Connection: keep-alive
//! Access-Control-Allow-Origin: https://foo.bar.org
//! Access-Control-Allow-Methods: POST, GET, OPTIONS, DELETE
//! Access-Control-Allow-Headers: X-Requested-With
//! Access-Control-Max-Age: 86400

use http_body_util::Full;
use hyper::StatusCode;
use hyper::body::Bytes;
use hyper::header::{
    ACCESS_CONTROL_ALLOW_HEADERS, ACCESS_CONTROL_ALLOW_METHODS, ACCESS_CONTROL_ALLOW_ORIGIN,
    ACCESS_CONTROL_MAX_AGE, CONNECTION,
};

use crate::core::context::AppContext;
use crate::core::http::{AppRequest, AppResponse};
pub async fn cors_options_handler(
    _req: AppRequest,
    _ctx: &'static AppContext,
) -> anyhow::Result<AppResponse> {
    hyper::Response::builder()
        .status(StatusCode::NO_CONTENT)
        .header(CONNECTION, "close")
        .header(ACCESS_CONTROL_ALLOW_METHODS, "POST, GET, OPTIONS, DELETE")
        .header(ACCESS_CONTROL_ALLOW_ORIGIN, "*")
        .header(
            ACCESS_CONTROL_ALLOW_HEADERS,
            "X-Requested-With, Authorization",
        )
        .header(ACCESS_CONTROL_MAX_AGE, 86400)
        .body(Full::new(Bytes::new()))
        .map_err(|e| {
            tracing::error!("unexpected response build failure: {e}");
            anyhow::Error::msg("failed to create a response")
        })
}
