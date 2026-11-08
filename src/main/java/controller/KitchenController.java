package controller;

import boundary.KitchenView;
import database.DatabaseConnection;
import entity.Order;
import entity.OrderLineItem;
import entity.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class KitchenController {
    private static KitchenController instance;
    private KitchenView kitchenView;

    private KitchenController() {}

    public static KitchenController getInstance() {
        if (instance == null) {
            instance = new KitchenController();
        }
        return instance;
    }

    public void setKitchenView(KitchenView view) {
        this.kitchenView = view;
    }

    // 1. SELECT: Lấy danh sách các đơn đang chờ từ SQL Server
    public List<Order> getPendingOrdersFromSQL() {
        List<Order> pendingList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Lấy ra mã của các đơn hàng có trạng thái đang chờ bếp làm
            String sqlOrder = "SELECT OrderID FROM Orders WHERE Status = N'Đang chế biến'";
            PreparedStatement pstOrder = conn.prepareStatement(sqlOrder);
            ResultSet rsOrder = pstOrder.executeQuery();

            while (rsOrder.next()) {
                String orderId = rsOrder.getString("OrderID");
                Order order = new Order(orderId, "");

                // Lấy chi tiết các món ăn thuộc về mã đơn hàng đó
               String sqlDetail = "SELECT d.Quantity, d.Note, p.ProductID, p.ProductName " +
                                   "FROM OrderDetails d JOIN Product p ON d.ProductID = p.ProductID " +
                                   "WHERE d.OrderID = ?";
                PreparedStatement pstDetail = conn.prepareStatement(sqlDetail);
                pstDetail.setString(1, orderId);
                ResultSet rsDetail = pstDetail.executeQuery();

                while (rsDetail.next()) {
                    Product p = new Product(rsDetail.getString("ProductID"), rsDetail.getString("ProductName"), "", 0, "");
                    OrderLineItem item = new OrderLineItem(p, rsDetail.getInt("Quantity"), rsDetail.getString("Note"));
                    order.getLineItems().add(item);
                }
                pendingList.add(order);
            }
        } catch (Exception e) {
            System.out.println("Lỗi tải đơn bếp từ SQL: " + e.getMessage());
        }
        return pendingList;
    }
   // 2. UPDATE: Bếp báo làm xong -> Sửa trạng thái SQL
    public void finishOrderInSQL(String orderId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE Orders SET Status = N'Hoàn thành' WHERE OrderID = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, orderId);
            pst.executeUpdate();

            // Ép màn hình bếp vẽ lại (xóa thẻ vàng đi)
            if (kitchenView != null) {
                kitchenView.refreshKitchenBoard(); 
            }
            
            // --- THÊM 3 DÒNG NÀY ---
            // Bắn tín hiệu gọi màn hình Thu Ngân hiển thị thông báo
            if (boundary.POSView.getInstance() != null) {
                boundary.POSView.getInstance().notifyOrderReady(orderId);
            }
            // -----------------------

        } catch (Exception e) {
            System.out.println("Lỗi cập nhật trạng thái món: " + e.getMessage());
        }
    }
}