package xyz.poeschl.pathseeker.security.restcontroller

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(@NotBlank val username: String, @NotBlank val password: String)

data class LoginResponse(val accessToken: String)

data class RegisterRequest(@NotBlank @Size(min = 3) val username: String, @NotBlank @Size(min = 8) val password: String)
