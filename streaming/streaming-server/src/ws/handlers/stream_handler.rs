use std::collections::HashMap;
use std::str::FromStr;

use crate::core::context::AppContext;
use crate::services::device_store::Device;
use futures_util::SinkExt;
use tokio::net::TcpStream;
use tokio_tungstenite::WebSocketStream;
use tokio_tungstenite::tungstenite::protocol::CloseFrame;

#[derive(Debug)]
struct StreamRequestParams {
    //   token: String,
    device_id: i32,
}
impl FromStr for StreamRequestParams {
    type Err = anyhow::Error;
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let params: HashMap<String, String> = url::form_urlencoded::parse(s.as_bytes())
            .into_owned()
            .collect();

        Ok(Self {
            //         token: params
            //           .get("token")
            //          .ok_or(anyhow::Error::msg("missing jwt token"))?
            //        .to_owned(),
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
    let params = match StreamRequestParams::from_str(req.uri().query().unwrap_or_default()) {
        Err(e) => {
            tracing::warn!(event = "invalid_stream_request", err = e.to_string());
            let _ = socket
                .close(Some(CloseFrame {
                    code: tokio_tungstenite::tungstenite::protocol::frame::coding::CloseCode::Bad(
                        1,
                    ),
                    reason: e.to_string().into(),
                }))
                .await;
            return;
        }
        Ok(p) => p,
    };

    let device = match ctx.devices.lock().await.get_device(params.device_id) {
        Some(d) => d,
        None => {
            tracing::debug!("no device found for {}", params.device_id);
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
            ctx.devices
                .lock()
                .await
                .return_device(params.device_id, device);
            return;
        }
    };

    tracing::info!(
        event = "stream_start",
        from = params.device_id,
        to = peer_addr.to_string()
    );

    let result = match stream_until_err(ctx, &params, &mut socket, device).await {
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
    ctx: &'static AppContext,
    params: &StreamRequestParams,
    socket: &mut WebSocketStream<TcpStream>,
    mut device: Device,
) -> anyhow::Result<()> {
    device.command_sender.send("START".into()).await?;
    while let Some(stream_packet) = device.stream_receiver.recv().await {
        if let Err(e) = socket.send(stream_packet).await {
            tracing::debug!("stream ended: {e}");
            break;
        }
    }

    device.command_sender.send("STOP".into()).await?;
    ctx.devices
        .lock()
        .await
        .return_device(params.device_id, device);
    Ok(())
}
