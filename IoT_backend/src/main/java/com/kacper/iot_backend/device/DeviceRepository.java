package com.kacper.iot_backend.device;

import com.kacper.iot_backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Integer>
{
    List<Device> findByUser(User user);

    Optional<Device> findByUuid(String uuid);

}
