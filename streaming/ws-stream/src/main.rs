#![feature(type_changing_struct_update)]
#![feature(let_chains)]
//! Binary utility to stream to a websocket.
mod config;
mod stream;
use clap::Parser;
use config::Config;
use futures::SinkExt;
use stream::stream_factory::{self, StreamKind};
use stream::{StreamBuffer, VideoStream};
use tokio::net::TcpStream;
use tokio::task::spawn_blocking;
use tokio_tungstenite::tungstenite::Message;
use tokio_tungstenite::MaybeTlsStream;
use tracing::Level;
use tracing_subscriber::FmtSubscriber;

type AppWebSocket = tokio_tungstenite::WebSocketStream<MaybeTlsStream<TcpStream>>;

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    let config = config::Config::parse();
    if !config.silent {
        let subscriber = FmtSubscriber::builder()
            .with_writer(std::io::stderr) // Write logs to stderr
            .with_max_level(Level::INFO) // Set the maximum level of logs (optional)
            .finish();
        tracing::subscriber::set_global_default(subscriber)
            .expect("Setting default subscriber failed");
    }

    connect_ws(config)
        .await
        .inspect_err(|e| tracing::error!("Stream resulted in failure: {e}"))?;

    Ok(())
}

/// Make an connection to a websocket server and stream.
async fn connect_ws(config: Config) -> anyhow::Result<()> {
    tracing::info!("Attempting to connect to {}", &config.addr);

    let (ws, res) = tokio_tungstenite::connect_async(&config.addr).await?;

    tracing::info!("WebSocket created");
    tracing::debug!("{res:?}");
    stream_until_disconnect(ws, config).await;

    Ok(())
}

async fn stream_until_disconnect(mut ws: AppWebSocket, config: Config) {
    tracing::info!("Initializing stream");
    let stream = initialize_stream(&config).unwrap();

    tracing::info!("Starting streaming...");

    let (tx, mut rx) = tokio::sync::mpsc::channel::<Message>(size_of::<Message>() * 30);

    let handle = spawn_blocking(move || {
        let mut reader = Box::pin(StreamBuffer::new(1024, stream));
        while let Ok(buf) = reader.read() {
            let buf = buf.to_vec();
            if tx.blocking_send(Message::Binary(buf.into())).is_err() {
                break;
            }
        }
    });

    while let Some(msg) = rx.recv().await {
        tracing::info!("{}", msg.len());

        if ws.send(msg).await.is_err() {
            tracing::warn!("Cannot send packet, connection broken!");
            break;
        }
    }

    handle.abort();

    tracing::info!("Stream ended");
}

fn initialize_stream(config: &Config) -> anyhow::Result<StreamKind> {
    let mut stream =
        stream_factory::create_stream(config).inspect_err(|e| tracing::error!("{e}"))?;

    stream.start();
    Ok(stream)
}

// /// Stream while listening for START / STOP packets
// /// This should be nonblocking
// async fn stream_to_streaming_server(mut ws: AppWebSocket, config: Config) -> anyhow::Result<()> {
//     loop {
//         let res = match ws.next().await {
//             Some(v) => v?,
//             None => return Err(anyhow::Error::msg("socket connection broken")),
//         };
//         if res.into_text().is_ok_and(|msg| msg == "START") {
//             // stream until STOP received
//             tracing::info!("Starting streaming...");
//             let stream = initialize_stream(&config)?;

//             let (tx, mut rx) = tokio::sync::mpsc::channel::<Message>(size_of::<Message>() * 30);

//             let handle = spawn_blocking(move || {
//                 let mut reader = Box::pin(StreamBuffer::new(1024, stream));
//                 while let Ok(buf) = reader.read() {
//                     let buf = buf.to_vec();
//                     if tx.blocking_send(Message::Binary(buf.into())).is_err() {
//                         break;
//                     }
//                 }
//             });
//             loop {
//                 tokio::select! {
//                     msg = rx.recv() => {
//                         let msg = match msg {
//                             Some(m) => m,
//                             None => break
//                         };
//                         if ws.send(msg).await.is_err() {
//                             tracing::warn!("Cannot send packet, connection broken!");
//                             break;
//                         }
//                     },
//                     msg = ws.next() => {
//                         let msg = match msg {
//                             Some(m) => m?,
//                             None => break,
//                         };
//                         if msg.into_text()? == "STOP" {
//                             break;
//                         }
//                     }

//                 }
//             }

//             tracing::info!("Stream ended");
//             handle.abort();
//         }
//     }
// }
