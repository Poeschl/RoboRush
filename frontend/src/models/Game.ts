export interface Game {
  currentState: GameState;
}

export enum GameState {
  PREPARE = "PREPARE",
  WAIT_FOR_PLAYERS = "WAIT_FOR_PLAYERS",
  WAIT_FOR_ACTION = "WAIT_FOR_ACTION",
  ACTION = "ACTION",
  ENDED = "ENDED",
}

export interface Error {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
