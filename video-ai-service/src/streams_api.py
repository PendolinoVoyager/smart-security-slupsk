import requests

from config import STREAMING_SERVER_URL, BIND_TO
STREAMING_SERVER_STREAMS_ENDPOINT: str = "/streaming-server/v1/http/streams/all"
STREAMING_SERVER_UDP_ENDPOINT: str = "/streaming-server/v1/http/udp_stream_start"

def fetch_all_streams():
    response = requests.get(f"{STREAMING_SERVER_URL}{STREAMING_SERVER_STREAMS_ENDPOINT}")
    if response.status_code != 200:
        raise Exception(f"Failed to get streams: {response.text}")
    res_json = response.json()
    if res_json["status"] != "success":
        raise Exception(res_json["message"] if res_json["message"] is not None else "Unknown error on streaming server: " + response.status_code)
    return res_json["payload"]["devices"]    


"""Make a request to start streaming. Return capture bound to the udp address"""
def make_udp_request(device_id, port):

    payload = {
        "device_id": device_id,
        "address": f"{BIND_TO}:{port}"
    }

    headers = {"Content-Type": "application/json"}

    response = requests.post(f"{STREAMING_SERVER_URL}{STREAMING_SERVER_UDP_ENDPOINT}", json=payload, headers=headers)
    body = response.json()
    if response.status_code != 200:
        raise Exception(f"Failed to start stream: {body['payload'] if 'payload' in body else response.text}")    


    
