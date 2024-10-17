#[tokio::main]
async fn main() -> loco_rs::Result<()> {
    ass_backend::mqtt::run_client();
    Ok(())
}
