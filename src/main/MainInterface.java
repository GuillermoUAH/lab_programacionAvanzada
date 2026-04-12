package main;

import gui.VentanaPrincipal;
import javax.swing.*;

public class MainInterface {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentanaPrincipal().setVisible(true);
        });
    }
}