use chrono::NaiveDateTime;
use sqlx::prelude::FromRow;

use crate::core::context::AppContext;

#[derive(Debug, Clone, serde::Serialize, serde::Deserialize, FromRow)]
pub struct User {
    pub id: super::CoreDBId,
    pub email: String,
    pub name: String,
    pub last_name: String,
    pub password: String,
    pub role: String,
    pub created_at: NaiveDateTime,
    pub is_enabled: bool,
}

impl User {
    pub async fn find_by_email(ctx: &'static AppContext, email: &str) -> anyhow::Result<Self> {
        let query = sqlx::query(
            "SELECT id, email, name, last_name, password, role, created_at, is_enabled
            FROM users 
            WHERE email = $1",
        )
        .bind(email);
        let row = query.fetch_one(&ctx.core_db).await.inspect_err(|e| {
            tracing::debug!("cannot fetch user {email}: {e}");
        })?;
        User::from_row(&row).map_err(|e| {
            tracing::error!(event = "bad_row_parse", "cannot parse user from row: {e}");
            anyhow::Error::msg("cannot parse user from row")
        })
    }
    pub async fn find_by_id(ctx: &'static AppContext, id: super::CoreDBId) -> anyhow::Result<Self> {
        let query = sqlx::query(
            "SELECT id, email, name, last_name, password, role, created_at, is_enabled
            FROM users 
            WHERE id = $1",
        )
        .bind(id);
        let row = query.fetch_one(&ctx.core_db).await.inspect_err(|e| {
            tracing::debug!("cannot fetch user {id}: {e}");
        })?;
        User::from_row(&row).map_err(|e| {
            tracing::error!(event = "bad_row_parse", "cannot parse user from row: {e}");
            anyhow::Error::msg("cannot parse user from row")
        })
    }
}
