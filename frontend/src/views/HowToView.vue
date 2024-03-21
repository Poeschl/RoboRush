<template>
  <div class="columns is-multiline">
    <div class="column is-full">
      <HelpText title="What is PathSeeker?">
        <div class="mb-2">
          PathSeeker is a multiplayer coding game about finding a path from A to B for a robot on a 2 dimensional map. The game is played in a round-based
          manner, so that every player's robot moves at the exact same time.
        </div>
        <div class="mb-2">
          Every player will control its robot via REST interfaces to check its environment or execute a robot action. Since the robots are not aware of the map
          around him he might need to have a clever way to navigate through the map. All actions will deplete the robots fuel tank a little. If a robot runs out
          of fuel it is immobile for the current game.
        </div>
        <div class="mb-2">TODO: Goal</div>
      </HelpText>
    </div>
    <div class="column is-half">
      <HelpText title="Game Lifecycle"> // TODO </HelpText>
    </div>
    <div class="column is-half">
      <HelpText title="Robot">
        <div class="mb-2">
          Every player has exactly one robot and the robot has some attributes. The robot is named after the players username and can be instructed via a rest
          interface. To monitor the live attributes look at the main page and be logged in.
        </div>
        <div class="is-size-4">Register a robot</div>
        <div class="mb-2 is-flex is-flex-direction-column">
          To get a robot you need to register yourself via the button in the top right corner in the navigation bar. After registration you can login yourself
          with you new credentials and click on your username in the top bar to get your api key.

          <ImageComponent class="m-3 is-block" image-src="/img/how-to/user-profile.png" title="A screenshot of the user profile dialog." />
        </div>
        <div class="is-size-4">Robot actions</div>
        <div class="mb-2">
          A robot can be instructed via a REST interface, which require your API access token. For a overview of the available commands and how the
          authentication works in detail, we provide a
          <a href="/api/swagger-ui?urls.primaryName=public" target="_blank"> OpenApi Spec for the robot </a>. To see example requests, please enter you api
          token in the top "Authorize" button dialog and execute some requests with the "Try it out" button.
        </div>
        <div class="is-size-5">Move Action</div>
        <div class="mb-2">
          With this action the robot moves to one of the neighbor map tiles. The robot can only move one tile at once in one of the 4 directions (east, south,
          west, north). Every movement costs the height difference of the map tiles plus 1 static fuel.
        </div>
        <div class="mb-2">As cost formula: <code>abs(heightCurrentTile - heightDestinationTile) + 1</code></div>
        <div class="is-size-5">Scan Action</div>
        <div class="mb-2">
          With the scan the robot will retrieve the map tiles around hin according to the given scan distance. The scan return every tile which has a
          <a href="https://en.wikipedia.org/wiki/Taxicab_geometry" target="_blank">Manhattan geometry</a> equal or less then the scan distance parameter. A scan
          is very fuel consuming on large patches, since the cost is the distance squared.
        </div>
        <div class="mb-2">As cost formula: <code>distance * distance</code></div>
      </HelpText>
    </div>
    <div class="column is-half">
      <HelpText title="Map">
        <p class="is-flex is-flex-direction-column">
          The map is the playground where every robot will move. It is described by tiles which are arranged as a grid with variable size. The size of a map is
          fixed after during a game, but can change at the start of a new one.

          <ImageComponent class="m-3 is-block" image-src="/img/how-to/basic-map.png" title="A small map with its tile as an example." />
        </p>
        <p class="mb-2">
          Every tile position can be described by a two-dimensional coordinate system with x and y. The origin tile with <code>(0, 0)</code> is in the top left
          corner and x increases towards the right and y to the bottom. As a example the robots in the map above are at position <code>(2, 3)</code> and
          <code>(5, 7)</code>.
        </p>
        <div class="is-size-5">Tile attributes</div>
        <p class="mb-2">
          Every tile has a height which is displayed in different shades of green. Higher tiles are brighter then tiles at zero level. For now the heights are
          all positive, if a robot can move on them.
        </p>
        <div class="is-size-5">How the robot sees the map</div>
        <p class="mb-2">
          The map at the main webpage is fully drawn to show the full game to you or interested people, but the robots see it differently. They will start in
          the black and don't know anything about the surrounding tiles. In order to know more about the tiles around them they need to make scans (see robot
          actions).
        </p>
      </HelpText>
    </div>
  </div>
</template>

<script setup lang="ts">
import HelpText from "@/components/HelpText.vue";
import ImageComponent from "@/components/ImageComponent.vue";
</script>

<style scoped lang="scss">
.highlighted {
  font-style: italic;
}
</style>
