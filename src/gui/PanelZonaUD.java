package gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PanelZonaUD extends JPanel {

    private final JTextArea areaNinos;
    private final JTextArea areaDemos;

    public PanelZonaUD(String nombre) {
        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel box = new JPanel(new BorderLayout(0, 3)) {
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
        box.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7));

        JLabel lbl = new JLabel(nombre);
        lbl.setForeground(VentanaPrincipal.GREEN_MID);
        lbl.setFont(new Font("Monospaced", Font.ITALIC, 11));
        box.add(lbl, BorderLayout.NORTH);

        // Fila central: niños izq, demogorgon der
        JPanel fila = new JPanel(new GridLayout(1, 2, 4, 0));
        fila.setOpaque(false);

        areaNinos = crearArea(VentanaPrincipal.GREEN_HI);
        areaDemos = crearArea(new Color(200, 100, 80));

        fila.add(wrapArea(areaNinos));
        fila.add(wrapArea(areaDemos));
        box.add(fila, BorderLayout.CENTER);

        add(box, BorderLayout.CENTER);
    }

    private JTextArea crearArea(Color fg) {
        JTextArea a = new JTextArea();
        a.setEditable(false);
        a.setOpaque(false);
        a.setForeground(fg);
        a.setFont(new Font("Monospaced", Font.BOLD, 11));
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        return a;
    }

    private JPanel wrapArea(JTextArea area) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(VentanaPrincipal.BG_SUBZONE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 5, 5);
                g2.setColor(VentanaPrincipal.GREEN_FAINT);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 5, 5);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        p.add(area, BorderLayout.CENTER);
        return p;
    }

    public void setContenido(List<String> ninos, List<String> demos) {
        areaNinos.setText(ninos != null && !ninos.isEmpty()
                ? String.join(", ", ninos) : "");
        areaDemos.setText(demos != null && !demos.isEmpty()
                ? String.join(", ", demos) : "");
    }
}