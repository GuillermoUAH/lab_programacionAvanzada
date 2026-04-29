// modelo/Nino.java
package modelo;

import sistema.EstadoGlobal;
import sistema.Logger;
import portales.Portal;
import zonas.ZonaUpsideDown;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import static java.lang.Thread.sleep;

public class Nino extends Thread {

    private final String id;
    private volatile boolean capturado = false;
    private final ReentrantLock lockCaptura = new ReentrantLock();
    private final Condition condCaptura = lockCaptura.newCondition();

    // Bloqueado mientras el demogorgon realiza su ataque (independiente de si captura o no)
    private boolean bajoAtaque = false;
    private final ReentrantLock lockAtaque = new ReentrantLock();
    private final Condition condAtaque = lockAtaque.newCondition();

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
                sleep(1000 + (int)(Math.random() * 1000)); // 1-2 seg

                eg.checkPausa();

                // ── 2. ELEGIR PORTAL Y ESPERAR GRUPO ─────────────────────
                int portalIdx = (int)(Math.random() * 4);
                Portal portal = eg.getPortal(portalIdx);
                eg.getSotanoByers().salir(this);
                log.log("El niño " + id + " espera en portal " + portal.getNombre());
                portal.cruzarHaciaUpsideDown(this);

                eg.checkPausa();

                // ── 3. UPSIDE DOWN: recolectar sangre ─────────────────────
                ZonaUpsideDown zona = portal.getZonaDestino();
                log.log("El niño " + id + " explora " + zona.getNombre());

                // Tiempo base 3-5 seg, x2 si hay tormenta
                int tiempoBase = 3000 + (int)(Math.random() * 2000);
                int tiempoRecoleccion = eg.isTormentaActiva() ? tiempoBase * 2 : tiempoBase;
                sleep(tiempoRecoleccion);

                // Esperar a que el demogorgon termine su ataque antes de seguir
                esperarFinAtaque();

                // Si fue capturado durante el ataque, esperar liberación por Eleven
                if (capturado) {
                    lockCaptura.lock();
                    try {
                        while (capturado) condCaptura.await();
                    } finally {
                        lockCaptura.unlock();
                    }
                    // Eleven nos libera en Calle Principal, reiniciar ciclo
                    eg.getCallePrincipal().entrar(this);
                    log.log("El niño " + id + " regresa a CALLE PRINCIPAL tras ser liberado");
                    sleep(3000 + (int)(Math.random() * 2000)); // deambular 3-5 seg
                    continue;
                }

                // Recolectar sangre
                eg.aniadirSangre(1);
                log.log("El niño " + id + " recoge 1 unidad de sangre en " + zona.getNombre());
                eg.checkPausa();

                // ── 4. VOLVER POR EL PORTAL ───────────────────────────────
                log.log("El niño " + id + " regresa al portal " + portal.getNombre());
                portal.cruzarHaciaHawkins(this);
                eg.checkPausa();

                // ── 5. RADIO WSQK ─────────────────────────────────────────
                eg.getRadioWSQK().entrar(this);
                log.log("El niño " + id + " descansa en RADIO WSQK");
                sleep(2000 + (int)(Math.random() * 2000)); // 2-4 seg
                eg.getRadioWSQK().salir(this);
                eg.checkPausa();

                // ── 6. CALLE PRINCIPAL ────────────────────────────────────
                eg.getCallePrincipal().entrar(this);
                log.log("El niño " + id + " deambula por CALLE PRINCIPAL");
                sleep(3000 + (int)(Math.random() * 2000)); // 3-5 seg
                eg.checkPausa();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("InterruptedException en "+id);
        }
    }

    /** El demogorgon llama esto al INICIAR el ataque: el niño se bloquea al terminar su sleep. */
    public void iniciarAtaque() {
        lockAtaque.lock();
        try { bajoAtaque = true; }
        finally { lockAtaque.unlock(); }
    }

    /** El demogorgon llama esto al FINALIZAR el ataque (haya capturado o no). */
    public void finalizarAtaque() {
        lockAtaque.lock();
        try {
            bajoAtaque = false;
            condAtaque.signalAll();
        } finally {
            lockAtaque.unlock();
        }
    }

    /** El niño llama esto tras su sleep en Upside Down: bloquea si el ataque aún no terminó. */
    public void esperarFinAtaque() throws InterruptedException {
        lockAtaque.lock();
        try {
            while (bajoAtaque) condAtaque.await();
        } finally {
            lockAtaque.unlock();
        }
    }

    /** Llamado por el Demogorgon para marcar la captura. */
    public void capturar() {
        lockCaptura.lock();
        try {
            capturado = true;
            Logger.getInstancia().log("El niño " + id + " ha sido capturado");
        } finally {
            lockCaptura.unlock();
        }
    }

    public void liberar() {
        lockCaptura.lock();
        try {
            capturado = false;
            condCaptura.signalAll();
        } finally {
            lockCaptura.unlock();
        }
    }

    public boolean isCapturado() { return capturado; }
    public String getIdHawkins() { return id; }
}