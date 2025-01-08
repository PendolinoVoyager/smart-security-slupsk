//! This module is responsible for GET endpoint for the available devices the user can view stream from.

use crate::core::context::AppContext;
use crate::core::http::{AppRequest, AppResponse};
use crate::http::JSONAppResponse;
use crate::services::app_db::RedisDeviceSchema;
use crate::services::jwt::verify_user;
use hyper::StatusCode;
use hyper::header::AUTHORIZATION;
use serde::Serialize;

#[derive(Debug, Serialize)]
pub struct StreamsResponse {
    count: usize,
    available: Vec<RedisDeviceSchema>,
}

pub async fn streams_handler(
    req: AppRequest,
    ctx: &'static AppContext,
) -> anyhow::Result<AppResponse> {
    let mut conn = ctx.app_db.get().await?;

    let Some(token) = req
        .headers()
        .get(AUTHORIZATION)
        .and_then(|hv| hv.to_str().ok())
    else {
        return JSONAppResponse::pack(
            "bad token or missing Authorization header",
            StatusCode::UNAUTHORIZED,
        );
    };
    let Ok(claims) = verify_user(token) else {
        return JSONAppResponse::pack("bad token", StatusCode::FORBIDDEN);
    };
    if claims.is_expired() {
        return JSONAppResponse::pack("expired token", StatusCode::FORBIDDEN);
    };

    let devices = RedisDeviceSchema::find_by_user(&mut conn, &claims.sub).await?;

    let payload = StreamsResponse {
        count: devices.len(),
        available: devices,
    };

    tracing::debug!("fetched and returned devices: {:?}", payload.available);

    JSONAppResponse::pack(payload, StatusCode::OK)
}
