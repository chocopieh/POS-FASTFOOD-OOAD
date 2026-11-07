package entity;

public class Product {
    private String productId;
    private String productName;
    private String description;
    private double price;
    private String status; // "Đang kinh doanh", "Hết hàng"

    // Constructor (Hàm khởi tạo)
    public Product(String productId, String productName, String description, double price, String status) {
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.status = status;
    }

    // Getters và Setters (Tính bao đóng - Encapsulation)
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}