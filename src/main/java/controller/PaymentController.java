package controller;

import database.DatabaseConnection;
import entity.Order;
import entity.OrderLineItem;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class PaymentController {
    
    // UC04: Thanh toán tiền mặt
    public void processCashPayment(Order order, double cashProvided) {
        System.out.println("\n--- XỬ LÝ THANH TOÁN TIỀN MẶT ---");
        double total = order.calculateTotal();
        
        System.out.println("Tổng tiền hóa đơn: " + total + "đ");
        System.out.println("Tiền khách đưa: " + cashProvided + "đ");

        if (cashProvided >= total) {
            double change = cashProvided - total;
            order.setStatus("Đang chế biến"); // Chuyển trạng thái sang đang chế biến cho bếp
            
            // ---> GỌI HÀM LƯU VÀO SQL SERVER <---
            saveOrderToDatabase(order);

            System.out.println("=> THANH TOÁN THÀNH CÔNG! Tiền thừa trả khách: " + change + "đ");
        } else {
            System.out.println("=> LỖI: Số tiền khách đưa không đủ!");
        }
    }

    // UC15: Thanh toán qua QR / Chuyển khoản
    public boolean processQRPayment(Order order) {
        System.out.println("\n--- XỬ LÝ THANH TOÁN QR / CHUYỂN KHOẢN ---");
        double total = order.calculateTotal();
        
        System.out.println(">>> Hệ thống đang sinh mã QR cho số tiền: " + total + "đ...");
        System.out.println(">>> Chờ Cổng thanh toán xác thực (Callback)...");
        
        order.setStatus("Đang chế biến");
        
        // ---> GỌI HÀM LƯU VÀO SQL SERVER <---
        saveOrderToDatabase(order);

        System.out.println("=> THANH TOÁN QR THÀNH CÔNG!");
        return true; 
    }

    // --- HÀM KẾT NỐI VÀ LƯU XUỐNG CƠ SỞ DỮ LIỆU SQL SERVER ---
    private void saveOrderToDatabase(Order order) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            
            // 1. Lưu thông tin chung vào bảng Orders
            String sqlOrder = "INSERT INTO Orders (OrderID, ServiceType, Total, Status) VALUES (?, ?, ?, N'Đang chế biến')";
            PreparedStatement pstOrder = conn.prepareStatement(sqlOrder);
            pstOrder.setString(1, order.getOrderId());
            pstOrder.setString(2, order.getServiceType());
            pstOrder.setDouble(3, order.calculateTotal());
            pstOrder.executeUpdate();

            // 2. Lưu chi tiết từng món vào bảng OrderDetails
            String sqlDetail = "INSERT INTO OrderDetails (OrderID, ProductID, Quantity, Note) VALUES (?, ?, ?, ?)";
            PreparedStatement pstDetail = conn.prepareStatement(sqlDetail);
            
            for (OrderLineItem item : order.getLineItems()) {
                pstDetail.setString(1, order.getOrderId());
                pstDetail.setString(2, item.getProduct().getProductId());
                pstDetail.setInt(3, item.getQuantity());
                pstDetail.setString(4, item.getNote() != null ? item.getNote() : "");
                pstDetail.executeUpdate();
            }
            
            System.out.println(">>> Đã đẩy đơn hàng [" + order.getOrderId() + "] xuống SQL Server thành công!");
            
        } catch (Exception e) {
            System.out.println("Lỗi lưu đơn hàng vào SQL (PaymentController): " + e.getMessage());
        }
    }
}