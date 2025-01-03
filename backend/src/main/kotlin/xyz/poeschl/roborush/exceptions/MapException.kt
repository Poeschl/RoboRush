package xyz.poeschl.roborush.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class UnknownTileType(message: String) : Exception(message)

class NoStartingPosition(message: String) : ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, message)

class NoTargetPosition(message: String) : ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, message)

class MapNotFound(message: String) : ResponseStatusException(HttpStatus.NOT_FOUND, message)
