package xyz.poeschl.pathseeker.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class UnknownTileType(message: String) : Exception(message)
class NoStartingPosition(message: String) : ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, message)
class NoTargetPosition(message: String) : ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, message)
