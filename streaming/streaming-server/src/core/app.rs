use std::collections::HashMap;
use std::error::Error;
use std::net::{Ipv4Addr, SocketAddr};

use tracing::level_filters::LevelFilter;
/// Entry point for the app, never returns.
pub async fn init_app() -> ! {
    let ctx = AppContext::create().expect("cannot create app context with current settings");
    setup_tracing_subscriber();
    start_servers(ctx).await;
    unreachable!("critical failure: app exited early")
}

/// App wide Context. Available in 'static lifetime for all tasks / function calls in the app
/// Context must be static, Sync, and Send.
/// Interior mutability can be made with sync primitives or other methods.
#[derive(Debug)]
pub struct AppContext {
    env: String,
    addr_http: SocketAddr,
    addr_ws: SocketAddr,
    devices: tokio::sync::Mutex<HashMap<SocketAddr, String>>,
}
impl Default for AppContext {
    fn default() -> Self {
        Self {
            env: "dev".to_owned(),
            addr_http: SocketAddr::new(Ipv4Addr::LOCALHOST.into(), 8000),
            addr_ws: SocketAddr::new(Ipv4Addr::LOCALHOST.into(), 9000),
            devices: tokio::sync::Mutex::new(HashMap::new()),
        }
    }
}
impl AppContext {
    /// Create app wide Context, available in all handlers.
    /// Context must be static, Sync, and Send.
    /// Interior mutability can be made with sync primitives or other methods.
    pub fn create() -> Result<&'static mut Self, Box<dyn Error>> {
        let ctx = Box::new(AppContext::default());
        let ctx = Box::leak(ctx);

        Ok(ctx)
    }
}
async fn start_servers(ctx: &'static AppContext) {
    let http_listener = tokio::net::TcpListener::bind(ctx.addr_http)
        .await
        .unwrap_or_else(|err| {
            tracing::error!(
                "failure to bind the TCP listener for HTTP server to {}:\n{err}",
                ctx.addr_http
            );
            panic!("{err}")
        });

    let ws_listener = tokio::net::TcpListener::bind(ctx.addr_ws)
        .await
        .unwrap_or_else(|err| {
            tracing::error!(
                "failure to bind the TCP listener for HTTP server to {}:\n{err}",
                ctx.addr_ws
            );
            panic!("{err}")
        });
    let http_handle = tokio::spawn(super::http::handle_http(http_listener, ctx));
    let ws_handle = tokio::spawn(super::ws::handle_ws(ws_listener, ctx));
    let _ = tokio::join!(http_handle, ws_handle);
}

fn setup_tracing_subscriber() {
    let subscriber = tracing_subscriber::FmtSubscriber::builder()
        .with_writer(std::io::stderr)
        .with_max_level(LevelFilter::TRACE)
        .finish();
    tracing::subscriber::set_global_default(subscriber).expect("Setting default subscriber failed");
    tracing::debug!("tracing subscriber set up");
}
