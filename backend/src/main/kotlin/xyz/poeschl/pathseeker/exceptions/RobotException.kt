package xyz.poeschl.pathseeker.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class MoveOutOfMapException(message: String) : ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, message)

class InsufficientFuelException(message: String) : ResponseStatusException(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, message)
