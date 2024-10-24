lazy_static! {
    static ref BACKEND_URL: Url = Url::from_str(&std::env::var("BACKEND_URL").unwrap()).unwrap();
}

#[derive(Serialize, Deserialize)]
struct AuthBody {
    name: String,
    password: String,
}
use std::str::FromStr;

use lazy_static::lazy_static;
use reqwest::{StatusCode, Url};
use serde::{Deserialize, Serialize};

pub fn auth(_client_id: String, name: String, password: String) -> bool {
    let client = reqwest::blocking::Client::new();
    if let Ok(body) = serde_json::to_string(&AuthBody { name, password }) {
        let res = client.post(BACKEND_URL.as_str()).body(body).send();
        if res.is_err() {
            return false;
        }
        let res = res.expect("already checked");
        return res.status() == StatusCode::OK;
    }
    false
}

#[cfg(test)]
mod tests {
    use super::*;
    const TEST_USERNAME: &str = "test";
    const TEST_PASS: &str = "12345678";
    #[test]
    pub fn test_request_body() {
        let mut body = serde_json::to_string(&AuthBody {
            name: TEST_USERNAME.into(),
            password: TEST_PASS.into(),
        })
        .unwrap();
        body.retain(|char| !char.is_whitespace());
        assert_eq!(body, "{\"name\":\"test\",\"password\":\"12345678\"}");
    }
}
