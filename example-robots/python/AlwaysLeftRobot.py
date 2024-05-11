import logging
from time import sleep

import requests

# This is the url to the RoboRush host.
ROBORUSH_URL = "http://localhost:8080/api"

# Replace this token value with your own api token.
# The token can be retrieved in the profile settings on the web ui.
API_TOKEN = ""

def main():
  logging.basicConfig(level=logging.INFO)
  logging.info("Always Left Robot booting up...")

  # We need to remember that we are already attending a game
  is_attending = False

  # Now lets move our robot always to the left (WEST) if a new instruction is required
  while True:

    # Let's get the general available information about the current game
    current_game_info = getGameInfos()

    # If the game is open for new players
    if not is_attending and current_game_info["currentState"] == "WAIT_FOR_PLAYERS":
      is_attending = send_robot_attending()

    # Check if we need to send a new action
    elif is_attending and current_game_info["currentState"] == "WAIT_FOR_ACTION":
      send_robot_east_move_action()

    elif current_game_info["currentState"] == "ENDED":
      is_attending = False

    # Make sure to avoid a CPU hell!
    sleep(0.25)


def getGameInfos():
  # Send a request to the game endpoint. For this no authentication is needed.
  response = requests.get(ROBORUSH_URL + "/game")

  # The response contains information about the current game
  gameInfo = response.json()
  logging.info("Received game info %s", gameInfo)
  return gameInfo


def send_robot_attending() -> bool:
  # Send a request to mark your robot as participating in the current game.
  # For this endpoint we need to be authenticated with a token.
  response = requests.post(ROBORUSH_URL + "/robot/attend", data="", headers={"Authorization": "Bearer " + API_TOKEN})

  # The status of the attending call indicated the participation
  logging.info("Attend the current game: %s", response.status_code == 200)
  return response.status_code == 200

def send_robot_east_move_action():
  # Create a request to tell the robot to move to east in the next round
  # For this endpoint we need to be authenticated with a token.
  response = requests.post(ROBORUSH_URL + "/robot/action/move", json={"direction": "WEST"}, headers={"Authorization": "Bearer " + API_TOKEN})

  # If an error happens, it will be returned in the response body
  if response.status_code == 200:
    logging.info("Move robot to EAST on next round")
  else:
    error_json = response.json()
    logging.info("Error: %s %s", error_json["status"], error_json["message"])


if __name__ == '__main__':
  main()
