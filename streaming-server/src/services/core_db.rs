use sqlx::postgres::PgPoolOptions;
use sqlx::{Pool, Postgres, Row};

use crate::core::config::AppConfig;
use crate::core::context::AppContext;

pub async fn init(cfg: &AppConfig) -> Result<Pool<Postgres>, sqlx::Error> {
    tracing::debug!("Attempting to connect to the database...");
    PgPoolOptions::new()
        .max_connections(5)
        .connect(&cfg.db_uri)
        .await
}
pub type CoreDBId = i32;

pub async fn find_user_id(
    ctx: &'static AppContext,
    email: &str,
) -> anyhow::Result<Option<CoreDBId>> {
    let query = sqlx::query(
        "SELECT id 
        FROM users 
        WHERE email = $1",
    )
    .bind(email);
    let row = query.fetch_one(&ctx.core_db).await.inspect_err(|e| {
        tracing::debug!("cannot fetch user {email}: {e}");
    })?;

    Ok(row.try_get("id")?)
}
