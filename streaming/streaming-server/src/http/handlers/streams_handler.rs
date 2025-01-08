//! This module is responsible for GET endpoint for the available devices the user can view stream from.

use crate::core::context::AppContext;
use crate::core::http::{AppRequest, AppResponse};
use crate::http::JSONAppResponse;
use crate::services::app_db::RedisDeviceSchema;
use hyper::StatusCode;
use serde::Serialize;

#[derive(Debug, Serialize)]
pub struct StreamsResponse {
    count: usize,
    available: Vec<RedisDeviceSchema>,
}

pub async fn streams_handler(
    _req: AppRequest,
    ctx: &'static AppContext,
) -> anyhow::Result<AppResponse> {
    let mut conn = ctx.app_db.get().await?;
    let devices = RedisDeviceSchema::find_by_user(&mut conn, 1).await?;

    let payload = StreamsResponse {
        count: devices.len(),
        available: devices,
    };

    tracing::debug!("fetched and returned devices: {:?}", payload.available);

    JSONAppResponse::pack(payload, StatusCode::OK)
}
