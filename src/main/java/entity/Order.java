package entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {
    private String orderId;
    private Date orderTime;
    private String serviceType; 
    private String status;      
    private List<OrderLineItem> lineItems;

    // BẮT BUỘC PHẢI CÓ 'public' Ở HÀM KHỞI TẠO
    public Order(String orderId, String serviceType) {
        this.orderId = orderId;
        this.orderTime = new Date(); 
        this.serviceType = serviceType;
        this.status = "Chờ thanh toán";
        this.lineItems = new ArrayList<>(); 
    }

    // BẮT BUỘC PHẢI CÓ 'public' Ở CÁC HÀM XỬ LÝ
    public void addLineItem(OrderLineItem item) {
        this.lineItems.add(item);
    }

    public double calculateTotal() {
        double total = 0;
        for (OrderLineItem item : lineItems) {
            total += item.getSubTotal();
        }
        return total;
    }

    public String getOrderId() { return orderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<OrderLineItem> getLineItems() { return lineItems; }
}