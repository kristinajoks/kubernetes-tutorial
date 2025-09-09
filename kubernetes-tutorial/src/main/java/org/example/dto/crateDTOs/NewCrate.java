package org.example.dto.crateDTOs;

import jakarta.validation.constraints.*;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NewCrate {
    @PositiveOrZero(message = "Bottle ID must be a positive integer")
    @XmlElement(required = true)
    private int bottleId;
    @Positive(message = "Number of bottles must be at least 1")
    @XmlElement(required = true)
    private int noOfBottles;
    @PositiveOrZero(message = "Price must be a positive number")
    @XmlElement(required = true)
    private double price;
    @Min(value = 0, message = "In stock must be 0 or more")
    private int inStock;

    public NewCrate() {}
    public int getBottleId() { return bottleId; }
    public void setBottleId(int bottleId) { this.bottleId = bottleId; }

    public int getNoOfBottles() { return noOfBottles; }
    public void setNoOfBottles(int noOfBottles) { this.noOfBottles = noOfBottles; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getInStock() { return inStock; }
    public void setInStock(int inStock) { this.inStock = inStock; }
}
