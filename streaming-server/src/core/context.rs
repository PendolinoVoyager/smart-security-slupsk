use core::panic;

use crate::services::core_db::CoreDBId;
use crate::services::device_store::Device;

use super::config::AppConfig;

use sqlx::Postgres;
use tokio::sync::Mutex;
use tokio_tungstenite::tungstenite::Message;
/// App wide Context. Available in 'static lifetime for all tasks / function calls in the app
/// Context must be static, Sync, and Send.
/// Interior mutability can be made with sync primitives or other methods.
/// TODO: Make db generic over db type
#[derive(Debug)]
pub struct AppContext {
    /// Locally stored devices
    pub devices: Mutex<crate::services::device_store::DeviceStore>,
    /// App wide config
    pub config: AppConfig,
    /// Connection pool to main Postgre database
    pub core_db: sqlx::Pool<Postgres>,
    /// Connection pool to app redis DB to manage devices
    pub app_db: deadpool::managed::Pool<deadpool_redis::Manager, deadpool_redis::Connection>,
}

impl AppContext {
    /// Create app wide Context, available in all handlers.
    /// Context must be static, Sync, and Send.
    /// Interior mutability can be made with sync primitives or other methods.
    pub async fn create(config: AppConfig) -> anyhow::Result<&'static mut Self> {
        let core_db = crate::services::core_db::init(&config)
            .await
            .inspect_err(|e| {
                tracing::error!(
                    event = "db_connection_failed",
                    uri = config.db_uri,
                    err = e.to_string()
                );
            })?;
        tracing::info!(event = "db_connection");
        let app_db = crate::services::app_db::init(&config)
            .await
            .inspect_err(|e| {
                tracing::error!(
                    event = "redis_connection_failed",
                    uri = config.app_db_uri,
                    err = e.to_string()
                )
            })?;
        tracing::info!(event = "app_db_connection");

        let ctx = Box::new(Self {
            devices: Mutex::new(crate::services::device_store::DeviceStore::default()),
            config,
            core_db,
            app_db,
        });
        let ctx = Box::leak(ctx);

        Ok(ctx)
    }

    /// Clean up self without dropping so other threads don't do stupid stuff with memory.
    pub async fn cleanup(&'static self) {
        let _ = crate::services::app_db::RedisDeviceSchema::remove_all_connections(self).await;
    }
    /// Register a device to the database and store. If either fails, a cleanup will be performed in both of them.
    pub async fn register_device(
        &'static self,
        device_id: crate::services::core_db::CoreDBId,
        stream_receiver: tokio::sync::mpsc::Receiver<Message>,
        command_sender: tokio::sync::mpsc::Sender<Message>,
    ) -> anyhow::Result<()> {
        let store_fut = self.devices.lock();
        let redis_fut =
            crate::services::app_db::RedisDeviceSchema::register_device(self, device_id);
        let (mut store, redis_result) = tokio::join!(store_fut, redis_fut);

        redis_result?;

        let store_result = store.register_device(device_id, stream_receiver, command_sender);

        if store_result.is_err() {
            crate::services::app_db::RedisDeviceSchema::remove_device(self, device_id).await?;
            return store_result;
        }
        Ok(())
    }

    pub async fn get_device(&'static self, device_id: CoreDBId) -> anyhow::Result<Device> {
        let mut conn = self.app_db.get().await?;
        let store_fut = self.devices.lock();
        let redis_fut = crate::services::app_db::RedisDeviceSchema::get(&mut conn, device_id);
        let (mut store, redis_result) = tokio::join!(store_fut, redis_fut);
        let _redis_result = redis_result?;

        match store.get_device(device_id) {
            Some(d) => Ok(d),
            None => panic!("fatal synchronization error:\n{device_id} is missing but is in redis"),
        }
    }
    pub async fn return_device(&'static self, device: Device) -> anyhow::Result<()> {
        let mut store = self.devices.lock().await;
        store.return_device(device);
        Ok(())
    }

    pub async fn remove_device(&'static self, device_id: CoreDBId) -> anyhow::Result<()> {
        let redis_fut = crate::services::app_db::RedisDeviceSchema::remove_device(self, device_id);
        let store_fut = self.devices.lock();
        let (mut store, redis_res) = tokio::join!(store_fut, redis_fut);
        store.poison_or_remove(device_id);
        redis_res
    }
}
