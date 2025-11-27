package com.kacper.iot_backend.measurements;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/measurements")
public class MeasurementsController
{
    private final MeasurementService measurementService;

    public MeasurementsController(MeasurementService measurementService) {
        this.measurementService = measurementService;
    }

    public ResponseEntity<Void> AddMeasurement(@Valid @RequestBody AddMeasurementRequest addMeasurementRequest) {
        measurementService.addMeasurement(addMeasurementRequest);
        return ResponseEntity.ok().build();
    }
}
