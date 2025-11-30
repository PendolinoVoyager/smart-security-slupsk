package com.kacper.iot_backend.measurements;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementRepository extends JpaRepository<Measurement, Integer>
{
    Page<Measurement> findByDeviceId(int deviceId, Pageable pageable);
}
