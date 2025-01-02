//! This module is responsible for GET endpoint for the available devices the user can view stream from.

use crate::core::context::AppContext;
use crate::core::http::{AppRequest, AppResponse};
use crate::http::JSONAppResponse;
use hyper::StatusCode;
use serde::Serialize;
use sqlx::postgres::PgRow;
use sqlx::{Execute, Row};

#[derive(Debug, Serialize)]
pub struct StreamsResponseDevice {
    id: i32,
    device_name: String,
}

impl StreamsResponseDevice {
    fn from_row(row: PgRow) -> anyhow::Result<Self> {
        Ok(Self {
            id: row.try_get("id")?,
            device_name: row.try_get("device_name")?,
        })
    }
}

#[derive(Debug, Serialize)]
pub struct StreamsResponse {
    count: usize,
    available: Vec<StreamsResponseDevice>,
}

pub async fn streams_handler(
    _req: AppRequest,
    ctx: &'static AppContext,
) -> anyhow::Result<AppResponse> {
    let user_id = 1;
    tracing::debug!("USER ID PLACEHOLDER USED!");

    let query = sqlx::query("SELECT id, device_name FROM devices WHERE user_id = $1").bind(user_id);
    tracing::debug!("executed query {}", query.sql());
    let devices = query.fetch_all(&ctx.core_db).await?;

    let available_devices: Vec<StreamsResponseDevice> = devices
        .into_iter()
        .filter_map(|row| StreamsResponseDevice::from_row(row).ok())
        .collect();

    let payload = StreamsResponse {
        count: available_devices.len(),
        available: available_devices,
    };

    tracing::debug!("fetched and returned devices: {:?}", payload.available);

    JSONAppResponse::pack(payload, StatusCode::OK)
}
