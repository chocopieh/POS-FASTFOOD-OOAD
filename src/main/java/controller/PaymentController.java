package controller;

import entity.Order;

public class PaymentController {
    
    // UC04: Thanh toán tiền mặt
    public void processCashPayment(Order order, double cashProvided) {
        System.out.println("\n--- XỬ LÝ THANH TOÁN TIỀN MẶT ---");
        double total = order.calculateTotal();
        
        System.out.println("Tổng tiền hóa đơn: " + total + "đ");
        System.out.println("Tiền khách đưa: " + cashProvided + "đ");

        if (cashProvided >= total) {
            double change = cashProvided - total;
            order.setStatus("Chờ chế biến");
            System.out.println("=> THANH TOÁN THÀNH CÔNG! Tiền thừa trả khách: " + change + "đ");
            System.out.println(">>> Đơn hàng [" + order.getOrderId() + "] đã được chuyển sang Bếp.");
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
        
        // Mô phỏng: Cổng thanh toán (Ngân hàng/MoMo) trả về kết quả Thành công
        order.setStatus("Chờ chế biến");
        System.out.println("=> THANH TOÁN QR THÀNH CÔNG!");
        System.out.println(">>> Đơn hàng [" + order.getOrderId() + "] đã được chuyển sang Bếp.");
        return true; 
    }
}