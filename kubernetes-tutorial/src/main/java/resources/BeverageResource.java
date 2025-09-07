package resources;

import db.DB;
import dto.beverageDTOs.UnifiedBeverage;
import model.exceptions.CustomException;
import services.BeverageService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.logging.Logger;

@Path("/beverages")
@Produces(MediaType.APPLICATION_JSON)
public class BeverageResource {
    private static final Logger logger = Logger.getLogger("BeverageResource");

    @GET
    public Response getAll(@Context SecurityContext securityContext,
                           @QueryParam("name") String name,
                           @QueryParam("minPrice") Double minPrice,
                           @QueryParam("maxPrice") Double maxPrice,
                           @QueryParam("page") @DefaultValue("1") int page,
                           @QueryParam("perPage") @DefaultValue("2147483647") int perPage
                           ) {
        synchronized (DB.instance) {
            boolean inStockOnly = !securityContext.isUserInRole("employee");

            logger.info("Get all beverages called as" + (inStockOnly ? "customer" : "employee") + "with parameters: " +
                    "inStockOnly=" + inStockOnly +
                    ", name=" + name +
                    ", minPrice=" + minPrice +
                    ", maxPrice=" + maxPrice +
                    ", page=" + page +
                    ", perPage=" + perPage);

            try {
                if (page <= 0 || perPage <= 0) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Invalid page or perPage values").build();
                }

                List<UnifiedBeverage> beverages = BeverageService.instance.getAll(inStockOnly, name, minPrice, maxPrice);
                if (beverages.isEmpty()) {
                    return Response.status(Response.Status.NOT_FOUND).entity("No beverages found").build();
                }

                int start = (page - 1) * perPage;
                if (start >= beverages.size()) {
                    return Response.status(Response.Status.BAD_REQUEST)
                            .entity("Page number out of range").build();
                }

                int end = Math.min(start + perPage, beverages.size());
                List<UnifiedBeverage> dtoList = beverages.subList(start, end);

                return Response.status(Response.Status.OK).entity(dtoList).build();
            } catch (IllegalArgumentException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid query parameters").build();
            }
        }
    }

    @GET
    @Path("/bottle/{id}")
    public Response getByBottleId(@PathParam("id") int id) throws CustomException {
        synchronized (DB.instance) {
            logger.info("Get beverage by bottle ID: " + id);

            UnifiedBeverage dto = BeverageService.instance.getByBottleId(id);

            if (dto == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Beverage with bottle ID " + id + " not found").build();
            }

            return Response.status(Response.Status.OK).entity(dto).build();
        }
    }
}
