import logging
import math
from time import sleep
from typing import List, Dict, Tuple, Optional, Set
import heapq

import requests

# This is the url to the RoboRush host.
ROBORUSH_URL = "http://localhost:8080/api"

# Replace this token value with your own api token.
# The token can be retrieved in the profile settings on the web ui.
API_TOKEN = ""

class Position:
    def __init__(self, x: int, y: int):
        self.x = x
        self.y = y
    
    def __eq__(self, other):
        return self.x == other.x and self.y == other.y
    
    def __hash__(self):
        return hash((self.x, self.y))
    
    def __repr__(self):
        return f"Position({self.x}, {self.y})"
    
    def distance_to(self, other: 'Position') -> int:
        """Manhattan distance"""
        return abs(self.x - other.x) + abs(self.y - other.y)
    
    def euclidean_distance_to(self, other: 'Position') -> float:
        """Euclidean distance for heuristic"""
        return math.sqrt((self.x - other.x)**2 + (self.y - other.y)**2)

class PathNode:
    def __init__(self, position: Position, g_score: float, f_score: float, parent: Optional['PathNode'] = None):
        self.position = position
        self.g_score = g_score  # Cost from start to this node
        self.f_score = f_score  # Estimated total cost through this node
        self.parent = parent
    
    def __lt__(self, other):
        return self.f_score < other.f_score

class SmartPathfindingRobot:
    """
    A sophisticated robot that demonstrates proper pathfinding techniques.
    
    This robot shows how to:
    1. Use A* algorithm for optimal pathfinding
    2. Consider height differences and fuel costs
    3. Handle obstacles and blocked positions
    4. Implement fallback strategies
    
    This is an example of how players should approach pathfinding instead of 
    using simple greedy approaches like "always go left/down".
    """
    
    def __init__(self):
        self.is_attending = False
        self.known_map = {}
        self.map_size = None
        self.target_position = None
        self.current_position = None
        self.fuel = 0
        self.max_fuel = 0
        
    def main(self):
        logging.basicConfig(level=logging.INFO)
        logging.info("Smart Pathfinding Robot booting up...")
        logging.info("This robot demonstrates advanced pathfinding techniques")
        logging.info("and shows why simple greedy approaches are suboptimal.")
        
        while True:
            try:
                current_game_info = self.get_game_infos()
                
                if not self.is_attending and current_game_info["currentState"] == "WAIT_FOR_PLAYERS":
                    self.is_attending = self.send_robot_attending()
                    
                elif self.is_attending and current_game_info["currentState"] == "WAIT_FOR_ACTION":
                    self.update_robot_state()
                    self.make_smart_move()
                    
                elif current_game_info["currentState"] == "ENDED":
                    self.is_attending = False
                    logging.info("Game ended. Resetting for next game.")
                    
                sleep(0.25)
                
            except Exception as e:
                logging.error(f"Error in main loop: {e}")
                sleep(1)
    
    def get_game_infos(self) -> Dict:
        """Get general game information"""
        response = requests.get(ROBORUSH_URL + "/game")
        game_info = response.json()
        
        # Update map size and target if available
        if "mapSize" in game_info:
            self.map_size = game_info["mapSize"]
        if "targetPosition" in game_info and game_info["targetPosition"]:
            self.target_position = Position(
                game_info["targetPosition"]["x"],
                game_info["targetPosition"]["y"]
            )
            
        return game_info
    
    def send_robot_attending(self) -> bool:
        """Register robot for the current game"""
        response = requests.post(
            ROBORUSH_URL + "/robot/attend", 
            data="", 
            headers={"Authorization": "Bearer " + API_TOKEN}
        )
        
        success = response.status_code == 200
        logging.info(f"Attend the current game: {success}")
        return success
    
    def update_robot_state(self):
        """Update robot's current state and known map"""
        try:
            response = requests.get(
                ROBORUSH_URL + "/robot", 
                headers={"Authorization": "Bearer " + API_TOKEN}
            )
            
            if response.status_code == 200:
                robot_data = response.json()
                self.current_position = Position(
                    robot_data["position"]["x"],
                    robot_data["position"]["y"]
                )
                self.fuel = robot_data["fuel"]
                self.max_fuel = robot_data["maxFuel"]
                
                # Update known map with scan results
                if "lastResult" in robot_data and robot_data["lastResult"]:
                    self.update_known_map(robot_data["lastResult"])
                    
        except Exception as e:
            logging.error(f"Error updating robot state: {e}")
    
    def update_known_map(self, scan_result):
        """Update the known map with scan results"""
        if isinstance(scan_result, list):
            for tile in scan_result:
                pos = Position(tile["position"]["x"], tile["position"]["y"])
                self.known_map[pos] = {
                    "height": tile["height"],
                    "type": tile["type"]
                }
    
    def make_smart_move(self):
        """Make an intelligent move using pathfinding"""
        if not self.current_position or not self.target_position:
            logging.warning("Missing position information for pathfinding")
            self.send_fallback_move()
            return
        
        # Use A* pathfinding to find optimal route
        path = self.find_optimal_path()
        
        if path and len(path) > 1:
            next_position = path[1]  # First position in path is current position
            direction = self.get_direction_to(self.current_position, next_position)
            
            if direction:
                logging.info(f"Smart pathfinding: Moving {direction} towards target")
                self.send_move_action(direction)
            else:
                logging.warning("Could not determine direction from pathfinding")
                self.send_fallback_move()
        else:
            logging.warning("No path found with A* algorithm, using fallback")
            self.send_fallback_move()
    
    def find_optimal_path(self) -> Optional[List[Position]]:
        """
        Implement A* pathfinding algorithm considering fuel costs and height differences.
        This is a simplified version - in a real implementation, you'd want to 
        consider actual fuel costs based on height differences.
        """
        if not self.current_position or not self.target_position:
            return None
        
        open_set = []
        closed_set: Set[Position] = set()
        
        start_node = PathNode(
            position=self.current_position,
            g_score=0,
            f_score=self.heuristic(self.current_position, self.target_position)
        )
        
        heapq.heappush(open_set, start_node)
        node_map = {self.current_position: start_node}
        
        while open_set:
            current = heapq.heappop(open_set)
            
            if current.position == self.target_position:
                # Reconstruct path
                return self.reconstruct_path(current)
            
            closed_set.add(current.position)
            
            # Check all neighbors
            for neighbor_pos in self.get_valid_neighbors(current.position):
                if neighbor_pos in closed_set:
                    continue
                
                move_cost = self.get_move_cost(current.position, neighbor_pos)
                tentative_g_score = current.g_score + move_cost
                
                if neighbor_pos not in node_map:
                    # New node
                    neighbor_node = PathNode(
                        position=neighbor_pos,
                        g_score=tentative_g_score,
                        f_score=tentative_g_score + self.heuristic(neighbor_pos, self.target_position),
                        parent=current
                    )
                    node_map[neighbor_pos] = neighbor_node
                    heapq.heappush(open_set, neighbor_node)
                    
                elif tentative_g_score < node_map[neighbor_pos].g_score:
                    # Better path found
                    neighbor_node = node_map[neighbor_pos]
                    neighbor_node.g_score = tentative_g_score
                    neighbor_node.f_score = tentative_g_score + self.heuristic(neighbor_pos, self.target_position)
                    neighbor_node.parent = current
        
        return None  # No path found
    
    def reconstruct_path(self, node: PathNode) -> List[Position]:
        """Reconstruct the path from the goal node"""
        path = []
        current = node
        
        while current:
            path.append(current.position)
            current = current.parent
        
        path.reverse()
        return path
    
    def get_valid_neighbors(self, position: Position) -> List[Position]:
        """Get valid neighboring positions"""
        neighbors = [
            Position(position.x + 1, position.y),  # EAST
            Position(position.x - 1, position.y),  # WEST
            Position(position.x, position.y + 1),  # SOUTH
            Position(position.x, position.y - 1),  # NORTH
        ]
        
        # Filter out positions outside map bounds
        if self.map_size:
            neighbors = [
                pos for pos in neighbors
                if 0 <= pos.x < self.map_size["width"] and 0 <= pos.y < self.map_size["height"]
            ]
        
        return neighbors
    
    def get_move_cost(self, from_pos: Position, to_pos: Position) -> float:
        """
        Calculate the cost to move between two positions.
        This considers height differences if known.
        """
        base_cost = 1.0
        
        # If we know the height information, factor it in
        if from_pos in self.known_map and to_pos in self.known_map:
            from_height = self.known_map[from_pos]["height"]
            to_height = self.known_map[to_pos]["height"]
            height_diff = to_height - from_height
            
            # Going uphill costs more fuel
            if height_diff > 0:
                base_cost += height_diff
        
        return base_cost
    
    def heuristic(self, from_pos: Position, to_pos: Position) -> float:
        """
        Heuristic function for A* algorithm.
        Uses Euclidean distance with slight preference for straight lines.
        """
        euclidean = from_pos.euclidean_distance_to(to_pos)
        manhattan = from_pos.distance_to(to_pos)
        
        # Slight preference for straight lines
        return euclidean + 0.001 * manhattan
    
    def get_direction_to(self, from_pos: Position, to_pos: Position) -> Optional[str]:
        """Get the direction string to move from one position to another"""
        dx = to_pos.x - from_pos.x
        dy = to_pos.y - from_pos.y
        
        if dx == 1 and dy == 0:
            return "EAST"
        elif dx == -1 and dy == 0:
            return "WEST"
        elif dx == 0 and dy == 1:
            return "SOUTH"
        elif dx == 0 and dy == -1:
            return "NORTH"
        else:
            return None
    
    def send_move_action(self, direction: str):
        """Send a move action to the API"""
        response = requests.post(
            ROBORUSH_URL + "/robot/action/move",
            json={"direction": direction},
            headers={"Authorization": "Bearer " + API_TOKEN}
        )
        
        if response.status_code == 200:
            logging.info(f"Successfully moved {direction}")
        else:
            try:
                error_json = response.json()
                logging.warning(f"Move failed: {error_json.get('status', 'Unknown')} - {error_json.get('message', 'No message')}")
            except:
                logging.warning(f"Move failed with status code: {response.status_code}")
    
    def send_fallback_move(self):
        """
        Fallback move strategy when pathfinding fails.
        Still better than pure greedy as it considers multiple factors.
        """
        if not self.current_position or not self.target_position:
            # If we don't know where we are or where to go, just wait
            self.send_wait_action()
            return
        
        # Simple but improved approach: consider distance and fuel
        possible_moves = [
            ("EAST", Position(self.current_position.x + 1, self.current_position.y)),
            ("WEST", Position(self.current_position.x - 1, self.current_position.y)),
            ("SOUTH", Position(self.current_position.x, self.current_position.y + 1)),
            ("NORTH", Position(self.current_position.x, self.current_position.y - 1))
        ]
        
        # Filter valid moves and sort by distance to target
        valid_moves = []
        for direction, pos in possible_moves:
            if (not self.map_size or 
                (0 <= pos.x < self.map_size["width"] and 0 <= pos.y < self.map_size["height"])):
                distance = pos.distance_to(self.target_position)
                cost = self.get_move_cost(self.current_position, pos)
                valid_moves.append((direction, distance, cost))
        
        if valid_moves:
            # Sort by distance (primary) and cost (secondary)
            valid_moves.sort(key=lambda x: (x[1], x[2]))
            best_direction = valid_moves[0][0]
            logging.info(f"Fallback move: {best_direction}")
            self.send_move_action(best_direction)
        else:
            logging.warning("No valid moves available, waiting")
            self.send_wait_action()
    
    def send_wait_action(self):
        """Send a wait action when no move is possible"""
        response = requests.post(
            ROBORUSH_URL + "/robot/action/wait",
            headers={"Authorization": "Bearer " + API_TOKEN}
        )
        
        if response.status_code == 200:
            logging.info("Waiting this turn")
        else:
            logging.warning("Failed to send wait action")

def main():
    """
    Main entry point for the Smart Pathfinding Robot.
    
    This example demonstrates:
    1. A* pathfinding algorithm implementation
    2. Considering height differences and fuel costs
    3. Proper fallback strategies
    4. Educational comments explaining the approach
    
    Key learning points:
    - Simple "go left/down" strategies are suboptimal
    - Height differences significantly affect fuel consumption
    - Proper pathfinding considers obstacles and costs
    - Fallback strategies are important for robustness
    """
    robot = SmartPathfindingRobot()
    robot.main()

if __name__ == '__main__':
    main()