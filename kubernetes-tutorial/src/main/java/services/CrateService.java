package services;

import db.DB;
import model.Bottle;
import model.Crate;
import model.exceptions.CustomException;

import java.util.List;

public class CrateService {

    public static final CrateService instance = new CrateService();
    private final BottleService bottleService = BottleService.instance;

    private CrateService() {
    }

    public List<Crate> getAll() {
        synchronized (DB.instance) {
            return DB.instance.getCrates();
        }
    }

    public int getSize() {
        synchronized (DB.instance) {
            return DB.instance.getCrates().size();
        }
    }

    public Crate getById(int id) {
        synchronized (DB.instance) {
            return DB.instance.getCrates().stream()
                    .filter(crate -> crate.getId() == id)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Crate with id " + id + " not found"));
        }
    }

    public Crate getByName(String name) {
        synchronized (DB.instance) {
            return DB.instance.getCrates().stream()
                    .filter(crate -> crate.getBottle().getName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Crate with name " + name + " not found"));
        }
    }

    public List<Crate> getCratesInStock() {
        synchronized (DB.instance) {
            return DB.instance.getCrates().stream()
                    .filter(crate -> crate.getInStock() > 0)
                    .toList();
        }
    }

    public List<Crate> getCratesWithPriceLessThan(double price) {
        synchronized (DB.instance) {
            return DB.instance.getCrates().stream()
                    .filter(crate -> crate.getPrice() < price)
                    .toList();
        }
    }

    public List<Crate> getCratesWithPriceGreaterThan(double price) {
        synchronized (DB.instance) {
            return DB.instance.getCrates().stream()
                    .filter(crate -> crate.getPrice() > price)
                    .toList();
        }
    }

    public List<Crate> getCratesWithPriceBetween(double min, double max) {
        synchronized (DB.instance) {
        return DB.instance.getCrates().stream()
                .filter(crate -> crate.getPrice() >= min && crate.getPrice() <= max)
                .toList();
        }
    }

    public Crate add(Crate crate) throws CustomException {
        synchronized (DB.instance) {
            if (DB.instance.getCrates().stream().anyMatch(c -> c.getId() == crate.getId())) {
                throw new IllegalArgumentException("Crate with id " + crate.getId() + " already exists");
            }
            Bottle bottle = bottleService.getById(crate.getBottle().getId());
            if (bottle == null) {
                throw new IllegalArgumentException("Bottle ID not found for crate.");
            }

            crate.setId(DB.instance.getCrates().stream().mapToInt(Crate::getId).max().orElse(0) + 1);

            DB.instance.getCrates().add(crate);
            return crate;
        }
    }

    public void updateById(int id, Crate crate) {
        synchronized (DB.instance) {
            int index = DB.instance.getCrates().stream()
                    .map(Crate::getId)
                    .toList()
                    .indexOf(id);
            if (index == -1) {
                throw new IllegalArgumentException("Crate with id " + id + " not found");
            }

            Crate existing = DB.instance.getCrates().get(index);
            existing.setBottle(crate.getBottle());
            existing.setNoOfBottles(crate.getNoOfBottles());
            existing.setPrice(crate.getPrice());
            existing.setInStock(crate.getInStock());
        }
    }

    public void deleteById(int id) {
        synchronized (DB.instance) {
            int index = DB.instance.getCrates().stream()
                    .map(Crate::getId)
                    .toList()
                    .indexOf(id);
            if (index == -1) {
                throw new IllegalArgumentException("Crate with id " + id + " not found");
            }
            DB.instance.getCrates().remove(index);
        }
    }
}
