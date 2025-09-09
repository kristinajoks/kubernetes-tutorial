package org.example.dto.crateDTOs;

import org.example.model.Crate;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "singleCrate")
public class SingleCrate {
    @XmlElement(required = true)
    private int id;
    @XmlElement(required = true)
    private String bottleName;
    @XmlElement(required = true)
    private int noOfBottles;
    @XmlElement(required = true)
    private double price;
    private int inStock;

    public SingleCrate() {}
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBottleName() { return bottleName; }
    public void setBottleName(String bottleName) { this.bottleName = bottleName; }

    public int getNoOfBottles() { return noOfBottles; }
    public void setNoOfBottles(int noOfBottles) { this.noOfBottles = noOfBottles; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getInStock() { return inStock; }
    public void setInStock(int inStock) { this.inStock = inStock; }

    public static SingleCrate of(Crate crate){
        SingleCrate dto = new SingleCrate();
        dto.setId(crate.getId());
        dto.setBottleName(crate.getBottle().getName());
        dto.setNoOfBottles(crate.getNoOfBottles());
        dto.setPrice(crate.getPrice());
        dto.setInStock(crate.getInStock());
        return dto;
    }
}
