import RPi.GPIO as GPIO
import time
import os

PIR_PIN = 4  # Pin czujnika PIR

GPIO.setmode(GPIO.BCM)
GPIO.setup(PIR_PIN, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)

ALARM_FILE = "alarm.mp3"  #

print("Czujnik PIR - oczekiwanie na ruch...")

try:
    while True:
        if GPIO.input(PIR_PIN):  # Jeśli wykryto ruch
            print("🚨 Ruch wykryty! Odtwarzanie alarmu...")
            os.system(f"mpg321 {ALARM_FILE} &")  # Odtwarzanie MP3
            time.sleep(5)  # Czas działania alarmu (5 sekund)
        time.sleep(0.5)  # Krótkie opóźnienie pętli
except KeyboardInterrupt:
    print("Wyłączanie programu")
    GPIO.cleanup()
