use std::collections::HashMap;
use std::str::FromStr;
use std::sync::Arc;

use crate::core::context::AppContext;
use crate::ws::handlers::close_unauthorized;
use crate::ws::ws_task::WebSocketTask;
use tokio::net::TcpStream;
use tokio_tungstenite::WebSocketStream;
use tokio_tungstenite::tungstenite::Message;

#[derive(Debug)]
struct DeviceCheckoutParams {
    token: String,
}
impl FromStr for DeviceCheckoutParams {
    type Err = anyhow::Error;
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let params: HashMap<String, String> = url::form_urlencoded::parse(s.as_bytes())
            .into_owned()
            .collect();

        Ok(Self {
            token: params
                .get("token")
                .ok_or(anyhow::Error::msg("missing token"))?
                .to_string(),
        })
    }
}

pub async fn device_checkout_handler(
    req: hyper::Request<()>,
    mut socket: WebSocketStream<TcpStream>,
    ctx: &'static AppContext,
) {
    let Ok(token) = DeviceCheckoutParams::from_str(req.uri().query().unwrap_or_default()) else {
        close_unauthorized(&mut socket, "missing token in query params".into()).await;
        return;
    };
    // get this from token's claims
    let device_id = 100;

    let (mut task, command_sender) = WebSocketTask::new(socket);
    // this is a channel for raw stream data
    let (stream_sender, stream_receiver) =
        tokio::sync::mpsc::channel::<Message>(size_of::<Message>() * 100);
    let stream_sender = Arc::new(stream_sender);
    task.on_init(|_| async move {
        ctx.register_device(device_id, stream_receiver, command_sender)
            .await
            .inspect_err(|e| tracing::warn!(event = "device_register_fail", err = e.to_string()))?;
        crate::services::app_db::RedisDeviceSchema::register_device(ctx, device_id).await?;
        Ok(())
    });
    task.on_message({
        move |msg| {
            let stream_sender = Arc::clone(&stream_sender);
            async move {
                stream_sender.send(msg).await?;
                Ok(None)
            }
        }
    });

    task.on_cleanup(|_| async move {
        ctx.remove_device(device_id).await?;
        Ok(())
    });
    let res = task.run().await;
    tracing::debug!("device tasked resolved with: {:?}", res);
}
