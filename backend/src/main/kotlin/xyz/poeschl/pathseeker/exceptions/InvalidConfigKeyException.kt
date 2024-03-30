package xyz.poeschl.pathseeker.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class InvalidConfigKeyException(message: String) : ResponseStatusException(HttpStatus.NOT_FOUND, message)
