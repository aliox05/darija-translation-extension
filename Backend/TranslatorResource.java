package ma.project.translator.jakarta.hello;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import jakarta.annotation.security.RolesAllowed;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonArray;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

class LLMClient {

  private static final String API_KEY = "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";

  public static String translate(String text, String to) {
    try {
      HttpClient client = HttpClient.newHttpClient();

      String prompt = "Detect the language of the following text and translate it to " + to +
          ". Return only the translation, without extra explanation:\n" + text;

      String body = """
          {
            "model": "openai/gpt-oss-20b:free",
            "messages": [
              { "role": "user", "content": "%s" }
            ]
          }
          """.formatted(escapeJson(prompt));

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + API_KEY)
          .POST(HttpRequest.BodyPublishers.ofString(body))
          .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      System.out.println("OpenRouter status: " + response.statusCode());
      System.out.println("OpenRouter response: " + response.body());

      if (response.statusCode() != 200) {
        return "Error: OpenRouter returned status " + response.statusCode();
      }

      return parseOpenRouterResponse(response.body());

    } catch (Exception e) {
      System.out.println("Translation error: " + e.getMessage());
      return "Error: " + e.getMessage();
    }
  }

  private static String escapeJson(String s) {
    return s.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }

  private static String parseOpenRouterResponse(String responseJson) {
    try {
      JsonObject json = Json.createReader(new java.io.StringReader(responseJson)).readObject();

      // If error field exists, return it
      if (json.containsKey("error")) {
        return "Error: " + json.getJsonObject("error").getString("message", "Unknown error");
      }

      if (!json.containsKey("choices")) {
        return "Error: Unexpected response format";
      }

      JsonArray choices = json.getJsonArray("choices");
      if (choices.isEmpty()) {
        return "Error: No choices returned";
      }

      JsonObject choice = choices.getJsonObject(0);
      JsonObject message = choice.getJsonObject("message");
      return message.getString("content", "").trim();

    } catch (Exception e) {
      System.out.println("Parsing error: " + e.getMessage());
      return "Error: Failed to parse response";
    }
  }
}

@Path("translate")
public class TranslatorResource {

  @GET
  @RolesAllowed("user")
  @Produces(MediaType.APPLICATION_JSON)
  public Response translateGet(@QueryParam("text") String text,
      @QueryParam("to") @DefaultValue("darija") String toLang) {

    if (text == null || text.isBlank()) {
      JsonObject error = Json.createObjectBuilder()
          .add("error", "Text cannot be empty")
          .build();
      return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }

    return buildResponse(text, toLang);
  }

  @POST
  @RolesAllowed("user")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response translatePost(JsonObject requestJson,
      @QueryParam("to") @DefaultValue("darija") String toLang) {

    String text = requestJson.getString("text", "").trim();

    if (text.isEmpty()) {
      JsonObject error = Json.createObjectBuilder()
          .add("error", "Text cannot be empty")
          .build();
      return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }

    return buildResponse(text, toLang);
  }

  private Response buildResponse(String text, String toLang) {
    String result = LLMClient.translate(text, toLang);

    if (result == null || result.startsWith("Error")) {
      JsonObject error = Json.createObjectBuilder()
          .add("error", result == null ? "Translation failed" : result)
          .build();
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }

    JsonObject json = Json.createObjectBuilder()
        .add("translation", result)
        .build();

    return Response.ok(json).build();
  }
}
