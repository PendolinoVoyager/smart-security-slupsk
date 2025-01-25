use std::net::SocketAddr;

use crate::core::context::AppContext;
use tokio::net::TcpStream;
use tokio_tungstenite::WebSocketStream;

pub async fn handle_ws(listener: tokio::net::TcpListener, ctx: &'static AppContext) {
    while let Ok((tcp_stream, sock_addr)) = listener.accept().await {
        tracing::debug!("attempted WebSocket connection from {sock_addr}");
        tokio::spawn(_handle_ws(tcp_stream, sock_addr, ctx));
    }
}

#[inline(always)]
async fn _handle_ws(tcp_stream: TcpStream, sock_addr: SocketAddr, ctx: &'static AppContext) {
    let mut initial_request: Option<hyper::Request<()>> = None;
    // peek the http request because tokio_tungstenite doesn't allow async callbacks for auth
    let is_authenticated = preauth_websocket(&tcp_stream, ctx).await;

    let ws: WebSocketStream<TcpStream> =
        match tokio_tungstenite::accept_hdr_async(tcp_stream, |req: &hyper::Request<()>, res| {
            initial_request = Some(req.clone());
            if is_authenticated {
                Ok(res)
            } else {
                Err(hyper::Response::new(Some("unauthorized".into())))
            }
        })
        .await
        {
            Ok(c) => {
                tracing::debug!("successful WebSocket connection with {sock_addr}");
                tracing::info!(event = "ws_connection", host = sock_addr.to_string());
                c
            }
            Err(e) => {
                tracing::debug!("bad WebSocket handshake with {sock_addr}:\n{e}");
                tracing::info!(
                    event = "ws_bad_handshake",
                    host = sock_addr.to_string(),
                    err = e.to_string()
                );
                return;
            }
        };

    if let Some(req) = initial_request {
        crate::ws::router::route(req, ws, ctx).await;
    } else {
        tracing::error!(
            event = "ws_fail",
            addr = sock_addr.to_string(),
            "failed to capture ws request"
        );
    }
}

/// Asynchronously authenticate the WebSocket by peeking the Request.
/// It returns only a boolean, because the tokio_tungstenite::accept_hdr_async
/// actually creates valid responses.
///
/// `returns` - is_authenticated
async fn preauth_websocket(stream: &TcpStream, ctx: &'static AppContext) -> bool {
    let mut request_buf = vec![b' '; 1024];

    let read = match stream.peek(&mut request_buf).await {
        Ok(r) if r != 0 => r,
        _ => return false,
    };
    unsafe {
        request_buf.set_len(read.max(1024));
    }
    let mut headers = [httparse::EMPTY_HEADER; 16];
    let mut req: httparse::Request<'_, '_> = httparse::Request::new(&mut headers);

    if req.parse(&request_buf).is_err() {
        return false;
    }

    tracing::trace!("WS connection: {:?}", req.path);

    crate::services::ws_pre_auth::websocket_handshake_handler(req, ctx).await
}
