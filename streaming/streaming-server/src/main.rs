mod core;

#[tokio::main(flavor = "multi_thread")]
async fn main() {
    crate::core::app::init_app().await;
}
