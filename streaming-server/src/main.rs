mod core;
mod http;
mod services;
mod ws;

#[tokio::main(flavor = "multi_thread")]
async fn main() {
    let _ = crate::core::app::init_app().await;
}
