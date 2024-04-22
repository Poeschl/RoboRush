package xyz.poeschl.pathseeker.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class InvalidConfigKeyException(message: String) : ResponseStatusException(HttpStatus.NOT_FOUND, message)
class InvalidHeightMapException(message: String) : ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message)
