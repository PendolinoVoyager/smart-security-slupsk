use std::io::Read;

const STREAM_BUFFER_SIZE: usize = 1024 * 512; // 0.5MB just in case
/// General purpose buffer for stream. Prevents from sending very small or large packets via WebSockets.
/// ## Panics
/// Will absolutely panic if the <total length - minimum> chunk size exceeds the next pulled fragment.
/// Solution -> don't return massive chunks from the stream
pub struct StreamBuffer {
    min: usize,
    stream: std::net::UdpSocket,
    buf: Box<[u8; STREAM_BUFFER_SIZE]>,
    head: usize,
}

impl StreamBuffer {
    pub fn new(min: usize, stream: std::net::UdpSocket) -> Self {
        Self {
            min,
            stream,
            buf: Box::new([0; STREAM_BUFFER_SIZE]),
            head: 0,
        }
    }

    fn pull_chunk(&mut self) -> anyhow::Result<usize> {
        let res = self.stream.recv(&mut self.buf.as_mut_slice()[self.head..]);
        match res {
            Ok(bytes_read) => {
                self.head += bytes_read;
                Ok(bytes_read)
            }
            Err(e) => Err(e.into()),
        }
    }

    pub fn read(&mut self) -> anyhow::Result<&[u8]> {
        while self.head < self.min {
            self.pull_chunk()?;
        }

        let len = self.head;
        self.head = 0;
        Ok(&self.buf[0..len])
    }
}
