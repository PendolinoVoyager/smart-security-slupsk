use rumqttc::tokio_rustls::rustls::{ClientConfig, RootCertStore};
use rumqttc::v5::Connection;
use rumqttc::MqttOptions;

pub fn run_client() {
    let options = MqttOptions::new("2", "10.189.0.120", 8883);
    // let store = RootCertStore::empty()
    //     .add("../../certs/mqtt.cert.pem")
    //     .unwrap();
    // let client_cfg = ClientConfig::builder().with_root_certificates(store);

    // let tls_cfg = rumqttc::TlsConfiguration::Rustls(client_cfg);
    let (client, connection) = rumqttc::Client::new(options, 1);
    println!("Hello from client");
}
