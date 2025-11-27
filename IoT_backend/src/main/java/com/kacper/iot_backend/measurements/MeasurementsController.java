package com.kacper.iot_backend.measurements;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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

    /// Yes, 200 Ok on create action, I don't care about 201 create here.
    /// Too much additional implementation for 201 - 201 requires to return Location header with URI to created resource.
    @PostMapping("/")
    public ResponseEntity<Void> AddMeasurement(@Valid @RequestBody AddMeasurementRequest addMeasurementRequest) {
        measurementService.addMeasurement(addMeasurementRequest);
        return ResponseEntity.ok().build();
    }
}
