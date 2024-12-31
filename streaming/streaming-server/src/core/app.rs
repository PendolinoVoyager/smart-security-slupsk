use std::collections::HashSet;

use sqlx::{Pool, Postgres};

use crate::core::config::AppConfig;

/// Entry point for the app.
pub async fn init_app() -> anyhow::Result<()> {
    let config = AppConfig::load_from_yaml("cfg/cfg.yaml")
        .unwrap_or_else(|e| panic!("Cannot parse the config file: {e}"));
    super::logging::setup(&config)
        .await
        .expect("logger initialization failed");
    let ctx = AppContext::create(config)
        .await
        .expect("cannot create app context with current settings");

    print_logo();
    start_servers(ctx).await
}

/// App wide Context. Available in 'static lifetime for all tasks / function calls in the app
/// Context must be static, Sync, and Send.
/// Interior mutability can be made with sync primitives or other methods.
/// TODO: Make db generic over db type
#[allow(unused)]
#[derive(Debug)]
pub struct AppContext {
    /// Locally stored devices
    pub devices: tokio::sync::Mutex<HashSet<i64>>,
    /// App wide config
    pub config: AppConfig,
    /// Connection pool to main Postgre database
    pub core_db: Pool<Postgres>,
    /// Connection pool to app redis DB to manage devices
    pub app_db: deadpool::managed::Pool<deadpool_redis::Manager, deadpool_redis::Connection>,
}

impl AppContext {
    /// Create app wide Context, available in all handlers.
    /// Context must be static, Sync, and Send.
    /// Interior mutability can be made with sync primitives or other methods.
    pub async fn create(config: AppConfig) -> anyhow::Result<&'static mut Self> {
        let core_db = crate::services::core_db::init(&config)
            .await
            .inspect_err(|e| {
                tracing::error!(
                    event = "db_connection_failed",
                    uri = config.db_uri,
                    err = e.to_string()
                );
            })?;
        tracing::info!(event = "db_connection");
        let app_db = crate::services::app_db::init(&config)
            .await
            .inspect_err(|e| {
                tracing::error!(
                    event = "redis_connection_failed",
                    uri = config.app_db_uri,
                    err = e.to_string()
                )
            })?;
        tracing::info!(event = "app_db_connection");

        let ctx = Box::new(Self {
            devices: tokio::sync::Mutex::new(HashSet::new()),
            config,
            core_db,
            app_db,
        });
        let ctx = Box::leak(ctx);

        Ok(ctx)
    }
    /// Clean up self without dropping so other threads don't do stupid stuff with memory.
    pub async fn cleanup(&self) {
        let _ = crate::services::app_db::remove_all_connections(self).await;
        tracing::debug!("Starting app cleanup");
    }
}
async fn start_servers(ctx: &'static mut AppContext) -> anyhow::Result<()> {
    let http_listener = tokio::net::TcpListener::bind(ctx.config.http.address)
        .await
        .inspect_err(|err| {
            tracing::error!(
                "failure to bind the TCP listener for HTTP server to {}:\n{err}",
                ctx.config.http.address
            );
        })?;

    tracing::debug!(
        "Successfully setup HTTP listener on {}",
        ctx.config.http.address
    );

    let ws_listener = tokio::net::TcpListener::bind(ctx.config.ws.address)
        .await
        .inspect_err(|err| {
            tracing::info!(
                "failure to bind the TCP listener for WebSocket server to {}:\n{err}",
                ctx.config.ws.address
            );
        })?;
    tracing::debug!(
        "Successfully setup WebSocket listener on {}",
        ctx.config.ws.address
    );

    let http_handle = tokio::spawn(super::http::handle_http(http_listener, ctx));
    let ws_handle = tokio::spawn(super::ws::handle_ws(ws_listener, ctx));

    tracing::info!(
        event = "app_boot",
        result = "success",
        config = serde_json::to_string(&ctx.config).unwrap()
    );

    let res = tokio::select! {
        _ = tokio::signal::ctrl_c() => {
            tracing::debug!( "Shutting down the application...");

          tracing::info!(event = "app_shutdown");

          Ok(())
        }
        _ = http_handle => {
            tracing::error!(event = "app_error", "early exit due to http server");
            Err(anyhow::Error::msg("bad http exit"))
        }
        _ = ws_handle => {
            tracing::error!(event = "app_error", "early exit due to websocket server");
            Err(anyhow::Error::msg("bad ws exit"))

        }
    };

    ctx.cleanup().await;
    res
}

fn print_logo() {
    eprintln!(
        "==============================================================================\x1b[0;32m"
    );

    if let Ok(logo) = std::fs::read_to_string("logo.ascii") {
        let logo: Vec<&str> = logo.lines().collect();
        for line in logo {
            eprintln!("{}", line);
        }
    }
    eprintln!(
        "\x1b[0m=============================================================================="
    );
}
