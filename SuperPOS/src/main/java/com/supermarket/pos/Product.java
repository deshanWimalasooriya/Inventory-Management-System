package com.supermarket.pos;

public class Product {
    private String barcode;
    private String name;
    private double price;
    private int stock;
    private String imagePath;

    public Product(String barcode, String name, double price, int stock, String imagePath) {
        this.barcode = barcode;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.imagePath = imagePath;
    }

    public String getBarcode() { return barcode; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public String getImagePath() { return imagePath; }
}