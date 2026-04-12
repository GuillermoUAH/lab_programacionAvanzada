package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PanelZonaRadio extends JPanel {

    private final JTextArea areaIds;
    private final JLabel    labelSangreNum;

    public PanelZonaRadio() {
        setLayout(new BorderLayout(0, 4));
        setOpaque(false);

        JPanel box = new JPanel(new BorderLayout(0, 6)) {
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

        // Título
        JLabel lbl = new JLabel("RADIO WSQK  📡");
        lbl.setForeground(VentanaPrincipal.GREEN_MID);
        lbl.setFont(new Font("Monospaced", Font.ITALIC, 11));
        box.add(lbl, BorderLayout.NORTH);

        // Área niños
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

        // Panel sangre separado abajo
        JPanel panelSangre = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(35, 5, 5));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(VentanaPrincipal.RED_HI);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 6, 6);
                g2.dispose();
            }
        };
        panelSangre.setOpaque(false);
        panelSangre.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JLabel labelSangreTxt = new JLabel("SANGRE");
        labelSangreTxt.setForeground(VentanaPrincipal.RED_HI);
        labelSangreTxt.setFont(new Font("Monospaced", Font.BOLD, 10));
        panelSangre.add(labelSangreTxt, BorderLayout.WEST);

        labelSangreNum = new JLabel("0", SwingConstants.RIGHT);
        labelSangreNum.setForeground(VentanaPrincipal.RED_HI);
        labelSangreNum.setFont(new Font("Monospaced", Font.BOLD, 14));
        panelSangre.add(labelSangreNum, BorderLayout.EAST);

        box.add(panelSangre, BorderLayout.SOUTH);
        add(box, BorderLayout.CENTER);
    }

    public void setIds(List<String> ids) {
        areaIds.setText(ids != null && !ids.isEmpty()
                ? String.join(", ", ids) : "");
    }

    public void setSangre(int cantidad) {
        labelSangreNum.setText(String.valueOf(cantidad));
    }
}