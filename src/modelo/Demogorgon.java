// modelo/Demogorgon.java
package modelo;

import sistema.EstadoGlobal;
import sistema.Logger;
import zonas.ZonaUpsideDown;
import zonas.Colmena;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.List;

public class Demogorgon extends Thread {

    private final String id;
    private int capturas = 0;
    private ZonaUpsideDown zonaActual;

    public Demogorgon(String id) {
        this.id = id;
    }

    @Override
    public void run() {
        EstadoGlobal eg = EstadoGlobal.getInstancia();
        Logger log = Logger.getInstancia();

        try {
            // Empezar en una zona aleatoria
            zonaActual = zonaAleatoria(eg);
            zonaActual.entrarDemogorgon(this);
            log.log("El demogorgon " + id + " aparece en " + zonaActual.getNombre());

            while (true) {
                eg.checkPausa();

                // ── Paralizado por Eleven ─────────────────────────────────
                while (eg.isDemogorgonsParalizados()) {
                    sleep(200);
                    eg.checkPausa();
                }

                // ── Red Mental: ir a la zona con más niños ────────────────
                if (eg.isRedMentalActiva()) {
                    ZonaUpsideDown zonaMasNinos = getZonaConMasNinos(eg);
                    if (!zonaMasNinos.getNombre().equals(zonaActual.getNombre())) {
                        moverseA(zonaMasNinos, log);
                    }
                }

                Nino objetivo = zonaActual.getNinoAleatorio();

                if (objetivo != null && !objetivo.isCapturado()) {
                    // ── ATACAR ────────────────────────────────────────────
                    // Avisar al niño: queda bloqueado hasta que el ataque termine
                    objetivo.iniciarAtaque();
                    log.log("El demogorgon " + id + " ataca al niño "
                            + objetivo.getIdHawkins() + " (capturas: " + capturas + ")");

                    // Duración del ataque: 0.5 - 1.5 seg (x0.5 si tormenta)
                    int base = 500 + (int)(Math.random() * 1000);
                    int duracion = eg.isTormentaActiva() ? base / 2 : base;
                    sleep(duracion);

                    eg.checkPausa();
                    while (eg.isDemogorgonsParalizados()) {
                        sleep(200);
                        eg.checkPausa();
                    }

                    // Probabilidad de captura: 1/3
                    boolean capturado = (int)(Math.random() * 3) == 0;

                    if (capturado && !objetivo.isCapturado()) {
                        // ── CAPTURA EXITOSA ───────────────────────────────
                        zonaActual.salirNino(objetivo);
                        Colmena colmena = eg.getColmena();

                        // Llevar a la colmena: 0.5 - 1 seg
                        sleep(500 + (int)(Math.random() * 500));
                        colmena.depositar(objetivo);
                        objetivo.capturar(); // bloquea al niño en el ciclo de captura

                        capturas++;
                        log.log("El demogorgon " + id + " captura al niño "
                                + objetivo.getIdHawkins() + " (capturas: " + capturas + ")");

                        eg.notificarCaptura();
                    } else {
                        log.log("El niño " + objetivo.getIdHawkins()
                                + " resiste el ataque de " + id);
                    }

                    // Ataque terminado: desbloquear al niño (haya sido capturado o no)
                    objetivo.finalizarAtaque();

                } else {
                    // ── SIN OBJETIVO: esperar 4-5 seg ─────────────────────
                    log.log("El demogorgon " + id + " patrulla " + zonaActual.getNombre()
                            + " (vacío)");
                    sleep(4000 + (int)(Math.random() * 1000));
                }

                eg.checkPausa();

                // ── Moverse a zona aleatoria (si no está paralizado ni red mental) ──
                if (!eg.isDemogorgonsParalizados() && !eg.isPortalesBloqueados()) {
                    ZonaUpsideDown siguiente = eg.isRedMentalActiva()
                            ? getZonaConMasNinos(eg)
                            : zonaAleatoriaDiferente(eg);
                    moverseA(siguiente, log);
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("InterruptedException en "+id);
        }
    }

    private void moverseA(ZonaUpsideDown nueva, Logger log) {
        if (!nueva.getNombre().equals(zonaActual.getNombre())) {
            zonaActual.salirDemogorgon(this);
            zonaActual = nueva;
            zonaActual.entrarDemogorgon(this);
            log.log("El demogorgon " + id + " se mueve a " + zonaActual.getNombre());
        }
    }

    private ZonaUpsideDown zonaAleatoria(EstadoGlobal eg) {
        ZonaUpsideDown[] zonas = eg.getZonasUpsideDown();
        return zonas[(int)(Math.random() * zonas.length)];
    }

    private ZonaUpsideDown zonaAleatoriaDiferente(EstadoGlobal eg) {
        List<ZonaUpsideDown> otras = new ArrayList<>();
        for (ZonaUpsideDown z : eg.getZonasUpsideDown()) {
            if (!z.getNombre().equals(zonaActual.getNombre())) otras.add(z);
        }
        return otras.get((int)(Math.random() * otras.size()));
    }

    private ZonaUpsideDown getZonaConMasNinos(EstadoGlobal eg) {
        ZonaUpsideDown[] zonas = eg.getZonasUpsideDown();
        ZonaUpsideDown mejor = zonas[0];
        for (ZonaUpsideDown z : zonas) {
            if (z.getNumNinos() > mejor.getNumNinos()) mejor = z;
        }
        return mejor;
    }

    public String getIdHawkins()         { return id; }
    public int getCapturas()             { return capturas; }
    public ZonaUpsideDown getZonaActual() { return zonaActual; }
}