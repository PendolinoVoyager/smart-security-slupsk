#!/usr/bin/env python3
import sys
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
import os

PORT = 8088
DIRECTORY = "/var/localstream/html"

class NoCacheHandler(SimpleHTTPRequestHandler):
    def end_headers(self):
        # Prevent browser caching
        self.send_header("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
        self.send_header("Pragma", "no-cache")
        self.send_header("Expires", "0")
        super().end_headers()

    def translate_path(self, path):
        # Serve from the specified directory instead of cwd
        path = super().translate_path(path)
        relpath = os.path.relpath(path, os.getcwd())
        return os.path.join(DIRECTORY, relpath)

if __name__ == "__main__":
    os.chdir(DIRECTORY)
    server = ThreadingHTTPServer(("0.0.0.0", PORT), NoCacheHandler)
    print(f"Serving {DIRECTORY} at http://0.0.0.0:{PORT} (no-cache enabled)")
    server.serve_forever()

