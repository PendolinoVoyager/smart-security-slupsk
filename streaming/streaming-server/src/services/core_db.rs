use sqlx::postgres::PgPoolOptions;
use sqlx::{Pool, Postgres};

use crate::core::config::AppConfig;

pub async fn init(cfg: &AppConfig) -> Result<Pool<Postgres>, sqlx::Error> {
    tracing::debug!("Attempting to connect to the database...");
    PgPoolOptions::new()
        .max_connections(5)
        .connect(&cfg.db_uri)
        .await
}