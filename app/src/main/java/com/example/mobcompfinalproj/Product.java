package com.example.mobcompfinalproj;

public class Product {
    private String id;
    private String productName;
    private String category;
    private String price;
    private String weight;
    private int inventoryCount;
    private int selectedQuantity = 0;

    // Empty constructor needed for Firestore

    public Product() {}

    public Product(String id, String productName, String category, String price, String weight) {
        this.id = id;
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.weight = weight;
        this.inventoryCount = 0;
    }

    // Getters and Setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }

    public int getInventoryCount() { return inventoryCount; }
    public void setInventoryCount(int inventoryCount) { this.inventoryCount = inventoryCount; }

    public int getSelectedQuantity() { return selectedQuantity; }
    public void setSelectedQuantity(int selectedQuantity) { this.selectedQuantity = selectedQuantity; }

    // Helper method to get price as double for calculations

    public double getPriceAsDouble() {
        try {
            return Double.parseDouble(price);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
} 
