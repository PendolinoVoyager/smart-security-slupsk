import RPi.GPIO as GPIO
import time
import requests

PIR_PIN = 4      # Pin czujnika PIR
LED_PIN = 17
BUTTON_PIN = 27
BACKEND_IP = '192.168.10.89'

def send_notification(body):
    response = requests.post(f"{BACKEND_IP}/api/v1/notification/", json=body)
    if (response.status_code != 200):
        print(f"Error resoibse from API {response.status_code}")
    else:
        print("Powiadomienie wyslane, jest git")

GPIO.setmode(GPIO.BCM)
GPIO.setup(PIR_PIN, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)
GPIO.setup(LED_PIN, GPIO.OUT)
GPIO.setup(BUTTON_PIN, GPIO.IN, pull_up_down=GPIO.PUD_UP)

# LED na starcie zgaszona
GPIO.output(LED_PIN, GPIO.LOW)

print("Domofon- oczekiwanie na aktywność?...")

try:
    while True:
        pir_state = GPIO.input(PIR_PIN)
        button_state = GPIO.input(BUTTON_PIN)

        if pir_state == GPIO.HIGH or button_state == GPIO.LOW:
            GPIO.output(LED_PIN, GPIO.HIGH)
            print("Domofon aktywny!")

            body = {"type": "notification", "message": "Wykryto ruch"}
            send_notification(body)
        else:
            GPIO.output(LED_PIN, GPIO.LOW)

        time.sleep(0.5)

except KeyboardInterrupt:
    print("Wylaczanie programu")
    GPIO.output(LED_PIN, GPIO.LOW)
    GPIO.cleanup()