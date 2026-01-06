package ma.project.translator.jakarta.hello;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("signup")
public class SignupResource {
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response signup(User user) {
        boolean success = UserService.registerUser(user);
        
        if (!success) {
            JsonObject error = Json.createObjectBuilder()
                .add("error", "Username already exists")
                .build();
            return Response.status(Response.Status.CONFLICT).entity(error).build();
        }

        JsonObject msg = Json.createObjectBuilder()
            .add("message", "User registered successfully")
            .build();
        return Response.status(Response.Status.CREATED).entity(msg).build();
    }
}