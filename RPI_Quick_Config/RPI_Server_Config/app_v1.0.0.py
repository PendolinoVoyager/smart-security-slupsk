from flask import Flask, jsonify, request
import subprocess
import jwt
from jwt import get_unverified_header, decode as jwt_decode
from jwt.exceptions import (
    ExpiredSignatureError,
    InvalidSignatureError,
    DecodeError,
    InvalidAlgorithmError,
    InvalidTokenError
)

app = Flask(__name__)

CONFIG_FILE_PATH = "/etc/sss_firmware/config.cfg"
PUBLIC_KEY_PATH = "/home/kacper/Desktop/start_up_app/public_key.pem"

ACCESS_TOKEN_PATH = "/etc/sss_firmware/token.txt"
REFRESH_TOKEN_PATH = "/etc/sss_firmware/refresh_token.txt"

def scan_wifi_networks():
    try:
        result = subprocess.run(
            ['nmcli', '-t', '-f', 'SSID,SIGNAL', 'dev', 'wifi'],
            capture_output=True, text=True, check=True
        )
        networks = []
        for line in result.stdout.strip().split('\n'):
            if line:
                ssid, *rest = line.split(':')
                signal = rest[0] if rest else 'unknown'
                networks.append({'ssid': ssid, 'signal_strength': signal})
        return networks
    except subprocess.CalledProcessError as e:
        print(f"Błąd podczas skanowania sieci Wi-Fi: {e}")
        return []

@app.route('/api/v1/available-networks', methods=['GET'])
def available_networks():
    return jsonify({'networks': scan_wifi_networks()})

@app.route('/api/v1/config', methods=['POST'])
def change_wifi_network():
    data = request.get_json()
    if not data or 'ssid' not in data or 'password' not in data:
        return jsonify({'error': 'SSID and password are required'}), 400

    try:
        subprocess.run(
            ['nmcli', 'dev', 'wifi', 'connect', data['ssid'], 'password', data['password']],
            capture_output=True, text=True, check=True
        )
        return jsonify({'message': f"Successfully connected to {data['ssid']}"}), 200
    except subprocess.CalledProcessError as e:
        print(f"Błąd podczas zmiany sieci Wi-Fi: {e.stderr}")
        return jsonify({'error': f"Failed to connect to {data['ssid']}. Error: {e.stderr}"}), 500

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

    # DEBUG: pokaz nagłówek i niezweryfikowany payload
    try:
        header = get_unverified_header(token_jwt)
        print("JWT header:", header)
        print("JWT payload (no verify):", jwt_decode(token_jwt, options={"verify_signature": False}))
    except Exception as e:
        print("Nie udało się odczytać nagłówka/payload:", e)

    # Wczytaj klucz publiczny
    try:
        with open(PUBLIC_KEY_PATH, "r", encoding="utf-8") as f:
            public_key = f.read()
    except FileNotFoundError:
        return jsonify({'error': 'Public key not found'}), 500

    # Weryfikacja tokena
    try:
        payload = jwt.decode(token_jwt, public_key, algorithms=["RS256"])
    except ExpiredSignatureError:
        print("token expired")
        return jsonify({'error': 'Token has expired'}), 401
    except InvalidAlgorithmError as e:
        print("unsupported alg:", e)
        return jsonify({'error': 'Unsupported algorithm'}), 400
    except InvalidSignatureError as e:
        print("❌ Błędny podpis:", e)
        return jsonify({'error': 'Bad signature'}), 401
    except DecodeError as e:
        print("❌ Błąd dekodowania JWT:", e)
        return jsonify({'error': 'Malformed token'}), 400
    except InvalidTokenError as e:
        print("❌ Inny błąd JWT:", e)
        return jsonify({'error': 'Invalid token'}), 401

    # Zapis konfiguracji
    user_id = payload.get('user_id')
    email = payload.get('sub')
    if not user_id or not email:
        return jsonify({'error': 'Token payload missing user_id or sub'}), 400

    try:
        with open(CONFIG_FILE_PATH, "w", encoding="utf-8") as cfg:
            cfg.write("[Is Configured]\n1\n")
            cfg.write("[Owner ID]\n" + str(user_id) + "\n")
            cfg.write("[Owner email]\n" + email + "\n")
    except Exception as e:
        print("Error writing config.cfg:", e)
        return jsonify({'error': 'Failed to write config file'}), 500

    # Zapis tokenów
    try:
        with open(ACCESS_TOKEN_PATH, "w", encoding="utf-8") as f:
            f.write(token_jwt)
        with open(REFRESH_TOKEN_PATH, "w", encoding="utf-8") as f:
            f.write(refresh_token)
    except Exception as e:
        print("Error writing token files:", e)
        return jsonify({'error': 'Failed to write token file(s)'}), 500

    return jsonify({'message': 'Token verified and configuration updated'}), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)