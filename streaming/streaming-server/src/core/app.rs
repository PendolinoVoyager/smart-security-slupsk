use crate::core::config::AppConfig;

use super::context::AppContext;

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
