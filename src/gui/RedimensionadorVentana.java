package gui;

import java.awt.*;
import java.awt.event.*;

public class RedimensionadorVentana extends MouseAdapter {

    private static final int MARGEN = 6;

    private final Window ventana;
    private Rectangle bounds;
    private Point puntoInicio;
    private int direccion;

    private static final int N  = 1, S  = 2, E  = 4, O  = 8;
    private static final int NE = N|E, NO = N|O, SE = S|E, SO = S|O;

    public RedimensionadorVentana(Window ventana) {
        this.ventana = ventana;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        direccion = getDireccion(e.getPoint(), ventana.getSize());
        ventana.setCursor(Cursor.getPredefinedCursor(getCursor(direccion)));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        puntoInicio = e.getLocationOnScreen();
        bounds      = ventana.getBounds();
        direccion   = getDireccion(e.getPoint(), ventana.getSize());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (puntoInicio == null || direccion == 0) return;

        Point actual = e.getLocationOnScreen();
        int dx = actual.x - puntoInicio.x;
        int dy = actual.y - puntoInicio.y;

        int nx = bounds.x, ny = bounds.y;
        int nw = bounds.width, nh = bounds.height;
        Dimension min = ventana.getMinimumSize();

        if ((direccion & E) != 0) nw = Math.max(min.width,  bounds.width  + dx);
        if ((direccion & S) != 0) nh = Math.max(min.height, bounds.height + dy);
        if ((direccion & O) != 0) {
            nw = Math.max(min.width,  bounds.width  - dx);
            nx = bounds.x + bounds.width - nw;
        }
        if ((direccion & N) != 0) {
            nh = Math.max(min.height, bounds.height - dy);
            ny = bounds.y + bounds.height - nh;
        }

        ventana.setBounds(nx, ny, nw, nh);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        puntoInicio = null;
        ventana.setCursor(Cursor.getDefaultCursor());
    }

    private int getDireccion(Point p, Dimension size) {
        int d = 0;
        if (p.x <= MARGEN)            d |= O;
        if (p.x >= size.width - MARGEN)  d |= E;
        if (p.y <= MARGEN)            d |= N;
        if (p.y >= size.height - MARGEN) d |= S;
        return d;
    }

    private int getCursor(int dir) {
        return switch (dir) {
            case N  -> Cursor.N_RESIZE_CURSOR;
            case S  -> Cursor.S_RESIZE_CURSOR;
            case E  -> Cursor.E_RESIZE_CURSOR;
            case O  -> Cursor.W_RESIZE_CURSOR;
            case NE -> Cursor.NE_RESIZE_CURSOR;
            case NO -> Cursor.NW_RESIZE_CURSOR;
            case SE -> Cursor.SE_RESIZE_CURSOR;
            case SO -> Cursor.SW_RESIZE_CURSOR;
            default -> Cursor.DEFAULT_CURSOR;
        };
    }
}