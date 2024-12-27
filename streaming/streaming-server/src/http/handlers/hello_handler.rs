use hyper::StatusCode;
use serde_json::json;
use sys_info::{hostname, os_release, os_type};

use crate::core::app::AppContext;
use crate::core::http::{AppRequest, AppResponse};
use crate::http::JSONAppResponse;

pub async fn hello_handler(
    _req: AppRequest,
    ctx: &'static AppContext,
) -> anyhow::Result<AppResponse> {
    let os_type = os_type().unwrap_or_else(|_| "Unknown OS".to_string());
    let os_release = os_release().unwrap_or_else(|_| "Unknown Release".to_string());
    let hostname = hostname().unwrap_or_else(|_| "Unknown Hostname".to_string());

    let connected_devices = ctx.devices.lock().await.len();
    let body = json!({
         "hostname": hostname,
         "operating_system":  os_type,
         "os_release":  os_release,
         "connected_devices": connected_devices,    
         "config": ctx.config,
     } );

    JSONAppResponse::pack(body, StatusCode::OK)
}
