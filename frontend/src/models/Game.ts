export interface Game {
  currentState: GameState;
}

export enum GameState {
  PREPARE = "PREPARE",
  WAIT_FOR_ACTION = "WAIT_FOR_ACTION",
  ACTION = "ACTION",
  ENDED = "ENDED",
}
