use crate::core::app::AppContext;
use futures_util::{SinkExt, StreamExt};
use tokio::net::TcpStream;
use tokio_tungstenite::WebSocketStream;
pub async fn device_checkout_handler(
    _req: hyper::Request<()>,
    mut socket: WebSocketStream<TcpStream>,
    _ctx: &'static AppContext,
) {
    let _ = socket
        .send(tokio_tungstenite::tungstenite::Message::Text(
            "Welcome!".into(),
        ))
        .await;

    while let Some(msg) = socket.next().await {
        tracing::debug!("{:?}", msg);
        let _ = socket
            .send(tokio_tungstenite::tungstenite::Message::Text("echo".into()))
            .await;
    }
}
