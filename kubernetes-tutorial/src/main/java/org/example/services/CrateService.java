package org.example.services;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.daos.CrateDaoMongo;
import org.example.daos.CrateDaoMongo.CrateRecord;
import org.example.model.Bottle;
import org.example.model.Crate;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class CrateService {

    private final CrateDaoMongo dao;
    private final BottleService bottleService;

    @Inject
    public CrateService(CrateDaoMongo dao, BottleService bottleService) {
        this.dao = dao;
        this.bottleService = bottleService;
    }

    private Crate toDomain(CrateRecord r) {
        Bottle bottle = bottleService.getById(r.bottleId);
        if (bottle == null) return null;
        return new Crate(r.id, bottle, r.bottlesPerCrate, r.price, r.inStock);
    }
    private CrateRecord toRecord(Crate c) {
        return new CrateRecord(c.getId(), c.getBottle().getId(), c.getNoOfBottles(), c.getPrice(), c.getInStock());
    }

    public List<Crate> getAll() {
        List<Crate> out = new ArrayList<>();
        for (CrateRecord r : dao.findAllRecords()) {
            Crate c = toDomain(r);
            if (c != null) out.add(c);
        }
        return out;
    }

    public Crate getById(int id) {
        return dao.findByIdRecord(id).map(this::toDomain).orElse(null);
    }

    public Crate getByName(String name) {
        Bottle b = bottleService.getByName(name);
        if (b == null) return null;
        return dao.findByBottleIdRecord(b.getId()).map(this::toDomain).orElse(null);
    }

    public List<Crate> filter(Double minPrice, Double maxPrice, String name) {
        List<CrateRecord> base;
        if (name != null && !name.isBlank()) {
            Bottle b = bottleService.getByName(name);
            if (b == null) return List.of();
            base = dao.findByBottleIdRecord(b.getId()).map(List::of).orElse(List.of());
            base = base.stream()
                    .filter(r -> (minPrice == null || r.price >= minPrice) &&
                            (maxPrice == null || r.price <= maxPrice))
                    .toList();
        } else {
            base = (minPrice != null || maxPrice != null)
                    ? dao.findByPriceRangeRecords(minPrice, maxPrice)
                    : dao.findAllRecords();
        }
        List<Crate> out = new ArrayList<>();
        for (CrateRecord r : base) {
            Crate c = toDomain(r);
            if (c != null) out.add(c);
        }
        return out;
    }

    public int getSize() { return (int) dao.count(); }

    public Crate add(Crate crate) {
        CrateRecord rec = toRecord(crate);
        CrateRecord inserted = dao.insertRecord(rec);
        return toDomain(inserted);
    }

    public boolean updateById(int id, Crate updated) {
        return dao.updateByIdRecord(id, toRecord(updated));
    }

    public boolean deleteById(int id) {
        return dao.deleteById(id);
    }
}
