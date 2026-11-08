package boundary;

import controller.KitchenController;
import entity.Order;
import entity.OrderLineItem;

import javax.swing.*;
import java.awt.*;

public class KitchenView extends JFrame {
    private JPanel ordersPanel;

    // Biến Singleton để POSView có thể gọi làm mới giao diện Bếp
    private static KitchenView instance; 

    public KitchenView() {
        setTitle("MÀN HÌNH BẾP (KITCHEN DISPLAY SYSTEM)");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Tắt bếp không làm tắt thu ngân
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("DANH SÁCH ĐƠN ĐANG CHỜ CHẾ BIẾN", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setForeground(Color.RED);
        header.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(header, BorderLayout.NORTH);

        // Khu vực chứa các thẻ đơn hàng
        ordersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        add(new JScrollPane(ordersPanel), BorderLayout.CENTER);

        // Đăng ký màn hình này với Controller
        KitchenController.getInstance().setKitchenView(this);
        
        // Gán instance để các form khác gọi được
        instance = this; 
    }

    // Hàm lấy instance tĩnh
    public static KitchenView getInstance() {
        return instance;
    }

    public void refreshKitchenBoard() {
        ordersPanel.removeAll(); // Xóa thẻ cũ
        
        // SỬA ĐỔI 1: Gọi hàm lấy dữ liệu từ SQL
        java.util.List<Order> pending = KitchenController.getInstance().getPendingOrdersFromSQL();

        // Duyệt qua từng đơn hàng đang chờ và vẽ thành 1 cái thẻ vuông
        for (Order order : pending) {
            JPanel card = new JPanel(new BorderLayout());
            card.setPreferredSize(new Dimension(250, 300));
            card.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            card.setBackground(new Color(255, 255, 204)); // Nền vàng nhạt giống giấy note

            JLabel lblTitle = new JLabel("ĐƠN HÀNG: " + order.getOrderId(), SwingConstants.CENTER);
            lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
            lblTitle.setOpaque(true);
            lblTitle.setBackground(Color.ORANGE);
            card.add(lblTitle, BorderLayout.NORTH);

            // In danh sách món và ghi chú
            JTextArea txtItems = new JTextArea();
            txtItems.setEditable(false);
            txtItems.setFont(new Font("Arial", Font.PLAIN, 15));
            StringBuilder sb = new StringBuilder();
            
            for (OrderLineItem item : order.getLineItems()) {
                sb.append(item.getQuantity()).append("x ").append(item.getProduct().getProductName()).append("\n");
                if (item.getNote() != null && !item.getNote().trim().isEmpty()) {
                    sb.append("   -> Ghi chú: ").append(item.getNote()).append("\n");
                }
                sb.append("------------------------\n");
            }
            
            txtItems.setText(sb.toString());
            card.add(new JScrollPane(txtItems), BorderLayout.CENTER);

            // Nút xác nhận làm xong
            JButton btnDone = new JButton("ĐÃ NẤU XONG");
            btnDone.setBackground(new Color(0, 153, 51));
            btnDone.setForeground(Color.WHITE);
            btnDone.setFont(new Font("Arial", Font.BOLD, 14));
            
            // SỬA ĐỔI 2: Gọi hàm UPDATE trạng thái SQL bằng Mã đơn hàng
            btnDone.addActionListener(e -> KitchenController.getInstance().finishOrderInSQL(order.getOrderId()));
            
            card.add(btnDone, BorderLayout.SOUTH);

            ordersPanel.add(card);
        }

        ordersPanel.revalidate();
        ordersPanel.repaint();
    }
}