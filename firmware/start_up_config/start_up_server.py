from flask import Flask, jsonify, request
import subprocess
import json
import jwt 

app = Flask(__name__)

CONFIG_FILE_PATH = "/etc/sss_firmware/config.cfg"
PUBLIC_KEY_PATH = "public_key.pem"

ACCESS_TOKEN_PATH = "/etc/sss_firmware/token.txt"
REFRESH_TOKEN_PATH = "/etc/sss_firmware/refresh_token.txt"

def scan_wifi_networks():
    try:
        result = subprocess.run(
            ['nmcli', '-t', '-f', 'SSID,SIGNAL', 'dev', 'wifi'],
            capture_output=True,
            text=True,
            check=True
        )

        networks = []
        for line in result.stdout.strip().split('\n'):
            if line:
                parts = line.split(':')
                ssid = parts[0]
                signal_strength = parts[1] if len(parts) > 1 else 'unknown'
                networks.append({'ssid': ssid, 'signal_strength': signal_strength})

        return networks
    except subprocess.CalledProcessError as e:
        print(f"Błąd podczas skanowania sieci Wi-Fi: {e}")
        return []

@app.route('/api/v1/available-networks', methods=['GET'])
def available_networks():
    networks = scan_wifi_networks()
    return jsonify({'networks': networks})

@app.route('/api/v1/config', methods=['POST'])
def change_wifi_network():
    data = request.get_json()
    if not data or 'ssid' not in data or 'password' not in data:
        return jsonify({'error': 'SSID and password are required'}), 400

    ssid = data['ssid']
    password = data['password']

    try:
        subprocess.run(
            ['nmcli', 'dev', 'wifi', 'connect', ssid, 'password', password],
            capture_output=True,
            text=True,
            check=True
        )
        return jsonify({'message': f'Successfully connected to {ssid}'}), 200
    except subprocess.CalledProcessError as e:
        print(f"Błąd podczas zmiany sieci Wi-Fi: {e}")
        return jsonify({'error': f'Failed to connect to {ssid}. Error: {e.stderr}'}), 500

@app.route('/api/v1/uuid', methods=['GET'])
def get_uuid():
    return jsonify({'uuid': 'Default_UUID'})

@app.route('/api/v1/token', methods=['POST'])
def set_token():
    data = request.get_json()
    if not data or 'token' not in data:
        return jsonify({'error': 'Missing token'}), 400

    token_jwt = data['token']
    refresh_token = data.get('refreshToken', '')

    try:
        with open(PUBLIC_KEY_PATH, "r", encoding="utf-8") as key_file:
            public_key = key_file.read()
    except FileNotFoundError:
        return jsonify({"error": "Public key not found. Critical Error"}), 500

    try:
        payload = jwt.decode(token_jwt, public_key, algorithms=["RS256"])
    except jwt.ExpiredSignatureError:
        return jsonify({'error': 'Token has expired'}), 401
    except jwt.InvalidTokenError:
        return jsonify({'error': 'Invalid token'}), 401

    user_id = payload.get('user_id')
    email = payload.get('sub')

    if not user_id or not email:
        return jsonify({'error': 'Token payload missing user_id or sub'}), 400

    try:
        with open(CONFIG_FILE_PATH, "w", encoding="utf-8") as config_file:
            config_file.write("[Is Configured]\n")
            config_file.write("1\n")
            config_file.write("[Owner ID]\n")
            config_file.write(f"{user_id}\n")
            config_file.write("[Owner email]\n")
            config_file.write(f"{email}\n")
    except Exception as e:
        print(f"Error saving data to: config.cfg: {e}")
        return jsonify({'error': 'Failed to write config file'}), 500

    try:
        with open(ACCESS_TOKEN_PATH, "w", encoding="utf-8") as file:
            file.write(token_jwt)
    except Exception as e:
        print(f"Error saving data to: token.txt: {e}")
        return jsonify({'error': 'Failed to write access token'}), 500

    try:
        with open(REFRESH_TOKEN_PATH, "w", encoding="utf-8") as file:
            file.write(refresh_token)
    except Exception as e:
        print(f"Field to save: refresh_token.txt: {e}")
        return jsonify({'error': 'Failed to write refresh token'}), 500

    return jsonify({'message': 'Token verified and configuration updated'}), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
