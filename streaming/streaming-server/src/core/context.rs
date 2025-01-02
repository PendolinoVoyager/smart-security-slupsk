use super::config::AppConfig;

use sqlx::Postgres;
use tokio::sync::Mutex;
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
        let _ = crate::services::app_db::remove_all_connections(self).await;
    }
}
