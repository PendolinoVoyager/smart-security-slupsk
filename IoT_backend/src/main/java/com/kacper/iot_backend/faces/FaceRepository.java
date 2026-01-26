package com.kacper.iot_backend.faces;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaceRepository extends JpaRepository<Face, Integer> {
    List<Face> findByDeviceId(Integer deviceId);
}
