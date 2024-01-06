package xyz.poeschl.pathseeker.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class InvalidGameStateException(message: String) : ResponseStatusException(HttpStatus.TOO_EARLY, message)
