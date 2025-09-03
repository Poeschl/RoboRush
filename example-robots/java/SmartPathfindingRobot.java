import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Smart Pathfinding Robot - A demonstration of advanced pathfinding techniques
 * 
 * This robot shows how to implement proper pathfinding algorithms that consider:
 * 1. A* algorithm for optimal path finding
 * 2. Height differences and fuel costs
 * 3. Obstacle avoidance
 * 4. Fallback strategies
 * 
 * This is an educational example showing why simple greedy approaches 
 * (like "always go left/down") are suboptimal for RoboRush.
 */
public class SmartPathfindingRobot {
    
    private static final String ROBORUSH_URL = "http://localhost:8080/api";
    // Replace this token value with your own api token.
    private static final String API_TOKEN = "";
    
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    
    private boolean isAttending = false;
    private final Map<Position, TileInfo> knownMap = new ConcurrentHashMap<>();
    private Position currentPosition;
    private Position targetPosition;
    private MapSize mapSize;
    private int fuel = 0;
    private int maxFuel = 0;
    
    /**
     * Position class to represent coordinates
     */
    public static class Position {
        public final int x, y;
        
        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Position)) return false;
            Position pos = (Position) obj;
            return x == pos.x && y == pos.y;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
        
        @Override
        public String toString() {
            return String.format("Position(%d, %d)", x, y);
        }
        
        public int manhattanDistanceTo(Position other) {
            return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
        }
        
        public double euclideanDistanceTo(Position other) {
            return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
        }
        
        public List<Position> getNeighbors() {
            return Arrays.asList(
                new Position(x + 1, y), // EAST
                new Position(x - 1, y), // WEST
                new Position(x, y + 1), // SOUTH
                new Position(x, y - 1)  // NORTH
            );
        }
    }
    
    /**
     * Tile information from the map
     */
    public static class TileInfo {
        public final int height;
        public final String type;
        
        public TileInfo(int height, String type) {
            this.height = height;
            this.type = type;
        }
    }
    
    /**
     * Map size information
     */
    public static class MapSize {
        public final int width, height;
        
        public MapSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
    
    /**
     * A* pathfinding node
     */
    private static class PathNode implements Comparable<PathNode> {
        public final Position position;
        public final double gScore; // Cost from start
        public final double fScore; // Estimated total cost
        public final PathNode parent;
        
        public PathNode(Position position, double gScore, double fScore, PathNode parent) {
            this.position = position;
            this.gScore = gScore;
            this.fScore = fScore;
            this.parent = parent;
        }
        
        @Override
        public int compareTo(PathNode other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }
    
    /**
     * Main robot loop
     */
    public void run() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("Smart Pathfinding Robot booting up...");
        System.out.println("This robot demonstrates advanced pathfinding techniques");
        System.out.println("and shows why simple greedy approaches are suboptimal.");
        
        while (true) {
            try {
                JsonNode gameInfo = getGameInfo();
                String gameState = gameInfo.get("currentState").asText();
                
                switch (gameState) {
                    case "WAIT_FOR_PLAYERS":
                        if (!isAttending) {
                            isAttending = sendRobotAttending();
                        }
                        break;
                        
                    case "WAIT_FOR_ACTION":
                        if (isAttending) {
                            updateRobotState();
                            makeSmartMove();
                        }
                        break;
                        
                    case "ENDED":
                        isAttending = false;
                        System.out.println("Game ended. Resetting for next game.");
                        break;
                }
                
                Thread.sleep(250);
                
            } catch (Exception e) {
                System.err.println("Error in main loop: " + e.getMessage());
                Thread.sleep(1000);
            }
        }
    }
    
    /**
     * Get general game information
     */
    private JsonNode getGameInfo() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(new URI(ROBORUSH_URL + "/game"))
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode gameInfo = mapper.readTree(response.body());
        
        // Update map size and target if available
        if (gameInfo.has("mapSize")) {
            JsonNode mapSizeNode = gameInfo.get("mapSize");
            this.mapSize = new MapSize(
                mapSizeNode.get("width").asInt(),
                mapSizeNode.get("height").asInt()
            );
        }
        
        if (gameInfo.has("targetPosition") && !gameInfo.get("targetPosition").isNull()) {
            JsonNode targetNode = gameInfo.get("targetPosition");
            this.targetPosition = new Position(
                targetNode.get("x").asInt(),
                targetNode.get("y").asInt()
            );
        }
        
        return gameInfo;
    }
    
    /**
     * Register robot for the current game
     */
    private boolean sendRobotAttending() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(new URI(ROBORUSH_URL + "/robot/attend"))
                .header("Authorization", "Bearer " + API_TOKEN)
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        boolean success = response.statusCode() == 200;
        System.out.println("Attend the current game: " + success);
        return success;
    }
    
    /**
     * Update robot's current state and known map
     */
    private void updateRobotState() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(new URI(ROBORUSH_URL + "/robot"))
                .header("Authorization", "Bearer " + API_TOKEN)
                .GET()
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonNode robotData = mapper.readTree(response.body());
            
            // Update position and fuel
            JsonNode posNode = robotData.get("position");
            this.currentPosition = new Position(posNode.get("x").asInt(), posNode.get("y").asInt());
            this.fuel = robotData.get("fuel").asInt();
            this.maxFuel = robotData.get("maxFuel").asInt();
            
            // Update known map with scan results
            if (robotData.has("lastResult") && !robotData.get("lastResult").isNull()) {
                updateKnownMap(robotData.get("lastResult"));
            }
        }
    }
    
    /**
     * Update the known map with scan results
     */
    private void updateKnownMap(JsonNode scanResult) {
        if (scanResult.isArray()) {
            for (JsonNode tile : scanResult) {
                JsonNode posNode = tile.get("position");
                Position pos = new Position(posNode.get("x").asInt(), posNode.get("y").asInt());
                TileInfo tileInfo = new TileInfo(
                    tile.get("height").asInt(),
                    tile.get("type").asText()
                );
                knownMap.put(pos, tileInfo);
            }
        }
    }
    
    /**
     * Make an intelligent move using pathfinding
     */
    private void makeSmartMove() throws URISyntaxException, IOException, InterruptedException {
        if (currentPosition == null || targetPosition == null) {
            System.out.println("Missing position information for pathfinding");
            sendFallbackMove();
            return;
        }
        
        // Use A* pathfinding to find optimal route
        List<Position> path = findOptimalPath();
        
        if (path != null && path.size() > 1) {
            Position nextPosition = path.get(1); // First position is current position
            String direction = getDirectionTo(currentPosition, nextPosition);
            
            if (direction != null) {
                System.out.println("Smart pathfinding: Moving " + direction + " towards target");
                sendMoveAction(direction);
            } else {
                System.out.println("Could not determine direction from pathfinding");
                sendFallbackMove();
            }
        } else {
            System.out.println("No path found with A* algorithm, using fallback");
            sendFallbackMove();
        }
    }
    
    /**
     * Implement A* pathfinding algorithm considering fuel costs and height differences
     */
    private List<Position> findOptimalPath() {
        if (currentPosition == null || targetPosition == null) {
            return null;
        }
        
        PriorityQueue<PathNode> openSet = new PriorityQueue<>();
        Set<Position> closedSet = new HashSet<>();
        Map<Position, PathNode> nodeMap = new HashMap<>();
        
        PathNode startNode = new PathNode(
            currentPosition,
            0,
            heuristic(currentPosition, targetPosition),
            null
        );
        
        openSet.add(startNode);
        nodeMap.put(currentPosition, startNode);
        
        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();
            
            if (current.position.equals(targetPosition)) {
                // Reconstruct path
                return reconstructPath(current);
            }
            
            closedSet.add(current.position);
            
            // Check all neighbors
            for (Position neighborPos : getValidNeighbors(current.position)) {
                if (closedSet.contains(neighborPos)) {
                    continue;
                }
                
                double moveCost = getMoveCost(current.position, neighborPos);
                double tentativeGScore = current.gScore + moveCost;
                
                if (!nodeMap.containsKey(neighborPos)) {
                    // New node
                    PathNode neighborNode = new PathNode(
                        neighborPos,
                        tentativeGScore,
                        tentativeGScore + heuristic(neighborPos, targetPosition),
                        current
                    );
                    nodeMap.put(neighborPos, neighborNode);
                    openSet.add(neighborNode);
                    
                } else if (tentativeGScore < nodeMap.get(neighborPos).gScore) {
                    // Better path found
                    PathNode existingNode = nodeMap.get(neighborPos);
                    openSet.remove(existingNode); // Remove old node
                    
                    PathNode improvedNode = new PathNode(
                        neighborPos,
                        tentativeGScore,
                        tentativeGScore + heuristic(neighborPos, targetPosition),
                        current
                    );
                    nodeMap.put(neighborPos, improvedNode);
                    openSet.add(improvedNode);
                }
            }
        }
        
        return null; // No path found
    }
    
    /**
     * Reconstruct the path from the goal node
     */
    private List<Position> reconstructPath(PathNode node) {
        List<Position> path = new ArrayList<>();
        PathNode current = node;
        
        while (current != null) {
            path.add(current.position);
            current = current.parent;
        }
        
        Collections.reverse(path);
        return path;
    }
    
    /**
     * Get valid neighboring positions
     */
    private List<Position> getValidNeighbors(Position position) {
        List<Position> neighbors = new ArrayList<>();
        
        for (Position neighbor : position.getNeighbors()) {
            // Filter out positions outside map bounds
            if (mapSize != null && 
                (neighbor.x < 0 || neighbor.x >= mapSize.width || 
                 neighbor.y < 0 || neighbor.y >= mapSize.height)) {
                continue;
            }
            neighbors.add(neighbor);
        }
        
        return neighbors;
    }
    
    /**
     * Calculate the cost to move between two positions considering height differences
     */
    private double getMoveCost(Position from, Position to) {
        double baseCost = 1.0;
        
        // If we know the height information, factor it in
        TileInfo fromTile = knownMap.get(from);
        TileInfo toTile = knownMap.get(to);
        
        if (fromTile != null && toTile != null) {
            int heightDiff = toTile.height - fromTile.height;
            // Going uphill costs more fuel
            if (heightDiff > 0) {
                baseCost += heightDiff;
            }
        }
        
        return baseCost;
    }
    
    /**
     * Heuristic function for A* algorithm
     */
    private double heuristic(Position from, Position to) {
        // Euclidean distance with slight preference for straight lines
        double euclidean = from.euclideanDistanceTo(to);
        double manhattan = from.manhattanDistanceTo(to);
        
        return euclidean + 0.001 * manhattan;
    }
    
    /**
     * Get the direction string to move from one position to another
     */
    private String getDirectionTo(Position from, Position to) {
        int dx = to.x - from.x;
        int dy = to.y - from.y;
        
        if (dx == 1 && dy == 0) return "EAST";
        if (dx == -1 && dy == 0) return "WEST";
        if (dx == 0 && dy == 1) return "SOUTH";
        if (dx == 0 && dy == -1) return "NORTH";
        
        return null;
    }
    
    /**
     * Send a move action to the API
     */
    private void sendMoveAction(String direction) throws URISyntaxException, IOException, InterruptedException {
        String json = String.format("{\"direction\": \"%s\"}", direction);
        
        HttpRequest request = HttpRequest.newBuilder(new URI(ROBORUSH_URL + "/robot/action/move"))
                .header("Authorization", "Bearer " + API_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            System.out.println("Successfully moved " + direction);
        } else {
            try {
                JsonNode error = mapper.readTree(response.body());
                System.out.println("Move failed: " + error.get("status") + " - " + error.get("message"));
            } catch (Exception e) {
                System.out.println("Move failed with status code: " + response.statusCode());
            }
        }
    }
    
    /**
     * Fallback move strategy when pathfinding fails
     */
    private void sendFallbackMove() throws URISyntaxException, IOException, InterruptedException {
        if (currentPosition == null || targetPosition == null) {
            sendWaitAction();
            return;
        }
        
        // Improved fallback: consider distance and cost
        List<String> directions = Arrays.asList("EAST", "WEST", "SOUTH", "NORTH");
        List<Position> positions = Arrays.asList(
            new Position(currentPosition.x + 1, currentPosition.y),
            new Position(currentPosition.x - 1, currentPosition.y),
            new Position(currentPosition.x, currentPosition.y + 1),
            new Position(currentPosition.x, currentPosition.y - 1)
        );
        
        String bestDirection = null;
        int bestDistance = Integer.MAX_VALUE;
        double bestCost = Double.MAX_VALUE;
        
        for (int i = 0; i < directions.size(); i++) {
            Position pos = positions.get(i);
            
            // Check if position is valid
            if (mapSize != null && 
                (pos.x < 0 || pos.x >= mapSize.width || pos.y < 0 || pos.y >= mapSize.height)) {
                continue;
            }
            
            int distance = pos.manhattanDistanceTo(targetPosition);
            double cost = getMoveCost(currentPosition, pos);
            
            // Choose based on distance first, then cost
            if (distance < bestDistance || (distance == bestDistance && cost < bestCost)) {
                bestDirection = directions.get(i);
                bestDistance = distance;
                bestCost = cost;
            }
        }
        
        if (bestDirection != null) {
            System.out.println("Fallback move: " + bestDirection);
            sendMoveAction(bestDirection);
        } else {
            System.out.println("No valid moves available, waiting");
            sendWaitAction();
        }
    }
    
    /**
     * Send a wait action when no move is possible
     */
    private void sendWaitAction() throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(new URI(ROBORUSH_URL + "/robot/action/wait"))
                .header("Authorization", "Bearer " + API_TOKEN)
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            System.out.println("Waiting this turn");
        } else {
            System.out.println("Failed to send wait action");
        }
    }
    
    /**
     * Main entry point for the Smart Pathfinding Robot.
     * 
     * This example demonstrates:
     * 1. A* pathfinding algorithm implementation
     * 2. Considering height differences and fuel costs  
     * 3. Proper fallback strategies
     * 4. Educational comments explaining the approach
     * 
     * Key learning points:
     * - Simple "go left/down" strategies are suboptimal
     * - Height differences significantly affect fuel consumption
     * - Proper pathfinding considers obstacles and costs
     * - Fallback strategies are important for robustness
     */
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        new SmartPathfindingRobot().run();
    }
}