//! This module contains handlers for WebSocket connections, meaning:
//! - no response,
//! - takes WebSocket and its initial request

mod device_checkout_handler;
mod stream_handler;
pub use device_checkout_handler::*;
pub use stream_handler::*;
use tokio::net::TcpStream;
use tokio_tungstenite::WebSocketStream;
use tokio_tungstenite::tungstenite::Utf8Bytes;
use tokio_tungstenite::tungstenite::protocol::CloseFrame;

/// Helper function to close the socket when it's unauthorized
pub async fn close_unauthorized(socket: &mut WebSocketStream<TcpStream>, reason: Utf8Bytes) {
    let _ = socket
        .close(Some(CloseFrame {
            code: tokio_tungstenite::tungstenite::protocol::frame::coding::CloseCode::Bad(3001),
            reason,
        }))
        .await;
}
