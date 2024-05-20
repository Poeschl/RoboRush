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
            Every player will control its robot via REST interfaces to check its environment or execute a robot action. Since the robots are not aware of the
            map around them, they might need to have a clever way to navigate through the map. But take care, since all actions will deplete the robots fuel
            tank a little. If a robot runs out of fuel it is immobile and can't execute a action anymore.
          </div>
          <div>
            To win the game your robot needs to be <span class="highlighted">the first one reaching the target position</span>. Every win adds one point on the
            highscore list to your entry.
          </div>
        </div>
      </InfoBoxTemplate>
    </div>
    <div class="column is-half">
      <InfoBoxTemplate title="Robot">
        <div class="doc">
          <div>
            Every player has exactly one robot and the robot has some attributes. The robot is named after the players username and can be instructed via a rest
            interface. To visually monitor the live attributes look at the main page and be logged in.
          </div>
          <div class="doc-title">Robot attributes</div>
          <div>
            Every robot has a fuel tank, which is filled at the start of each game. For every map the maximum amount of fuel can be different. We made sure you
            have a good chance to reach the target tile, no worries. The tank will be drained on most of the robot actions. When it
            <span class="highlighted">reaches zero fuel units your robot can not execute a action with a cost anymore</span>.
          </div>
          <div class="doc-title">Robot actions</div>
          <div>
            A robot can be instructed via a REST interface, which require your API access token. For a overview of the available commands and how the
            authentication works in detail, we provide a
            <a href="/api/swagger-ui?urls.primaryName=public" target="_blank">OpenApi Spec for the robot</a>. To send example requests, please enter you api
            token in the top "Authorize" button dialog and execute some requests with the "Try it out" button.
          </div>
          <div class="doc-title">Registration</div>
          <div>
            To get a robot you need to register yourself via the button in the top right corner in the navigation bar. After registration you can login yourself
            with you new credentials and click on your username in the top bar to get your api key.
          </div>
          <div class="doc-img">
            <ImageComponent class="m-3 is-block" image-src="/img/how-to/user-profile.png" title="A screenshot of the user profile dialog." />
          </div>
        </div>
      </InfoBoxTemplate>
      <InfoBoxTemplate title="Possible robot actions">
        <div class="doc">
          <div>
            There are several actions a robot can do in a turn. If no participating robot execute a action at all for a few rounds the game will be ended.
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
            With the scan the robot will retrieve the map tiles around hin according to the given scan distance. The scan return every tile which has a
            <a href="https://en.wikipedia.org/wiki/Taxicab_geometry" target="_blank">Manhattan geometry</a> equal or less then the scan distance parameter. A
            scan is very fuel consuming on large patches, since the cost is the distance squared. The results of the scan are available by the
            <code>lastResult</code> attribute of the robot information GET request after the execution phase.
          </div>
          <div>Cost: <code>distance * distance</code></div>
          <div class="doc-title">Wait</div>
          <div>Let your robot skip the next turn. Just chilling...</div>
          <div>Gain: <code>Nothing</code></div>
          <div class="doc-title">Refuel</div>
          <div>
            <span class="highlighted">When standing on a fuel station tile</span> your robot can schedule the refuel action. After execution your robot's fuel
            tank will be fully refilled. Just remember, that there can only one robot on a single tile.
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
            The map is the playground where every robot will move. It is described by tiles which are arranged as a grid with variable size. Every game start
            the map can be switched to a different one, depending on the liking of the operators. During a game the map sizes stays the same, obviously.
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
            The map at the main webpage is fully drawn to show the full game to you or interested people, but the robots see it differently. Robots will start
            without any knowledge of the surrounding tiles. In order to retrieve the tile heights and attributes around themselves the need to execute a scan
            action (see robot actions).
          </div>
          <div class="doc-title">Tile attributes</div>
          <div>
            Every tile has a height which is displayed in different shades of green on the web UI. Higher tiles are brighter then tiles at zero level. Robots
            can move on all tiles with a height in the range of 0 - 255 (including both). There are some tiles which are marked with colored borders. For them
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
            them are marked with a <span style="color: rgb(210, 110, 0)">orange</span> tile border. The count of starting tiles also defines the maximum players
            for a map. If more robots participate then tiles are available, <span class="highlighted">only the early ones</span> will be included.
          </div>
          <div>Tile type: <code>START_TILE</code></div>
          <div class="doc-title">Target tile</div>
          <div>
            The target tile is marked on the map with a <span style="color: rgb(0, 130, 255)">cyan</span> colored tile border. Every map has exactly one target
            tile, which must be reached to win the current game. It's position can be retrieved by the global game infos. The position is normally given, but
            could be hidden by the operators for a higher difficulty.
          </div>
          <div>Tile type: <code>TARGET_TILE</code></div>
          <div class="doc-title">Fuel stations</div>
          <div>
            The map can contain fuel stations which allow your robot to use the refuel action on this special tile. A magenta border shows the locations on the
            web ui.
          </div>
          <div>Tile type: <code>FUEL_TILE</code></div>
        </div>
      </InfoBoxTemplate>
    </div>
    <div class="column is-half">
      <InfoBoxTemplate title="Game Lifecycle">
        <div class="doc">
          <div>
            RoboRush is a pure turn-based game to ensure that all actions happens at the same time. For that the game engine has defined game states, which
            describes what is currently happening. In the Overview below you can see normal state transitions as well as transitions with timeouts (little clock
            icons). The timeouts will just block the game flow for a defined time in the current game state. All timeouts for the current game can be looked up
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
          <div>This state selects on of the available maps and prepares everything for a new game.</div>
          <div class="doc-title">WAIT_FOR_PLAYERS</div>
          <div>
            During this phase the game allows the registration of participating robots for the upcoming game. New participants will be spawned immediately at a
            random start tile of the map. In case of no players after the timeout, the game will reset to the preparation state.
          </div>
          <div class="doc-title">WAIT_FOR_ACTION</div>
          <div>
            While the game is in this state, every robot can schedule its next action. This is typically done by a decision based on the current game and robot
            information. If a invalid action is intended errors with a detailed description will be returned. Also if a action with returning data is planned,
            remember that the data will available <span class="highlighted">after</span> the upcoming action state.
          </div>
          <div class="doc-title">ACTION</div>
          <div>
            Now all robot actions will be executed and all action results are stored in the robot attributes. After the execution the target tile will be
            checked for a standing robot. If no winner is found, the state transitions to <code>WAIT_FOR_ACTION</code>. Otherwise the winner will be announced
            and the game went into the ending phase.
          </div>
          <div class="doc-title">ENDED</div>
          <div>
            The final state acts like a little cooldown in which no robot interaction is possible and the winning robot is shown at the web ui as well as in the
            game info endpoint. After this chill-phase a new game starts with entering the <code>PREPARE</code> state again.
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
