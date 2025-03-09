use hyper::Response;
use hyper::header::{CONTENT_LANGUAGE, CONTENT_TYPE};

use crate::core::context::AppContext;
use crate::core::http::{AppRequest, AppResponse};
use crate::http::add_common_headers;

const OPENAPI_YAML: &str = include_str!("../../../openapi.yaml");

pub async fn openapi_handler(
    _req: AppRequest,
    ctx: &'static AppContext,
) -> anyhow::Result<AppResponse> {
    let mut res = Response::builder()
        .header(CONTENT_LANGUAGE, OPENAPI_YAML.len())
        .header(CONTENT_TYPE, "application/yaml")
        .body(OPENAPI_YAML.into())?;
    add_common_headers(&mut res, ctx);
    Ok(res)
}
