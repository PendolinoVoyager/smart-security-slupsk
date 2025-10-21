use hyper::Response;
use hyper::header::{CONTENT_LANGUAGE, CONTENT_TYPE};

use crate::core::context::AppContext;
use crate::core::http::{AppRequest, AppResponse};
use crate::http::add_common_headers;

const OPENAPI_YAML_DEFAULT_PATH: &str = concat!(env!("CARGO_MANIFEST_DIR"), "/cfg/openapi.yaml");

const OPENAPI_YAML_ENV_VAR: &str = "STRSRV_OPENAPI_YAML_PATH";

lazy_static::lazy_static! {
    pub static ref OPENAPI_YAML_PATH: String = std::env::var(OPENAPI_YAML_ENV_VAR)
        .unwrap_or(OPENAPI_YAML_DEFAULT_PATH.to_string());
    pub static ref OPENAPI_YAML: String = std::fs::read_to_string(OPENAPI_YAML_PATH.as_str())
        .unwrap_or_else(|_| panic!("Failed to read OpenAPI YAML file at {}", OPENAPI_YAML_PATH.as_str()));
}

pub async fn openapi_handler(
    _req: AppRequest,
    ctx: &'static AppContext,
) -> anyhow::Result<AppResponse> {
    let mut res = Response::builder()
        .header(CONTENT_LANGUAGE, OPENAPI_YAML.len())
        .header(CONTENT_TYPE, "application/yaml")
        .body(OPENAPI_YAML.clone().into())?;
    add_common_headers(&mut res, ctx);
    Ok(res)
}
