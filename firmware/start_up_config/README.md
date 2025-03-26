
# Manager

Manager is a linux service used to manage device state.
If device is not configured in route /etc/sss_firmware/config.cfg then run http server (and to implement start IoT Network). Else turn off http server.

### INFO
1. WARNING: CHANGE PATHS ON YOURS IN THIS FILE AND TO YOUR VENV IN MANAGER.PY!!!!
2. WARNING DEFAULT LED PIN IS GPIO 4

# Register service

To register service you have to create service file
```
sudo nano /etc/systemd/system/manager.service
```

And then put this config in file:

```
[Unit]
Description=State_Manager
After=multi-user.target

[Service]
ExecStart=/usr/bin/python3 /home/kacper/Desktop/start_up_app/manager.py
WorkingDirectory=/home/kacper
StandardOutput=inherit
StandardError=inherit
Restart=always
User=kacper

[Install]
WantedBy=multi-user.target
```

Then just type:

```
sudo systemctl daemon-reload
sudo systemctl enable manager.service
sudo systemctl start manager.service
```

# Not implemented
1. Turn on and off IoT Network




