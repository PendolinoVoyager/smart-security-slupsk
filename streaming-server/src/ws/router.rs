use tokio::net::TcpStream;
use tokio_tungstenite::WebSocketStream;

use crate::core::context::AppContext;
use crate::ws::handlers::*;

macro_rules! route {
    ($name:expr, $handler:ident, $req:expr, $socket:expr, $ctx:expr ) => {{
        tracing::info!(event = "ws_route_accessed", route = stringify!($name));
        tracing::debug!("WebSocket accessed with handler {}", stringify!($handler));
        $handler($req, $socket, $ctx).await;
    }};
}
/// Route WebSocket traffic just after the handshake.
/// It assumes that the WebSocket is already authenticated
pub async fn route(
    req: hyper::Request<()>,
    socket: WebSocketStream<TcpStream>,
    ctx: &'static AppContext,
) {
    match req.uri().path() {
        "/streaming-server/v1/ws/device_checkout" => route!("device_checkout", device_checkout_handler, req, socket, ctx),
        "/streaming-server/v1/ws/stream" => route!("stream", stream_handler, req, socket, ctx),
        _ => {
            tracing::info!(event = "ws_route_accessed", "404");
        }
    }
}
