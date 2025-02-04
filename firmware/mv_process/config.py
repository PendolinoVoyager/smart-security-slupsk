import argparse
import os
CONFIG = {
    "token": None,
    "server_addr": None,
}
TOKEN_OVERRIDE = False

def load_config():
    global TOKEN_OVERRIDE
    parser = argparse.ArgumentParser()
    parser.add_argument("--token", type=str, help="Authentication token")
    parser.add_argument("--server-addr", type=str, default="127.0.0.1:8080", help="Server address")
    args = parser.parse_args()

    if args.token:
        TOKEN_OVERRIDE = True
        CONFIG["token"] = args.token
    else:
        try:
            with open("/etc/sss_firmware/token.txt", "r") as f:
                CONFIG["token"] = f.read().strip()
        except FileNotFoundError:
            print("Warning: Token file not found and no token provided via args.")

    CONFIG["server_addr"] = args.server_addr
    print(CONFIG)

def get_video_source():
    if os.environ.get("MV_DEBUG") == "1":
        return 0
    else:
        return "udp://127.0.0.1:10000?overrun_nonfatal=1&fifo_size=50000000"

def update_token():
    if TOKEN_OVERRIDE:
        return
    try:
        with open("/etc/sss_firmware/token.txt", "r") as f:
            CONFIG["token"] = f.read().strip()
    except FileNotFoundError:
        print("Warning: Token file not found and no token provided via args.")
            
load_config()
