use std::net::SocketAddr;
use std::time::Duration;

use crate::core::context::AppContext;
use crate::core::http::{AppRequest, AppResponse};
use crate::http::JSONAppResponse;
use crate::services::core_db::CoreDBId;
use crate::services::ip_utils;
use http_body_util::BodyExt;
use hyper::StatusCode;
use tokio_tungstenite::tungstenite::Message;

#[derive(Clone, Debug, serde::Deserialize, serde::Serialize)]
struct UdpStreamStartRequest {
    device_id: CoreDBId,
    address: SocketAddr,
}

/// Start a UDP stream from the device with device_id to the remote address provided with the request.
/// To keep the stream going the client must ping the server every <N> seconds in with other endpoint.
pub async fn udp_stream_start_handler(
    mut req: AppRequest,
    ctx: &'static AppContext,
) -> anyhow::Result<AppResponse> {
    if ip_utils::filter_non_service_ips(&ctx.config, &req) == false {
        return JSONAppResponse::pack(ctx, "Forbidden IP address.", StatusCode::FORBIDDEN);
    }
    
    let body = req.body_mut().collect().await?.to_bytes();
    let Ok(body): Result<UdpStreamStartRequest, serde_json::Error> = serde_json::from_slice(&body)
    else {
        tracing::debug!("failed to parse request body");
        return JSONAppResponse::pack(
            ctx,
            "bad request: expected device_id and address".to_string(),
            StatusCode::BAD_REQUEST,
        );
    };
    tracing::debug!("got stream request: {body:?}");
    let Ok(device) = ctx.get_device(body.device_id).await else {
        return JSONAppResponse::pack(
            ctx,
            format!("no device found with id {}", body.device_id),
            StatusCode::NOT_FOUND,
        );
    };

    let receiver = device.stream_receiver.subscribe();
    drop(device);
    spawn_stream(ctx, receiver, body.address);

    JSONAppResponse::pack(
        ctx,
        format!("streaming to {}", body.address),
        StatusCode::OK,
    )
}

/// Temporary bandaid solution to spawn a stream
/// It will only stop when the timeout is reached
/// Even if connection is refused, it will keep trying to send.
/// This ensures that any synchronization issues are handled by the client and the server has some leeway
fn spawn_stream(
    ctx: &'static AppContext,
    mut receiver: tokio::sync::broadcast::Receiver<Message>,
    client_address: SocketAddr,
) {
    let future = async move {
        // give time for the client to set up the listener
        tokio::time::sleep(Duration::from_millis(200)).await;
        let mut sock_addr = ctx.config.http.address;
        sock_addr.set_port(0);
        let udp_socket = tokio::net::UdpSocket::bind(sock_addr).await.unwrap();
        udp_socket.connect(client_address).await.unwrap();
        while let Ok(msg) = receiver.recv().await {
            //result doesn't concern us, only timeout
            match udp_socket.send(&msg.into_data()).await {
                Err(e) => {
                    tracing::error!("udp send failed: {e}");
                    break;
                }
                Ok(n) => {
                    tracing::debug!("udp send success: {n}");
                }
            }
        }
        tracing::debug!("stream ended with {client_address}");
    };
    tokio::spawn(future);
}
