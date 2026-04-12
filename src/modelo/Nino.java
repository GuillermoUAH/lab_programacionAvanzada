// distribuido/Nino.java
package modelo;

import sistema.EstadoGlobal;
import sistema.Logger;
import portales.Portal;
import zonas.ZonaUpsideDown;

import java.util.Random;

public class Nino extends Thread {

    private final String id;
    private volatile boolean capturado = false;
    private final Object lockCaptura = new Object();
    private static final Random rnd = new Random();

    public Nino(String id) {
        this.id = id;
    }

    @Override
    public void run() {
        EstadoGlobal eg = EstadoGlobal.getInstancia();
        Logger log = Logger.getInstancia();

        try {
            // Empezamos en Calle Principal
            eg.getCallePrincipal().entrar(this);
            log.log("El niño " + id + " llega a CALLE PRINCIPAL");

            while (true) {
                eg.checkPausa();

                // ── 1. SÓTANO BYERS ──────────────────────────────────────
                eg.getCallePrincipal().salir(this);
                eg.getSotanoByers().entrar(this);
                log.log("El niño " + id + " entra al SÓTANO BYERS");
                Thread.sleep(1000 + rnd.nextInt(1000)); // 1-2 seg
                eg.checkPausa();

                // ── 2. ELEGIR PORTAL Y ESPERAR GRUPO ─────────────────────
                int portalIdx = rnd.nextInt(4);
                Portal portal = eg.getPortal(portalIdx);
                eg.getSotanoByers().salir(this);
                log.log("El niño " + id + " espera en portal " + portal.getNombre());
                portal.cruzarHaciaUpsideDown(this);
                eg.checkPausa();

                // ── 3. UPSIDE DOWN: recolectar sangre ─────────────────────
                ZonaUpsideDown zona = portal.getZonaDestino();
                log.log("El niño " + id + " explora " + zona.getNombre());

                // Tiempo base 3-5 seg, x2 si hay tormenta
                int tiempoBase = 3000 + rnd.nextInt(2000);
                int tiempoRecoleccion = eg.isTormentaActiva() ? tiempoBase * 2 : tiempoBase;
                Thread.sleep(tiempoRecoleccion);

                // Si fue capturado durante el sleep, esperar liberación
                if (capturado) {
                    synchronized (lockCaptura) {
                        while (capturado) lockCaptura.wait();
                    }
                    // Eleven nos libera en Calle Principal, reiniciar ciclo
                    eg.getCallePrincipal().entrar(this);
                    log.log("El niño " + id + " regresa a CALLE PRINCIPAL tras ser liberado");
                    Thread.sleep(3000 + rnd.nextInt(2000)); // deambular 3-5 seg
                    continue;
                }

                // Recolectar sangre
                eg.añadirSangre(1);
                log.log("El niño " + id + " recoge 1 unidad de sangre en " + zona.getNombre());
                eg.checkPausa();

                // ── 4. VOLVER POR EL PORTAL ───────────────────────────────
                log.log("El niño " + id + " regresa al portal " + portal.getNombre());
                portal.cruzarHaciaHawkins(this);
                eg.checkPausa();

                // ── 5. RADIO WSQK ─────────────────────────────────────────
                eg.getRadioWSQK().entrar(this);
                log.log("El niño " + id + " descansa en RADIO WSQK");
                Thread.sleep(2000 + rnd.nextInt(2000)); // 2-4 seg
                eg.getRadioWSQK().salir(this);
                eg.checkPausa();

                // ── 6. CALLE PRINCIPAL ────────────────────────────────────
                eg.getCallePrincipal().entrar(this);
                log.log("El niño " + id + " deambula por CALLE PRINCIPAL");
                Thread.sleep(3000 + rnd.nextInt(2000)); // 3-5 seg
                eg.checkPausa();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void liberar() {
        synchronized (lockCaptura) {
            capturado = false;
            lockCaptura.notifyAll();
        }
    }

    public void serCapturado() throws InterruptedException {
        synchronized (lockCaptura) {
            capturado = true;
            Logger.getInstancia().log("El niño " + id + " ha sido capturado");
            while (capturado) lockCaptura.wait();
        }
    }

    public boolean isCapturado() { return capturado; }
    public String getIdHawkins() { return id; }
}