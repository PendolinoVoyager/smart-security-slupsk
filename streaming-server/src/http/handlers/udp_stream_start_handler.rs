use std::net::SocketAddr;

use crate::core::context::AppContext;
use crate::core::http::{AppRequest, AppResponse};
use crate::http::JSONAppResponse;
use crate::services::core_db::CoreDBId;
use http_body_util::BodyExt;
use hyper::StatusCode;

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

    let mut receiver = device.stream_receiver.subscribe();
    drop(device);

    // this is for debug only, there's no way to stop the stream for now
    let future = async move {
        let mut addr = ctx.config.http.address;
        addr.set_port(0);
        let udp_socket = tokio::net::UdpSocket::bind(addr).await.unwrap();
        udp_socket.connect(body.address).await.unwrap();
        while let Ok(msg) = receiver.recv().await {
            //result doesn't concern us, only timeout
            let _ = udp_socket.send(&msg.into_data()).await;
        }
    };

    tokio::spawn(future);
    JSONAppResponse::pack(
        ctx,
        format!("streaming to {}", body.address),
        StatusCode::OK,
    )
}
