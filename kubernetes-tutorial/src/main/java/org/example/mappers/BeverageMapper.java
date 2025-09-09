package org.example.mappers;

import org.example.dto.beverageDTOs.UnifiedBeverage;
import org.example.model.Bottle;
import org.example.model.Crate;

public class BeverageMapper {
    public static UnifiedBeverage fromBottleAndCrate(Bottle bottle, Crate crate) {
        UnifiedBeverage dto = new UnifiedBeverage();
        dto.setName(bottle.getName());
        dto.setBottleId(bottle.getId());
        dto.setVolume(bottle.getVolume());
        dto.setPricePerBottle(bottle.getPrice());
        dto.setBottlesInStock(bottle.getInStock());
        dto.setIsAlcoholic(bottle.getIsAlcoholic());
        dto.setVolumePercent(bottle.getVolumePercent());

        if (crate != null) {
            dto.setCrateId(crate.getId());
            dto.setPricePerCrate(crate.getPrice());
            dto.setCratesInStock(crate.getInStock());
            dto.setTotalBottlesInCrates(crate.getNoOfBottles() * crate.getInStock());
        }

        return dto;
    }
}
