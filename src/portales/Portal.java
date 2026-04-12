// portales/Portal.java
package portales;

import modelo.Nino;
import sistema.EstadoGlobal;
import sistema.Logger;
import zonas.ZonaUpsideDown;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Portal {

    private final String nombre;
    private final ZonaUpsideDown zonaDestino;
    private final int tamanoGrupo;

    private final ReentrantLock lock = new ReentrantLock(true); // fair
    private final Condition condGrupoIda      = lock.newCondition();
    private final Condition condGrupoVuelta   = lock.newCondition();
    private final Condition condCruce         = lock.newCondition();

    // Niños esperando para entrar al Upside Down
    private int esperandoIda     = 0;
    // Niños esperando para volver a Hawkins
    private int esperandoVuelta  = 0;
    // Si alguien está cruzando ahora mismo
    private boolean cruzando     = false;

    // Grupo de ida formándose
    private int grupoActualIda   = 0;
    // Generación del grupo (para que grupos distintos no se mezclen)
    private int generacionGrupo  = 0;

    public Portal(String nombre, ZonaUpsideDown zonaDestino, int tamanoGrupo) {
        this.nombre      = nombre;
        this.zonaDestino = zonaDestino;
        this.tamanoGrupo = tamanoGrupo;
    }

    /**
     * Un niño cruza desde Hawkins al Upside Down.
     * Espera a formar grupo del tamaño requerido.
     * Si hay niños esperando para volver, ellos tienen prioridad.
     */
    public void cruzarHaciaUpsideDown(Nino nino) throws InterruptedException {
        lock.lock();
        try {
            // Esperar si portales bloqueados (apagón)
            while (EstadoGlobal.getInstancia().isPortalesBloqueados()) {
                condGrupoIda.await();
            }

            // Unirse al grupo actual
            esperandoIda++;
            int miGeneracion = generacionGrupo;

            // Esperar hasta que el grupo esté completo Y no haya nadie volviendo
            while (grupoActualIda < tamanoGrupo - 1
                    || esperandoVuelta > 0
                    || miGeneracion != generacionGrupo
                    || EstadoGlobal.getInstancia().isPortalesBloqueados()) {

                if (grupoActualIda == 0 && miGeneracion != generacionGrupo) {
                    // Nuestro grupo ya pasó, unirnos al siguiente
                    miGeneracion = generacionGrupo;
                }
                grupoActualIda++;
                if (grupoActualIda >= tamanoGrupo) {
                    // Grupo completo, avisar
                    grupoActualIda = 0;
                    generacionGrupo++;
                    condGrupoIda.signalAll();
                    break;
                }
                condGrupoIda.await();
                while (EstadoGlobal.getInstancia().isPortalesBloqueados()) {
                    condGrupoIda.await();
                }
            }

            // Esperar turno para cruzar uno a uno
            while (cruzando || esperandoVuelta > 0) {
                condCruce.await();
            }
            cruzando = true;
            esperandoIda--;

        } finally {
            lock.unlock();
        }

        // Cruzar (1 segundo)
        Logger.getInstancia().log("El niño " + nino.getIdHawkins()
                + " cruza el portal hacia " + nombre);
        Thread.sleep(1000);
        zonaDestino.entrarNino(nino);

        lock.lock();
        try {
            cruzando = false;
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

            // Esperar turno para cruzar (prioridad sobre ida)
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
        Thread.sleep(1000);

        lock.lock();
        try {
            cruzando = false;
            // Avisar primero a los que vuelven, luego a los de ida
            if (esperandoVuelta > 0) {
                condCruce.signalAll();
            } else {
                condCruce.signalAll();
                condGrupoIda.signalAll(); // desbloquear ida si el apagón terminó
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
        try { condGrupoIda.signalAll(); }
        finally { lock.unlock(); }
    }

    public String getNombre()          { return nombre; }
    public ZonaUpsideDown getZonaDestino() { return zonaDestino; }
    public int getEsperandoIda()       {
        lock.lock();
        try { return esperandoIda; }
        finally { lock.unlock(); }
    }
    public int getEsperandoVuelta()    {
        lock.lock();
        try { return esperandoVuelta; }
        finally { lock.unlock(); }
    }
}