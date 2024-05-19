<template>
  <div class="columns is-multiline">
    <div class="column is-full">
      <InfoBoxTemplate title="What is RoboRush?">
        <div class="doc">
          <div>
            RoboRush is a multiplayer coding game about finding a path from A to B for a robot on a 2 dimensional map. The game is played in a turn-based
            manner, so that every player's robot moves at the exact same time.
          </div>
          <div>
            Every player will control its robot via REST interfaces to check its environment or execute a robot action. Since the robots are not aware of the
            map around them, they might need to have a clever way to navigate through the map. But take care, since all actions will deplete the robots fuel
            tank a little. If a robot runs out of fuel it is immobile and can't execute a action anymore.
          </div>
          <div>
            To win the game your robot needs to be the first one reaching the target position. Every win adds one point on the highscore list to your entry.
          </div>
        </div>
      </InfoBoxTemplate>
    </div>
    <div class="column is-half">
      <InfoBoxTemplate title="Robot">
        <div class="doc">
          <div>
            Every player has exactly one robot and the robot has some attributes. The robot is named after the players username and can be instructed via a rest
            interface. To monitor the live attributes look at the main page and be logged in.
          </div>
          <div class="doc-title">Registration</div>
          <div>
            To get a robot you need to register yourself via the button in the top right corner in the navigation bar. After registration you can login yourself
            with you new credentials and click on your username in the top bar to get your api key.
          </div>
          <div class="doc-img">
            <ImageComponent class="m-3 is-block" image-src="/img/how-to/user-profile.png" title="A screenshot of the user profile dialog." />
          </div>
          <div class="doc-title">Robot actions</div>
          <div>
            A robot can be instructed via a REST interface, which require your API access token. For a overview of the available commands and how the
            authentication works in detail, we provide a
            <a href="/api/swagger-ui?urls.primaryName=public" target="_blank">OpenApi Spec for the robot</a>. To send example requests, please enter you api
            token in the top "Authorize" button dialog and execute some requests with the "Try it out" button.
          </div>
        </div>
      </InfoBoxTemplate>
      <InfoBoxTemplate title="Possible robot actions">
        <div class="doc">
          <div>There are several actions a robot can do in a turn.</div>
          <div class="doc-title">Move Action</div>
          <div>
            With this action the robot moves to one of the neighbor map tiles. The robot can only move one tile at once in one of the 4 directions (east, south,
            west, north). Every movement costs the height difference of the map tiles plus 1 static fuel. "Rolling down" from a higher tile to a lower one,
            costs only 1 fuel unit.
          </div>
          <div>Cost: <code>max(heightDestinationTile - heightCurrentTile, 0) + 1</code></div>
          <div class="doc-title">Scan Action</div>
          <div>
            With the scan the robot will retrieve the map tiles around hin according to the given scan distance. The scan return every tile which has a
            <a href="https://en.wikipedia.org/wiki/Taxicab_geometry" target="_blank">Manhattan geometry</a> equal or less then the scan distance parameter. A
            scan is very fuel consuming on large patches, since the cost is the distance squared.
          </div>
          <div>Cost: <code>distance * distance</code></div>
          <div class="doc-title">Refuel</div>
          <div>//TBD</div>
          <div>Gain: <code>Full fuel tank</code></div>
          <div class="doc-title">Solar Charge</div>
          <div>//TBD</div>
          <div>Gain: <code>distance * distance</code></div>
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
        </div>
      </InfoBoxTemplate>
      <InfoBoxTemplate title="Map Special Tiles">
        <div class="doc">
          <div class="doc-title">Starting tiles</div>
          <div>//TBD</div>
          <div class="doc-title">Target tile</div>
          <div>//TBD</div>
          <div class="doc-title">Fuel stations</div>
          <div>//TBD</div>
        </div>
      </InfoBoxTemplate>
    </div>
    <div class="column is-half">
      <InfoBoxTemplate title="Game Lifecycle">
        <div class="doc">//TODO</div>
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
  }

  .doc-title {
    margin-top: 0.25rem;
    margin-bottom: 0.25rem;
    font-size: 1.5rem;
  }
}
</style>
