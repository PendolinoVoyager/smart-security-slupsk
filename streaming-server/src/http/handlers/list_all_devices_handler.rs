use hyper::StatusCode;
use serde_json::json;

use crate::core::context::AppContext;
use crate::core::http::{AppRequest, AppResponse};
use crate::http::{JSONAppResponse};


pub async fn list_all_devices_handler(
    _req: AppRequest,
    ctx: &'static AppContext,
) -> anyhow::Result<AppResponse> {
    let devices = ctx.devices.lock().await.all_devices();
    
    let body = json!({
        "count": devices.len(),
        "devices": devices
    });

    JSONAppResponse::pack(ctx, body, StatusCode::OK)
}
