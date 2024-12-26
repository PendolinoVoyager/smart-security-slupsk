use super::app::AppContext;

pub async fn handle_ws(listener: tokio::net::TcpListener, _ctx: &AppContext) {
    while let Ok((tcp_stream, sock_addr)) = listener.accept().await {
        tracing::debug!("incoming WebSocket connection from {sock_addr}");
        let ws = match tokio_tungstenite::accept_hdr_async(tcp_stream, websocket_handshake_handler)
            .await
        {
            Ok(c) => {
                tracing::debug!("successful WebSocket connection with {sock_addr}");
                c
            }
            Err(e) => {
                tracing::debug!("bad WebSocket handshake with {sock_addr}:\n{e}");
                continue;
            }
        };
        tokio::spawn(_handle_ws(ws));
    }
}

#[allow(clippy::result_large_err)]
fn websocket_handshake_handler(
    req: &tokio_tungstenite::tungstenite::http::Request<()>,
    response: tokio_tungstenite::tungstenite::http::Response<()>,
) -> std::result::Result<
    tokio_tungstenite::tungstenite::http::Response<()>,
    tokio_tungstenite::tungstenite::http::Response<Option<String>>,
> {
    tracing::debug!("{:#?}", req);
    Ok(response)
}

async fn _handle_ws(_ws: tokio_tungstenite::WebSocketStream<tokio::net::TcpStream>) {
    todo!()
}
