import { Client, StompHeaders, Versions } from "@stomp/stompjs";
import type { ActiveRobot, PublicRobot } from "@/models/Robot";
import { correctTypesFromJson } from "@/models/Robot";
import Color from "@/models/Color";
import type { ComputedRef } from "vue";
import { watch } from "vue";
import type { User } from "@/models/User";
import type { GameState } from "@/models/Game";
import log from "loglevel";
import type { Position, Tile } from "@/models/Map";
import type { ClientSettings } from "@/models/Config";

export enum WebSocketTopic {
  PUBLIC_ROBOT_TOPIC = 0,
  PRIVATE_ROBOT_TOPIC,
  GAME_STATE_TOPIC,
  GAME_TURN_TOPIC,
  CLIENT_SETTINGS_TOPIC,
  PRIVATE_KNOWN_POSITIONS_TOPIC,
  MAP_TILES_UPDATE,
}

export function useWebSocket(): { initWebsocket: Function; registerForTopicCallback: Function } {
  const websocketPath = "/api/ws";
  const publicRobotUpdateTopic = "/topic/robot";
  const publicGameStateUpdateTopic = "/topic/game/state";
  const publicGameTurnUpdateTopic = "/topic/game/turn";
  const publicMapTileUpdateTopic = "/topic/map/tiles";
  const publicClientSettingsTopic = "/topic/config/client";
  const userRobotUpdateQueue = "/queue/robot";
  const userRobotKnownPositionsUpdateTopic = "/queue/robot/knownPositions";
  const topicListener = new Map<WebSocketTopic, Function>();
  let websocketClient: Client | undefined = undefined;

  const initWebsocket = (user: ComputedRef<User | undefined>) => {
    startSharedClient(user.value);
    watch(
      () => user.value == undefined,
      (current, previous, onCleanup) => {
        destroySharedClient();
        startSharedClient(user.value);
      },
    );
  };

  const registerForTopicCallback = (topic: WebSocketTopic, callback: Function) => {
    topicListener.set(topic, callback);
  };

  const startSharedClient = (user: User | undefined) => {
    const client = createClient(user);

    client.onConnect = () => {
      log.info("Websocket connected");

      connectToPublicTopics(client);

      if (user != undefined) {
        connectToUserQueue(client, user.username);
      }
    };

    client.activate();
    websocketClient = client;
  };

  const destroySharedClient = () => {
    if (websocketClient != undefined) {
      websocketClient.deactivate();
    }
  };

  /**
   * Authentication will be automatically done by the first request which is a plain http request and secured by Spring Security.
   * @private
   */
  const createClient = (user: User | undefined): Client => {
    const protocol = location.protocol == "https:" ? "wss" : "ws";
    const websocketUrl = `${protocol}://${location.host}${websocketPath}`;

    let connectHeaders: StompHeaders | undefined = undefined;
    if (user != undefined) {
      connectHeaders = { authentication: user.accessToken };
    }

    return new Client({
      brokerURL: websocketUrl,
      stompVersions: new Versions(["1.2"]),
      connectHeaders: connectHeaders,

      onWebSocketError: (error) => {
        log.error(`Error with websocket (${JSON.stringify(error)})`);
      },

      onStompError: (frame) => {
        log.error(`Broker reported error: ${frame.headers["message"]}`);
        log.error(`Additional details: ${frame.body}`);
      },

      onWebSocketClose: (closeEvent: CloseEvent) => {
        log.error("Websocket disconnected.");
      },
    });
  };

  const connectToPublicTopics = (client: Client) => {
    client.subscribe(publicRobotUpdateTopic, (message) => {
      const publicRobot: PublicRobot = JSON.parse(message.body);
      publicRobot.color = new Color(publicRobot.color.r, publicRobot.color.g, publicRobot.color.b);
      topicListener.get(WebSocketTopic.PUBLIC_ROBOT_TOPIC)?.call(null, publicRobot);
    });

    client.subscribe(publicGameStateUpdateTopic, (message) => {
      const gameState: GameState = JSON.parse(message.body);
      topicListener.get(WebSocketTopic.GAME_STATE_TOPIC)?.call(null, gameState);
    });
    client.subscribe(publicGameTurnUpdateTopic, (message) => {
      const turnCount: number = parseInt(message.body);
      topicListener.get(WebSocketTopic.GAME_TURN_TOPIC)?.call(null, turnCount);
    });
    client.subscribe(publicClientSettingsTopic, (message) => {
      const settings: ClientSettings = JSON.parse(message.body);
      topicListener.get(WebSocketTopic.CLIENT_SETTINGS_TOPIC)?.call(null, settings);
    });
    client.subscribe(publicMapTileUpdateTopic, (message) => {
      const tiles: Tile[] = JSON.parse(message.body);
      topicListener.get(WebSocketTopic.MAP_TILES_UPDATE)?.call(null, tiles);
    });
  };

  const connectToUserQueue = (client: Client, username: string) => {
    client.subscribe(`/user/${username}${userRobotUpdateQueue}`, (message) => {
      if (message.body.length > 0) {
        const userRobot: ActiveRobot = JSON.parse(message.body);
        topicListener.get(WebSocketTopic.PRIVATE_ROBOT_TOPIC)?.call(null, correctTypesFromJson(userRobot));
      } else {
        topicListener.get(WebSocketTopic.PRIVATE_ROBOT_TOPIC)?.call(null, undefined);
      }
    });
    client.subscribe(`/user/${username}${userRobotKnownPositionsUpdateTopic}`, (message) => {
      const knownPositions: Position[] = JSON.parse(message.body);
      topicListener.get(WebSocketTopic.PRIVATE_KNOWN_POSITIONS_TOPIC)?.call(null, knownPositions);
    });
  };

  return { initWebsocket, registerForTopicCallback };
}
