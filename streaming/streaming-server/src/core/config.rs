use anyhow::{self, Context};
use serde::Serialize;
use std::fmt::Display;
use std::str::FromStr;
use std::{fs::File, io::Read, net::SocketAddr};
use yaml_rust2::{Yaml, YamlLoader};

/// Global App environment
#[derive(Debug, Clone, Default, Serialize)]
pub enum Env {
    #[default]
    Developement,
    Production,
}
impl FromStr for Env {
    type Err = anyhow::Error;
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s.to_lowercase().as_str() {
            "development" => Ok(Self::Developement),
            "production" => Ok(Self::Production),
            _ => Err(anyhow::Error::msg(format!("invalid environment: {s}"))),
        }
    }
}
impl Display for Env {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.write_str(match self {
            Self::Developement => "development",
            Self::Production => "production",
        })
    }
}

/// Global App config related to HTTP server
#[derive(Debug, Clone, Serialize)]
pub struct HttpConfig {
    pub address: SocketAddr,
}
/// Global App config related to WebSocket server
#[derive(Debug, Clone, Serialize)]
pub struct WsConfig {
    pub address: SocketAddr,
}

#[allow(unused)]
/// Global App config. Available from AppContext.
#[derive(Debug, Clone, Serialize)]
pub struct AppConfig {
    pub env: Env,
    pub db_uri: String,
    pub http: HttpConfig,
    pub ws: WsConfig,
    pub log: String,
}

impl AppConfig {
    pub fn load_from_yaml(path: &str) -> anyhow::Result<Self> {
        // Read YAML file into a string
        let mut s = String::with_capacity(1024);
        File::open(path)
            .with_context(|| format!("Failed to open the YAML file at {path}"))?
            .read_to_string(&mut s)?;

        // Parse YAML content
        let yaml = YamlLoader::load_from_str(&s)?;
        let yaml = yaml
            .first()
            .cloned()
            .ok_or_else(|| anyhow::Error::msg("Empty or invalid YAML file"))?;
        let env = yaml["env"]
            .as_str()
            .ok_or_else(|| anyhow::Error::msg("env string not found"))?;
        let env = Env::from_str(env)?;

        Self::load_from_env(yaml, env)
    }

    fn load_from_env(yaml: Yaml, env: Env) -> anyhow::Result<Self> {
        let env_str = env.to_string();
        let yaml = yaml[env_str.as_str()].clone();
        if yaml.is_badvalue() || yaml.is_null() {
            return Err(anyhow::Error::msg(
                "cannot find selected environment YAML configuration",
            ));
        }

        let db_uri = yaml["db_uri"]
            .as_str()
            .ok_or_else(|| anyhow::Error::msg("Missing or invalid `db_uri`"))?
            .to_string();

        let http_addr = yaml["http"]["addr"]
            .as_str()
            .ok_or_else(|| anyhow::Error::msg("Missing or invalid `http.addr`"))?
            .to_string();

        let http_port = yaml["http"]["port"]
            .as_i64()
            .ok_or_else(|| anyhow::Error::msg("Missing or invalid `http.port`"))?
            as u16;

        let http = HttpConfig {
            address: format!("{}:{}", http_addr, http_port).parse()?,
        };

        // Parse WebSocket config
        let ws_addr = yaml["ws"]["addr"]
            .as_str()
            .ok_or_else(|| anyhow::Error::msg("Missing or invalid `ws.addr`"))?
            .to_string();
        let ws_port = yaml["ws"]["port"]
            .as_i64()
            .ok_or_else(|| anyhow::Error::msg("Missing or invalid `ws.port`"))?
            as u16;

        let ws = WsConfig {
            address: format!("{}:{}", ws_addr, ws_port).parse()?,
        };

        let log = yaml["log"]
            .as_str()
            .ok_or_else(|| anyhow::Error::msg("Missing or invalid log level"))?
            .to_string();

        Ok(Self {
            env,
            db_uri,
            log,
            http,
            ws,
        })
    }
}
