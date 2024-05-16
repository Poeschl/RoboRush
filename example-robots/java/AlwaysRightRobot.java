import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * This sample robot is a very, very, very simple example how to communicate with the RoboRush api with Java (JDK > 11).
 */
public class AlwaysRightRobot {

  /**
   * This is the url to the RoboRush host.
   */
  private static final String ROBORUSH_URL = "http://localhost:8080/api";

  /**
   * Replace this token value with your own api token.
   * The token can be retrieved in the profile settings on the web ui.
   */
  private static final String API_TOKEN = "";

  private static final HttpClient client = HttpClient.newHttpClient();

  // Ignore the exceptions in this example. Please catch them in your real code
  private void run() throws URISyntaxException, IOException, InterruptedException {
    System.out.println("Always Right Robot booting up...");

    // Remember if the robot is attending the current game
    boolean isAttending = false;
    // Now lets move our robot always to the right (east) if a new instruction is required
    while (true) {

      // Let's get the general available information about the current game.
      final String currentGameInfo = getGameInfos();

      // If the game is open for new players
      if (!isAttending && currentGameInfo.contains("WAIT_FOR_PLAYERS")) {
        // The robot register itself
        isAttending = sendRobotAttending();

        // Check if we need to send a new action
      } else if (isAttending && currentGameInfo.contains("WAIT_FOR_ACTION")) {
        // If needed, send the move command
        sendRobotEastMoveAction();

      } else if (currentGameInfo.contains("ENDED")) {
        isAttending = false;
      }
      // Make sure to avoid a CPU hell!
      Thread.sleep(250);
    }
  }

  private String getGameInfos() throws URISyntaxException, IOException, InterruptedException {
    // Create a request to the game endpoint. For this no authentication is needed.
    final HttpRequest request = HttpRequest.newBuilder(new URI(ROBORUSH_URL + "/game"))
        .GET()
        .build();

    // To simplify the example the json result is parsed as raw string.
    // For a real robot a json parsing framework like Jackson is recommended to create a Java object.
    final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    // In case of this example we only print the game infos
    System.out.printf("Received game info: %s\n", response.body());
    return response.body();
  }

  private boolean sendRobotAttending() throws URISyntaxException, IOException, InterruptedException {
    // Create a request to mark your robot as participating in the current game.
    // For this endpoint we need to be authenticated with a token.
    final HttpRequest request = HttpRequest.newBuilder(new URI(ROBORUSH_URL + "/robot/attend"))
        .header("Authorization", "Bearer " + API_TOKEN)
        .POST(HttpRequest.BodyPublishers.ofString(""))
        .build();

    // The status of the attending call indicated the participation
    final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    // In case of this example we only print the attending info
    System.out.printf("Attend the current game: %s\n", response.statusCode() == 200);
    return response.statusCode() == 200;
  }

  private void sendRobotEastMoveAction() throws URISyntaxException, IOException, InterruptedException {
    // Create a request to tell the robot to move to east in the next round
    // For this endpoint we need to be authenticated with a token.
    final HttpRequest request = HttpRequest.newBuilder(new URI(ROBORUSH_URL + "/robot/action/move"))
        .header("Authorization", "Bearer " + API_TOKEN)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString("{\"direction\": \"EAST\"}"))
        .build();

    // If an error happens, it will be returned in the response body
    final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() == 200) {
      System.out.println("Move robot to EAST on next round");
    } else {
      System.out.printf("Error: %s\n", response.body());
    }
  }


  public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
    new AlwaysRightRobot().run();
  }
}
