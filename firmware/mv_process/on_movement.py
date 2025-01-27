import requests
from auth import get_token
URL = "http://127.0.0.1:8080/api/v1/notification/"

def on_movement():
    token = get_token()
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
        return response.json()  # Return the server's response if needed
    except requests.exceptions.RequestException as e:
        print(f"Error sending request: {e}")
        return None
