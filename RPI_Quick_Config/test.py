import subprocess
from pathlib import Path

CLIP_URL = "https://www.twitch.tv/eslcs/clip/CleanNiceAubergineTBTacoLeft-eBeU0SbN-y4abeJv"

def download_clip(url: str, out_dir: str = "downloads") -> None:
    Path(out_dir).mkdir(parents=True, exist_ok=True)

    # -o: template nazwy pliku
    # --no-part: bez .part (czasem wygodniejsze w pipeline)
    cmd = [
        "yt-dlp",
        "--no-part",
        "-o", f"{out_dir}/%(uploader)s_%(id)s.%(ext)s",
        url,
    ]

    p = subprocess.run(cmd, capture_output=True, text=True)
    if p.returncode != 0:
        raise RuntimeError(f"yt-dlp failed:\nSTDOUT:\n{p.stdout}\nSTDERR:\n{p.stderr}")

if __name__ == "__main__":
    download_clip(CLIP_URL)
