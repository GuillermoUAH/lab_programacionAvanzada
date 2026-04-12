package gui;

import java.awt.*;
import java.awt.event.*;

public class HacerArrastrable extends MouseAdapter {

    private final Window ventana;
    private Point origen;

    public HacerArrastrable(Window ventana) {
        this.ventana = ventana;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        origen = e.getPoint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (origen != null) {
            Point loc = ventana.getLocation();
            ventana.setLocation(
                    loc.x + e.getX() - origen.x,
                    loc.y + e.getY() - origen.y
            );
        }
    }
}