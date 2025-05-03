//! This module is responsible for GET endpoint for the available devices the user can view stream from.

use std::collections::HashMap;

use crate::core::context::AppContext;
use crate::core::http::{AppRequest, AppResponse};
use crate::http::JSONAppResponse;
use crate::services::app_db::RedisDeviceSchema;
use crate::services::core_db::{CoreDBId, User};
use crate::services::jwt::{extract_token, verify_user};
use hyper::StatusCode;
use serde::Serialize;

#[derive(Debug, Serialize)]
pub struct StreamsResponse {
    count: usize,
    available: Vec<RedisDeviceSchema>,
}
/// Fetch available devices from the Redis database and return them as a JSON response.
/// Takes token from the request header, verifies it, and then fetches the associated devices.
pub async fn streams_handler(
    req: AppRequest,
    ctx: &'static AppContext,
) -> anyhow::Result<AppResponse> {
    let mut conn = ctx.app_db.get().await?;

    let Some(token) = extract_token(&req) else {
        return JSONAppResponse::pack(
            ctx,
            "missing Authorization: Bearer <token> header",
            StatusCode::UNAUTHORIZED,
        );
    };
    let Ok(claims) = verify_user(ctx, token)
        .await
        .inspect_err(|e| tracing::debug!("failed token verification: {e}"))
    else {
        return JSONAppResponse::pack(ctx, "bad token", StatusCode::FORBIDDEN);
    };

    tracing::debug!("stream request from: {}", claims.sub);
    let Ok(User { id, .. }) = User::find_by_email(ctx, &claims.sub).await else {
        return JSONAppResponse::pack(
            ctx,
            format!("no such user exists: {}", claims.sub),
            StatusCode::BAD_REQUEST,
        );
    };
    let devices = RedisDeviceSchema::find_by_user(&mut conn, id).await?;

    let payload = StreamsResponse {
        count: devices.len(),
        available: devices,
    };

    tracing::debug!("fetched and returned devices: {:?}", payload.available);

    JSONAppResponse::pack(ctx, payload, StatusCode::OK)
}

/// This handler is responsible for the `/streams/availability` endpoint.
/// It checks the availability of a device and returns the result.
pub async fn availability_handler(
    req: AppRequest,
    ctx: &'static AppContext,
) -> anyhow::Result<AppResponse> {
    let Some(token) = extract_token(&req) else {
        return JSONAppResponse::pack(
            ctx,
            "missing Authorization: Bearer <token> header",
            StatusCode::UNAUTHORIZED,
        );
    };

    let Ok(_claims) = verify_user(ctx, token)
        .await
        .inspect_err(|e| tracing::debug!("failed token verification: {e}"))
    else {
        return JSONAppResponse::pack(ctx, "bad token", StatusCode::FORBIDDEN);
    };

    let query_str = req.uri().query().unwrap_or_default();
    let params: HashMap<&str, &str> = query_str
        .split('&')
        .filter_map(|kv_str| kv_str.split_once('='))
        .collect();
    tracing::debug!(params = ?params);
    tracing::debug!("query string: {}", query_str);
    let device_id = match params.get("device_id") {
        Some(id) => match id.parse::<CoreDBId>() {
            Ok(id) => id,
            Err(_) => {
                return JSONAppResponse::pack(
                    ctx,
                    "device_id must be a number",
                    StatusCode::BAD_REQUEST,
                );
            }
        },
        None => {
            return JSONAppResponse::pack(
                ctx,
                "missing device_id parameter",
                StatusCode::BAD_REQUEST,
            );
        }
    };
    let mut con = ctx.app_db.get().await?;
    match RedisDeviceSchema::get_device(&mut con, device_id).await {
        Ok(device) => {
            let payload = StreamsResponse {
                count: 1,
                available: vec![device],
            };
            JSONAppResponse::pack(ctx, payload, StatusCode::OK)
        }
        Err(e) => JSONAppResponse::pack(
            ctx,
            format!("device not found: {}", e),
            StatusCode::NOT_FOUND,
        ),
    }
}
