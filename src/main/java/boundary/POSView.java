package boundary;

import controller.OrderController;
import controller.PaymentController;
import database.DatabaseConnection; // Thư viện kết nối SQL
import entity.OrderLineItem;
import entity.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class POSView extends JFrame {
    private OrderController orderCtrl;
    private PaymentController paymentCtrl;

    private DefaultTableModel tableModel;
    private JTable billTable;
    private JLabel lblTotal;
    private CardLayout cardLayout;
    private JPanel rightPanel;
    private JTextField txtEditNote;
    private JTextField txtEditQuantity;
    private OrderLineItem editingItem = null; // Lưu món đang được chọn sửa

    // Danh sách menu tải từ Database (hoặc dùng mẫu dự phòng)
    private List<Product> menuList;
    private static POSView instance;
    
    public POSView() {
        orderCtrl = new OrderController();
        paymentCtrl = new PaymentController();
        
        orderCtrl.createNewOrder("Ăn tại chỗ");
        instance = this; // 2. Gán instance ngay đầu hàm khởi tạo

        orderCtrl = new OrderController();
        paymentCtrl = new PaymentController();
        

        // 1. TẢI MENU TỪ CƠ SỞ DỮ LIỆU SQL
        loadMenuFromDatabase();

        // 2. CẤU HÌNH CỬA SỔ CHÍNH
        setTitle("HỆ THỐNG POS - CỬA HÀNG THỨC ĂN NHANH");
        setSize(1050, 650); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 3. TẠO KHU VỰC MENU BÊN TRÁI (Lưới các nút bấm)
        JPanel menuPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        menuPanel.setBorder(BorderFactory.createTitledBorder("MENU THỰC ĐƠN (TỪ SQL)"));
        menuPanel.setBackground(new Color(255, 245, 230));

        for (Product p : menuList) {
            JButton btnItem = new JButton("<html><center>" + p.getProductName() + "<br><b>" + p.getPrice() + "đ</b></center></html>");
            btnItem.setFont(new Font("Times New Roman", Font.PLAIN, 16));
            btnItem.setBackground(new Color(255, 153, 51));
            btnItem.setForeground(Color.WHITE);
            btnItem.setFocusPainted(false);
            
            btnItem.addActionListener(e -> handleSelectProduct(p));
            menuPanel.add(btnItem);
        }
        // Bọc menuPanel vào một thanh cuộn (JScrollPane) để lướt xem nhiều món
        JScrollPane menuScrollPane = new JScrollPane(menuPanel);
        menuScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Chỉnh tốc độ cuộn cho mượt
        add(menuScrollPane, BorderLayout.CENTER);

// 4. TẠO KHU VỰC BÊN PHẢI (Dùng CardLayout để ẩn thanh Tab)
    cardLayout = new CardLayout();
    rightPanel = new JPanel(cardLayout);
    rightPanel.setPreferredSize(new Dimension(450, 600));
    rightPanel.setBorder(BorderFactory.createTitledBorder("CHI TIẾT ĐƠN HÀNG"));

    // --- PANEL 1: CHI TIẾT ĐƠN HÀNG (HÓA ĐƠN) ---
    JPanel orderTabPanel = new JPanel(new BorderLayout());
    
    // 4.1 Bảng hiển thị món
    String[] columns = {"Tên món", "SL", "Đơn giá", "Thành tiền"};
    tableModel = new DefaultTableModel(columns, 0);
    billTable = new JTable(tableModel);
    billTable.setRowHeight(50); 
    billTable.setFont(new Font("Arial", Font.PLAIN, 14));
    
    billTable.getColumnModel().getColumn(0).setPreferredWidth(220); 
    billTable.getColumnModel().getColumn(1).setPreferredWidth(40);  
    billTable.getColumnModel().getColumn(2).setPreferredWidth(80);  
    billTable.getColumnModel().getColumn(3).setPreferredWidth(90);  

    // Sự kiện click chuyển sang Panel Sửa
    billTable.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            int selectedRow = billTable.getSelectedRow();
            if (selectedRow >= 0 && orderCtrl.getCurrentOrder() != null) {
                editingItem = orderCtrl.getCurrentOrder().getLineItems().get(selectedRow);
                txtEditNote.setText(editingItem.getNote() != null ? editingItem.getNote() : "");
                txtEditQuantity.setText(String.valueOf(editingItem.getQuantity()));
                
                // Lệnh trượt sang màn hình Sửa món
                cardLayout.show(rightPanel, "EDIT_PANEL");
            }
        }
    });

    orderTabPanel.add(new JScrollPane(billTable), BorderLayout.CENTER);

    // 4.2 Khu vực Tổng tiền & Các nút Thanh toán
    JPanel checkoutPanel = new JPanel(new GridLayout(4, 1, 5, 5));
    lblTotal = new JLabel("Tổng cộng: 0đ", SwingConstants.RIGHT);
    lblTotal.setFont(new Font("Arial", Font.BOLD, 22));
    lblTotal.setForeground(Color.RED);
    
    JButton btnThanhToan = new JButton("THANH TOÁN TIỀN MẶT");
    btnThanhToan.setFont(new Font("Arial", Font.BOLD, 15));
    btnThanhToan.setBackground(new Color(0, 153, 51));
    btnThanhToan.setForeground(Color.WHITE);

    JButton btnThanhToanQR = new JButton("THANH TOÁN QR / CHUYỂN KHOẢN");
    btnThanhToanQR.setFont(new Font("Arial", Font.BOLD, 15));
    btnThanhToanQR.setBackground(new Color(0, 102, 204));
    btnThanhToanQR.setForeground(Color.WHITE);
    
    JButton btnHuyDon = new JButton("HỦY ĐƠN / TẠO MỚI");
    btnHuyDon.setFont(new Font("Arial", Font.BOLD, 14));

    checkoutPanel.add(lblTotal);
    checkoutPanel.add(btnThanhToan);
    checkoutPanel.add(btnThanhToanQR);
    checkoutPanel.add(btnHuyDon);
    orderTabPanel.add(checkoutPanel, BorderLayout.SOUTH);

    // --- PANEL 2: GIAO DIỆN CHỈNH SỬA MÓN ---
    JPanel editTabPanel = new JPanel(new GridLayout(6, 1, 10, 10));
    editTabPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    
    JLabel lblEditTitle = new JLabel("THÔNG TIN CHI TIẾT MÓN", SwingConstants.CENTER);
    lblEditTitle.setFont(new Font("Arial", Font.BOLD, 16));
    
    txtEditNote = new JTextField();
    txtEditQuantity = new JTextField();
    
    JButton btnSaveItem = new JButton("LƯU THAY ĐỔI");
    btnSaveItem.setFont(new Font("Arial", Font.BOLD, 15));
    btnSaveItem.setBackground(new Color(255, 153, 51));
    btnSaveItem.setForeground(Color.WHITE);
    
    JButton btnBackToOrder = new JButton("QUAY LẠI HÓA ĐƠN");
    btnBackToOrder.setFont(new Font("Arial", Font.PLAIN, 14));

    editTabPanel.add(lblEditTitle);
    editTabPanel.add(new JLabel("Ghi chú món ăn (VD: Không hành, ít đá...):"));
    editTabPanel.add(txtEditNote);
    editTabPanel.add(new JLabel("Số lượng:"));
    editTabPanel.add(txtEditQuantity);
    
    JPanel editButtonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
    editButtonPanel.add(btnBackToOrder);
    editButtonPanel.add(btnSaveItem);
    editTabPanel.add(editButtonPanel);

    // --- QUAN TRỌNG: Nạp 2 màn hình vào rightPanel với tên mã ---
    rightPanel.add(orderTabPanel, "BILL_PANEL");
    rightPanel.add(editTabPanel, "EDIT_PANEL");

    // Gắn vào cửa sổ chính
    add(rightPanel, BorderLayout.EAST);

    // Bắt sự kiện cho các nút bấm
    btnThanhToan.addActionListener(e -> handleCheckout());
    btnThanhToanQR.addActionListener(e -> handleQRCheckout());
    btnHuyDon.addActionListener(e -> resetPOS());
    
    btnSaveItem.addActionListener(e -> handleSaveEditedItem());
    // Sự kiện nút Quay lại trượt về màn hình Hóa đơn
    btnBackToOrder.addActionListener(e -> cardLayout.show(rightPanel, "BILL_PANEL"));
    }
    public static POSView getInstance() {
        return instance;
    }
    // 4. Hàm nhận tín hiệu báo xong từ Bếp
    public void notifyOrderReady(String orderId) {
        // Hiển thị một bảng thông báo Popup nổi lên trên màn hình Thu ngân
        JOptionPane.showMessageDialog(this, 
            "🔔 TINH TINH!\nBếp vừa báo: Đơn hàng [" + orderId + "] ĐÃ NẤU XONG.\nVui lòng gọi khách nhận đồ!", 
            "THÔNG BÁO TỪ NHÀ BẾP", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    // --- KẾT NỐI DATABASE LẤY MENU ---
    private void loadMenuFromDatabase() {
        menuList = new ArrayList<>();
        String query = "SELECT ProductID, ProductName, Price, Status FROM Product";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String id = rs.getString("ProductID");
                String name = rs.getString("ProductName");
                double price = rs.getDouble("Price");
                String status = rs.getString("Status");
                menuList.add(new Product(id, name, "", price, status));
            }
        } catch (Exception e) {
            System.out.println("Lỗi kết nối SQL, sử dụng menu mẫu dự phòng: " + e.getMessage());
            // Dữ liệu mẫu dự phòng khi chưa bật SQL
            menuList.add(new Product("P01", "Burger Bò Phô Mai", "", 45000, "Đang kinh doanh"));
            menuList.add(new Product("P02", "Gà Rán (1 miếng)", "", 35000, "Đang kinh doanh"));
            menuList.add(new Product("P03", "Khoai Tây Chiên", "", 20000, "Đang kinh doanh"));
            menuList.add(new Product("P04", "Trà Sữa Trân Châu", "", 30000, "Đang kinh doanh"));
            menuList.add(new Product("P05", "Nước ngọt Pepsi", "", 15000, "Đang kinh doanh"));
            menuList.add(new Product("P06", "Combo Bò + Khoai", "", 60000, "Đang kinh doanh"));
        }
    }

    // --- CÁC HÀM XỬ LÝ NGHIỆP VỤ ---

        private void handleSelectProduct(Product p) {
            if (orderCtrl.getCurrentOrder() == null || !orderCtrl.getCurrentOrder().getStatus().equals("Chờ thanh toán")) {
                orderCtrl.createNewOrder("Ăn tại chỗ");
            }

            boolean isExist = false;

            // 1. Duyệt danh sách món trong đơn
            for (OrderLineItem item : orderCtrl.getCurrentOrder().getLineItems()) {
                if (item.getProduct().getProductName().trim().equalsIgnoreCase(p.getProductName().trim())) {

                    // Chỉ cần tăng số lượng, tiền sẽ tự động được getSubTotal() tính lại!
                    item.setQuantity(item.getQuantity() + 1);

                    isExist = true;
                    break; 
                }
            }

            // 2. Nếu món chưa từng có trong đơn -> Thêm dòng mới
            if (!isExist) {
                orderCtrl.addProductToOrder(p, 1, ""); 
            }

            // 3. Làm mới bảng hiển thị (tổng tiền sẽ tự động nhảy số)
            refreshBillTable();
        }

   private void refreshBillTable() {
        if (orderCtrl.getCurrentOrder() != null && tableModel != null) {
            tableModel.setRowCount(0); // Xóa sạch bảng cũ
            
            for (OrderLineItem item : orderCtrl.getCurrentOrder().getLineItems()) {
                String productName = item.getProduct().getProductName();
                String note = item.getNote();
                
                // --- XỬ LÝ HTML ĐỂ HIỆN GHI CHÚ XUỐNG DÒNG ---
                String displayHtml;
                if (note != null && !note.trim().isEmpty()) {
                    displayHtml = "<html><b>" + productName + "</b><br><font color='gray' size='3'><i>Ghi chú: " + note + "</i></font></html>";
                } else {
                    displayHtml = "<html><b>" + productName + "</b></html>";
                }

                Object[] row = {
                    displayHtml,                  
                    item.getQuantity(),           
                    item.getProduct().getPrice(), 
                    item.getSubTotal()            
                };
                tableModel.addRow(row);
            }
            
            lblTotal.setText("Tổng cộng: " + orderCtrl.getOrderTotal() + "đ");
            
            // Ép giao diện vẽ lại ngay lập tức
            tableModel.fireTableDataChanged();
            billTable.revalidate();
            billTable.repaint();
        }
    }
   
    private void handleCheckout() {
        if (orderCtrl.getCurrentOrder() != null && orderCtrl.getOrderTotal() > 0) {
            double total = orderCtrl.getOrderTotal();
            
            String input = JOptionPane.showInputDialog(this, "TỔNG TIỀN: " + total + "đ\nNhập số tiền khách đưa:");
            if (input != null && !input.isEmpty()) {
                try {
                    double cash = Double.parseDouble(input);
                    if (cash >= total) {
                        // 1. Lưu đơn vào SQL
                        paymentCtrl.processCashPayment(orderCtrl.getCurrentOrder(), cash);
                        
                        JOptionPane.showMessageDialog(this, "THANH TOÁN THÀNH CÔNG!\nTiền thừa trả khách: " + (cash - total) + "đ\n(Đơn đã đẩy xuống bếp)");
                        
                        // 2. SỬA Ở ĐÂY: Đánh thức màn hình bếp lấy dữ liệu mới từ SQL
                        if (boundary.KitchenView.getInstance() != null) {
                            boundary.KitchenView.getInstance().refreshKitchenBoard();
                        }
                        
                        resetPOS();
                    } else {
                        JOptionPane.showMessageDialog(this, "LỖI: Khách đưa thiếu tiền!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chỉ nhập số!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Chưa có món nào trong đơn!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
    }

   private void handleQRCheckout() {
        if (orderCtrl.getCurrentOrder() != null && orderCtrl.getOrderTotal() > 0) {
            double total = orderCtrl.getOrderTotal();
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Hệ thống đã sinh mã QR với số tiền: " + total + "đ\n\nKhách hàng đã quét mã và chuyển khoản thành công chưa?", 
                "CỔNG THANH TOÁN QR", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.INFORMATION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                // 1. Lưu đơn vào SQL
                paymentCtrl.processQRPayment(orderCtrl.getCurrentOrder());
                
                JOptionPane.showMessageDialog(this, "GIAO DỊCH QR THÀNH CÔNG!\n(Đơn đã đẩy xuống bếp)");
                
                // 2. SỬA Ở ĐÂY: Đánh thức màn hình bếp lấy dữ liệu mới từ SQL
                if (boundary.KitchenView.getInstance() != null) {
                    boundary.KitchenView.getInstance().refreshKitchenBoard();
                }
                
                resetPOS();
            } else {
                JOptionPane.showMessageDialog(this, "Giao dịch QR đã bị hủy hoặc chưa nhận được tiền.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Chưa có món nào trong đơn!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void resetPOS() {
        orderCtrl.createNewOrder("Ăn tại chỗ");
        refreshBillTable();
        lblTotal.setText("Tổng cộng: 0đ");
    }
    private void handleEditItemNote(int rowIndex) {
        if (orderCtrl.getCurrentOrder() != null) {
            OrderLineItem item = orderCtrl.getCurrentOrder().getLineItems().get(rowIndex);
            
            // Lấy ghi chú cũ (nếu có) để hiển thị sẵn lên ô nhập
            String oldNote = item.getNote(); 
            
            String newNote = JOptionPane.showInputDialog(this, 
                "Nhập/Sửa ghi chú cho món [" + item.getProduct().getProductName() + "]:", 
                oldNote);
                
            if (newNote != null) {
                item.setNote(newNote); // Cập nhật ghi chú mới vào đối tượng
                refreshBillTable();    // Vẽ lại bảng để hiển thị ghi chú mới
            }
        }
    }
    private void handleSaveEditedItem() {
        if (editingItem != null) {
            try {
                // 1. Cập nhật ghi chú mới từ ô text
                String newNote = txtEditNote.getText();
                editingItem.setNote(newNote);
                
                // 2. Cập nhật số lượng
                int newQty = Integer.parseInt(txtEditQuantity.getText().trim());
                if (newQty > 0) {
                    editingItem.setQuantity(newQty);
                } else {
                    JOptionPane.showMessageDialog(this, "Số lượng phải > 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // 3. QUAN TRỌNG: Gọi làm mới bảng và quay lại Tab Hóa đơn
                refreshBillTable();
                cardLayout.show(rightPanel, "BILL_PANEL");
                
                editingItem = null;
                
                // 4. Xóa trạng thái món đang sửa
                editingItem = null; 
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số lượng hợp lệ!", "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}   