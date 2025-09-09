package org.example.resources;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.example.dto.crateDTOs.NewCrate;
import org.example.dto.crateDTOs.SingleCrate;
import org.example.model.Bottle;
import org.example.model.Crate;
import org.example.services.BottleService;
import org.example.services.CrateService;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/crates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CrateResource {

    private static final Logger logger = Logger.getLogger("CrateResource");

    @Inject CrateService crateService;
    @Inject BottleService bottleService;

    @Context SecurityContext securityContext;

    @GET
    public Response getAllCrates(@DefaultValue("1") @QueryParam("page") int page,
                                 @DefaultValue("2147483647") @QueryParam("perPage") int perPage) {
        if (!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied for non-employee users").build();
        }

        logger.info("GET /crates page=" + page + " perPage=" + perPage);

        if (page <= 0 || perPage <= 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid page or perPage values").build();
        }

        List<Crate> crates = crateService.getAll();
        if (crates.isEmpty()) return Response.status(Response.Status.NOT_FOUND).entity("No crates found").build();

        int start = (page - 1) * perPage;
        if (start >= crates.size()) return Response.status(Response.Status.BAD_REQUEST).entity("Page number out of range").build();

        int end = Math.min(start + perPage, crates.size());

        List<SingleCrate> dtoList = crates.subList(start, end).stream()
                .map(SingleCrate::of)
                .collect(Collectors.toList());

        return Response.ok(dtoList).build();
    }

    @GET @Path("/filter")
    public Response filterCrates(@QueryParam("minPrice") Double minPrice,
                                 @QueryParam("maxPrice") Double maxPrice,
                                 @QueryParam("name") String name) {
        if (!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied for non-employee users").build();
        }

        logger.info("GET /crates/filter min=" + minPrice + " max=" + maxPrice + " name=" + name);

        List<Crate> crates = crateService.filter(minPrice, maxPrice, name);
        if (crates.isEmpty()) return Response.status(Response.Status.NOT_FOUND).entity("No matching crates").build();

        List<SingleCrate> result = crates.stream().map(SingleCrate::of).collect(Collectors.toList());
        return Response.ok(result).build();
    }

    @GET @Path("id/{id}")
    public Response getCrateById(@PathParam("id") int id, @Context UriInfo uriInfo) {
        logger.info("GET /crates/id/" + id);
        if (!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied for non-employee users").build();
        }

        Crate crate = crateService.getById(id);
        if (crate == null) return Response.status(Response.Status.NOT_FOUND).entity("Crate with ID " + id + " not found").build();
        return buildResponseWithLinks(id, uriInfo);
    }

    @GET @Path("name/{name}")
    public Response getCrateByName(@PathParam("name") String name, @Context UriInfo uriInfo) {
        logger.info("GET /crates/name/" + name);
        if (!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied for non-employee users").build();
        }

        Crate crate = crateService.getByName(name);
        if (crate == null) return Response.status(Response.Status.NOT_FOUND).entity("Crate with name " + name + " not found").build();
        return buildResponseWithLinks(crate.getId(), uriInfo);
    }

    @POST
    public Response addCrate(@Valid @NotNull NewCrate newCrate) {
        if (!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied for non-employee users").build();
        }

        logger.info("POST /crates");
        Bottle bottle = bottleService.getById(newCrate.getBottleId());
        if (bottle == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Bottle with ID " + newCrate.getBottleId() + " not found").build();
        }

        int nextId = crateService.getSize() + 1; // or let DAO assign by sending 0
        Crate crate = new Crate(nextId, bottle, newCrate.getNoOfBottles(), newCrate.getPrice(), newCrate.getInStock());

        Crate created = crateService.add(crate);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    private Response buildResponseWithLinks(int id, UriInfo uriInfo) {
        Crate crate = crateService.getById(id);

        String self = UriBuilder.fromUri(uriInfo.getBaseUri())
                .path(CrateResource.class).path(CrateResource.class, "getCrateById").build(id).toString();
        String prev = UriBuilder.fromUri(uriInfo.getBaseUri())
                .path(CrateResource.class).path(CrateResource.class, "getCrateById").build(id - 1).toString();
        String next = UriBuilder.fromUri(uriInfo.getBaseUri())
                .path(CrateResource.class).path(CrateResource.class, "getCrateById").build(id + 1).toString();

        Optional<Crate> prevCrate = Optional.ofNullable(crateService.getById(id - 1));
        Optional<Crate> nextCrate = Optional.ofNullable(crateService.getById(id + 1));

        Response.ResponseBuilder rb = Response.ok(SingleCrate.of(crate));
        if (prevCrate.isPresent() && nextCrate.isPresent()) {
            rb.header("Link", "<" + self + ">; rel=\"self\", <" + prev + ">; rel=\"prev\", <" + next + ">; rel=\"next\"");
        } else if (prevCrate.isPresent()) {
            rb.header("Link", "<" + self + ">; rel=\"self\", <" + prev + ">; rel=\"prev\"");
        } else if (nextCrate.isPresent()) {
            rb.header("Link", "<" + self + ">; rel=\"self\", <" + next + ">; rel=\"next\"");
        } else {
            rb.header("Link", "<" + self + ">; rel=\"self\"");
        }
        return rb.build();
    }
}
