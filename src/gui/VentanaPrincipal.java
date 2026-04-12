package gui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class VentanaPrincipal extends JFrame {

    // ── Zonas Hawkins
    private PanelZona panelCallePrincipal;
    private PanelZona panelSotanoByers;
    private PanelZonaRadio panelRadioWSQK;

    // ── Portales
    private PanelPortal panelPortal1;
    private PanelPortal panelPortal2;
    private PanelPortal panelPortal3;
    private PanelPortal panelPortal4;

    // ── Upside Down
    private PanelZonaUD panelBosque;
    private PanelZonaUD panelLaboratorio;
    private PanelZonaUD panelCentroComercial;
    private PanelZonaUD panelAlcantarillado;

    // ── Colmena
    private JLabel labelColmenaNum;

    // ── Info global
    private JLabel  labelEvento;
    private JButton botonPausa;

    // Maximizar ventana
    private boolean maximizado = false;
    private Rectangle boundsAntes;

    // ── Paleta
    static final Color BG_OUTER    = new Color(28,  38,  22);
    static final Color BG_INNER    = new Color(18,  28,  14);
    static final Color BG_ZONE     = new Color(22,  40,  18);
    static final Color BG_SUBZONE  = new Color(14,  26,  12);
    static final Color GREEN_HI    = new Color(120, 200, 100);
    static final Color GREEN_MID   = new Color(80,  150, 60);
    static final Color GREEN_DIM   = new Color(45,  90,  35);
    static final Color GREEN_FAINT = new Color(25,  50,  18);
    static final Color AMBER_HI    = new Color(200, 160, 50);
    static final Color RED_HI      = new Color(200, 50,  40);
    static final Color RED_DIM     = new Color(100, 20,  15);

    static final Font FONT_TITLE   = new Font("Monospaced", Font.BOLD,  14);
    static final Font FONT_ZONE    = new Font("Monospaced", Font.ITALIC, 11);
    static final Font FONT_IDS     = new Font("Monospaced", Font.BOLD,  12);
    static final Font FONT_SMALL   = new Font("Monospaced", Font.PLAIN, 10);
    static final Font FONT_VERTICAL= new Font("Monospaced", Font.BOLD,  11);
    static final Font FONT_BIG_NUM = new Font("Monospaced", Font.BOLD,  36);

    public VentanaPrincipal() {
        setTitle("STRANGER THINGS — La Batalla de Hawkins");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(BG_OUTER);

        add(buildTitleBar(), BorderLayout.NORTH);
        add(buildMain(),     BorderLayout.CENTER);

        setMinimumSize(new Dimension(800, 500));
        setSize(900, 560);
        setLocationRelativeTo(null);
        setResizable(true);
    }

    // ════════════════════════════════════════════════════════
    //  BARRA DE TÍTULO personalizada
    // ════════════════════════════════════════════════════════

    private JPanel buildTitleBar() {
        JPanel bar = getJPanel();

        // Icono + título
        JLabel titulo = new JLabel("▓▒░  STRANGER THINGS");;
        titulo.setForeground(GREEN_HI);
        titulo.setFont(FONT_TITLE);
        bar.add(titulo, BorderLayout.WEST);
        return bar;
    }

    private static JPanel getJPanel() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 32, 16));
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 10, 16, 16);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, GREEN_DIM),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        return bar;
    }

    private JButton winBtn(String txt, Color color,
                           java.awt.event.ActionListener al) {
        JButton b = new JButton(txt);
        b.setFont(new Font("Monospaced", Font.PLAIN, 12));
        b.setForeground(color);
        b.setBackground(new Color(0,0,0,0));
        b.setOpaque(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(al);
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setForeground(color);
            }
        });
        return b;
    }

    // ════════════════════════════════════════════════════════
    //  Metodo maximizado de ventata
    // ════════════════════════════════════════════════════════

    private void toggleMaximizar() {
        if (maximizado) {
            setBounds(boundsAntes);
            maximizado = false;
        } else {
            boundsAntes = getBounds();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle pantalla = ge.getMaximumWindowBounds(); // respeta la taskbar
            setBounds(pantalla);
            maximizado = true;
        }
    }

    // ════════════════════════════════════════════════════════
    //  PANEL PRINCIPAL — 3 columnas
    // ════════════════════════════════════════════════════════

    private JPanel buildMain() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weighty = 1.0;
        g.insets = new Insets(0, 0, 0, 0);

        // Columna Hawkins ~28%
        g.gridx = 0; g.weightx = 0.28;
        p.add(buildColHawkins(), g);

        // Columna Portales ~22% (con etiqueta vertical)
        g.gridx = 1; g.weightx = 0.22;
        p.add(buildColPortales(), g);

        // Columna Upside Down ~35% (con etiqueta vertical)
        g.gridx = 2; g.weightx = 0.35;
        p.add(buildColUpsideDown(), g);

        // Colmena ~15%
        g.gridx = 3; g.weightx = 0.15;
        p.add(buildColmena(), g);

        return p;
    }

    // ════════════════════════════════════════════════════════
    //  COLUMNA HAWKINS
    // ════════════════════════════════════════════════════════

    private JPanel buildColHawkins() {
        JPanel col = new JPanel(new GridBagLayout());
        col.setOpaque(false);
        col.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));

        panelCallePrincipal = new PanelZona("CALLE PRINCIPAL");
        panelSotanoByers    = new PanelZona("SOTANO BYERS");
        panelRadioWSQK      = new PanelZonaRadio();

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.BOTH;
        g.weightx = 1.0; g.insets = new Insets(0, 0, 4, 0);

        g.gridy = 0; g.weighty = 0;
        col.add(labelIzq("HAWKINS"), g);

        g.weighty = 1.0;
        g.gridy = 1; col.add(panelCallePrincipal, g);
        g.gridy = 2; col.add(panelSotanoByers,    g);
        g.gridy = 3; col.add(panelRadioWSQK,      g);

        return col;
    }

    // ════════════════════════════════════════════════════════
    //  COLUMNA PORTALES
    // ════════════════════════════════════════════════════════

    private JPanel buildColPortales() {
        JPanel wrap = new JPanel(new BorderLayout(0, 0));
        wrap.setOpaque(false);

        wrap.add(labelVertical("P\nO\nR\nT\nA\nL\nE\nS"), BorderLayout.WEST);

        JPanel col = new JPanel(new GridBagLayout());
        col.setOpaque(false);
        col.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

        panelPortal1 = new PanelPortal();
        panelPortal2 = new PanelPortal();
        panelPortal3 = new PanelPortal();
        panelPortal4 = new PanelPortal();

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.BOTH;
        g.weightx = 1.0; g.weighty = 1.0;
        g.insets = new Insets(3, 0, 3, 0); // más espacio entre portales

        g.gridy = 0; col.add(panelPortal1, g);
        g.gridy = 1; col.add(panelPortal2, g);
        g.gridy = 2; col.add(panelPortal3, g);
        g.gridy = 3; col.add(panelPortal4, g);

        wrap.add(col, BorderLayout.CENTER);
        return wrap;
    }

    // ════════════════════════════════════════════════════════
    //  COLUMNA UPSIDE DOWN
    // ════════════════════════════════════════════════════════

    private JPanel buildColUpsideDown() {
        JPanel wrap = new JPanel(new BorderLayout(0, 0));
        wrap.setOpaque(false);

        wrap.add(labelVertical("U\nP\nS\nI\nD\nE\n \nD\nO\nW\nN"), BorderLayout.WEST);

        panelBosque          = new PanelZonaUD("BOSQUE");
        panelLaboratorio     = new PanelZonaUD("LABORATORIO");
        panelCentroComercial = new PanelZonaUD("CENTRO COMERCIAL");
        panelAlcantarillado  = new PanelZonaUD("ALCANTARILLADO");

        JPanel grid = new JPanel(new GridLayout(2, 2, 8, 8)); // gap aumentado
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        grid.add(panelBosque);
        grid.add(panelLaboratorio);
        grid.add(panelCentroComercial);
        grid.add(panelAlcantarillado);

        wrap.add(grid, BorderLayout.CENTER);
        return wrap;
    }

    // ════════════════════════════════════════════════════════
    //  COLMENA
    // ════════════════════════════════════════════════════════

    private JPanel buildColmena() {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 12, 10));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(RED_DIM);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel titulo = new JLabel("COLMENA", SwingConstants.CENTER);
        titulo.setForeground(GREEN_MID);
        titulo.setFont(FONT_ZONE);
        p.add(titulo, BorderLayout.NORTH);

        JLabel subtitulo = new JLabel("NIÑOS", SwingConstants.CENTER);
        subtitulo.setForeground(GREEN_MID);
        subtitulo.setFont(FONT_ZONE);
        p.add(subtitulo, BorderLayout.CENTER);

        labelColmenaNum = new JLabel("0", SwingConstants.CENTER);
        labelColmenaNum.setForeground(RED_HI);
        labelColmenaNum.setFont(FONT_BIG_NUM);
        p.add(labelColmenaNum, BorderLayout.SOUTH);

        return p;
    }

    // ════════════════════════════════════════════════════════
    //  HELPERS
    // ════════════════════════════════════════════════════════

    private JLabel labelIzq(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(GREEN_HI);
        l.setFont(new Font("Monospaced", Font.ITALIC, 13));
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return l;
    }

    private JPanel labelVertical(String letras) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));

        // Panel wrapper para centrar verticalmente
        JPanel centrado = new JPanel(new GridBagLayout());
        centrado.setOpaque(false);

        JPanel letrasPanel = new JPanel();
        letrasPanel.setOpaque(false);
        letrasPanel.setLayout(new BoxLayout(letrasPanel, BoxLayout.Y_AXIS));

        for (char c : letras.toCharArray()) {
            JLabel l = new JLabel(String.valueOf(c));
            l.setForeground(GREEN_DIM);
            l.setFont(FONT_VERTICAL);
            l.setAlignmentX(Component.CENTER_ALIGNMENT);
            letrasPanel.add(l);
        }

        centrado.add(letrasPanel); // GridBagLayout centra por defecto
        return centrado;
    }

    private boolean pausado = false;

    // ════════════════════════════════════════════════════════
    //  API PÚBLICA
    // ════════════════════════════════════════════════════════

    public void actualizarZona(String zona, java.util.List<String> ninos,
                               java.util.List<String> demos) {
        SwingUtilities.invokeLater(() -> {
            switch (zona) {
                case "CALLE_PRINCIPAL"  -> panelCallePrincipal.setIds(ninos);
                case "SOTANO_BYERS"     -> panelSotanoByers.setIds(ninos);
                case "RADIO_WSQK"       -> panelRadioWSQK.setIds(ninos);
                case "BOSQUE"           -> panelBosque.setContenido(ninos, demos);
                case "LABORATORIO"      -> panelLaboratorio.setContenido(ninos, demos);
                case "CENTRO_COMERCIAL" -> panelCentroComercial.setContenido(ninos, demos);
                case "ALCANTARILLADO"   -> panelAlcantarillado.setContenido(ninos, demos);
            }
        });
    }

    public void actualizarPortal(int n, java.util.List<String> entrando,
                                 java.util.List<String> saliendo) {
        SwingUtilities.invokeLater(() -> {
            PanelPortal pp = switch (n) {
                case 1 -> panelPortal1;
                case 2 -> panelPortal2;
                case 3 -> panelPortal3;
                default -> panelPortal4;
            };
            pp.setContenido(entrando, saliendo);
        });
    }

    public void actualizarSangre(int cantidad) {
        SwingUtilities.invokeLater(() -> panelRadioWSQK.setSangre(cantidad));
    }

    public void actualizarColmena(int cantidad) {
        SwingUtilities.invokeLater(() ->
                labelColmenaNum.setText(String.valueOf(cantidad)));
    }

    public void actualizarEvento(String desc) {
        SwingUtilities.invokeLater(() -> {
            if (labelEvento != null) {
                labelEvento.setText(desc);
            }
        });
    }
}