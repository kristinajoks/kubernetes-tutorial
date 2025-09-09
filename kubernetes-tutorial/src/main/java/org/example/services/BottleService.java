package org.example.services;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.example.daos.BottleDaoMongo;
import org.example.model.Bottle;

import java.util.List;

@Singleton
public class BottleService {

    private final BottleDaoMongo dao;

    @Inject
    public BottleService(BottleDaoMongo dao) {
        this.dao = dao;
    }

    public List<Bottle> getAll() { return dao.findAll(); }

    public List<Bottle> getAlcoholicBottles() { return dao.findAlcoholic(); }

    public List<Bottle> getNonAlcoholicBottles() { return dao.findNonAlcoholic(); }

    public List<Bottle> getBottlesInStock() { return dao.findInStock(); }

    public List<Bottle> filter(Double minPrice, Double maxPrice, String name) {
        return dao.findFiltered(minPrice, maxPrice, name);
    }

    public Bottle getById(int id) { return dao.findById(id).orElse(null); }

    public Bottle getByName(String name) { return dao.findByName(name).orElse(null); }

    public int getSize() { return (int) dao.count(); }

    public Bottle addBottle(Bottle b) { return dao.insert(b); }

    public boolean updateById(int id, Bottle updated) { return dao.updateById(id, updated); }

    public boolean deleteById(int id) { return dao.deleteById(id); }
}
