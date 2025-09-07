package services;

import dto.beverageDTOs.UnifiedBeverage;
import mappers.BeverageMapper;
import model.Bottle;
import model.Crate;
import model.exceptions.CustomException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BeverageService {
    public static final BeverageService instance = new BeverageService();

    private BeverageService() {
    }

    public List<UnifiedBeverage> getAll(boolean inStockOnly, String nameFilter, Double minPrice, Double maxPrice) {
        List<Bottle> bottles = BottleService.instance.getAll();
        Map<Integer, Crate> crateMap = CrateService.instance.getAll().stream()
                .collect(Collectors.toMap(c -> c.getBottle().getId(), Function.identity()));

        return bottles.stream()
                .map(b -> BeverageMapper.fromBottleAndCrate(b, crateMap.get(b.getId())))
                .filter(dto -> {
                    if (nameFilter != null && !dto.getName().toLowerCase().contains(nameFilter.toLowerCase())) {
                        return false;
                    }

                  if (inStockOnly && dto.getBottlesInStock() <= 0 &&
                            (dto.getTotalBottlesInCrates() == null || dto.getTotalBottlesInCrates() <= 0)) {
                        return false;
                    }

                    // Filter by price range (match if either bottle OR crate price is within range)
                    boolean bottleOk = dto.getPricePerBottle() >= (minPrice != null ? minPrice : Double.NEGATIVE_INFINITY)
                            && dto.getPricePerBottle() <= (maxPrice != null ? maxPrice : Double.POSITIVE_INFINITY);

                    boolean crateOk = dto.getPricePerCrate() != null &&
                            dto.getPricePerCrate() >= (minPrice != null ? minPrice : Double.NEGATIVE_INFINITY) &&
                            dto.getPricePerCrate() <= (maxPrice != null ? maxPrice : Double.POSITIVE_INFINITY);

                    return bottleOk || crateOk;
                })
                .toList();
    }

    public UnifiedBeverage getByBottleId(int bottleId) throws CustomException {
        Bottle bottle = BottleService.instance.getById(bottleId);
        Crate crate = CrateService.instance.getAll().stream()
                .filter(c -> c.getBottle().getId() == bottleId)
                .findFirst()
                .orElse(null);
        return BeverageMapper.fromBottleAndCrate(bottle, crate);
    }

}
