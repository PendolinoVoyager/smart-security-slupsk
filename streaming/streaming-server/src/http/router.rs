use crate::core::app::AppContext;
use crate::core::http::{AppRequest, AppResponse};

use super::NOT_FOUND_RESPONSE;
use super::handlers::*;

macro_rules! route {
    ($handler:ident, $req:expr, $ctx:expr ) => {{
        tracing::info!("matched route with {}", stringify!($handler));
        $handler($req, $ctx)
            .await
            .unwrap_or(super::INTERNAL_SERVER_ERROR_RESPONSE.clone())
    }};
}

pub async fn route(req: AppRequest, ctx: &'static AppContext) -> AppResponse {
    match req.uri().path() {
        "/hello" => route!(hello_handler, req, ctx),
        _ => {
            tracing::info!("route not found: {}", req.uri());
            NOT_FOUND_RESPONSE.clone()
        }
    }
}
