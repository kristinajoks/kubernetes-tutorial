package resources;

import dto.crateDTOs.NewCrate;
import dto.crateDTOs.SingleCrate;
import model.Bottle;
import model.Crate;
import services.BottleService;
import services.CrateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/crates")
public class CrateResource {

    private static final Logger logger = Logger.getLogger("CrateResource");

    private final CrateService crateService = CrateService.instance;
    private final BottleService bottleService = BottleService.instance;

    @Context
    SecurityContext securityContext;

    @GET
    @Produces({MediaType.APPLICATION_JSON , MediaType.APPLICATION_XML})
    public Response getAllCrates(@DefaultValue("1") @QueryParam("page") int page,
                                 @DefaultValue("2147483647") @QueryParam(
                                         "perPage") int perPage) {
        if(!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied for non-employee users")
                    .build();
        }

        logger.info("GET /crates called with page: " + page + " and perPage: " + perPage);

        if (page <= 0 || perPage <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid page or perPage values")
                    .build();
        }

        List<Crate> crates = crateService.getAll();

        if (crates.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No crates found")
                    .build();
        }

        int start = (page - 1) * perPage;

        if (start >= crates.size()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Page number out of range")
                    .build();
        }

        int end = Math.min(start + perPage, crates.size());

        List<SingleCrate> dtoList = crates.subList(start, end).stream()
                .map(SingleCrate::of)
                .collect(Collectors.toList());

        return Response.status(Response.Status.OK).entity(dtoList).build();
    }

    @GET
    @Path("/filter")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response filterCrates(
            @QueryParam("minPrice") Double minPrice,
            @QueryParam("maxPrice") Double maxPrice,
            @QueryParam("name") String name) {

        logger.info("GET /crates/filter called with minPrice: " + minPrice + ", maxPrice: " + maxPrice + ", name: " + name);

        List<Crate> crates = crateService.getAll().stream()
                .filter(c -> (minPrice == null || c.getPrice() >= minPrice) &&
                        (maxPrice == null || c.getPrice() <= maxPrice) &&
                        (name == null || c.getBottle().getName().toLowerCase().contains(name.toLowerCase())))
                .toList();

        if (crates.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("No matching crates").build();
        }

        List<SingleCrate> result = crates.stream().map(SingleCrate::of).collect(Collectors.toList());

        return Response.status(Response.Status.OK).entity(result).build();
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("id/{id}")
    public Response getCrateById(@PathParam("id") int id, @Context UriInfo uriInfo) {
        logger.info("GET /crates/id/" + id + " called");

        if(!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied for non-employee users")
                    .build();
        }

        try {
            Crate crate = crateService.getById(id);
            if (crate == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Crate with ID " + id + " not found").build();
            }
            return Response.status(Response.Status.OK).entity(buildResponseWithLinks(id, uriInfo).getEntity()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            logger.severe("Error in getCrateById: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error").build();
        }
    }


    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("name/{name}")
    public Response getCrateByName(@PathParam("name") String name, @Context UriInfo uriInfo) {
        logger.info("GET /crates/name/" + name + " called");

        if(!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied for non-employee users")
                    .build();
        }

        try {
            Crate crate = crateService.getByName(name);
            if (crate == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Crate with name " + name + " not found").build();
            }
            return Response.status(Response.Status.OK).entity(buildResponseWithLinks(crate.getId(), uriInfo).getEntity()).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            logger.severe("Error in getCrateByName: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error").build();
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addCrate(@Valid @NotNull NewCrate newCrate) {
        if (!securityContext.isUserInRole("employee")) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Access denied for non-employee users")
                    .build();
        }

        logger.info("POST /crates called");
        try {
            Bottle bottle = bottleService.getById(newCrate.getBottleId());
            if (bottle == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Bottle with ID " + newCrate.getBottleId() + " not found").build();
            }

            int nextId = crateService.getSize() + 1;
            Crate crate = new Crate(nextId, bottle, newCrate.getNoOfBottles(), newCrate.getPrice(), newCrate.getInStock());
            crateService.add(crate);

            return Response.status(Response.Status.CREATED).entity(crate).build();
        } catch (Exception e) {
            logger.severe("Error in addCrate: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error").build();
        }
    }

    // Utility methods
    private Response buildResponseWithLinks(int id, UriInfo uriInfo) {
        Crate crate = CrateService.instance.getById(id);
        String self = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CrateResource.class).path(CrateResource.class, "getCrateById").build(id).toString();
        String prev = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CrateResource.class).path(CrateResource.class, "getCrateById").build(id - 1).toString();
        String next = UriBuilder.fromUri(uriInfo.getBaseUri()).path(CrateResource.class).path(CrateResource.class, "getCrateById").build(id + 1).toString();
        Optional<Crate> prevCrate = CrateService.instance.getAll().stream().filter(c -> c.getId() == id - 1).findAny();
        Optional<Crate> nextCrate = CrateService.instance.getAll().stream().filter(c -> c.getId() == id + 1).findAny();
        Response response;
        if(prevCrate.isPresent() && nextCrate.isPresent()) {
            // prev and next id exists
            response = Response.ok(SingleCrate.of(crate)).header("Link", "<" + self + ">" + "; rel=\"self\", " + "<" + prev + ">" + "; rel=\"prev\", " + "<" + next + ">" + "; rel=\"next\"").build();
        } else if (prevCrate.isPresent()) {
            // next id doesn't exist
            response = Response.ok(SingleCrate.of(crate)).header("Link", "<" + self + ">" + "; rel=\"self\", " + "<" + prev + ">" + "; rel=\"prev\"").build();
        } else if (nextCrate.isPresent()) {
            // prev id doesn't exist
            response = Response.ok(SingleCrate.of(crate)).header("Link", "<" + self + ">" + "; rel=\"self\", " + "<" + next + ">" + "; rel=\"next\"").build();
        } else {
            // only this crate exists
            response = Response.ok(SingleCrate.of(crate)).header("Link", self + "; rel=\"self\"").build();
        }
        return response;
    }
}
