package org.example.services;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.daos.BeverageDaoMongo;
import org.example.dto.beverageDTOs.UnifiedBeverage;

@Singleton
public class BeverageService {

    private final BeverageDaoMongo dao;

    @Inject
    public BeverageService(BeverageDaoMongo dao) { this.dao = dao; }

    public BeverageDaoMongo.PagedResult<UnifiedBeverage> getAll(
            boolean inStockOnly, String name, Double minPrice, Double maxPrice, int page, int perPage) {
        return dao.findFiltered(inStockOnly, name, minPrice, maxPrice, page, perPage);
    }

    public UnifiedBeverage getByBottleId(int bottleId) {
        return dao.findByBottleId(bottleId).orElse(null);
    }
}
