#[tokio::main]
async fn main() -> loco_rs::Result<()> {
    ass_backend::mqtt::main();
    Ok(())
}
