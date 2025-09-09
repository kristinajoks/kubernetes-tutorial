package org.example.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.example.daos.BeverageDaoMongo;
import org.example.dto.beverageDTOs.UnifiedBeverage;
import org.example.model.exceptions.CustomException;
import org.example.services.BeverageService;

import java.util.List;
import java.util.logging.Logger;

@Path("/beverages")
@Produces(MediaType.APPLICATION_JSON)
public class BeverageResource {
    private static final Logger logger = Logger.getLogger("BeverageResource");

    @Inject
    BeverageService beverageService;

    @GET
    public Response getAll(@Context SecurityContext securityContext,
                           @QueryParam("name") String name,
                           @QueryParam("minPrice") Double minPrice,
                           @QueryParam("maxPrice") Double maxPrice,
                           @QueryParam("page") @DefaultValue("1") int page,
                           @QueryParam("perPage") @DefaultValue("2147483647") int perPage) {

        boolean inStockOnly = !securityContext.isUserInRole("employee");

        logger.info("GET /beverages as " + (inStockOnly ? "customer" : "employee")
                + " name=" + name + " min=" + minPrice + " max=" + maxPrice
                + " page=" + page + " perPage=" + perPage);

        if (page <= 0 || perPage <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid page or perPage values").build();
        }

        BeverageDaoMongo.PagedResult<UnifiedBeverage> pr =
                beverageService.getAll(inStockOnly, name, minPrice, maxPrice, page, perPage);

        if (pr.total == 0) {
            return Response.status(Response.Status.NOT_FOUND).entity("No beverages found").build();
        }
        if (pr.items.isEmpty() && page > 1) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Page number out of range").build();
        }

        return Response.ok(pr.items).build();
    }

    @GET
    @Path("/bottle/{id}")
    public Response getByBottleId(@PathParam("id") int id) throws CustomException {
        logger.info("GET /beverages/bottle/" + id);
        UnifiedBeverage dto = beverageService.getByBottleId(id);
        if (dto == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Beverage with bottle ID " + id + " not found").build();
        }
        return Response.ok(dto).build();
    }
}
