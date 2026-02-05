//! This module is responsible for IP utils, like get IP from request etc.

use ipnet::IpNet;

use crate::core::{config::AppConfig, http::AppRequest};
use std::{net::IpAddr, str::FromStr};

#[allow(unused)]
pub fn get_ip_from_request(req: &AppRequest) -> Option<String> {
    let header = req.headers().get(hyper::header::FORWARDED)?;
    tracing::debug!("Extracted FORWARDED header: {:?}", header);
    header.to_str().ok().and_then(|v| Some(v.to_owned()))
}


pub fn filter_non_service_ips(cfg: &AppConfig, req: &AppRequest) -> bool {
    let ip = match get_ip_from_request(req) {
        Some(ip) => ip,
        None => {
            tracing::error!("Failed to extract request IP");
            return false;
        }
    };

    is_allowed_service_ip(cfg, &ip)
}

pub fn is_allowed_service_ip(cfg: &AppConfig, ip: &str) -> bool {
    let allowed = cfg.allowed_ips.iter().any(|rule| {
        // check exact match for dns names etc.
        if rule == ip {
            return true
        }
        // then try parsing it to ip rules
        let ip =  IpAddr::from_str(ip);
        if ip.is_err() {
            return false;
        }
        let ip = ip.unwrap();
        match parse_allowed_rule(rule) {
            Some(net) => net.contains(&ip),
            None => {
                tracing::warn!("Invalid allowed IP rule: {}", rule);
                false
            }
        }
    });

    if !allowed {
        tracing::warn!("Unauthorized access from {} to a service endpoint", ip);
    }

    allowed
}

fn wildcard_to_cidr(wildcard: &str) -> Option<IpNet> {
    // Example: 172.18.*.* → 172.18.0.0/16
    let parts: Vec<&str> = wildcard.split('.').collect();
    if parts.len() != 4 {
        return None;
    }

    let mut fixed = Vec::new();
    let mut bits = 0;

    for part in parts {
        if part == "*" {
            fixed.push("0");
        } else {
            fixed.push(part);
            bits += 8;
        }
    }

    let ip = fixed.join(".");
    format!("{}/{}", ip, bits).parse().ok()
}

fn parse_allowed_rule(rule: &str) -> Option<IpNet> {
    // 1. CIDR
    if let Ok(net) = rule.parse::<IpNet>() {
        return Some(net);
    }

    // 2. Exact IP
    if let Ok(ip) = rule.parse::<IpAddr>() {
        return Some(IpNet::from(ip));
    }

    // 3. Wildcard
    if rule.contains('*') {
        return wildcard_to_cidr(rule);
    }

    None
}


#[cfg(test)]
mod iputils_test {
    use std::net::{Ipv4Addr, SocketAddrV4};

    use super::*;


    fn create_ip_config() -> AppConfig {
        AppConfig {
        env: crate::core::config::Env::Developement,
        db_uri: ".".to_owned(),
        http: crate::core::config::HttpConfig { 
            address: std::net::SocketAddr::V4(SocketAddrV4::new(Ipv4Addr::LOCALHOST, 9002)),
             allow_origin: "*".to_owned()
        },
        ws: crate::core::config::WsConfig { 
            address: std::net::SocketAddr::V4(SocketAddrV4::new(Ipv4Addr::LOCALHOST, 9002)),
        },
        log: "debug".to_owned(),
        app_db_uri: ".".to_owned(),
        tokens_are_ids: true,
        allowed_ips: vec!["ai-service".to_owned(), "127.18.*.*".to_owned(), "10.0.0.0/8".to_owned()]
    }
    }

    #[test]
    fn test_ip_wildcard() {
        let cfg = create_ip_config();
        assert!(is_allowed_service_ip(&cfg, "127.18.1.0"));
        assert!(!is_allowed_service_ip(&cfg, "127.11.1.0"));
    }

    #[test]
    fn test_ip_ranges() {
        let cfg = create_ip_config();
        assert!(is_allowed_service_ip(&cfg, "10.0.1.1"));
    }
  #[test]
    fn test_dns() {
        let cfg = create_ip_config();
        assert!(is_allowed_service_ip(&cfg, "ai-service"));
        assert!(!is_allowed_service_ip(&cfg, "ai-service-not!"));
    }
}
