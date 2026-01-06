package ma.project.translator.jakarta.hello;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.Json;
import jakarta.json.JsonObject;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import java.security.Key;
import java.util.Optional;

@Path("auth")
public class LoginResource {

  // Use environment variable if available, otherwise fallback to default secret
  private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(
      Optional.ofNullable(System.getenv("JWT_SECRET"))
          .orElse("default-secret-key-12345678901234567890") // must be at least 32 chars
          .getBytes()
  );

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response login(Credentials creds) {
    // ✅ Use static UserService map
    User user = UserService.getUser(creds.getUsername());

    if (user == null || !user.getPassword().equals(creds.getPassword())) {
      JsonObject error = Json.createObjectBuilder()
          .add("error", "Invalid username or password")
          .build();
      return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
    }

    // ✅ Build JWT
    String jwt = Jwts.builder()
        .setSubject(user.getUsername())
        .claim("role", user.getRole()) // e.g. "user"
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 3600_000)) // 1 hour
        .signWith(SIGNING_KEY, SignatureAlgorithm.HS256)
        .compact();

    JsonObject tokenJson = Json.createObjectBuilder()
        .add("token", jwt)
        .build();

    return Response.ok(tokenJson).build();
  }

  // Inner class for login credentials
  public static class Credentials {
    private String username;
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
  }
}
