use std::collections::HashMap;
use std::str::FromStr;

use crate::core::context::AppContext;
use crate::services::core_db::CoreDBId;
use crate::services::device_store::Device;
use crate::services::jwt::UserJWTClaims;
use crate::ws::handlers::close_unauthorized;
use futures_util::SinkExt;
use tokio::net::TcpStream;
use tokio_tungstenite::WebSocketStream;
use tokio_tungstenite::tungstenite::protocol::CloseFrame;

#[derive(Debug)]
struct StreamRequestParams {
    token: String,
    device_id: CoreDBId,
}
impl FromStr for StreamRequestParams {
    type Err = anyhow::Error;
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let params: HashMap<String, String> = url::form_urlencoded::parse(s.as_bytes())
            .into_owned()
            .collect();

        Ok(Self {
            token: params
                .get("token")
                .ok_or(anyhow::Error::msg("missing jwt token"))?
                .to_owned(),
            device_id: params
                .get("device_id")
                .ok_or(anyhow::Error::msg("missing device_id"))?
                .parse()?,
        })
    }
}

/// Handle the request to stream from a given device.
/// Assuming that the auth is passed and device_id belongs to the user.  
pub async fn stream_handler(
    req: hyper::Request<()>,
    mut socket: WebSocketStream<TcpStream>,
    ctx: &'static AppContext,
) {
    let (_claims, params) = match check_user_authorized(ctx, req, &mut socket).await {
        Err(e) => {
            tracing::warn!(event = "stream_auth_fail", err = e.to_string());
            return;
        }
        Ok(v) => v,
    };
    let device = match ctx.get_device(params.device_id).await {
        Ok(d) => d,
        Err(e) => {
            tracing::debug!("no device found for {}:\n{e}", params.device_id);
            let _ = socket
                .close(Some(CloseFrame {
                    code: tokio_tungstenite::tungstenite::protocol::frame::coding::CloseCode::Bad(
                        2,
                    ),
                    reason: "no such device ready for stream".into(),
                }))
                .await;
            return;
        }
    };
    let peer_addr = match socket.get_ref().peer_addr() {
        Ok(a) => a,
        Err(e) => {
            tracing::debug!("host ended connection before stream: {e}");

            return;
        }
    };

    tracing::info!(
        event = "stream_start",
        from = params.device_id,
        to = peer_addr.to_string()
    );

    let result = match stream_until_err(ctx, &mut socket, device).await {
        Ok(_) => "ok".to_owned(),
        Err(e) => e.to_string(),
    };
    tracing::info!(
        event = "stream_end",
        from = params.device_id,
        to = peer_addr.to_string(),
        result
    );

    let _ = socket.close(None).await;
}

async fn stream_until_err(
    _ctx: &'static AppContext,
    socket: &mut WebSocketStream<TcpStream>,
    device: Device,
) -> anyhow::Result<()> {
    let mut receiver = device.stream_receiver.subscribe();
    drop(device);
    while let Ok(msg) = receiver.recv().await {
        if let Err(e) = socket.send(msg).await {
            tracing::debug!("stream ended: {e}");
            break;
        }
    }

    Ok(())
}

/// Helper function to check if the user is authorized to the stream.
/// Closes the socket if errors.
/// `returns` - Ok() if authorized, Err() if failed to authorize for any reason
async fn check_user_authorized(
    ctx: &'static AppContext,
    req: hyper::Request<()>,
    socket: &mut WebSocketStream<TcpStream>,
) -> anyhow::Result<(UserJWTClaims, StreamRequestParams)> {
    let params = match StreamRequestParams::from_str(req.uri().query().unwrap_or_default()) {
        Err(e) => {
            close_unauthorized(socket, e.to_string().into()).await;
            return Err(e);
        }
        Ok(p) => p,
    };

    let Ok(claims) = crate::services::jwt::verify_user(&params.token) else {
        close_unauthorized(socket, "invalid jwt token".into()).await;
        return Err(anyhow::Error::msg("invalid jwt token"));
    };

    let user_id = crate::services::core_db::find_user_id(ctx, &claims.sub)
        .await?
        .unwrap_or(-1);

    if let Ok(dev) = crate::services::app_db::RedisDeviceSchema::get(
        &mut ctx.app_db.get().await?,
        params.device_id,
    )
    .await
    {
        if dev.user_id == user_id {
            return Ok((claims, params));
        } else {
            return Err(anyhow::Error::msg("user does not own the device"));
        }
    }
    Err(anyhow::Error::msg("failed to verify ownership"))
}
