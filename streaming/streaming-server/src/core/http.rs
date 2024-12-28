use std::convert::Infallible;

use super::app::AppContext;
use hyper::service::service_fn;
use hyper::{Request, Response};

/// Type alias for hyper Request
pub type AppRequest = Request<hyper::body::Incoming>;
/// Type alias for hyper Response
pub type AppResponse = Response<http_body_util::Full<hyper::body::Bytes>>;

pub async fn handle_http(listener: tokio::net::TcpListener, ctx: &'static AppContext) {
    while let Ok((tcp_stream, sock_addr)) = listener.accept().await {
        tracing::debug!("incoming TCP connection from {sock_addr}");
        // sync io adapter
        tokio::spawn(async move {
            let io = hyper_util::rt::TokioIo::new(tcp_stream);

            let conn = hyper::server::conn::http1::Builder::new()
                .half_close(false)
                .serve_connection(io, service_fn(async |req| _handle_http(req, ctx).await));

            tracing::info!(event = "http_request", from = sock_addr.to_string());

            if let Err(e) = conn.await {
                tracing::error!(
                    event = "invalid_http_request",
                    from = sock_addr.to_string(),
                    err = e.to_string()
                );
            }
        });
    }
}

async fn _handle_http(
    req: AppRequest,
    ctx: &'static AppContext,
) -> Result<AppResponse, Infallible> {
    tracing::debug!("{} {} HTTP/1.1", req.method(), req.uri());
    let res = crate::http::router::route(req, ctx).await;
    Ok(res)
}
