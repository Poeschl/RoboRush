package xyz.poeschl.roborush.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class GameStateException(message: String) : ResponseStatusException(HttpStatus.TOO_EARLY, message)
