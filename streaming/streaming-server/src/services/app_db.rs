//! This module is responsible for handling the redis database for this service.
//! The core_db module is for the main Postgresql database
//! The entry point is `init` function.
//! Device connection schema in the database is as follows:
//! ```ignore
//! | ID |  DEVICE_NAME   | USER_ID |     SERVER_ADDR    |
//! -----+----------------+---------+---------------------
//! | 10 | Default Device |    1    | 154.21.23.211:8080 |
//! ```
//!

use std::net::SocketAddr;

use crate::core::config::AppConfig;
use crate::core::context::AppContext;
use deadpool_redis::Runtime;
use redis::AsyncCommands;
use sqlx::{Execute, Row};
/// Initialize the Redis connection pool from the config provided in AppConfig.
pub async fn init(
    cfg: &AppConfig,
) -> anyhow::Result<deadpool::managed::Pool<deadpool_redis::Manager, deadpool_redis::Connection>> {
    tracing::debug!("Attempting to connect to redis database...");

    let deadpool_cfg = deadpool_redis::Config::from_url(&cfg.app_db_uri);
    let pool = deadpool_cfg.create_pool(Some(Runtime::Tokio1))?;

    Ok(pool)
}

#[derive(Debug, Clone, serde::Serialize, serde::Deserialize)]
pub struct RedisDeviceSchema {
    device_name: String,
    user_id: i32,
    server_addr: SocketAddr,
}
impl RedisDeviceSchema {
    pub async fn fetch_from_sql(ctx: &AppContext, device_id: i64) -> anyhow::Result<Self> {
        let query =
            sqlx::query("SELECT device_name, user_id FROM devices WHERE id = $1").bind(device_id);
        tracing::debug!("executed query {}", query.sql());
        let row = query.fetch_one(&ctx.core_db).await.inspect_err(|e| {
            tracing::debug!("cannot fetch device {device_id}: {e}");
        })?;

        Ok(Self {
            device_name: row.try_get("device_name")?,
            user_id: row.try_get("user_id")?,
            server_addr: ctx.config.ws.address,
        })
    }
}
/// Gets the device (and check if exists) from the main database.
/// Then, it converts the value to redis value, as in schema defined in services::app_db module.
pub async fn register_device(ctx: &AppContext, device_id: i64) -> anyhow::Result<()> {
    let dev = RedisDeviceSchema::fetch_from_sql(ctx, device_id)
        .await
        .inspect_err(|e| tracing::info!("{e}"))?;
    tracing::debug!("attempting to insert device_id {device_id}");
    let dev = serde_json::to_string(&dev).inspect_err(|e| tracing::debug!("{e}"))?;
    ctx.app_db
        .get()
        .await
        .inspect_err(|e| tracing::debug!("{e}"))?
        .set::<&std::string::String, &str, String>(&device_id.to_string(), dev.as_str())
        .await
        .inspect_err(|e| tracing::debug!("{e}"))?;
    tracing::info!(event = "device_register", device_id = device_id.to_string());
    Ok(())
}

pub async fn remove_device(ctx: &AppContext, device_id: i64) -> anyhow::Result<()> {
    tracing::debug!("attempting to remove device_id {device_id}");
    ctx.app_db
        .get()
        .await?
        .del::<_, ()>(device_id.to_string())
        .await?;

    tracing::info!(event = "device_dropped", device_id = device_id.to_string());

    Ok(())
}

pub async fn remove_all_connections(ctx: &AppContext) -> anyhow::Result<usize> {
    let guard = ctx.devices.lock().await;
    let connections: Vec<String> = guard.all_devices().iter().map(|n| n.to_string()).collect();
    let mut conn = ctx.app_db.get().await?;
    let res = conn.del::<_, usize>(connections).await?;
    Ok(res)
}
