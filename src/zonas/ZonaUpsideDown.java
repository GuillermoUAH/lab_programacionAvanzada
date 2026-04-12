// zonas/ZonaUpsideDown.java
package zonas;

import modelo.Nino;
import modelo.Demogorgon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZonaUpsideDown {

    private final String nombre;
    private final List<Nino> ninos           = new ArrayList<>();
    private final List<Demogorgon> demogorgons = new ArrayList<>();
    private final Object lock = new Object();

    public ZonaUpsideDown(String nombre) {
        this.nombre = nombre;
    }

    // ── Niños ──────────────────────────────────────────────────────
    public void entrarNino(Nino nino) {
        synchronized (lock) { ninos.add(nino); }
    }

    public void salirNino(Nino nino) {
        synchronized (lock) { ninos.remove(nino); }
    }

    public List<Nino> getNinos() {
        synchronized (lock) {
            return Collections.unmodifiableList(new ArrayList<>(ninos));
        }
    }

    public int getNumNinos() {
        synchronized (lock) { return ninos.size(); }
    }

    /**
     * Devuelve un niño aleatorio para que el demogorgon lo ataque.
     * Devuelve null si no hay niños.
     */
    public Nino getNinoAleatorio() {
        synchronized (lock) {
            if (ninos.isEmpty()) return null;
            int idx = (int)(Math.random() * ninos.size());
            return ninos.get(idx);
        }
    }

    // ── Demogorgons ────────────────────────────────────────────────
    public void entrarDemogorgon(Demogorgon d) {
        synchronized (lock) { demogorgons.add(d); }
    }

    public void salirDemogorgon(Demogorgon d) {
        synchronized (lock) { demogorgons.remove(d); }
    }

    public List<Demogorgon> getDemogorgons() {
        synchronized (lock) {
            return Collections.unmodifiableList(new ArrayList<>(demogorgons));
        }
    }

    public int getNumDemogorgons() {
        synchronized (lock) { return demogorgons.size(); }
    }

    public String getNombre() { return nombre; }
}