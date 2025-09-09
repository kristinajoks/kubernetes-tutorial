package org.example.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public class HealthResource {
    // Readiness: return 200 when the app is ready to serve
    @GET
    @Path("health")
    @Produces(MediaType.TEXT_PLAIN)
    public Response health() {
        // Keep it simple for now; later you can check DBs, downstreams, etc.
        return Response.ok("ok").build();
    }

    // Liveness: return 200 if the JVM/process is healthy
    @GET
    @Path("live")
    @Produces(MediaType.TEXT_PLAIN)
    public Response live() {
        return Response.ok("ok").build();
    }
}
