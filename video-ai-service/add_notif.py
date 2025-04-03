import requests
BACKEND_URL = "http://127.0.0.1:8000/api/v1/notification/debug_add"  

def send_notification(message: str):
    payload = {
        "type": "Info",
        "message": message
    }

    headers = {"Content-Type": "application/json"}

    response = requests.post(BACKEND_URL, json=payload, headers=headers)
    if response.status_code != 200:
        print(f"Failed to send notification: {response.text}")
        exit(1)

    print(response.json())
