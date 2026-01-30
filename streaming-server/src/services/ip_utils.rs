//! This module is responsible for IP utils, like get IP from request etc.

use crate::core::{config::AppConfig, http::AppRequest};

#[allow(unused)]
pub fn get_ip_from_request(req: &AppRequest) -> Option<&str>{
    req.headers().get(hyper::header::FORWARDED)
                .and_then(|hyper_value| hyper_value.to_str().ok())
}

pub fn filter_non_service_ips(cfg: &AppConfig, req: &AppRequest) -> bool {
    let ip = req.headers().get(hyper::header::FORWARDED);
    if ip.is_none() {
        tracing::error!("Failed to get request ip! This shouldn't ever happen.");
        return false;
    }
    let ip = ip.unwrap().to_str();
    match ip {
        Ok(ip) => is_allowed_service_ip(cfg, ip),
        Err(e) => {
            tracing::warn!("Cannot extract ip.\nError {:?}.",  &e);
            return false
        },
    }
    

}
pub fn is_allowed_service_ip(cfg: &AppConfig, ip: &str) -> bool {
    let res = cfg.allowed_ips.iter().any(|allowed| allowed == ip);
    if res == false {
        tracing::warn!("Unauthorized access from {} to a service endpoint", ip); 
    }
    res
}
