use crate::core::app::AppContext;

/// Handle websocket auth.
/// And yes, it uses httparse Request instead of hypers.
/// The reason is that hyper doesn't let you to peek the request for some reason,
/// and tokio_tungstenite doesn't allow to make async auth with accept_hdr_async.
pub async fn websocket_handshake_handler(
    _req: httparse::Request<'_, '_>,
    _ctx: &'static AppContext,
) -> bool {
    true
}
