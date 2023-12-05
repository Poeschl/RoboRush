import { Client } from "@stomp/stompjs";
import type { PublicRobot } from "@/models/Robot";
import type { Store } from "pinia";
import { useGameStore } from "@/stores/GameStore";
import Color from "@/models/Color";

export default class WebsocketService {
  private websocketBrokerUrl = "ws://localhost:8888/api/ws";
  private robotUpdateTopic = "/topic/robots";
  private gameStore: Store;

  constructor(gameStore: Store) {
    this.gameStore = gameStore;
    this.initWebsocket();
  }

  initWebsocket(): void {
    const client = new Client({
      brokerURL: this.websocketBrokerUrl,

      onWebSocketError: (error) => {
        console.error(`Error with websocket (${JSON.stringify(error)})`);
      },

      onStompError: (frame) => {
        console.error(`Broker reported error: ${frame.headers["message"]}`);
        console.error(`Additional details: ${frame.body}`);
      },
    });

    client.onConnect = () => {
      console.info("Websocket connected");

      client.subscribe(this.robotUpdateTopic, (message) => {
        const publicRobot: PublicRobot = JSON.parse(message.body);
        publicRobot.color = new Color(publicRobot.color.r, publicRobot.color.g, publicRobot.color.b);
        // @ts-ignore
        this.gameStore.updateRobot(publicRobot);
      });
    };

    client.activate();
  }
}
