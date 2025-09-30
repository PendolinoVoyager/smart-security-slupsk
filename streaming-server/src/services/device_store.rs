//! Service for local device and channels managment.
//! When a device registers itself, it will produce a message channel receiver, which guarantees to send only stream data.
//! Additionally, there is a sender responsible for commands.

use std::collections::HashMap;
use tokio::sync::broadcast::*;
use tokio_tungstenite::tungstenite::Message;

use super::core_db::CoreDBId;
/// Device registered in DeviceStore
#[derive(Debug, Clone)]
pub struct Device {
    pub id: CoreDBId,
    pub stream_receiver: Sender<Message>,
}
impl Device {
    pub fn new(id: CoreDBId, stream_receiver: Sender<Message>) -> Self {
        Self {
            id,
            stream_receiver,
        }
    }
}

type DeviceId = crate::services::core_db::CoreDBId;
#[derive(Default, Debug)]
pub struct DeviceStore {
    devices: HashMap<DeviceId, Device>,
}

impl DeviceStore {
    /// Register a device for the first time.
    /// Should be only used once in a socket's lifetime: at its creation when the device sents its initial request.
    /// Consequently, calling poison_or_remove should be the end of the socket's lifecycle.
    /// This way, if something took the ownership of the device, when trying to return it,
    /// it will silently drop it instead of causing conflicts.
    pub fn register_device(
        &mut self,
        device: DeviceId,
        stream_receiver: Sender<Message>,
    ) -> anyhow::Result<()> {
        match self.devices.get(&device) {
            None => {
                self.devices
                    .insert(device, Device::new(device, stream_receiver));
            }
            Some(Device { id, .. }) => {
                return Err(anyhow::Error::msg(format!(
                    "device store already contains {id}"
                )));
            }
        }
        Ok(())
    }

    pub fn remove_device(&mut self, device: DeviceId) {
        self.devices.remove(&device);
    }
    /// Get the device from the store.
    pub fn get_device(&mut self, device: DeviceId) -> Option<Device> {
        self.devices.get(&device).cloned()
    }

    pub fn all_devices(&self) -> Vec<DeviceId> {
        self.devices.keys().cloned().collect()
    }
}
