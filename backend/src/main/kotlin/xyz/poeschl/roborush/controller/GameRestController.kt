package xyz.poeschl.roborush.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.extensions.Extension
import io.swagger.v3.oas.annotations.extensions.ExtensionProperty
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import xyz.poeschl.roborush.configuration.OpenApiConfig
import xyz.poeschl.roborush.controller.restmodels.PlaygroundMap
import xyz.poeschl.roborush.gamelogic.GameHandler
import xyz.poeschl.roborush.models.Game

@RestController
@RequestMapping("/game")
class GameRestController(private val gameHandler: GameHandler) {

  @GetMapping("/map", produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getMap(): PlaygroundMap {
    return PlaygroundMap(gameHandler.getCurrentMap(), gameHandler.getCurrentMapHeightMetadata())
  }

  @Operation(
    summary = "Retrieves information about the current game. For more information look at the help page.",
    extensions = [
      Extension(
        name = OpenApiConfig.VISIBILITY_KEY,
        properties = [ExtensionProperty(name = OpenApiConfig.VISIBILITY_KEY, value = OpenApiConfig.VISIBILITY_PUBLIC)]
      )
    ]
  )
  @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getPublicGameInfo(): Game {
    return gameHandler.getPublicGameInfo()
  }
}
