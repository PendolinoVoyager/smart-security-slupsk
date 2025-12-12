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

class AudioPlayer:
    def __init__(self, server_url):
        self.server_url = server_url
        self.ffplay_process = None
        self.ws = None
        self.running = False
        
    async def connect_and_play(self):
        """Connect to WebSocket server and stream audio to ffplay"""
        try:
            print(f"Connecting to {self.server_url}...")
            self.ws = await websockets.connect(self.server_url)
            print("Connected! Starting audio playback...")
            
            # Identify as Pi client
            await self.ws.send(b'PI:client')
            
            # Start ffplay process
            # -nodisp: no video window
            # -autoexit: exit when stream ends
            # -probesize 32: smaller probe for lower latency
            # -fflags nobuffer: reduce buffering
            # -flags low_delay: optimize for low latency
            # -: read from stdin
            self.ffplay_process = subprocess.Popen([
                'ffplay',
                '-nodisp',
                '-autoexit',
                '-probesize', '32',
                '-fflags', 'nobuffer',
                '-flags', 'low_delay',
                '-'
            ], stdin=subprocess.PIPE, stderr=subprocess.DEVNULL)
            
            self.running = True
            chunk_count = 0
            
            # Receive and forward audio chunks
            async for message in self.ws:
                if not self.running:
                    break
                    
                # Write audio chunk to ffplay stdin
                try:
                    self.ffplay_process.stdin.write(message)
                    self.ffplay_process.stdin.flush()
                    chunk_count += 1
                    
                    if chunk_count % 100 == 0:
                        print(f"Received {chunk_count} chunks")
                        
                except BrokenPipeError:
                    print("ffplay process died")
                    break
                    
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
        
        if self.ffplay_process:
            try:
                self.ffplay_process.stdin.close()
                self.ffplay_process.terminate()
                self.ffplay_process.wait(timeout=2)
            except:
                self.ffplay_process.kill()
        
        if self.ws and not self.ws.closed:
            await self.ws.close()
        
        print("Stopped")


async def main():
    parser = argparse.ArgumentParser(description='Raspberry Pi Audio Client')
    parser.add_argument('--url', type=str, default='ws://localhost:8888/device',
                        help='WebSocket server URL (default: ws://localhost:8888/device)')
    parser.add_argument('--token', type=str, default='100',
                        help='Authentication token (default: 100)')
    parser.add_argument('--deviceId', type=str, default='100',
                        help='Device ID (default: 100)')
    
    args = parser.parse_args()
    
    # Construct the server URL with query parameters
    server_url = f"{args.url}?token={args.token}&deviceId={args.deviceId}"
    
    player = AudioPlayer(server_url)
    
    # Handle Ctrl+C gracefully
    def signal_handler(sig, frame):
        print("\nReceived interrupt signal")
        player.running = False
    
    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)
    
    await player.connect_and_play()

if __name__ == "__main__":
    print("Raspberry Pi Audio Client")
    print("=" * 40)
    asyncio.run(main())