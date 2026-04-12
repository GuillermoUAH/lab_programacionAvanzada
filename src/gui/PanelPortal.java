package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PanelPortal extends JPanel {

    private final JTextArea areaIzq;
    private final JTextArea areaDer;

    public PanelPortal() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));

        JPanel box = new JPanel(new GridLayout(1, 2, 4, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(VentanaPrincipal.BG_ZONE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(VentanaPrincipal.GREEN_DIM);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        areaIzq = crearSubarea();
        areaDer = crearSubarea();
        box.add(wrapSubarea(areaIzq));
        box.add(wrapSubarea(areaDer));

        add(box, BorderLayout.CENTER);
    }

    private JTextArea crearSubarea() {
        JTextArea a = new JTextArea();
        a.setEditable(false);
        a.setOpaque(false);
        a.setForeground(VentanaPrincipal.GREEN_HI);
        a.setFont(new Font("Monospaced", Font.BOLD, 11));
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        return a;
    }

    private JPanel wrapSubarea(JTextArea area) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(VentanaPrincipal.BG_SUBZONE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(VentanaPrincipal.GREEN_DIM);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        p.add(area, BorderLayout.CENTER);
        return p;
    }

    public void setContenido(List<String> entrando, List<String> saliendo) {
        areaIzq.setText(entrando != null && !entrando.isEmpty()
                ? String.join("\n", entrando) : "");
        areaDer.setText(saliendo != null && !saliendo.isEmpty()
                ? String.join("\n", saliendo) : "");
    }
}