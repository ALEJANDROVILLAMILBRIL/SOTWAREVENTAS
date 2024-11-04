package com.example.softwareventas.models;

public class Cart
{
    private String id;
    private String productId;
    private String productName;
    private String categoryId;
    private String userId;
    private double price;
    private int quantity;
    private long purchaseDate;

    public Cart() {
    }

    public Cart(String id, String productId, String productName, String categoryId, String userId, double price, int quantity, long purchaseDate) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.categoryId = categoryId;
        this.userId = userId;
        this.price = price;
        this.quantity = quantity;
        this.purchaseDate = purchaseDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(long purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
}