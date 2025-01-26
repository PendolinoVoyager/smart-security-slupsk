import os

TOKEN_PATH = "/etc/sss_firmware/token.txt"
DEBUG_TOKEN = "123"

def get_token():
    if os.environ['MV_DEBUG'] == "1":
        return DEBUG_TOKEN

    file = open(TOKEN_PATH, "r")
    return file.readline()
