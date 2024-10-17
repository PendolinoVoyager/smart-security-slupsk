use ass_backend::app::App;
use loco_rs::{task, testing};

use loco_rs::boot::run_task;
use serial_test::serial;

#[tokio::test]
#[serial]
async fn test_can_run_register_device() {
    let boot = testing::boot_test::<App>().await.unwrap();

    assert!(
        run_task::<App>(&boot.app_context, Some(&"register_device".to_string()), &task::Vars::default())
            .await
            .is_ok()
    );
}
