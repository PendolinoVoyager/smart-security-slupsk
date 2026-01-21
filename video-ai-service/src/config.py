import argparse

DEFAULT_STREAMING_SERVER_URL = "http://127.0.0.1:9002"
DEFAULT_BACKEND_URL = "http://127.0.0.1:8080/"

BACKEND_URL = None
STREAMING_SERVER_URL = None
DEBUG = False

def parse_args():
    parser = argparse.ArgumentParser(
        description="Video streaming and tracking service"
    )

    parser.add_argument(
        "--strsrv-url",
        dest="streaming_server_url",
        type=str,
        default=DEFAULT_STREAMING_SERVER_URL,
        help=f"Streaming server URL (default: {DEFAULT_STREAMING_SERVER_URL})",
    )

    parser.add_argument(
        "--backend-url",
        dest="backend_url",
        type=str,
        default=DEFAULT_BACKEND_URL,
        help=f"Backend API URL (default: {DEFAULT_BACKEND_URL})",
    )

    parser.add_argument(
        "--debug",
        action="store_true",
        help="Enable debug mode from video device 0",
    )

    return parser.parse_args()

def update_globals(args: argparse.Namespace):
    global BACKEND_URL, STREAMING_SERVER_URL, DEBUG
    BACKEND_URL = args.backend_url
    STREAMING_SERVER_URL = args.streaming_server_url
    DEBUG = args.debug
