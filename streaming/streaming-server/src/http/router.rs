use crate::core::app::AppContext;
use crate::core::http::{AppRequest, AppResponse};

use super::NOT_FOUND_RESPONSE;
use super::handlers::*;

macro_rules! route {
    ($name:expr, $handler:ident, $req:expr, $ctx:expr ) => {{
        tracing::info!(event = "route_accessed", route = stringify!($name));
        tracing::debug!("route accessed with handler {}", stringify!($handler));
        $handler($req, $ctx).await.unwrap_or_else(|e| {
            tracing::warn!(
                event = "handler_failure",
                route = stringify!($handler),
                err = e.to_string()
            );
            super::INTERNAL_SERVER_ERROR_RESPONSE.clone()
        })
    }};
}
pub async fn route(req: AppRequest, ctx: &'static AppContext) -> AppResponse {
    match req.uri().path() {
        "/hello" => route!("hello", hello_handler, req, ctx),
        "/benchmark" => route!("benchmark", bench_handler, req, ctx),
        "/streams" => route!("streams", streams_handler, req, ctx),
        _ => {
            tracing::info!(event = "route_accessed", "404");
            NOT_FOUND_RESPONSE.clone()
        }
    }
}
