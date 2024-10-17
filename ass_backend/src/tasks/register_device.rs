use loco_rs::prelude::*;

pub struct RegisterDevice;
#[async_trait]
impl Task for RegisterDevice {
    fn task(&self) -> TaskInfo {
        TaskInfo {
            name: "register_device".to_string(),
            detail:
                "Register a device. It will create an unowned device, with \"user_id\" field NULL."
                    .to_string(),
        }
    }
    async fn run(&self, _app_context: &AppContext, _vars: &task::Vars) -> Result<()> {
        println!("Task RegisterDevice generated");
        Ok(())
    }
}
