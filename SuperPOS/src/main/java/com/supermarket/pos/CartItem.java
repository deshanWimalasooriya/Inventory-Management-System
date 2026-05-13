package com.supermarket.pos;

public class CartItem {
    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    // Getters for the TableView
    public String getBarcode() { return product.getBarcode(); }
    public String getName() { return product.getName(); }
    public double getPrice() { return product.getPrice(); }
    public int getQuantity() { return quantity; }
    public double getTotal() { return product.getPrice() * quantity; }
    public Product getProduct() { return product; }

    // Setters
    public void setQuantity(int quantity) { this.quantity = quantity; }
}