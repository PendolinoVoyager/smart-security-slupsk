//! This module contains handlers for WebSocket connections, meaning:
//! - no response,
//! - takes WebSocket and its initial request

mod device_checkout_handler;
mod stream_handler;
pub use device_checkout_handler::*;
pub use stream_handler::*;
