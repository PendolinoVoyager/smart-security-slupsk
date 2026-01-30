package com.kacper.iot_backend.faces;

import jakarta.validation.constraints.NotEmpty;

public record ChangeFaceNameRequest(
    @NotEmpty(message = "New face name is required")
    String newFaceName

)
{
};
