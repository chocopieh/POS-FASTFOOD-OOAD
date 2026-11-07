package com.mycompany.thucannhanh;

import boundary.POSView;

public class ThucAnNhanh {
    public static void main(String[] args) {
        // Khởi động Cửa sổ giao diện POS (Boundary)
        java.awt.EventQueue.invokeLater(() -> {
            new POSView().setVisible(true);
        });
    }
}