import Adafruit_DHT
import time

# Konfiguracja czujnika
sensor = Adafruit_DHT.DHT11
pin = 4 # GPIO4 (pin 7)

while True:
    wilgotnosc, temperatura = Adafruit_DHT.read(sensor, pin)

    if wilgotnosc is not None and temperatura is not None:
        print(f"Temperatura: {temperatura:.1f}°C")
        print(f"Wilgotność: {wilgotnosc:.1f}%")
    else:
        print("Błąd odczytu czujnika!")

    time.sleep(2) # Odczyt co 2 sekundy