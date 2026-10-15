package com.demo.example.mscustomerdummy.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerDto(
        @NotBlank
        String nombre,
        @NotBlank
        String apellido,
        @NotBlank
        String correo,
        @NotBlank
        String dni
) {
}
