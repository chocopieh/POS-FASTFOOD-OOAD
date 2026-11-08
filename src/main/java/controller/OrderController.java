package controller;

import entity.Order;
import entity.OrderLineItem;
import entity.Product;

public class OrderController {
    private Order currentOrder;
    private int orderCounter = 1;

    // UC03: Tạo đơn hàng mới
   public void createNewOrder(String serviceType) {
        // Dùng thời gian hiện tại làm hậu tố để mã đơn luôn là duy nhất, không bao giờ trùng khóa chính
        String newOrderId = "ORD" + System.currentTimeMillis() % 10000; 
        currentOrder = new Order(newOrderId, serviceType);
        System.out.println("\n>>> HỆ THỐNG: Đã tạo đơn hàng mới [" + newOrderId + "] - " + serviceType);
    }

    // UC03: Thêm món vào đơn
    public void addProductToOrder(Product product, int quantity, String note) {
        if (currentOrder != null) {
            OrderLineItem item = new OrderLineItem(product, quantity, note);
            currentOrder.addLineItem(item);
            System.out.println(" + Đã thêm: " + product.getProductName() + " (SL: " + quantity + ")");
        } else {
            System.out.println("LỖI: Vui lòng tạo đơn hàng trước khi chọn món!");
        }
    }

    // UC13: Tính tổng tiền
    public double getOrderTotal() {
        return (currentOrder != null) ? currentOrder.calculateTotal() : 0;
    }

    public Order getCurrentOrder() { return currentOrder; }
}