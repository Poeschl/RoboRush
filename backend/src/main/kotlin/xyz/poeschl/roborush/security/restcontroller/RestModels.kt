package xyz.poeschl.roborush.security.restcontroller

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(@param:NotBlank val username: String, @param:NotBlank val password: String)

data class LoginResponse(val accessToken: String)

data class RegisterRequest(@param:NotBlank @param:Size(min = 3) val username: String, @param:NotBlank @param:Size(min = 8) val password: String)
