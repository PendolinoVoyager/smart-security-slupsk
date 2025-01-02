use std::collections::HashMap;
use std::sync::Arc;

use crate::core::context::AppContext;
use crate::ws::ws_task::WebSocketTask;
use tokio::net::TcpStream;
use tokio_tungstenite::WebSocketStream;
use tokio_tungstenite::tungstenite::Message;

pub async fn device_checkout_handler(
    req: hyper::Request<()>,
    socket: WebSocketStream<TcpStream>,
    ctx: &'static AppContext,
) {
    let device_id = match get_device_id(req) {
        Some(d) => d,
        None => {
            tracing::warn!(
                event = "invalid_device_register",
                "invalid request for device_checkout"
            );
            return;
        }
    };

    let (mut task, command_sender) = WebSocketTask::new(socket);
    // this is a channel for raw stream data
    let (stream_sender, stream_receiver) =
        tokio::sync::mpsc::channel::<Message>(size_of::<Message>() * 100);
    let stream_sender = Arc::new(stream_sender);
    task.on_init(|_| async move {
        ctx.devices
            .lock()
            .await
            .register_device(device_id, stream_receiver, command_sender)
            .inspect_err(|e| tracing::warn!(event = "device_register_fail", err = e.to_string()))?;
        crate::services::app_db::register_device(ctx, device_id).await?;
        Ok(())
    });
    task.on_message({
        let stream_sender = Arc::clone(&stream_sender);
        move |msg| {
            let stream_sender = Arc::clone(&stream_sender);
            async move {
                stream_sender.send(msg).await?;
                Ok(None)
            }
        }
    });

    task.on_cleanup(|_| async move {
        crate::services::app_db::remove_device(ctx, device_id).await?;
        let mut devices = ctx.devices.lock().await;
        devices.poison_or_remove(device_id);
        Ok(())
    });
    let res = task.run().await;
    tracing::debug!("device tasked resolved with: {:?}", res);
}

fn get_device_id(req: hyper::Request<()>) -> Option<i64> {
    let params: HashMap<String, String> = req
        .uri()
        .query()
        .map(|v| {
            url::form_urlencoded::parse(v.as_bytes())
                .into_owned()
                .collect()
        })
        .unwrap_or_default();

    params
        .get("device_id")
        .and_then(|id_str| id_str.parse::<i64>().ok())
}
