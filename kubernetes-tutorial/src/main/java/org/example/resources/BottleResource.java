package org.example.resources;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.example.dto.bottleDTOs.NewBottle;
import org.example.dto.bottleDTOs.SingleBottle;
import org.example.model.Bottle;
import org.example.model.exceptions.CustomException;
import org.example.services.BottleService;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/bottles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BottleResource {
    private static final Logger logger = Logger.getLogger("BottleResource");

    @Inject
    BottleService bottleService;

    @Context
    SecurityContext securityContext;

    @GET
    public Response getBottles(@DefaultValue("1") @QueryParam("page") int page,
                               @DefaultValue("2147483647") @QueryParam("perPage") int perPage) {
        if (!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied for non-employee users").build();
        }

        logger.info("GET /bottles page=" + page + " perPage=" + perPage);

        if (page <= 0 || perPage <= 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid page or perPage values").build();
        }

        List<Bottle> bottles = bottleService.getAll();
        if (bottles.isEmpty()) return Response.status(Response.Status.NOT_FOUND).entity("No bottles found").build();

        int start = (page - 1) * perPage;
        if (start >= bottles.size()) return Response.status(Response.Status.BAD_REQUEST).entity("Page number out of range").build();

        int end = Math.min(start + perPage, bottles.size());

        List<SingleBottle> dtoList = bottles.subList(start, end).stream()
                .map(SingleBottle::of)
                .collect(Collectors.toList());

        return Response.ok(dtoList).build();
    }

    @GET
    @Path("/filter")
    public Response filterBottles(
            @PositiveOrZero(message = "Lower bound must not be negative") @QueryParam("minPrice") Double minPrice,
            @PositiveOrZero(message = "Lower bound must not be negative") @QueryParam("maxPrice") Double maxPrice,
            @QueryParam("name") String name) {

        logger.info("GET /bottles/filter min=" + minPrice + " max=" + maxPrice + " name=" + name);

        List<SingleBottle> filtered = bottleService.filter(minPrice, maxPrice, name).stream()
                .map(SingleBottle::of).toList();

        if (filtered.isEmpty()) return Response.status(Response.Status.NOT_FOUND).entity("No matching bottles").build();
        return Response.ok(filtered).build();
    }

    @GET @Path("/alcoholic")
    public Response getAlcoholicBottles() {
        logger.info("GET /bottles/alcoholic");
        var list = bottleService.getAlcoholicBottles().stream().map(SingleBottle::of).toList();
        if (list.isEmpty()) return Response.status(Response.Status.NOT_FOUND).entity("No alcoholic bottles found").build();
        return Response.ok(list).build();
    }

    @GET @Path("/non-alcoholic")
    public Response getNonAlcoholicBottles() {
        logger.info("GET /bottles/non-alcoholic");
        var list = bottleService.getNonAlcoholicBottles().stream().map(SingleBottle::of).toList();
        if (list.isEmpty()) return Response.status(Response.Status.NOT_FOUND).entity("No non-alcoholic bottles found").build();
        return Response.ok(list).build();
    }

    @GET @Path("/in-stock")
    public Response getBottlesInStock() {
        if (!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied for non-employee users").build();
        }
        logger.info("GET /bottles/in-stock");
        var list = bottleService.getBottlesInStock().stream().map(SingleBottle::of).toList();
        if (list.isEmpty()) return Response.status(Response.Status.NOT_FOUND).entity("No bottles in stock found").build();
        return Response.ok(list).build();
    }

    @GET @Path("/id/{id}")
    public Response getBottleById(@PathParam("id") int id, @Context UriInfo uriInfo) throws CustomException {
        if (!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN).entity("Access denied for non-employee users").build();
        }
        logger.info("GET /bottles/" + id);
        Bottle bottle = bottleService.getById(id);
        if (bottle == null) return Response.status(Response.Status.NOT_FOUND).entity("Bottle with ID " + id + " not found").build();
        return buildResponseWithLinks(id, uriInfo);
    }

    @GET @Path("/name/{name}")
    public Response getBottleByName(@PathParam("name") String name, @Context UriInfo uriInfo) throws CustomException {
        if (!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN).entity("Access denied for non-employee users").build();
        }
        logger.info("GET /bottles/" + name);
        Bottle bottle = bottleService.getByName(name);
        if (bottle == null) return Response.status(Response.Status.NOT_FOUND).entity("Bottle with name " + name + " not found").build();
        return buildResponseWithLinks(bottle.getId(), uriInfo);
    }

    @POST
    public Response addBottle(@Valid @NotNull NewBottle newBottle){
        if (!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN).entity("Access denied for non-employee users").build();
        }
        logger.info("POST /bottles name=" + newBottle.getName());

        int nextId = bottleService.getSize() + 1; // or rely on DAO.nextId() by passing id=0
        Bottle bottle = new Bottle(nextId, newBottle.getName(), newBottle.getVolume(),
                newBottle.getIsAlcoholic(), newBottle.getVolumePercent(), newBottle.getPrice(),
                newBottle.getSupplier(), newBottle.getInStock());

        Bottle created = bottleService.addBottle(bottle);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT @Path("/{id}")
    public Response updateBottle(@PathParam("id") int id, @Valid @NotNull NewBottle newBottle) throws CustomException {
        if (!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN).entity("Access denied for non-employee users").build();
        }
        logger.info("PUT /bottles/" + id + " name=" + newBottle.getName());

        Bottle existing = bottleService.getById(id);
        if (existing == null) return Response.status(Response.Status.NOT_FOUND).entity("Bottle with id " + id + " not found").build();

        Bottle updated = new Bottle(id, newBottle.getName(), newBottle.getVolume(),
                newBottle.getIsAlcoholic(), newBottle.getVolumePercent(), newBottle.getPrice(),
                newBottle.getSupplier(), newBottle.getInStock());

        bottleService.updateById(id, updated);
        return Response.ok(SingleBottle.of(updated)).build();
    }

    @DELETE @Path("/{id}")
    public Response deleteBottle(@PathParam("id") int id) throws CustomException {
        if (!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN).entity("Access denied for non-employee users").build();
        }
        logger.info("DELETE /bottles/" + id);
        Bottle existing = bottleService.getById(id);
        if (existing == null) return Response.status(Response.Status.NOT_FOUND).entity("Bottle with id " + id + " not found").build();
        bottleService.deleteById(id);
        return Response.noContent().build();
    }

    // ---------- Utility ----------
    private Response buildResponseWithLinks(int id, UriInfo uriInfo) throws CustomException {
        Bottle bottle = bottleService.getById(id);

        String self = UriBuilder.fromUri(uriInfo.getBaseUri())
                .path(BottleResource.class).path(BottleResource.class, "getBottleById").build(id).toString();
        String prev = UriBuilder.fromUri(uriInfo.getBaseUri())
                .path(BottleResource.class).path(BottleResource.class, "getBottleById").build(id - 1).toString();
        String next = UriBuilder.fromUri(uriInfo.getBaseUri())
                .path(BottleResource.class).path(BottleResource.class, "getBottleById").build(id + 1).toString();

        Optional<Bottle> prevBottle = bottleService.getById(id - 1) != null ? Optional.of(bottleService.getById(id - 1)) : Optional.empty();
        Optional<Bottle> nextBottle = bottleService.getById(id + 1) != null ? Optional.of(bottleService.getById(id + 1)) : Optional.empty();

        Response.ResponseBuilder rb = Response.ok(SingleBottle.of(bottle));
        if (prevBottle.isPresent() && nextBottle.isPresent()) {
            rb.header("Link", "<" + self + ">; rel=\"self\", <" + prev + ">; rel=\"prev\", <" + next + ">; rel=\"next\"");
        } else if (prevBottle.isPresent()) {
            rb.header("Link", "<" + self + ">; rel=\"self\", <" + prev + ">; rel=\"prev\"");
        } else if (nextBottle.isPresent()) {
            rb.header("Link", "<" + self + ">; rel=\"self\", <" + next + ">; rel=\"next\"");
        } else {
            rb.header("Link", "<" + self + ">; rel=\"self\"");
        }
        return rb.build();
    }
}