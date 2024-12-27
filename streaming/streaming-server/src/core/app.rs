use std::collections::HashMap;
use std::error::Error;

use crate::core::config::AppConfig;

/// Entry point for the app, never returns.
pub async fn init_app() -> ! {
    let config = AppConfig::load_from_yaml("cfg/cfg.yaml")
        .unwrap_or_else(|e| panic!("Cannot parse the config file: {e}"));
    super::logging::setup(&config)
        .await
        .expect("logger initialization failed");
    let ctx = AppContext::create(config)
        .await
        .expect("cannot create app context with current settings");

    print_logo();
    start_servers(ctx).await;
    unreachable!("critical failure: app exited early")
}

/// App wide Context. Available in 'static lifetime for all tasks / function calls in the app
/// Context must be static, Sync, and Send.
/// Interior mutability can be made with sync primitives or other methods.
#[allow(unused)]
#[derive(Debug)]
pub struct AppContext {
    pub devices: tokio::sync::Mutex<HashMap<String, String>>,
    pub config: AppConfig,
}

impl AppContext {
    /// Create app wide Context, available in all handlers.
    /// Context must be static, Sync, and Send.
    /// Interior mutability can be made with sync primitives or other methods.
    pub async fn create(config: AppConfig) -> Result<&'static mut Self, Box<dyn Error>> {
        let ctx = Box::new(Self {
            devices: tokio::sync::Mutex::new(HashMap::new()),
            config,
        });
        let ctx = Box::leak(ctx);

        Ok(ctx)
    }
}
async fn start_servers(ctx: &'static AppContext) {
    let http_listener = tokio::net::TcpListener::bind(ctx.config.http.address)
        .await
        .unwrap_or_else(|err| {
            tracing::error!(
                "failure to bind the TCP listener for HTTP server to {}:\n{err}",
                ctx.config.http.address
            );
            panic!("{err}")
        });

    tracing::info!(
        "Successfully setup HTTP listener on {}",
        ctx.config.http.address
    );

    let ws_listener = tokio::net::TcpListener::bind(ctx.config.ws.address)
        .await
        .unwrap_or_else(|err| {
            tracing::error!(
                "failure to bind the TCP listener for WebSocket server to {}:\n{err}",
                ctx.config.ws.address
            );
            panic!("{err}")
        });
    tracing::info!(
        "Successfully setup WebSocket listener on {}",
        ctx.config.ws.address
    );

    let http_handle = tokio::spawn(super::http::handle_http(http_listener, ctx));
    let ws_handle = tokio::spawn(super::ws::handle_ws(ws_listener, ctx));
    tracing::info!("All tasks spawned, ready to go!");
    let _ = tokio::join!(http_handle, ws_handle);
}

fn print_logo() {
    eprintln!(
        "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\x1b[0;32m"
    );

    if let Ok(logo) = std::fs::read_to_string("logo.ascii") {
        let logo: Vec<&str> = logo.lines().collect();
        for line in logo {
            eprintln!("{}", line);
        }
    }
    eprintln!(
        "\x1b[0m~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
    );
}
