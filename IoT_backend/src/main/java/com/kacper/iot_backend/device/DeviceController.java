package com.kacper.iot_backend.device;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/v1/device")
@RestController
public class DeviceController
{
    @GetMapping("/")
    public ResponseEntity<List<Device>> getUserDevices() {
        List<Device> devices = List.of(
                new Device(1),
                new Device(2),
                new Device(3)
        );
        return new ResponseEntity<>(devices, HttpStatus.OK);
    }
}
