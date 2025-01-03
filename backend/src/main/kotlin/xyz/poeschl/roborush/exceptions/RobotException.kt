package xyz.poeschl.roborush.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class PositionOutOfMapException(message: String) : ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, message)

class InsufficientFuelException(message: String) : ResponseStatusException(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, message)

class PositionNotAllowedException(message: String) : ResponseStatusException(HttpStatus.CONFLICT, message)

class RobotNotActiveException(message: String) : ResponseStatusException(HttpStatus.NOT_FOUND, message)

class WrongTileTypeException(message: String) : ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, message)

class ActionDeniedByConfig(message: String) : ResponseStatusException(HttpStatus.PRECONDITION_FAILED, message)

class TankFullException(message: String) : ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, message)
