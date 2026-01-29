use hyper::StatusCode;
use serde_json::json;

use crate::core::context::AppContext;
use crate::core::http::{AppRequest, AppResponse};
use crate::http::{JSONAppResponse};
use crate::services::ip_utils;


pub async fn list_all_devices_handler(
    req: AppRequest,
    ctx: &'static AppContext,
) -> anyhow::Result<AppResponse> {
    if ip_utils::filter_non_service_ips(&ctx.config, &req) == false {
        return JSONAppResponse::pack(ctx, "Forbidden IP address.", StatusCode::FORBIDDEN);
    }


    let devices = ctx.devices.lock().await.all_devices();
    
    let body = json!({
        "count": devices.len(),
        "devices": devices
    });

    JSONAppResponse::pack(ctx, body, StatusCode::OK)
}
