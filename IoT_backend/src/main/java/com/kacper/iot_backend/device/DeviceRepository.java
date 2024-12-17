package com.kacper.iot_backend.device;

import com.kacper.iot_backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Integer>
{
    List<Device> findByUser(User user);
}
