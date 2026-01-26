import time
import requests
import adafruit_dht
import board

# =========================
# CONFIG
# =========================

ENDPOINT_URL = "http://18.184.69.19:8080/api/v1/notification/"

TOKEN = (
    "eyJhbGciOiJSUzI1NiJ9."
    "eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImV4cCI6MTc2OTc5NzQ2MiwidXNlcl9pZCI6MTAwLCJpc0RldmljZSI6dHJ1ZSwiZGV2aWNlVXVpZCI6IkRlZmF1bHRfVVVJRCIsImRldmljZUlkIjoxMDAsImlhdCI6MTc2ODkzMzQ2Mn0."
    "h7IHrwmCTeGgDm0tyPcJPfQXDwtBkNswhesfBbyzK86o8M5MdfP4_VGz5NJ_Wj0C1jifBot2j1AV_l66Qizk_LW3USgBHudsqbbR-O-zbvVxL5cpM8xJXmXTyg1CWX_g3QszAiKpHy2XUT0ErOjRWJgndl3XJgmLcCZs5avgHX21cFbeJyf0d2fbDY_Qtn-9hNtKFHke8U-U970OENmuYav1HyZMKQLN9oIJzwrJTTjkw6YTlg1nlrvZxD7b6mBrwlHgB9ZYdmJ0LnWpPzFwqCjstMOZy_8c8vl1zN2LqH5bIfz2TYBTbSFKu1RaVNzi1-lkT_upqus91L80z6kdBw"
)

HEADERS = {
    "Authorization": f"Bearer {TOKEN}",
    "Content-Type": "application/json",
}

# GPIO4 (pin fizyczny 7)
dht = adafruit_dht.DHT11(board.D4)

# =========================
# LOGIC
# =========================

def read_sensor():
    try:
        temperature = dht.temperature
        humidity = dht.humidity

        if temperature is None or humidity is None:
            raise RuntimeError("Invalid sensor read")

        return temperature, humidity

    except RuntimeError as e:
        print("Sensor read error:", e)
        return None, None


def send_notification(msg_type: str, value):
    payload = {
        "type": msg_type,
        "message": value,
    }

    try:
        response = requests.post(
            ENDPOINT_URL,
            json=payload,
            headers=HEADERS,
            timeout=5,
        )

        print(
            f"[{msg_type}] status={response.status_code} body={response.text}"
        )

    except requests.RequestException as e:
        print(f"HTTP error ({msg_type}):", e)


def main():
    temperature, humidity = read_sensor()

    if temperature is None or humidity is None:
        print("Skipping send – invalid sensor data")
        return

    print(f"Temperature: {temperature}°C")
    print(f"Humidity: {humidity}%")

    send_notification("DHT11-TEMP", temperature)
    send_notification("DHT11-HUM", humidity)


if __name__ == "__main__":
    main()
