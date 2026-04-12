// zonas/Colmena.java
package zonas;

import modelo.Nino;
import sistema.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Colmena {

    private final List<Nino> ninosCapturados = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void depositar(Nino nino) {
        lock.lock();
        try {
            ninosCapturados.add(nino);
            Logger.getInstancia().log("El niño " + nino.getIdHawkins() + " ha sido depositado en la COLMENA");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Eleven libera 'cantidad' niños capturados.
     * Los niños liberados vuelven a la Calle Principal.
     */
    public void liberarNinos(int cantidad) {
        lock.lock();
        try {
            int aLiberar = Math.min(cantidad, ninosCapturados.size());
            for (int i = 0; i < aLiberar; i++) {
                Nino nino = ninosCapturados.remove(0);
                nino.liberar(); // el niño reanuda su ciclo desde Calle Principal
                Logger.getInstancia().log("Eleven libera al niño " + nino.getIdHawkins());
            }
        } finally {
            lock.unlock();
        }
    }

    public int getNumCapturados() {
        lock.lock();
        try { return ninosCapturados.size(); }
        finally { lock.unlock(); }
    }
}