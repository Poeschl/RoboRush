import { Client, StompHeaders, Versions } from "@stomp/stompjs";
import type { ActiveRobot, Move, PublicRobot, Scan } from "@/models/Robot";
import type { Store } from "pinia";
import Color from "@/models/Color";
import { watch } from "vue";
import type { User } from "@/models/User";
import { correctTypesFromJson } from "@/models/Robot";

export enum WebSocketTopic {
  PUBLIC_ROBOT_TOPIC = 0,
  PRIVATE_ROBOT_TOPIC,
}

export default class WebsocketService {
  private websocketPath = "/api/ws";
  private publicRobotUpdateTopic = "/topic/robot";
  private userRobotUpdateQueue = "/queue/robot";
  private systemStore: Store;
  private websocketClient: Client | undefined;
  private topicListener = new Map<WebSocketTopic, Function>();

  constructor(systemStore: Store, userStore: Store) {
    this.systemStore = systemStore;
    this.initWebsocket(userStore);
  }

  initWebsocket(userStore: Store) {
    this.startSharedClient(userStore.user);
    watch(
      () => userStore.loggedIn,
      (current, previous, onCleanup) => {
        this.destroySharedClient();
        this.startSharedClient(userStore.user);
      },
    );
  }

  registerForTopicCallback(topic: WebSocketTopic, callback: Function) {
    this.topicListener.set(topic, callback);
  }

  private startSharedClient(user: User | undefined) {
    const client = this.createClient(user);

    client.onConnect = () => {
      console.info("Websocket connected");

      this.connectToPublicTopics(client);

      if (user != undefined) {
        this.connectToUserQueue(client, user.username);
      }
    };

    client.activate();
    this.websocketClient = client;
  }

  private destroySharedClient() {
    if (this.websocketClient != undefined) {
      this.websocketClient.deactivate();
    }
  }

  /**
   * Authentication will be automatically done by the first request which is a plain http request and secured by Spring Security.
   * @private
   */
  private createClient(user: User | undefined): Client {
    const protocol = location.protocol == "https:" ? "wss" : "ws";
    const websocketUrl = `${protocol}://${location.host}${this.websocketPath}`;

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
        // @ts-ignore Since the type injection with store is black magic
        if (!closeEvent.wasClean) {
          this.systemStore.backendAvailable = false;
        }
      },
    });
  }

  private connectToPublicTopics(client: Client) {
    client.subscribe(this.publicRobotUpdateTopic, (message) => {
      const publicRobot: PublicRobot = JSON.parse(message.body);
      publicRobot.color = new Color(publicRobot.color.r, publicRobot.color.g, publicRobot.color.b);
      this.topicListener.get(WebSocketTopic.PUBLIC_ROBOT_TOPIC)?.call(null, publicRobot);
    });
  }

  private connectToUserQueue(client: Client, username: string) {
    client.subscribe(`/user/${username}${this.userRobotUpdateQueue}`, (message) => {
      if (message.body.length > 0) {
        const userRobot: ActiveRobot = JSON.parse(message.body);
        this.topicListener.get(WebSocketTopic.PRIVATE_ROBOT_TOPIC)?.call(null, correctTypesFromJson(userRobot));
      } else {
        this.topicListener.get(WebSocketTopic.PRIVATE_ROBOT_TOPIC)?.call(null, undefined);
      }
    });
  }
}
