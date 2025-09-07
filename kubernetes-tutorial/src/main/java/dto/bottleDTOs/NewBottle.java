package dto.bottleDTOs;
import jakarta.validation.constraints.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "bottle")
public class NewBottle {
    @NotNull(message = "A name must be provided")
    @NotEmpty(message = "Name must not be empty")
    @XmlElement(required = true)
    private String name;
    @Positive(message = "Volume must be a positive number")
    @XmlElement(required = true)
    private double volume;
    @XmlElement(required = true)
    private boolean isAlcoholic;
    private double volumePercent;
    @PositiveOrZero(message = "Price must be a positive number")
    @XmlElement(required = true)
    private double price;
    private String supplier;
    @Min(value = 0, message = "In stock must be 0 or more")
    private int inStock;

    public NewBottle() {
    }

    public NewBottle(String name, double volume, boolean isAlcoholic, double volumePercent, double price, String supplier, int inStock) {
        this.name = name;
        this.volume = volume;
        this.isAlcoholic = isAlcoholic;
        this.volumePercent = volumePercent;
        this.price = price;
        this.supplier = supplier;
        this.inStock = inStock;
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

    public boolean getIsAlcoholic() {
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
}
