use std::convert::Infallible;

use super::app::AppContext;
use http_body_util::Full;
use hyper::body::Bytes;
use hyper::service::service_fn;
use hyper::{Request, Response};

pub async fn handle_http(listener: tokio::net::TcpListener, ctx: &'static AppContext) {
    while let Ok((tcp_stream, sock_addr)) = listener.accept().await {
        tracing::debug!("incoming TCP connection from {sock_addr}");
        // sync io adapter
        tokio::spawn(async move {
            let io = hyper_util::rt::TokioIo::new(tcp_stream);
            if let Err(e) = hyper::server::conn::http1::Builder::new()
                .half_close(false)
                .serve_connection(io, service_fn(async |req| _handle_http(req, ctx).await))
                .await
            {
                tracing::error!("failed handling of HTTP connection with {sock_addr}");
            }
        });
    }
}

async fn _handle_http(
    req: Request<hyper::body::Incoming>,
    ctx: &AppContext,
) -> Result<Response<http_body_util::Full<hyper::body::Bytes>>, Infallible> {
    tracing::info!("incoming HTTP connection: {}", req.uri());
    tracing::debug!("{:?}", req.headers());
    let response = Response::builder()
        .status(200)
        .body(http_body_util::Full::new(Bytes::from_static(b"hello\n")))
        .unwrap_or(Response::new(Full::new(Bytes::from_static(
            b"Something went wrong, invalid response format",
        ))));
    Ok(response)
}
