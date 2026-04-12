package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PanelZona extends JPanel {

    private final JTextArea areaIds;

    public PanelZona(String nombre) {
        setLayout(new BorderLayout(0, 4));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Panel exterior con borde redondeado
        JPanel box = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(VentanaPrincipal.BG_ZONE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(VentanaPrincipal.GREEN_DIM);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        JLabel lbl = new JLabel(nombre);
        lbl.setForeground(VentanaPrincipal.GREEN_MID);
        lbl.setFont(new Font("Monospaced", Font.ITALIC, 11));
        box.add(lbl, BorderLayout.NORTH);

        // Subpanel interior
        JPanel inner = new JPanel(new BorderLayout()) {
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
        inner.setOpaque(false);
        inner.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        areaIds = new JTextArea();
        areaIds.setEditable(false);
        areaIds.setOpaque(false);
        areaIds.setForeground(VentanaPrincipal.GREEN_HI);
        areaIds.setFont(new Font("Monospaced", Font.BOLD, 12));
        areaIds.setLineWrap(true);
        areaIds.setWrapStyleWord(true);
        inner.add(areaIds, BorderLayout.CENTER);
        box.add(inner, BorderLayout.CENTER);

        add(box, BorderLayout.CENTER);
        areaIds.setText("—");
    }

    public void setIds(List<String> ids) {
        areaIds.setText(ids != null && !ids.isEmpty()
                ? String.join(", ", ids) : "");
    }
}