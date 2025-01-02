use crate::core::context::AppContext;

/// Handle websocket auth.
/// And yes, it uses httparse Request instead of hypers.
/// The reason is that hyper doesn't let you to peek the request for some reason,
/// and tokio_tungstenite doesn't allow to make async auth with accept_hdr_async.
pub async fn websocket_handshake_handler(
    req: httparse::Request<'_, '_>,
    _ctx: &'static AppContext,
) -> bool {
    match req.path {
        Some(path) if path.starts_with("/device_checkout") => true,
        Some(path) if path.starts_with("/stream") => true,
        _ => true,
    }
}

/// Handle user authentication
pub async fn user_auth_handler(email: String, token: String) -> bool {
    true
}
