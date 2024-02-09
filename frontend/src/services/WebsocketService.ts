import { Client, StompHeaders, Versions } from "@stomp/stompjs";
import type { ActiveRobot, PublicRobot } from "@/models/Robot";
import { correctTypesFromJson } from "@/models/Robot";
import Color from "@/models/Color";
import type { ComputedRef } from "vue";
import { watch } from "vue";
import type { User } from "@/models/User";

export enum WebSocketTopic {
  PUBLIC_ROBOT_TOPIC = 0,
  PRIVATE_ROBOT_TOPIC,
}

export function useWebSocket() {
  const websocketPath = "/api/ws";
  const publicRobotUpdateTopic = "/topic/robot";
  const userRobotUpdateQueue = "/queue/robot";
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
      console.info("Websocket connected");

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
        console.error(`Error with websocket (${JSON.stringify(error)})`);
      },

      onStompError: (frame) => {
        console.error(`Broker reported error: ${frame.headers["message"]}`);
        console.error(`Additional details: ${frame.body}`);
      },

      onWebSocketClose: (closeEvent: CloseEvent) => {
        console.error("Websocket disconnected.");
      },
    });
  };

  const connectToPublicTopics = (client: Client) => {
    client.subscribe(publicRobotUpdateTopic, (message) => {
      const publicRobot: PublicRobot = JSON.parse(message.body);
      publicRobot.color = new Color(publicRobot.color.r, publicRobot.color.g, publicRobot.color.b);
      topicListener.get(WebSocketTopic.PUBLIC_ROBOT_TOPIC)?.call(null, publicRobot);
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
  };

  return { initWebsocket, registerForTopicCallback };
}
