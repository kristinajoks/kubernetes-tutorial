package dto.bottleDTOs;

import model.Bottle;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "singleBottle")
public class SingleBottle {
    @XmlElement(required = true)
    private int id;
    @XmlElement(required = true)
    private String name;
    @XmlElement(required = true)
    private double volume;
    @XmlElement(required = true)
    private boolean isAlcoholic;
    private double volumePercent;
    @XmlElement(required = true)
    private double price;
    private String supplier;
    private int inStock;

    public SingleBottle(int id, String name, double volume, boolean isAlcoholic, double volumePercent, double price, String supplier, int inStock) {
        this.id = id;
        this.name = name;
        this.volume = volume;
        this.isAlcoholic = isAlcoholic;
        this.volumePercent = volumePercent;
        this.price = price;
        this.supplier = supplier;
        this.inStock = inStock;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public boolean isAlcoholic() {
        return isAlcoholic;
    }

    public void setAlcoholic(boolean alcoholic) {
        isAlcoholic = alcoholic;
    }

    public double getVolumePercent() {
        return volumePercent;
    }

    public void setVolumePercent(double volumePercent) {
        this.volumePercent = volumePercent;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public int getInStock() {
        return inStock;
    }

    public void setInStock(int inStock) {
        this.inStock = inStock;
    }

    public static SingleBottle of(Bottle bottle){
        return new SingleBottle(
                bottle.getId(),
                bottle.getName(),
                bottle.getVolume(),
                bottle.isAlcoholic(),
                bottle.getVolumePercent(),
                bottle.getPrice(),
                bottle.getSupplier(),
                bottle.getInStock()
        );
    }
}
