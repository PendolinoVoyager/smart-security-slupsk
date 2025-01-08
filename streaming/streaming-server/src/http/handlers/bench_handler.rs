use hyper::Response;
use hyper::header::CONTENT_TYPE;

use crate::core::context::AppContext;
use crate::core::http::{AppRequest, AppResponse};

pub async fn bench_handler(
    _req: AppRequest,
    _ctx: &'static AppContext,
) -> anyhow::Result<AppResponse> {
    Ok(Response::builder()
        .status(200)
        .header(CONTENT_TYPE, "text/plain")
        .body("Hello, world!\n".into())
        .unwrap())
}
