package entity;

public class OrderLineItem {
    private Product product;
    private int quantity;
    private String note; // <-- 1. Thêm thuộc tính note ở đây

    // Constructor
    public OrderLineItem(Product product, int quantity, String note) {
        this.product = product;
        this.quantity = quantity;
        this.note = note;
    }

    // Các hàm Getter và Setter cơ bản
    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // --- 2. BỔ SUNG HAI HÀM NÀY ĐỂ HẾT GẠCH ĐỎ ---
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
    // ----------------------------------------------

    // Hàm tính thành tiền cho từng dòng món ăn
    public double getSubTotal() {
        return product.getPrice() * quantity;
    }
}