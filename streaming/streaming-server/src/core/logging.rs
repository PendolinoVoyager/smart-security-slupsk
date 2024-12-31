use std::io::stderr;

use anyhow::Result;
use sys_info::hostname;
use tracing_loki::url::Url;
use tracing_subscriber::EnvFilter;
use tracing_subscriber::filter::LevelFilter;
use tracing_subscriber::{Layer, layer::SubscriberExt, util::SubscriberInitExt};

use super::config::AppConfig;

pub async fn setup(config: &AppConfig) -> Result<()> {
    if config.log == "off" {
        return Ok(());
    }

    let (loki_layer, loki_task) = tracing_loki::builder()
        .label("host", hostname()?)?
        .label("service", "sss-streaming-server")?
        .build_url(Url::parse("http://127.0.0.1:3100").unwrap())?;

    // Set log level based on configuration
    let level = match config.log.as_str() {
        "trace" => LevelFilter::TRACE,
        "debug" => LevelFilter::DEBUG,
        "on" => LevelFilter::INFO,
        _ => LevelFilter::INFO,
    };

    // Layer for Loki, only for `info!`, `warn!`, `error!`
    let loki_filter = EnvFilter::default().add_directive(LevelFilter::INFO.into());

    let loki_layer: tracing_subscriber::filter::Filtered<
        tracing_loki::Layer,
        tracing_subscriber::EnvFilter,
        _,
    > = loki_layer.with_filter(loki_filter);

    // Layer for stderr, including all logs
    let stderr_layer = tracing_subscriber::fmt::Layer::new()
        .with_writer(stderr) // Custom writer
        .with_filter(level);

    tracing_subscriber::registry()
        .with(loki_layer)
        .with(stderr_layer)
        .init();

    tokio::spawn(loki_task);

    tracing::debug!("tracing subscriber set up");
    Ok(())
}
