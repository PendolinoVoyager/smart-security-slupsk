use std::collections::HashMap;

use crate::core::app::AppContext;
use crate::ws::ws_task::WebSocketTask;
use tokio::net::TcpStream;
use tokio_tungstenite::WebSocketStream;

pub async fn device_checkout_handler(
    req: hyper::Request<()>,
    socket: WebSocketStream<TcpStream>,
    ctx: &'static AppContext,
) {
    let device_id = match get_device_id(req) {
        Some(d) => d,
        None => {
            return;
        }
    };

    let mut task = WebSocketTask::new(socket);
    task.on_init(|_| async move {
        ctx.devices.lock().await.insert(device_id);
        crate::services::app_db::register_device(ctx, device_id).await?;
        Ok(())
    });
    task.on_cleanup(|_| async move {
        crate::services::app_db::remove_device(ctx, device_id).await?;
        ctx.devices.lock().await.remove(&device_id);
        Ok(())
    });
    task.run().await;
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
