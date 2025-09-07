package services;

import db.DB;
import model.Bottle;
import model.exceptions.CustomException;
import model.exceptions.ErrorType;

import java.util.List;

public class BottleService {

    public static final BottleService instance = new BottleService();

    private BottleService() {
    }

    public List<Bottle> getAll(){
        synchronized (DB.instance) {
            return DB.instance.getBottles();
        }
    }

    public int getSize(){
        synchronized (DB.instance) {
            return DB.instance.getBottles().size();
        }
    }

    public Bottle getById(int id) throws CustomException {
        synchronized (DB.instance) {
            return DB.instance.getBottles().stream()
                    .filter(bottle -> bottle.getId() == id)
                    .findFirst()
                    .orElseThrow(() -> new CustomException("Bottle with id " + id + " not found", ErrorType.NOT_FOUND));
        }
    }

    public Bottle getByName(String name) throws CustomException {
        synchronized (DB.instance) {
            return DB.instance.getBottles().stream()
                    .filter(bottle -> bottle.getName().equalsIgnoreCase(name))
                    .findFirst()
                    .orElseThrow(() -> new CustomException("Bottle with name " + name + " not found", ErrorType.NOT_FOUND));
        }
    }

    public List<Bottle> getAlcoholicBottles() {
        synchronized (DB.instance) {
            return DB.instance.getBottles().stream()
                    .filter(Bottle::isAlcoholic)
                    .toList();
        }
    }

    public List<Bottle> getNonAlcoholicBottles() {
        synchronized (DB.instance) {
            return DB.instance.getBottles().stream()
                    .filter(bottle -> !bottle.isAlcoholic())
                    .toList();
        }
    }

    public List<Bottle> getBottlesInStock(){
        synchronized (DB.instance) {
            return DB.instance.getBottles().stream()
                    .filter(bottle -> bottle.getInStock() > 0)
                    .toList();
        }
    }

    public List<Bottle> getBottlesWithPriceLessThan(double price) {
        synchronized (DB.instance) {
            return DB.instance.getBottles().stream()
                    .filter(bottle -> bottle.getPrice() < price)
                    .toList();
        }
    }

    public List<Bottle> getBottlesWithPriceGreaterThan(double price) {
        synchronized (DB.instance) {
            return DB.instance.getBottles().stream()
                    .filter(bottle -> bottle.getPrice() > price)
                    .toList();
        }
    }

    public List<Bottle> getBottlesWithPriceBetween(double minPrice, double maxPrice) {
        synchronized (DB.instance) {
            return DB.instance.getBottles().stream()
                    .filter(bottle -> bottle.getPrice() >= minPrice && bottle.getPrice() <= maxPrice)
                    .toList();
        }
    }

    public Bottle addBottle(Bottle bottle) {
        synchronized (DB.instance) {
            if (DB.instance.getBottles().stream().anyMatch(b -> b.getId() == bottle.getId())) {
                throw new IllegalArgumentException("Bottle with id " + bottle.getId() + " already exists");
            }
            if (DB.instance.getBottles().stream().anyMatch(b -> b.getName().equalsIgnoreCase(bottle.getName()))) {
                throw new IllegalArgumentException("Bottle with name" + bottle.getName() + "already exists.");
            }
            bottle.setId(DB.instance.getBottles().stream().mapToInt(Bottle::getId).max().orElse(0) + 1);

            DB.instance.getBottles().add(bottle);
            return bottle;
        }
    }

    public void updateById(int id, Bottle bottle) {
        synchronized (DB.instance) {
            int index = DB.instance.getBottles().stream()
                    .map(Bottle::getId)
                    .toList()
                    .indexOf(id);
            if (index == -1) {
                throw new IllegalArgumentException("Bottle with id " + id + " does not exist");
            }
            Bottle existingBottle = DB.instance.getBottles().get(index);
            existingBottle.setName(bottle.getName());
            existingBottle.setVolume(bottle.getVolume());
            existingBottle.setAlcoholic(bottle.isAlcoholic());
            existingBottle.setVolumePercent(bottle.getVolumePercent());
            existingBottle.setPrice(bottle.getPrice());
            existingBottle.setSupplier(bottle.getSupplier());
            existingBottle.setInStock(bottle.getInStock());
        }
    }

    public void deleteById(int id) throws CustomException {
        synchronized (DB.instance) {
            int index = DB.instance.getBottles().stream()
                    .map(Bottle::getId)
                    .toList()
                    .indexOf(id);
            if (index == -1) {
                throw new CustomException("Bottle with id " + id + " not found", ErrorType.NOT_FOUND);
            }
            DB.instance.getBottles().remove(index);
        }
    }
}