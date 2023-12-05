package xyz.poeschl.pathseeker.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class PositionOutOfMapException(message: String) : ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, message)

class InsufficientFuelException(message: String) : ResponseStatusException(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, message)

class PositionNotAllowedException(message: String) : ResponseStatusException(HttpStatus.CONFLICT, message)
