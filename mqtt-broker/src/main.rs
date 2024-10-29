use std::sync::Arc;

use rumqttd::{Broker, Config};

use tracing::info;

mod auth;
fn main() {
    let builder = tracing_subscriber::fmt()
        .pretty()
        .with_line_number(false)
        .with_file(false)
        .with_thread_ids(false)
        .with_thread_names(false);

    builder
        .try_init()
        .expect("initialized subscriber succesfully");

    info!("Preparing to run MQTT Broker");

    let config = config::Config::builder()
        //  let file =  concat!(env!("CARGO_MANIFEST_DIR"), "rumqttd.tom");
        .add_source(config::File::with_name("rumqttd.toml"))
        .build()
        .unwrap();

    let mut config: Config = config.try_deserialize().unwrap();

    let server = config.v5
        .as_mut()
        .and_then(|v5| v5.get_mut("1"))
        .expect("expected key 1 but get none");
    info!("Got MQTT v5.1 server");

    server.connections.external_auth = Some(Arc::new(auth::auth));
    info!("Auth handler added.");

    let mut broker = Broker::new(config);

    info!("Broker constructed, now running...");

    broker.start().unwrap();
}
