//! This module is responsible for GET endpoint for the available devices the user can view stream from.

use crate::core::context::AppContext;
use crate::core::http::{AppRequest, AppResponse};
use crate::http::JSONAppResponse;
use crate::services::app_db::RedisDeviceSchema;
use crate::services::core_db::find_user_id;
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
        .and_then(|raw_header| crate::services::jwt::parse_authorization_header(raw_header))
    else {
        return JSONAppResponse::pack(
            ctx,
            "bad token or missing Authorization header",
            StatusCode::UNAUTHORIZED,
        );
    };
    let Ok(claims) =
        verify_user(token).inspect_err(|e| tracing::debug!("failed token verification: {e}"))
    else {
        return JSONAppResponse::pack(ctx, "bad token", StatusCode::FORBIDDEN);
    };
    if claims.is_expired() {
        return JSONAppResponse::pack(ctx, "expired token", StatusCode::FORBIDDEN);
    };
    tracing::debug!("stream request from: {}", claims.sub);
    let Some(user_id) = find_user_id(ctx, &claims.sub).await? else {
        return JSONAppResponse::pack(
            ctx,
            format!("no such user exists: {}", claims.sub),
            StatusCode::BAD_REQUEST,
        );
    };
    let devices = RedisDeviceSchema::find_by_user(&mut conn, user_id).await?;

    let payload = StreamsResponse {
        count: devices.len(),
        available: devices,
    };

    tracing::debug!("fetched and returned devices: {:?}", payload.available);

    JSONAppResponse::pack(ctx, payload, StatusCode::OK)
}
