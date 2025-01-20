pub mod audio;
pub mod libcamera_ha_stream;
pub mod stream_factory;
pub mod v4l2_stream;

/// Trait for different kind of streams
/// It's unclear at this stage what the source of the video stream is, so a bit of abstraction is needed.
pub trait VideoStream {
    fn read(&mut self, buf: &mut [u8]) -> Result<usize, StreamReadError>;
    fn start(&mut self);
    fn stop(&mut self);
    /// Peek the stream state without needing to read.
    fn stream_state(&self) -> StreamState;
}
/// Generalized stream error implementation that may hapen during stream read.
/// Due to many crates used, these errors should be used to convey meaning.
#[derive(Debug, Clone, PartialEq)]
pub enum StreamReadError {
    /// Sequence of packets may have been broken, so any buffered data should be discarded or piped along.
    /// Subsequent reads should proceed normally.
    SequenceBreak,
    /// End of source, shouldn't happen, but maybe the camera got yoinked by kernel or something.
    Eos,
    /// Stream temporarily paused or uninitialized
    Paused,
    /// Generic error, meaning that the stream is broken, and should be reinitialized
    PipelineBroken,
}
impl std::fmt::Display for StreamReadError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        f.write_str(match self {
            StreamReadError::SequenceBreak => "read sequence broken",
            StreamReadError::Eos => "eos encountered",
            StreamReadError::PipelineBroken => "pipeline broken, stream aborted",
            StreamReadError::Paused => "stream paused",
        })
    }
}
impl std::error::Error for StreamReadError {}

impl From<std::io::Error> for StreamReadError {
    fn from(value: std::io::Error) -> Self {
        match value.kind() {
            std::io::ErrorKind::UnexpectedEof => Self::Eos,
            _ => Self::PipelineBroken,
        }
    }
}

pub enum StreamState {
    Idle,
    Running,
    Errored,
}

const STREAM_BUFFER_SIZE: usize = 1024 * 512; // 0.5MB just in case
/// General purpose buffer for stream. Prevents from sending very small or large packets via WebSockets.
/// ## Panics
/// Will absolutely panic if the <total length - minimum> chunk size exceeds the next pulled fragment.
/// Solution -> don't return massive chunks from the stream
pub struct StreamBuffer<T>
where
    T: VideoStream,
{
    min: usize,
    stream: T,
    buf: Box<[u8; STREAM_BUFFER_SIZE]>,
    head: usize,
}

impl<T: VideoStream> StreamBuffer<T> {
    pub fn new(min: usize, stream: T) -> Self {
        Self {
            min,
            stream,
            buf: Box::new([0; STREAM_BUFFER_SIZE]),
            head: 0,
        }
    }

    fn pull_chunk(&mut self) -> Result<usize, StreamReadError> {
        let res = self.stream.read(&mut self.buf.as_mut_slice()[self.head..]);
        match res {
            Ok(bytes_read) => {
                self.head += bytes_read;
                Ok(bytes_read)
            }
            Err(e) => Err(e),
        }
    }

    pub fn read(&mut self) -> Result<&[u8], StreamReadError> {
        while self.head < self.min {
            if let Err(e) = self.pull_chunk() {
                match e {
                    StreamReadError::SequenceBreak => {
                        return Ok(&[]);
                    }
                    e => {
                        if e == StreamReadError::SequenceBreak {
                            break;
                        } else {
                            return Err(e);
                        }
                    }
                }
            }
        }

        let len = self.head;
        self.head = 0;
        Ok(&self.buf[0..len])
    }
    #[allow(unused)]
    pub fn into_inner(self) -> T {
        self.stream
    }
}
