// portales/Portal.java
package portales;

import modelo.Nino;
import sistema.EstadoGlobal;
import sistema.Logger;
import zonas.ZonaUpsideDown;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import static java.lang.Thread.sleep;

public class Portal {

    private final String nombre;
    private final ZonaUpsideDown zonaDestino;
    private final int tamanoGrupo;

    private final ReentrantLock lock = new ReentrantLock(true); // fair
    private final Condition condIda   = lock.newCondition(); // espera formación/cruce de grupo
    private final Condition condCruce = lock.newCondition(); // espera turno de cruce individual

    // Niños acumulándose para formar el siguiente grupo
    private int esperandoIda    = 0;
    // El grupo está completo (N alcanzados), nadie más puede unirse hasta que todos transicionen
    private boolean grupoCompleto = false;
    // Miembros del grupo actual esperando su turno de cruce
    private int enGrupo         = 0;
    // Alguien cruzando físicamente ahora mismo
    private boolean cruzando    = false;
    // Niños esperando regresar a Hawkins (prioridad)
    private int esperandoVuelta = 0;

    public Portal(String nombre, ZonaUpsideDown zonaDestino, int tamanoGrupo) {
        this.nombre      = nombre;
        this.zonaDestino = zonaDestino;
        this.tamanoGrupo = tamanoGrupo;
    }

    /**
     * Un niño cruza desde Hawkins al Upside Down.
     * Espera a formar un grupo del tamaño requerido (exclusivo).
     * Una vez formado, cruzan uno a uno; los que regresan tienen prioridad.
     */
    public void cruzarHaciaUpsideDown(Nino nino) throws InterruptedException {
        lock.lock();
        try {
            // Fase 1: esperar para poder unirse a la formación del grupo
            // No se puede unir si: hay un grupo cruzando, hay un grupo completo formándose,
            // o los portales están bloqueados (apagón).
            while (enGrupo > 0 || grupoCompleto
                    || EstadoGlobal.getInstancia().isPortalesBloqueados()) {
                condIda.await();
            }

            // Unirse al grupo en formación
            esperandoIda++;

            // Si somos el niño N, el grupo está completo
            if (esperandoIda == tamanoGrupo) {
                grupoCompleto = true;
                condIda.signalAll(); // despertar a los N-1 que esperan
            }

            // Fase 2: esperar a que el grupo esté completo, no haya vuelta pendiente
            // y el portal no esté bloqueado
            while (!grupoCompleto || esperandoVuelta > 0
                    || EstadoGlobal.getInstancia().isPortalesBloqueados()) {
                condIda.await();
            }

            // Transición: pasar de "formando grupo" a "en grupo cruzando"
            esperandoIda--;
            enGrupo++;

            // El último en transicionar resetea grupoCompleto para que se pueda
            // empezar a formar el siguiente (pero enGrupo > 0 lo bloqueará hasta que crucemos)
            if (esperandoIda == 0) {
                grupoCompleto = false;
                condIda.signalAll();
            }

            // Fase 3: esperar turno para cruzar uno a uno (prioridad a vuelta)
            while (cruzando || esperandoVuelta > 0) {
                condCruce.await();
            }
            cruzando = true;
            enGrupo--;

        } finally {
            lock.unlock();
        }

        // Cruzar (1 segundo)
        Logger.getInstancia().log("El niño " + nino.getIdHawkins()
                + " cruza el portal hacia " + nombre);
        sleep(1000);
        zonaDestino.entrarNino(nino);

        lock.lock();
        try {
            cruzando = false;
            if (enGrupo == 0) {
                condIda.signalAll(); // todos cruzaron, el siguiente grupo puede formarse
            }
            condCruce.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Un niño vuelve desde el Upside Down a Hawkins.
     * Tiene prioridad sobre los que van hacia el Upside Down.
     */
    public void cruzarHaciaHawkins(Nino nino) throws InterruptedException {
        lock.lock();
        try {
            zonaDestino.salirNino(nino);
            esperandoVuelta++;

            // Esperar turno (prioridad: solo espera si alguien ya está cruzando)
            while (cruzando) {
                condCruce.await();
            }
            cruzando = true;
            esperandoVuelta--;

        } finally {
            lock.unlock();
        }

        // Cruzar (1 segundo)
        Logger.getInstancia().log("El niño " + nino.getIdHawkins()
                + " regresa por el portal desde " + nombre);
        sleep(1000);

        lock.lock();
        try {
            cruzando = false;
            // Despertar a los que esperan: si hay más vuelta, los primeros en cruzar;
            // si no, despertar también a los de ida para que el grupo pueda proceder
            condCruce.signalAll();
            if (esperandoVuelta == 0) {
                condIda.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Llamado por GestorEventos cuando termina el apagón.
     */
    public void desbloquear() {
        lock.lock();
        try {
            condIda.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public String getNombre()              { return nombre; }
    public ZonaUpsideDown getZonaDestino() { return zonaDestino; }

    public int getEsperandoIda() {
        lock.lock();
        try { return esperandoIda + enGrupo; }
        finally { lock.unlock(); }
    }

    public int getEsperandoVuelta() {
        lock.lock();
        try { return esperandoVuelta; }
        finally { lock.unlock(); }
    }
}