import paho.mqtt.client as mqtt
import time
import random  # zamiast prawdziwych danych z czujników, na razie użyjemy losowych liczb

# Ustawienia MQTT
broker = "mqtt.example.com"  # Adres twojego brokera
port = 1883  # Port do połączenia (zwykle 1883 dla niezabezpieczonego połączenia)
temat = "czujniki/dane"  # Temat, do którego będziemy publikować dane

# Funkcja wywoływana po pomyślnym połączeniu z brokerem
def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("Połączenie udane")
    else:
        print(f"Błąd połączenia. Kod: {rc}")

# Funkcja do wysyłania danych do brokera
def publikuj_dane(client, czujnik_id):
    while True:
        

        # Tworzenie wiadomości w formacie JSON, w tym ID czujnika
        ladunek = f'{{"czujnik_id": "{czujnik_id}", "temperatura": {temperatura}, "wilgotnosc": {wilgotnosc}}}'

        # Publikowanie danych do brokera
        client.publish(temat, ladunek)
        print(f"Wysłane dane z czujnika {czujnik_id}: {ladunek}")

        # Oczekiwanie przed kolejną wysyłką
        time.sleep(5)  # Wysyłanie danych co 5 sekund

# Tworzenie klienta MQTT
client = mqtt.Client()

# Ustawianie obsługi zdarzeń
client.on_connect = on_connect

# Połączenie z brokerem
client.connect(broker, port)

# Uruchomienie pętli obsługującej zdarzenia i publikację danych
client.loop_start()  # Wątek w tle do obsługi sieci

# Dodajemy unikalne ID czujników
czujniki_id = ["czujnik_1", "czujnik_2", "czujnik_3"]

try:
    # Dla każdego czujnika uruchamiamy publikację danych
    for czujnik_id in czujniki_id:
        publikuj_dane(client, czujnik_id)
except KeyboardInterrupt:
    print("Program zatrzymany")
finally:
    client.loop_stop()
    client.disconnect()
