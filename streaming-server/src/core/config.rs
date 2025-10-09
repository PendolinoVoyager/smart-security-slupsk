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
    /// CORS allow origin. Defaults to "*"
    pub allow_origin: String,
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
    pub app_db_uri: String,
    pub tokens_are_ids: bool,
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

        let ret = Self::load_from_env(yaml, env);

        match ret {
            Ok(mut cfg) if std::env::var("STRSRV_ENV_ON").unwrap_or_default() == "1" => {
                cfg.update_with_env_vars();
                Ok(cfg)
            }
            _ => ret,
        }
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

        let app_db_uri = yaml["redis_db_uri"]
            .as_str()
            .ok_or_else(|| anyhow::Error::msg("Invalid redis uri string"))?
            .to_string();

        let http_addr = yaml["http"]["addr"]
            .as_str()
            .ok_or_else(|| anyhow::Error::msg("Missing or invalid `http.addr`"))?
            .to_string();

        let http_port = yaml["http"]["port"]
            .as_i64()
            .ok_or_else(|| anyhow::Error::msg("Missing or invalid `http.port`"))?
            as u16;

        let allow_origin = yaml["http"]["allow-origin"]
            .as_str()
            .unwrap_or("*")
            .to_owned();

        let http = HttpConfig {
            address: format!("{}:{}", http_addr, http_port).parse()?,
            allow_origin,
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

        let tokens_are_ids = yaml["tokens_are_ids"].as_bool().unwrap_or(false);

        Ok(Self {
            env,
            db_uri,
            log,
            http,
            ws,
            app_db_uri,
            tokens_are_ids,
        })
    }
    // Override self values with environment variables if they exist
    fn update_with_env_vars(&mut self) {
        use std::env::var_os;

        if let Some(log) = var_os("STRSRV_LOG")
            && let Some(log) = log.to_str()
        {
            self.log = log.to_string();
        }

        if let Some(env) = var_os("STRSRV_ENV")
            && let Some(env) = env.to_str()
            && let Ok(env) = Env::from_str(env)
        {
            self.env = env;
        }

        if let Some(tokens_are_ids) = var_os("STRSRV_TOKENS_ARE_IDS")
            && let Some(tokens_are_ids) = tokens_are_ids.to_str()
            && let Ok(tokens_are_ids) = tokens_are_ids.parse::<bool>()
        {
            self.tokens_are_ids = tokens_are_ids;
        }

        if let Some(db_uri) = var_os("STRSRV_DB_URI")
            && let Some(db_uri) = db_uri.to_str()
        {
            self.db_uri = db_uri.to_string();
        }

        if let Some(app_db_uri) = var_os("STRSRV_REDIS_DB_URI")
            && let Some(app_db_uri) = app_db_uri.to_str()
        {
            self.app_db_uri = app_db_uri.to_string();
        }

        if let Some(http_addr) = var_os("STRSRV_HTTP_ADDR")
            && let Some(http_addr) = http_addr.to_str()
        {
            let port = self.http.address.port();
            self.http.address = format!("{}:{}", http_addr, port)
                .parse()
                .unwrap_or(self.http.address);
        }

        if let Some(http_port) = var_os("STRSRV_HTTP_PORT")
            && let Some(http_port) = http_port.to_str().and_then(|p| p.parse::<u16>().ok())
        {
            let addr = self.http.address.ip();
            self.http.address = format!("{}:{}", addr, http_port)
                .parse()
                .unwrap_or(self.http.address);
        }

        if let Some(ws_addr) = var_os("STRSRV_WS_ADDR")
            && let Some(ws_addr) = ws_addr.to_str()
        {
            let port = self.ws.address.port();
            self.ws.address = format!("{}:{}", ws_addr, port)
                .parse()
                .unwrap_or(self.ws.address);
        }

        if let Some(ws_port) = var_os("STRSRV_WS_PORT")
            && let Some(ws_port) = ws_port.to_str().and_then(|p| p.parse::<u16>().ok())
        {
            let addr = self.ws.address.ip();
            self.ws.address = format!("{}:{}", addr, ws_port)
                .parse()
                .unwrap_or(self.ws.address);
        }
    }
}
