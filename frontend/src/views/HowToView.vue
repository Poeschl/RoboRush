<template>
  <div class="columns is-multiline">
    <div class="column is-full">
      <InfoBoxTemplate title="What is RoboRush?">
        <div class="doc">
          <div>
            RoboRush is a multiplayer coding challenge about finding a path from A to B for a robot on a 2 dimensional map. The game is played in a turn-based
            manner, so that every player's robot moves at the exact same time.
          </div>
          <div>
            Each player controls their robot via REST interfaces to check its environment or perform a robot action. Since the robots are not aware of the
            map around them, they might need to have a clever way to navigate through the map. But be careful, because all actions will deplete the robot's fuel
            tank a little. If a robot runs out of fuel, it becomes immobile and can't perform any actions.
          </div>
          <div>
            To win the game, your robot must be <span class="highlighted">the first to reach the target position</span>. Each win adds one point to your entry on the high score list.
          </div>
        </div>
      </InfoBoxTemplate>
    </div>
    <div class="column is-full">
      <InfoBoxTemplate title="Global rules">
        <div class="doc content">
          <ul>
            <li>Use only the public REST interfaces we defined in our <a href="/api/swagger-ui?urls.primaryName=public" target="_blank">OpenApi Spec</a></li>
            <li>Fasten your seatbelts and have fun 🙌</li>
          </ul>
        </div>
      </InfoBoxTemplate>
    </div>
    <div class="column is-half">
      <InfoBoxTemplate title="Robot">
        <div class="doc">
          <div>
            Every player has exactly one robot, which possesses various attributes. The robot is named after the player's username and can be instructed via a REST
            interface. To visually monitor the live attributes, visit the main page and ensure you are logged in.
          </div>
          <div class="doc-title">Robot attributes</div>
          <div>
            Each robot has a fuel tank that is filled at the start of each game. The maximum amount of fuel can be different for each map, but rest assured, we have made sure that you
            have a good chance of reaching the target tile. Most robot actions will drain the fuel tank. When it
            <span class="highlighted">reaches zero fuel units, your robot will not be able to perform any action that requires fuel</span>.
          </div>
          <div class="doc-title">Robot actions</div>
          <div>
            A robot can be instructed via a REST interface, which requires your API access token. For an overview of the available commands and details on how the
            authentication works, we provide an
            <a href="/api/swagger-ui?urls.primaryName=public" target="_blank">OpenApi Spec for the robot</a>. To send example requests, please enter your API
            token in the "Authorize" button dialog at the top and execute some requests with the "Try it out" button.
          </div>
          <div class="doc-title">Registration</div>
          <div>
            To get a robot you need to register by clicking the button in the top right corner of the navigation bar. After registering, you can log in 
            with your new credentials and click on your username in the top bar to get your API key.
          </div>
          <div class="doc-img">
            <ImageComponent class="m-3 is-block" image-src="/img/how-to/user-profile.png" title="A screenshot of the user profile dialog." />
          </div>
        </div>
      </InfoBoxTemplate>
      <InfoBoxTemplate title="Possible robot actions">
        <div class="doc">
          <div>
            There are several actions a robot can perform in a turn. If no participating robot executes any action for a few rounds, the game will end.
          </div>
          <div class="doc-title">Move</div>
          <div>
            With this action the robot moves to one of the neighbor map tiles. The robot can only move one tile at once in one of the 4 directions (east, south,
            west, north). Every movement costs the height difference of the map tiles plus 1 static fuel. "Rolling down" from a higher tile to a lower one,
            costs only 1 fuel unit. After the movement is carried out, the new position of the robot can be retrieved by the <code>lastResult</code> attribute
            of the robot information GET request.
          </div>
          <div>Cost: <code>max(heightDestinationTile - heightCurrentTile, 0) + 1</code></div>
          <div class="doc-title">Scan</div>
          <div>
            With the scan the robot will retrieve the map tiles around it according to the given scan distance. The scan returns every tile which has a
            <a href="https://en.wikipedia.org/wiki/Taxicab_geometry" target="_blank">Manhattan geometry</a> equal or less then the scan distance parameter. A
            scan is very fuel-consuming on large patches, since the cost is the distance squared. The results of the scan are available by the
            <code>lastResult</code> attribute of the robot information GET request after the execution phase.
          </div>
          <div>Cost: <code>distance * distance</code></div>
          <div class="doc-title">Wait</div>
          <div>Let your robot skip the next turn. Just chilling...</div>
          <div>Gain: <code>Nothing</code></div>
          <div class="doc-title">Refuel</div>
          <div>
            <span class="highlighted">When standing on a fuel station tile</span> your robot can schedule the refuel action. After execution your robot's fuel
            tank will be fully refilled. Keep in mind that only one robot can occupy a single tile at a time.
          </div>
          <div>Gain: <code>Full fuel tank</code></div>
          <div class="doc-title">Solar Charge</div>
          <div>
            If the map allows it, the robot can spend a turn and charge its fuel tank to gain some fuel units. This amount is a small percentage of the fuel
            tank capacity. Typically below 5%. It is intended to be a emergency backup if a robot is stuck by mistake.
          </div>
          <div>Gain: <code>Small amount of fuel</code></div>
        </div>
      </InfoBoxTemplate>
    </div>
    <div class="column is-half">
      <InfoBoxTemplate title="Map">
        <div class="doc">
          <div>
            The map serves as the playground where every robot will move. It is described by tiles arranged in a grid with variable size. At the start of every game,
            the map can be switched to a different one, depending on the preferences of the operators. However, during a game, the map size remains constant.
          </div>
          <div class="doc-img">
            <ImageComponent class="m-3 is-block" image-src="/img/how-to/basic-map.png" title="A small map with its tile as an example." />
          </div>
          <div>
            Every tile position can be described by a two-dimensional coordinate system with x and y. The origin tile with <code>(0,0)</code> is in the top left
            corner and x increases towards the right and y to the bottom. As a example the robots in the map above are at position <code>(2,3)</code> and
            <code>(5,7)</code>.
          </div>
          <div class="doc-title">How the robot sees the map</div>
          <div>
            The map on the main webpage is fully drawn to show the full game to you or other interested people, but the robots see it differently. Robots start
            without any knowledge of the surrounding tiles. In order to retrieve the tile heights and attributes around themselves, they need to execute a scan
            action (see robot actions).
          </div>
          <div class="doc-title">Tile attributes</div>
          <div>
            Every tile has a height, which is displayed in different shades of green on the web UI. Higher tiles are brighter than tiles at zero level. Robots
            can move on all tiles with a height in the range of 0 to 255 (including both). Some tiles are marked with colored borders. For more information on these tiles,
            see the special tiles section.
          </div>
          <div class="doc-title">Robot Collision</div>
          <div>
            <span class="highlighted">Every tile can only contain one robot at a time.</span> If robot A wants to go to a tile where robot B is standing or
            wants to go in the next round, the move action will fail and the robot will not move.
          </div>
        </div>
      </InfoBoxTemplate>
      <InfoBoxTemplate title="Map special tiles">
        <div class="doc">
          <div class="doc-img">
            <ImageComponent class="m-3 is-block" image-src="/img/how-to/tiles-map.png" title="A small map with all special tiles in it." />
          </div>
          <div class="doc-title">Starting tiles</div>
          <div>
            Every map has several starting points, in which new participating robots are placed during the <code>WAITING_FOR_PLAYERS</code> game state. All of
            them are marked with an <span style="color: rgb(210, 110, 0)">orange</span> tile border. The count of starting tiles also defines the maximum number of players
            for a map. If more robots participate than there are tiles available, <span class="highlighted">only the early ones</span> will be included.
          </div>
          <div>Tile type: <code>START_TILE</code></div>
          <div class="doc-title">Target tile</div>
          <div>
            The target tile is marked on the map with a <span style="color: rgb(0, 130, 255)">cyan</span> colored tile border. Every map has exactly one target
            tile, which must be reached to win the current game. Its position can be retrieved from the global game information. The position is usually provided, but it
            could be hidden by the operators to increase the difficulty.
          </div>
          <div>Tile type: <code>TARGET_TILE</code></div>
          <div class="doc-title">Fuel stations</div>
          <div>
            The map can contain fuel stations which allow your robot to use the refuel action on this special tile. A
            <span style="color: rgb(210, 0, 130)">magenta</span>
            border shows the locations on the web ui.
          </div>
          <div>Tile type: <code>FUEL_TILE</code></div>
        </div>
      </InfoBoxTemplate>
    </div>
    <div class="column is-half">
      <InfoBoxTemplate title="Game Lifecycle">
        <div class="doc">
          <div>
            RoboRush is a pure turn-based game to ensure that all actions happen simultaneously. For this purpose, the game engine has defined game states, which
            describe what is currently happening. In the Overview below, you can see normal state transitions as well as transitions with timeouts (indicated by little clock
            icons). Timeouts simply pause the game flow for a defined time in the current game state. All timeouts for the current game can be viewed
            in the game information GET endpoint (See the <a href="/api/swagger-ui?urls.primaryName=public" target="_blank">OpenApi Spec</a>).
          </div>
          <div class="doc-img">
            <ImageComponent
              class="m-3 is-block is-background-grey"
              image-src="/img/how-to/game-states.drawio.svg"
              title="The game states with all possible transitions."
            />
          </div>
          <div class="doc-title">PREPARE</div>
          <div>This state selects one of the available maps and prepares everything for a new game.</div>
          <div class="doc-title">WAIT_FOR_PLAYERS</div>
          <div>
            During this phase the game allows the registration of participating robots for the upcoming game. New participants will be spawned immediately at a
            random start tile of the map. In case of no players after the timeout, the game will reset to the preparation state.
          </div>
          <div class="doc-title">WAIT_FOR_ACTION</div>
          <div>
            While the game is in this state, every robot can schedule its next action. This is typically done by making decisions based on the current game and robot
            information. If an invalid action is intended, errors with a detailed description will be returned. Additionally, if an action with returning data is planned,
            remember that the data will available <span class="highlighted">after</span> the upcoming action state.
          </div>
          <div class="doc-title">ACTION</div>
          <div>
            Now all robot actions will be executed and all action results are stored in the robot attributes. After the execution the target tile will be
            checked for a standing robot. If no winner is found, the state transitions to <code>WAIT_FOR_ACTION</code>. Otherwise the winner will be announced
            and the game will enter the ending phase.
          </div>
          <div class="doc-title">ENDED</div>
          <div>
            The final state acts like a little cooldown period during which no robot interaction is possible, and the winning robot is displayed on the web UI as well as in the
            game info endpoint. After this cooldown phase, a new game starts by transitioning to the <code>PREPARE</code> state again.
          </div>
        </div>
      </InfoBoxTemplate>
    </div>
  </div>
</template>

<script setup lang="ts">
import ImageComponent from "@/components/ImageComponent.vue";
import InfoBoxTemplate from "@/components/templates/InfoBoxTemplate.vue";
</script>

<style scoped lang="scss">
.highlighted {
  font-style: italic;
}

.doc {
  div {
    margin-bottom: 0.5rem;
  }

  .doc-img {
    display: flex;
    flex-direction: column;

    img {
      align-self: center;
    }
  }

  .doc-title {
    margin-top: 0.25rem;
    margin-bottom: 0.25rem;
    font-size: 1.5rem;
    text-transform: capitalize;
  }
}
</style>
