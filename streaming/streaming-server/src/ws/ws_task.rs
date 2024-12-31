//! This module exports WebSocket task handling the lifecycle of a WebSocket.
//! Provides callbacks for on_message, on_init and on_cleanup.
use futures_util::{SinkExt, StreamExt};
use std::pin::Pin;
use tokio::io::{AsyncRead, AsyncWrite};
use tokio_tungstenite::WebSocketStream;
use tokio_tungstenite::tungstenite::Message;

pub type CallbackMutSelf<'a, S> = Box<
    dyn Fn(&mut WebSocketTask<'a, S>) -> Pin<Box<dyn Future<Output = anyhow::Result<()>> + Send>>
        + Send
        + 'a,
>;
pub type CallbackMessage<'a> = Box<
    dyn Fn(Message) -> Pin<Box<dyn Future<Output = anyhow::Result<Option<Message>>> + Send>>
        + Send
        + 'a,
>;

/// A continously run task that handles WebSocket during its lifecycle.
pub(crate) struct WebSocketTask<'a, S> {
    socket: WebSocketStream<S>,
    cb_init: Option<CallbackMutSelf<'a, S>>,
    cb_msg: Option<CallbackMessage<'a>>,
    cb_cln: Option<CallbackMutSelf<'a, S>>,
}
impl<'a, S: AsyncRead + AsyncWrite + Unpin> WebSocketTask<'a, S> {
    pub fn new(socket: WebSocketStream<S>) -> Self {
        Self {
            socket,
            cb_init: None,
            cb_cln: None,
            cb_msg: None,
        }
    }
    /// Initialize the WebSocket task.
    /// Returning Err means initialization failed and the task is aborted, and the cleanup is ran.
    pub fn on_init<F, Fut>(&mut self, callback: F)
    where
        F: Fn(&mut WebSocketTask<'a, S>) -> Fut + Send + 'a,
        Fut: Future<Output = anyhow::Result<()>> + Send + 'static,
    {
        self.cb_init = Some(Box::new(move |stream| Box::pin(callback(stream))));
    }
    /// Receive the message and optionally send one back by returning Ok(Some(msg)).
    /// Returning Err means the callback failed and the task is aborted, and the cleanup is ran.
    pub fn on_message<F, Fut>(&mut self, callback: F)
    where
        F: Fn(Message) -> Fut + Send + 'a,
        Fut: Future<Output = anyhow::Result<Option<Message>>> + Send + 'static,
    {
        self.cb_msg = Some(Box::new(move |msg| Box::pin(callback(msg))));
    }
    /// Cleanup the task after disconnection or error.
    pub fn on_cleanup<F, Fut>(&mut self, callback: F)
    where
        F: Fn(&mut WebSocketTask<'a, S>) -> Fut + Send + 'a,
        Fut: Future<Output = anyhow::Result<()>> + Send + 'static,
    {
        self.cb_cln = Some(Box::new(move |task| Box::pin(callback(task))));
    }

    pub async fn run(mut self) {
        let cb_msg = self.cb_msg.take();
        let cb_init = self.cb_init.take();
        let cb_cln = self.cb_cln.take();

        if let Some(cb) = cb_init {
            if cb(&mut self).await.is_err() {
                return;
            }
        }

        while let Some(msg) = self.socket.next().await {
            match msg {
                Err(_) => break,
                Ok(msg) => {
                    if let Some(cb) = &cb_msg {
                        match cb(msg).await {
                            Ok(Some(res)) => {
                                if self.socket.send(res).await.is_err() {
                                    break;
                                }
                            }
                            Ok(None) => continue,
                            Err(_) => break,
                        }
                    }
                }
            }
        }

        if let Some(cb) = cb_cln {
            let _ = cb(&mut self).await;
        }
    }
}

#[allow(unreachable_code, unused_must_use, clippy::no_effect)]
#[cfg(test)]
/// ## Important!
/// All test here are just testing if the module can compile!
mod tests {
    use super::WebSocketTask;

    async fn __test_async_fn() {}

    #[ignore]
    #[test]
    fn test_callback_init() {
        async {
            let _task: WebSocketTask<'_, tokio::net::TcpStream> = WebSocketTask::new(todo!());
            _task.on_init(|_task| async {
                __test_async_fn().await;

                Ok(())
            });
        };
    }

    #[ignore]
    #[test]
    fn test_callback_msg() {
        async {
            let _task: WebSocketTask<'_, tokio::net::TcpStream> = WebSocketTask::new(todo!());
            _task.on_message(|_msg| async {
                __test_async_fn().await;

                Ok(None)
            });
        };
    }

    #[ignore]
    #[test]
    fn test_callback_cleanup() {
        async {
            let _task: WebSocketTask<'_, tokio::net::TcpStream> = WebSocketTask::new(todo!());
            _task.on_cleanup(|_task| async {
                __test_async_fn().await;

                Ok(())
            });
        };
    }
}
