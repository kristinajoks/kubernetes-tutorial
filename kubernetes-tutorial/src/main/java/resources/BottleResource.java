package resources;
import dto.bottleDTOs.NewBottle;
import dto.bottleDTOs.SingleBottle;
import model.Bottle;
import model.exceptions.CustomException;
import services.BottleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/bottles")
public class BottleResource{
    private static final Logger logger = Logger.getLogger("BottleResource");
    private final BottleService bottleService = BottleService.instance;

    @Context
    SecurityContext securityContext;

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getBottles(@DefaultValue("1") @QueryParam("page") int page,
                               @DefaultValue("2147483647") @QueryParam(
                                       "perPage") int perPage)
    {
        if(!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied for non-employee users")
                    .build();
        }

        logger.info("GET /bottles called with page: " + page + " and perPage: " + perPage);

        if (page <= 0 || perPage <= 0) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity("Invalid page or perPage values")
                .build();
        }

        List<Bottle> bottles = bottleService.getAll();

        if (bottles.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No bottles found")
                    .build();
        }

        int start = (page - 1) * perPage;

        if (start >= bottles.size()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Page number out of range")
                    .build();
        }

        int end = Math.min(start + perPage, bottles.size());

        List<SingleBottle> dtoList = bottles.subList(start, end).stream()
                .map(SingleBottle::of)
                .collect(Collectors.toList());

        return Response.status(Response.Status.OK).entity(dtoList).build();
    }

    @GET
    @Path("/filter")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response filterBottles(
            @PositiveOrZero(message = "Lower bound must not be negative") @QueryParam("minPrice") Double minPrice,
            @PositiveOrZero(message = "Lower bound must not be negative") @QueryParam("maxPrice") Double maxPrice,
            @QueryParam("name") String name) {

        logger.info("GET /bottles/filter called with minPrice: " + minPrice + ", maxPrice: " + maxPrice + ", name: " + name);

        List<SingleBottle> filteredBottles = bottleService.getAll().stream()
                .filter(b -> (minPrice == null || b.getPrice() >= minPrice) &&
                        (maxPrice == null || b.getPrice() <= maxPrice) &&
                        (name == null || b.getName().toLowerCase().contains(name.toLowerCase())))
                .map(SingleBottle::of) // Convert to DTO
                .toList();

        if (filteredBottles.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("No matching bottles").build();
        }

        return Response.status(Response.Status.OK).entity(filteredBottles).build();
    }


    @GET
    @Path("/alcoholic")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getAlcoholicBottles() {
        logger.info("GET /bottles/alcoholic called");
        try {
            List<SingleBottle> alcoholicBottles = bottleService.getAlcoholicBottles().stream()
                    .map(SingleBottle::of)
                    .toList();

            if (alcoholicBottles.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No alcoholic bottles found")
                        .build();
            }

            return Response.status(Response.Status.OK).entity(alcoholicBottles).build();
        } catch (Exception e) {
            logger.severe("Error in getAlcoholicBottles: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error").build();
        }
    }

    @GET
    @Path("/non-alcoholic")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getNonAlcoholicBottles() {
        logger.info("GET /bottles/non-alcoholic called");

        try {
            List<SingleBottle> nonAlcoholicBottles = bottleService.getNonAlcoholicBottles().stream()
                    .map(SingleBottle::of)
                    .toList();
            if (nonAlcoholicBottles.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No non-alcoholic bottles found")
                        .build();
            }
            return Response.status(Response.Status.OK).entity(nonAlcoholicBottles).build();
        } catch (Exception e) {
            logger.severe("Error in getNonAlcoholicBottles: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error").build();
        }
    }

    @GET
    @Path("/in-stock")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getBottlesInStock() {
        logger.info("GET /bottles/in-stock called");

        try {
            List<SingleBottle> bottlesInStock = bottleService.getBottlesInStock().stream()
                    .map(SingleBottle::of)
                    .toList();
            if (bottlesInStock.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No bottles in stock found")
                        .build();
            }
            return Response.status(Response.Status.OK).entity(bottlesInStock).build();
        } catch (Exception e) {
            logger.severe("Error in getBottlesInStock: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error").build();
        }
    }

    @GET
    @Path("/id/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getBottleById(@PathParam("id") int id, @Context UriInfo uriInfo) throws CustomException {
        if(!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied for non-employee users")
                    .build();
        }

        logger.info("GET /bottles/" + id + " called");

        Bottle bottle = bottleService.getById(id);
        if (bottle == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Bottle with ID " + id + " not found")
                    .build();
        }
        return Response.status(Response.Status.OK).entity(buildResponseWithLinks(id, uriInfo).getEntity()).build();
    }

    @GET
    @Path("/name/{name}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getBottleByName(@PathParam("name") String name, @Context UriInfo uriInfo) throws CustomException {
        if(!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied for non-employee users")
                    .build();
        }

        logger.info("GET /bottles/" + name + " called");

        Bottle bottle = bottleService.getByName(name);
        if (bottle == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Bottle with name " + name + " not found")
                    .build();
        }
        return Response.status(Response.Status.OK).entity(buildResponseWithLinks(bottle.getId(), uriInfo).getEntity()).build();
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addBottle(@Valid @NotNull NewBottle newBottle){
        if(!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied for non-employee users")
                    .build();
        }

        logger.info("POST /bottles called with new bottle: " + newBottle.getName());
        try {
            int nextId = bottleService.getSize() + 1;
            Bottle bottle = new Bottle(nextId, newBottle.getName(), newBottle.getVolume(),
                    newBottle.getIsAlcoholic(), newBottle.getVolumePercent(), newBottle.getPrice(),
                    newBottle.getSupplier(), newBottle.getInStock());

            Bottle createdBottle = bottleService.addBottle(bottle);
            return Response.status(Response.Status.CREATED)
                    .entity(createdBottle)
                    .build();
        } catch (Exception e) {
            logger.severe("Error in addBottle: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error").build();
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response updateBottle(@PathParam("id") int id, @Valid @NotNull NewBottle newBottle) throws CustomException {
        if(!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied for non-employee users")
                    .build();
        }

        logger.info("PUT /bottles/" + id + " called with new bottle: " + newBottle.getName());

        Bottle existingBottle = bottleService.getById(id);
        if (existingBottle == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Bottle with id " + id + " not found")
                    .build();
        }

        Bottle updatedBottle = new Bottle(id, newBottle.getName(), newBottle.getVolume(),
                newBottle.getIsAlcoholic(), newBottle.getVolumePercent(), newBottle.getPrice(),
                newBottle.getSupplier(), newBottle.getInStock());

        bottleService.updateById(id, updatedBottle);
        return Response.status(Response.Status.OK)
                .entity(SingleBottle.of(updatedBottle))
                .build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteBottle(@PathParam("id") int id) throws CustomException {
        if(!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied for non-employee users")
                    .build();
        }

        logger.info("DELETE /bottles/" + id + " called");

        bottleService.getById(id);
        bottleService.deleteById(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    // Utility methods
    private Response buildResponseWithLinks(int id, UriInfo uriInfo) throws CustomException {
        Bottle bottle = BottleService.instance.getById(id);
        String self = UriBuilder.fromUri(uriInfo.getBaseUri()).path(BottleResource.class).path(BottleResource.class, "getBottleById").build(id).toString();
        String prev = UriBuilder.fromUri(uriInfo.getBaseUri()).path(BottleResource.class).path(BottleResource.class, "getBottleById").build(id - 1).toString();
        String next = UriBuilder.fromUri(uriInfo.getBaseUri()).path(BottleResource.class).path(BottleResource.class, "getBottleById").build(id + 1).toString();
        Optional<Bottle> prevBottle = BottleService.instance.getAll().stream().filter(b -> b.getId() == id - 1).findAny();
        Optional<Bottle> nextBottle = BottleService.instance.getAll().stream().filter(b -> b.getId() == id + 1).findAny();
        Response response;
        if(prevBottle.isPresent() && nextBottle.isPresent()) {
            // prev and next id exists
            response = Response.ok(SingleBottle.of(bottle)).header("Link", "<" + self + ">" + "; rel=\"self\", " + "<" + prev + ">" + "; rel=\"prev\", " + "<" + next + ">" + "; rel=\"next\"").build();
        } else if (prevBottle.isPresent()) {
            // next id doesn't exist
            response = Response.ok(SingleBottle.of(bottle)).header("Link", "<" + self + ">" + "; rel=\"self\", " + "<" + prev + ">" + "; rel=\"prev\"").build();
        } else if (nextBottle.isPresent()) {
            // prev id doesn't exist
            response = Response.ok(SingleBottle.of(bottle)).header("Link", "<" + self + ">" + "; rel=\"self\", " + "<" + next + ">" + "; rel=\"next\"").build();
        } else {
            // only this bottle exists
            response = Response.ok(SingleBottle.of(bottle)).header("Link", self + "; rel=\"self\"").build();
        }
        return response;
    }
}