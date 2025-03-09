use sqlx::prelude::FromRow;

#[derive(Debug, Clone, serde::Serialize, serde::Deserialize, FromRow)]
pub struct Device {
    pub id: super::CoreDBId,
    pub address: String,
    pub device_name: String,
    pub uuid: String,
    pub user_id: super::CoreDBId,
}

impl Device {
    pub async fn find_by_id(
        ctx: &'static crate::core::context::AppContext,
        id: super::CoreDBId,
    ) -> anyhow::Result<Self> {
        let query = sqlx::query(
            "SELECT id, address, device_name, uuid, user_id 
            FROM devices 
            WHERE id = $1",
        )
        .bind(id);
        let row = query.fetch_one(&ctx.core_db).await.inspect_err(|e| {
            tracing::debug!("cannot fetch device {id}: {e}");
        })?;
        Device::from_row(&row).map_err(|e| {
            tracing::error!(event = "bad_row_parse", "cannot parse device from row: {e}");
            anyhow::Error::msg("cannot parse device from row")
        })
    }
    #[allow(unused)]
    pub async fn find_by_uuid(
        ctx: &'static crate::core::context::AppContext,
        uuid: &str,
    ) -> anyhow::Result<Self> {
        let query = sqlx::query(
            "SELECT id, address, device_name, uuid, user_id 
            FROM devices 
            WHERE uuid = $1",
        )
        .bind(uuid);
        let row = query.fetch_one(&ctx.core_db).await.inspect_err(|e| {
            tracing::debug!("cannot fetch device {uuid}: {e}");
        })?;
        Device::from_row(&row).map_err(|e| {
            tracing::error!(event = "bad_row_parse", "cannot parse device from row: {e}");
            anyhow::Error::msg("cannot parse device from row")
        })
    }
}
