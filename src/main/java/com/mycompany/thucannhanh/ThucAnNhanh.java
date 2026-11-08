package com.mycompany.thucannhanh;

import boundary.POSView;

public class ThucAnNhanh {
    public static void main(String[] args) {
     // 1. Mở cửa sổ Bếp (KDS)
     new boundary.KitchenView().setVisible(true);

     // 2. Mở cửa sổ Thu ngân (POS)
     new boundary.POSView().setVisible(true);
 }
}