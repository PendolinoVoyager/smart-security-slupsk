#!/usr/bin/env python3
"""
Raspberry Pi WebSocket Audio Client
Receives WebM/Opus audio chunks and plays them in real-time
"""

import argparse
import asyncio
import websockets
import subprocess
import sys
import signal

SUBPROCESS = None

class AudioPlayer:
    def __init__(self, server_url):
        self.server_url = server_url
        self.ffmpeg_process: subprocess.Popen | None = None
        self.ws = None
        
    async def connect_and_play(self):
        
        """Connect to WebSocket server and stream audio to ffplay"""
        try:
            print(f"Connecting to {self.server_url}...")
            self.ws = await websockets.connect(self.server_url)
            print("Connected! Starting audio playback...")
            
            # Identify as Pi client
            await self.ws.send(b'PI:client')
            
 
            self.ffmpeg_process = subprocess.Popen(
            [
                "ffmpeg",
                "-loglevel", "error",
                "-fflags", "nobuffer",
                "-flags", "low_delay",
                "-i", "pipe:0",
                "-f", "alsa",
                "default",
            ],
            stdin=subprocess.PIPE,
            )
            global SUBPROCESS
            SUBPROCESS = self.ffmpeg_process
            
            self.running = True
            async for message in self.ws:
                self.ffmpeg_process.stdin.write(message)
                self.ffmpeg_process.stdin.flush()

                            
        except websockets.exceptions.WebSocketException as e:
            print(f"WebSocket error: {e}")
        except Exception as e:
            print(f"Error: {e}")
        finally:
            await self.cleanup()

    async def cleanup(self):
        """Clean up resources"""
        print("\nCleaning up...")
        self.running = False
        
        if self.ffmpeg_process:
            try:
                self.ffmpeg_process.stdin.close()
                self.ffmpeg_process.terminate()
                self.ffmpeg_process.wait(timeout=2)
            except Exception:
                self.ffmpeg_process.kill()
        
        if self.ws and not self.ws.closed:
            await self.ws.close()
        
        print("Stopped")

async def main():
    parser = argparse.ArgumentParser(description='Raspberry Pi Audio Client')
    parser.add_argument('--url', type=str, default='ws://localhost:8888/audio-server/v1/device',
                        help='WebSocket server URL (default: ws://localhost:8888/audio-server/v1/device)')
    parser.add_argument('--token', type=str, default='100',
                        help='Authentication token (default: 100)')
    parser.add_argument('--deviceId', type=str, default='100',
                        help='Device ID (default: 100)')
    
    args = parser.parse_args()
    
    # Construct the server URL with query parameters
    server_url = f"{args.url}?token={args.token}&deviceId={args.deviceId}"
    
    player = AudioPlayer(server_url)
    
    try:
        handle = player.connect_and_play()
        await handle
    except KeyboardInterrupt:
        player.cleanup()

if __name__ == "__main__":
    print("Raspberry Pi Audio Client")
    print("=" * 40)
    asyncio.run(main())
