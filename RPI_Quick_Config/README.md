# RPI QUICK CONFIG
### Created by Kacper Karabinowski
#### Last update: 09-02-2025
#### Version: 1.0.0

## Install Avahi
1. Install avahi on RPI
```bash
  sudo apt-get install avahi-daemon
```
2. Change avahi config
```bash
  sudo nano /etc/avahi/avahi-daemon.conf
```
3. Change the following lines:
```bash
    [server]
    host-name=IoT_Device
    domain-name=local
    use-ipv4=yes
    use-ipv6=yes
    allow-interfaces=wlan0
    ratelimit-interval-usec=1000000
    ratelimit-burst=1000
    
    [wide-area]
    enable-wide-area=yes
    
    [publish]
    publish-hinfo=no
    publish-workstation=no
```
4. Register the service
```bash
  sudo nano /etc/avahi/services/http.service
```
5. Add the following lines. Port 5000 is for start_up_config app.
```bash
    <?xml version="1.0" standalone='no' ?>
    <!DOCTYPE service-group SYSTEM "avahi-service.dtd">
    <service-group>
        <name replace-wildcards="yes">IoT_Device</name>
        <service>
            <type>_http._tcp.</type>
            <port>5000</port>
            <txt-record>path=/</txt-record>
            <txt-record>uuid=Default_UUID</txt-record>
        </service>
    </service-group>
```
6. Restart avahi
```bash
  sudo systemctl restart avahi-daemon
```

## Install dnsmsq on RPI
1. Install dnsmasq
```bash
  sudo apt-get install dnsmasq
```
2. Change dnsmasq config
```bash
  sudo nano /etc/dnsmasq.conf
```
3. Add the following lines on bottom of the file. This is for the IoT_Config network.
```bash
    interface=wlan0
    bind-interfaces
    dhcp-range=192.168.100.100,192.168.100.200,255.255.255.0,24h
```
4. Restart dnsmasq
```bash
  sudo systemctl restart dnsmasq
```

## NetworkManager configuration
1. Change the NetworkManager config
```bash
  sudo nano /etc/NetworkManager/NetworkManager.conf
```
2. Add the following lines:
```bash
    [main]
    plugins=ifupdown,keyfile
    
    [ifupdown]
    managed=false
    
    [device]
    wifi.scan-rand-mac-address=no
```
3. Set up IoT_Config network
```bash
  sudo nano /etc/NetworkManager/system-connections/IoT_Config.nmconnection
```
4. Add the following lines. You can change password. Default is "jestemfajny".
```bash
    [connection]
    id=IoT-Config
    uuid=3d903620-e1a7-4399-a525-6846cb314f2e
    type=wifi
    interface-name=wlan0
    
    [wifi]
    band=bg
    channel=1
    mode=ap
    ssid=IoT-Config
    
    [wifi-security]
    key-mgmt=wpa-psk
    psk=jestemfajny
    
    [ipv4]
    address1=192.168.100.1/24
    method=manual
    
    [ipv6]
    addr-gen-mode=default
    method=auto
    
    [proxy]
```
5. Set permissions
```bash
  sudo chmod 600 /etc/NetworkManager/system-connections/IoT_Config.nmconnection
```
6. Restart NetworkManager
```bash
  sudo systemctl restart NetworkManager
```
7. Enable IoT_Config network
```bash
  sudo nmcli connection up IoT-Config
```

## Install start_up_config app
1. Start up config app is in the repository. You can copy it to the RPI. I use the /desktop/start_up_config directory.
In this directory you have to copy the public_key.pem file to check JWT tokens. Script requires this key.
2. After copying the app you can install the requirements
```bash
  cd /desktop/start_up_config
  pip3 install -r requirements.txt
```
3. Run the app. App requires sudo permissions to save tokens in /etc directory.
```bash
  sudo python3 start_up_config.py
```

