package xyz.poeschl.pathseeker.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SystemRestController {

  /***
   * Sends back an ok response to signal a running backend.
   */
  @GetMapping("/ping")
  fun ping() {
    return
  }
}
