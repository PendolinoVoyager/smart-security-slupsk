#!/usr/bin/env python3

"""
Raspberry Pi WebSocket Audio Client (robust)
Recovers from broken WebM/Opus chunks by restarting ffmpeg
"""

import argparse
import asyncio
import websockets
import subprocess
import sys
import signal
import threading
import time

class AudioPlayer:
    def __init__(self, server_url):
        self.server_url = server_url
        self.ffmpeg_process = None
        self.ws = None
        self.running = False

    # ---------------- FFmpeg handling ---------------- #

    def start_ffmpeg(self):
        self.stop_ffmpeg()

        print("Starting ffmpeg decoder…")
        self.ffmpeg_process = subprocess.Popen(
            [
                "ffmpeg",
                "-loglevel", "warning",
                "-err_detect", "ignore_err",
                "-fflags", "nobuffer",
                "-flags", "low_delay",
                "-i", "pipe:0",
                "-f", "alsa",
                "default",
            ],
            stdin=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )

        threading.Thread(
            target=self._monitor_ffmpeg,
            daemon=True
        ).start()

    def stop_ffmpeg(self):
        if self.ffmpeg_process:
            try:
                self.ffmpeg_process.stdin.close()
                self.ffmpeg_process.terminate()
                self.ffmpeg_process.wait(timeout=1)
            except Exception:
                self.ffmpeg_process.kill()
            finally:
                self.ffmpeg_process = None

    def _monitor_ffmpeg(self):
        """
        Watches ffmpeg stderr. If it dies or reports fatal errors,
        we restart it so the stream can recover.
        """
        try:
            for line in self.ffmpeg_process.stderr:
                msg = line.decode(errors="ignore").strip()
                if not msg:
                    continue

                print(f"[ffmpeg] {msg}")

                if (
                    "Invalid data found" in msg
                    or "Error while decoding" in msg
                    or "End of file" in msg
                ):
                    print("⚠️ Decoder desynced — restarting ffmpeg")
                    self.start_ffmpeg()
                    return
        except Exception:
            pass

    # ---------------- WebSocket handling ---------------- #

    async def connect_and_play(self):
        try:
            print(f"Connecting to {self.server_url}…")
            self.ws = await websockets.connect(self.server_url)
            print("Connected")

            await self.ws.send(b"PI:client")

            self.start_ffmpeg()
            self.running = True

            async for message in self.ws:
                if not self.running:
                    break

                if not self.ffmpeg_process or self.ffmpeg_process.poll() is not None:
                    print("ffmpeg not running — restarting")
                    self.start_ffmpeg()

                try:
                    self.ffmpeg_process.stdin.write(message)
                    self.ffmpeg_process.stdin.flush()
                except (BrokenPipeError, OSError):
                    print("Broken pipe — restarting decoder")
                    self.start_ffmpeg()

        except websockets.exceptions.WebSocketException as e:
            print(f"WebSocket error: {e}")
        except Exception as e:
            print(f"Fatal error: {e}")
        finally:
            await self.cleanup()

    async def cleanup(self):
        print("\nCleaning up…")
        self.running = False

        self.stop_ffmpeg()

        if self.ws and not self.ws.closed:
            await self.ws.close()

        print("Stopped cleanly")

# ---------------- Entry point ---------------- #

async def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--url",
        default="ws://localhost:8888/audio-server/v1/device"
    )
    parser.add_argument("--token", default="100")
    parser.add_argument("--deviceId", default="100")
    args = parser.parse_args()

    server_url = f"{args.url}?token={args.token}&deviceId={args.deviceId}"

    player = AudioPlayer(server_url)

    try:
        await player.connect_and_play()
    except KeyboardInterrupt:
        await player.cleanup()

if __name__ == "__main__":
    print("Raspberry Pi Audio Client")
    print("=" * 40)
    asyncio.run(main())

