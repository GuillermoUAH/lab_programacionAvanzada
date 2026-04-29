// zonas/ZonaUpsideDown.java
package zonas;

import modelo.Nino;
import modelo.Demogorgon;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ZonaUpsideDown {

    private final String nombre;
    private final List<Nino> ninos            = new ArrayList<>();
    private final List<Demogorgon> demogorgons = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public ZonaUpsideDown(String nombre) {
        this.nombre = nombre;
    }

    // ── Niños ──────────────────────────────────────────────────────
    public void entrarNino(Nino nino) {
        lock.lock();
        try { ninos.add(nino); }
        finally { lock.unlock(); }
    }

    public void salirNino(Nino nino) {
        lock.lock();
        try { ninos.remove(nino); }
        finally { lock.unlock(); }
    }

    public List<Nino> getNinos() {
        lock.lock();
        try { return new ArrayList<>(ninos); }
        finally { lock.unlock(); }
    }

    public int getNumNinos() {
        lock.lock();
        try { return ninos.size(); }
        finally { lock.unlock(); }
    }

    /** Devuelve un niño aleatorio para que el demogorgon lo ataque. Null si no hay niños. */
    public Nino getNinoAleatorio() {
        lock.lock();
        try {
            if (ninos.isEmpty()) return null;
            int idx = (int)(Math.random() * ninos.size());
            return ninos.get(idx);
        } finally {
            lock.unlock();
        }
    }

    // ── Demogorgons ────────────────────────────────────────────────
    public void entrarDemogorgon(Demogorgon d) {
        lock.lock();
        try { demogorgons.add(d); }
        finally { lock.unlock(); }
    }

    public void salirDemogorgon(Demogorgon d) {
        lock.lock();
        try { demogorgons.remove(d); }
        finally { lock.unlock(); }
    }

    public List<Demogorgon> getDemogorgons() {
        lock.lock();
        try { return new ArrayList<>(demogorgons); }
        finally { lock.unlock(); }
    }

    public int getNumDemogorgons() {
        lock.lock();
        try { return demogorgons.size(); }
        finally { lock.unlock(); }
    }

    public String getNombre() { return nombre; }
}