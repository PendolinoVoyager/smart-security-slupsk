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
use deadpool_redis::{Connection, Runtime};
use redis::{AsyncCommands, FromRedisValue, JsonAsyncCommands, cmd};
use serde::de::Error;
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
    pub id: i32,
    pub device_name: String,
    pub user_id: i32,
    pub server_addr: SocketAddr,
}
impl FromRedisValue for RedisDeviceSchema {
    fn from_redis_value(v: &redis::Value) -> redis::RedisResult<Self> {
        match v {
            redis::Value::BulkString(vec) => Ok(serde_json::from_slice(vec)?),
            redis::Value::SimpleString(s) => Ok(serde_json::from_str(s)?),
            // I can't seem to create RedisError other than casting a impl Error value
            _ => {
                let e: redis::RedisError = serde_json::Error::custom(format!(
                    "cannot deserialize to RedisDeviceSchema: {v:?} "
                ))
                .into();
                Err(e)
            }
        }
    }
}
impl RedisDeviceSchema {
    pub async fn fetch_from_sql(ctx: &AppContext, device_id: i32) -> anyhow::Result<Self> {
        let query =
            sqlx::query("SELECT device_name, user_id FROM devices WHERE id = $1").bind(device_id);
        tracing::debug!("executed query {}", query.sql());
        let row = query.fetch_one(&ctx.core_db).await.inspect_err(|e| {
            tracing::debug!("cannot fetch device {device_id}: {e}");
        })?;

        Ok(Self {
            id: device_id,
            device_name: row.try_get("device_name")?,
            user_id: row.try_get("user_id")?,
            server_addr: ctx.config.ws.address,
        })
    }
    pub async fn find_by_user(
        conn: &mut Connection,
        user_id: i32,
    ) -> anyhow::Result<Vec<RedisDeviceSchema>> {
        let query = format!("@user_id:[{user_id}, {user_id}]");
        let result = cmd("FT.SEARCH")
            .arg("idx_device")
            .arg(query)
            .query_async::<Vec<redis::Value>>(conn)
            .await
            .inspect_err(|e| tracing::debug!("search error: {e}"))?;
        tracing::debug!("{result:?}");

        // assuming result is like this:
        // redis_array[<total_count>, key, redis_array[trash_path, json], key, redis_array[trash_path, json]... ]
        // the array goes: [key, value, key, value...]
        // so take only odd indices in the array, and assume they are also an array
        Ok(result
            .into_iter()
            .skip(2)
            .step_by(2)
            .flat_map(|v| {
                if let redis::Value::Array(v) = v {
                    Self::from_redis_value(&v[1]).ok()
                } else {
                    None
                }
            })
            .collect())
    }

    /// Get all the devices registered in Redis database.
    /// Don't use this unless sure you want to debug or allow admin to do something
    #[allow(unused)]
    pub async fn find_all(conn: &mut Connection) -> anyhow::Result<Vec<RedisDeviceSchema>> {
        let keys: Vec<String> = conn.keys("device:*").await?;
        Ok(conn.json_get(keys, ".").await?)
    }
}
/// Gets the device (and check if exists) from the main database.
/// Then, it converts the value to redis value, as in schema defined in services::app_db module.
pub async fn register_device(ctx: &AppContext, device_id: i32) -> anyhow::Result<()> {
    let dev = RedisDeviceSchema::fetch_from_sql(ctx, device_id)
        .await
        .inspect_err(|e| tracing::info!("{e}"))?;
    tracing::debug!("attempting to insert device_id {device_id}");
    let dev = serde_json::to_string(&dev).inspect_err(|e| tracing::debug!("{e}"))?;
    let mut conn = ctx
        .app_db
        .get()
        .await
        .inspect_err(|e| tracing::debug!("{e}"))?;
    tracing::debug!("inserting device:\n{dev}");
    // create a value
    cmd("JSON.SET")
        .arg(format!("device:{device_id}"))
        .arg("$")
        .arg(dev)
        .query_async::<()>(&mut conn)
        .await
        .inspect_err(|e| tracing::debug!("{e}"))?;

    tracing::info!(event = "device_register", device_id = device_id.to_string());
    Ok(())
}

pub async fn remove_device(ctx: &AppContext, device_id: i32) -> anyhow::Result<()> {
    tracing::debug!("attempting to remove device_id {device_id}");
    let mut conn = ctx.app_db.get().await?;

    cmd("JSON.DEL")
        .arg(format!("device:{device_id}"))
        .arg("$")
        .exec_async(&mut conn)
        .await
        .inspect_err(|e| tracing::debug!("error deleting device: {e}"))?;

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
