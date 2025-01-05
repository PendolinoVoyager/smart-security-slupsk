//! Service for local device and channels managment.
//! When a device registers itself, it will produce a message channel receiver, which guarantees to send only stream data.
//! Additionally, there is a sender responsible for commands.

use std::collections::HashMap;
use tokio::sync::mpsc::*;
use tokio_tungstenite::tungstenite::Message;
type DeviceId = i32;
/// Device registered in DeviceStore
#[derive(Debug)]
pub struct Device {
    pub stream_receiver: Receiver<Message>,
    pub command_sender: Sender<Message>,
}
impl Device {
    pub fn new(stream_receiver: Receiver<Message>, command_sender: Sender<Message>) -> Self {
        Self {
            stream_receiver,
            command_sender,
        }
    }
}

#[derive(Debug)]
enum DeviceSlot {
    Ready(Device),
    Taken,
    Poisoned,
}
#[derive(Default, Debug)]
pub struct DeviceStore {
    devices: HashMap<DeviceId, DeviceSlot>,
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
        stream_receiver: Receiver<Message>,
        command_sender: Sender<Message>,
    ) -> anyhow::Result<()> {
        match self.devices.get(&device) {
            None => {
                self.devices.insert(
                    device,
                    DeviceSlot::Ready(Device::new(stream_receiver, command_sender)),
                );
            }
            Some(DeviceSlot::Ready(_) | DeviceSlot::Taken) => {
                return Err(anyhow::Error::msg(format!(
                    "device store already contains {device}"
                )));
            }
            Some(DeviceSlot::Poisoned) => {
                return Err(anyhow::Error::msg(format!(
                    "device {device} poisoned, remove slot first to acknowledge"
                )));
            }
        }
        Ok(())
    }
    pub fn return_device(&mut self, device_id: DeviceId, device: Device) {
        match self.devices.get(&device_id) {
            Some(DeviceSlot::Taken) => {
                self.devices.insert(device_id, DeviceSlot::Ready(device));
            }
            _ => {
                tracing::debug!("attempted to reinsert device {device_id}");
            }
        }
    }
    pub fn remove_device(&mut self, device: DeviceId) {
        self.devices.remove(&device);
    }
    /// Get the device from the store.
    /// It will actually remove the device to prevent issues with borrowing.
    pub fn get_device(&mut self, device: DeviceId) -> Option<Device> {
        match self.devices.remove(&device) {
            Some(DeviceSlot::Ready(d)) => {
                self.devices.insert(device, DeviceSlot::Taken);
                Some(d)
            }
            _ => None,
        }
    }
    /// Poison the device slot to make the channels unusable and trying to insert them back error.
    ///
    /// Works only if the device is taken. Otherwise, it's meaningless to do so and will do nothing
    pub fn poison(&mut self, device: DeviceId) {
        if matches!(self.devices.get(&device), Some(DeviceSlot::Taken)) {
            self.devices.insert(device, DeviceSlot::Poisoned);
        }
    }
    /// Poison the device if borrowed, remove if it's ok to.
    pub fn poison_or_remove(&mut self, device: DeviceId) {
        match self.devices.remove(&device) {
            Some(DeviceSlot::Taken) => {
                self.poison(device);
            }
            _ => self.remove_device(device),
        }
    }
    pub fn all_devices(&self) -> Vec<DeviceId> {
        self.devices.keys().cloned().collect()
    }
}
