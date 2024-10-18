use std::sync::Arc;

use once_cell::sync::OnceCell;
use rumqttd::{Broker, Config};
use sqlx::postgres::PgPoolOptions;
use sqlx::{Pool, Postgres};

use tracing::{debug, info};

#[tokio::main]
async fn main() {
    let builder = tracing_subscriber::fmt()
        .pretty()
        .with_line_number(false)
        .with_file(false)
        .with_thread_ids(false)
        .with_thread_names(false);

    builder
        .try_init()
        .expect("initialized subscriber succesfully");
    init_db().await;
    info!("Preparing to run MQTT Broker");

    let config = config::Config::builder()
        //  let file =  concat!(env!("CARGO_MANIFEST_DIR"), "rumqttd.tom");
        .add_source(config::File::with_name("rumqttd.toml"))
        .build()
        .unwrap();

    let mut config: Config = config.try_deserialize().unwrap();

    let server = config.v5.as_mut().and_then(|v5| v5.get_mut("1")).unwrap();
    info!("Got MQTT v5.1 server");

    server.connections.external_auth = Some(Arc::new(auth));
    info!("Auth handler added.");

    let mut broker = Broker::new(config);

    info!("Broker constructed, now running...");

    broker.start().unwrap();
}

/// Database connection. It should be initialized at the beginning, so expect for "get" to work.
/// It's Send + Sync, and only requires to get the connection from the pool to start working
static DB: OnceCell<Pool<Postgres>> = OnceCell::new();
/// Initialize the connection to the database and store it in OnceCell.
async fn init_db() {
    info!("Attempting connection to the database...");
    let pool = PgPoolOptions::new()
        .max_connections(5)
        .connect("postgres://master:12345678@localhost/ass_dev")
        .await
        .unwrap();
    info!("Database connection acquired");

    DB.set(pool).unwrap();
}

fn auth(_client_id: String, username: String, password: String) -> bool {
    debug!("Connection {username} with password {password}");

    if let Ok(res) = futures::executor::block_on(is_valid_device(&username, &password)) {
        return res;
    }
    // Errored so we have to return false
    false
}

/// Checks if the user is valid via database connection.
async fn is_valid_device(username: &str, password: &str) -> Result<bool, sqlx::Error> {
    let pool = DB.get().expect("Database not initialized");
    let conn = pool.acquire().await?;
    debug!("Fetching user...");

    Ok(true)
}
