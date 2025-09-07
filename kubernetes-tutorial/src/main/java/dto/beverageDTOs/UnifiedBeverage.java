package dto.beverageDTOs;

import jakarta.xml.bind.annotation.XmlElement;

public class UnifiedBeverage {
    @XmlElement(required = true)
    private String name;
    @XmlElement(required = true)
    private int bottleId;
    @XmlElement(required = true)
    private Integer crateId;
    private double volume;
    private double pricePerBottle;
    private Double pricePerCrate;
    private int bottlesInStock;
    private Integer cratesInStock;
    private Integer totalBottlesInCrates;
    private boolean isAlcoholic;
    private double volumePercent;

    public UnifiedBeverage() {
    }

    public UnifiedBeverage(String name, int bottleId, Integer crateId, double volume, double pricePerBottle, Double pricePerCrate,
                           int bottlesInStock, Integer cratesInStock, Integer totalBottlesInCrates, boolean isAlcoholic, double volumePercent) {
        this.name = name;
        this.bottleId = bottleId;
        this.crateId = crateId;
        this.volume = volume;
        this.pricePerBottle = pricePerBottle;
        this.pricePerCrate = pricePerCrate;
        this.bottlesInStock = bottlesInStock;
        this.cratesInStock = cratesInStock;
        this.totalBottlesInCrates = totalBottlesInCrates;
        this.isAlcoholic = isAlcoholic;
        this.volumePercent = volumePercent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBottleId() {
        return bottleId;
    }

    public void setBottleId(int bottleId) {
        this.bottleId = bottleId;
    }

    public Integer getCrateId() {
        return crateId;
    }

    public void setCrateId(Integer crateId) {
        this.crateId = crateId;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getPricePerBottle() {
        return pricePerBottle;
    }

    public void setPricePerBottle(double pricePerBottle) {
        this.pricePerBottle = pricePerBottle;
    }

    public Double getPricePerCrate() {
        return pricePerCrate;
    }

    public void setPricePerCrate(Double pricePerCrate) {
        this.pricePerCrate = pricePerCrate;
    }

    public int getBottlesInStock() {
        return bottlesInStock;
    }

    public void setBottlesInStock(int bottlesInStock) {
        this.bottlesInStock = bottlesInStock;
    }

    public Integer getCratesInStock() {
        return cratesInStock;
    }

    public void setCratesInStock(Integer cratesInStock) {
        this.cratesInStock = cratesInStock;
    }

    public Integer getTotalBottlesInCrates() {
        return totalBottlesInCrates;
    }

    public void setTotalBottlesInCrates(Integer totalBottlesInCrates) {
        this.totalBottlesInCrates = totalBottlesInCrates;
    }

    public boolean isAlcoholic() {
        return isAlcoholic;
    }

    public void setIsAlcoholic(boolean alcoholic) {
        isAlcoholic = alcoholic;
    }

    public double getVolumePercent() {
        return volumePercent;
    }

    public void setVolumePercent(double volumePercent) {
        this.volumePercent = volumePercent;
    }
}
