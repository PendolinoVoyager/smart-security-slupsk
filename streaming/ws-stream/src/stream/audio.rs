#[derive(Debug, Clone, Copy)]
pub enum AudioBackend {
    Alsa,
    Pulse,
}

impl AudioBackend {
    pub fn as_ffmpeg_str(&self) -> &str {
        match self {
            AudioBackend::Alsa => "alsa",
            AudioBackend::Pulse => "pulse",
        }
    }
    pub fn as_gstreamer_str(&self) -> &str {
        match self {
            AudioBackend::Alsa => "alsasrc",
            AudioBackend::Pulse => "pulsesrc",
        }
    }
}
pub fn get_audio_backend() -> Option<AudioBackend> {
    if check_alsa() {
        return Some(AudioBackend::Alsa);
    }
    if check_pulseaudio() {
        return Some(AudioBackend::Pulse);
    }
    None
}

fn check_pulseaudio() -> bool {
    std::process::Command::new("pactl")
        .arg("info")
        .output()
        .map(|output| output.status.success())
        .unwrap_or(false)
}

fn check_alsa() -> bool {
    alsa::pcm::PCM::new("default", alsa::Direction::Capture, false).is_ok()
}
