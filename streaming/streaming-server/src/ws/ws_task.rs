//! This module exports WebSocket task handling the lifecycle of a WebSocket.
//! Provides callbacks for on_message, on_init and on_cleanup.
use futures_util::{SinkExt, StreamExt};
use std::pin::Pin;
use tokio::io::{AsyncRead, AsyncWrite};
use tokio::sync::mpsc::*;
use tokio_tungstenite::WebSocketStream;
use tokio_tungstenite::tungstenite::Message;
pub type CallbackMutSelf<'a, S> = Box<
    dyn FnOnce(
            &mut WebSocketTask<'a, S>,
        ) -> Pin<Box<dyn Future<Output = anyhow::Result<()>> + Send>>
        + Send
        + 'a,
>;
pub type CallbackMessage<'a> = Box<
    dyn FnMut(Message) -> Pin<Box<dyn Future<Output = anyhow::Result<Option<Message>>> + Send + 'a>>
        + Send
        + 'a,
>;

/// A continously run task that handles WebSocket during its lifecycle.
pub(crate) struct WebSocketTask<'a, S> {
    socket: WebSocketStream<S>,
    message_queue: tokio::sync::mpsc::Receiver<Message>,
    cb_init: Option<CallbackMutSelf<'a, S>>,
    cb_msg: Option<CallbackMessage<'a>>,
    cb_cln: Option<CallbackMutSelf<'a, S>>,
}
impl<'a, S: AsyncRead + AsyncWrite + Unpin> WebSocketTask<'a, S> {
    /// Create a new task.
    /// Returns reference to task and a sender to send messages to the WebSocket.
    pub fn new(socket: WebSocketStream<S>) -> (Self, Sender<Message>) {
        let (tx, rx) = channel::<Message>(size_of::<Message>() * 10);

        (
            Self {
                socket,
                message_queue: rx,
                cb_init: None,
                cb_cln: None,
                cb_msg: None,
            },
            tx,
        )
    }
    /// Initialize the WebSocket task.
    /// Returning Err means initialization failed and the task is aborted, and the cleanup is ran.
    pub fn on_init<F, Fut>(&mut self, callback: F)
    where
        F: FnOnce(&mut WebSocketTask<'a, S>) -> Fut + Send + 'a,
        Fut: Future<Output = anyhow::Result<()>> + Send + 'static,
    {
        self.cb_init = Some(Box::new(move |stream| Box::pin(callback(stream))));
    }
    /// Receive the message and optionally send one back by returning Ok(Some(msg)).
    /// Returning Err means the callback failed and the task is aborted, and the cleanup is ran.
    pub fn on_message<F, Fut>(&mut self, mut callback: F)
    where
        F: FnMut(Message) -> Fut + Send + 'a,
        Fut: Future<Output = anyhow::Result<Option<Message>>> + Send + 'static,
    {
        self.cb_msg = Some(Box::new(move |msg| Box::pin(callback(msg))));
    }
    /// Cleanup the task after disconnection or error.
    pub fn on_cleanup<F, Fut>(&mut self, callback: F)
    where
        F: FnOnce(&mut WebSocketTask<'a, S>) -> Fut + Send + 'a,
        Fut: Future<Output = anyhow::Result<()>> + Send + 'static,
    {
        self.cb_cln = Some(Box::new(move |task| Box::pin(callback(task))));
    }
    /// Run the task, returning the WebSocket if it isn't shut down or error if something unexpected happens.
    pub async fn run(mut self) -> anyhow::Result<WebSocketStream<S>> {
        let cb_init = self.cb_init.take();
        let cb_cln = self.cb_cln.take();
        let cb_msg = self.cb_msg.take();
        let init_result = if let Some(cb) = cb_init {
            cb(&mut self).await
        } else {
            Ok(())
        };
        tracing::debug!("task initialization result: {init_result:?}");

        if init_result.is_ok() {
            let res = self.recv_and_send(cb_msg).await;
            tracing::debug!("lost WebSocket connection: {:?}", res);
        }

        if let Some(cb) = cb_cln {
            let _ = cb(&mut self).await;
        }
        Ok(self.socket)
    }

    async fn recv_and_send(&mut self, mut cb: Option<CallbackMessage<'a>>) -> anyhow::Result<()> {
        // polling both socket.next() and message_queue.recv() in a loop to
        // concurrently send and receive from the socket
        // breaking the loop with a value ends it
        loop {
            tokio::select! {
            // first future: receive
            Some(msg) = self.socket.next() => {
                let msg = msg?;
                if let Some(ref mut c) = cb {
                    match c(msg).await {
                        Ok(Some(res)) => {
                            self.socket.send(res).await?;
                        }
                        Err(e) => break Err(e),
                        _ => (),
                    }
                }
            },
            // second future: send
            msg = self.message_queue.recv() => {
                match msg {
                    Some(msg) => {
                        self.socket.send(msg).await?;
                    }
                    None =>  break Err(anyhow::Error::msg("ws msg channel broken"))
                    }
                }
            };
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
            let (_task, _) = WebSocketTask::<'_, tokio::net::TcpStream>::new(todo!());
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
            let (_task, _) = WebSocketTask::<'_, tokio::net::TcpStream>::new(todo!());
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
            let (_task, _) = WebSocketTask::<'_, tokio::net::TcpStream>::new(todo!());
            _task.on_cleanup(|_task| async {
                __test_async_fn().await;

                Ok(())
            });
        };
    }
}
