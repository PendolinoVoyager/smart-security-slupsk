import requests
from config import CONFIG, update_token
URL = f"http://{CONFIG.get('server_addr')}/api/v1/notification/"

def on_movement():
    update_token()
    token = CONFIG.get("token")
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    data = {
        "type": "INFO",
        "message": "There's something at your door!",   
    }
    
    try:
        response = requests.post(URL, json=data, headers=headers)
        response.raise_for_status()  # Raise an exception for HTTP errors
        print(response.json())
        return response.json() 
    except requests.exceptions.RequestException as e:
        print(f"Error sending request: {e}")
        return None
