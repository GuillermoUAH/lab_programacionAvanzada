// zonas/ZonaHawkins.java
package zonas;

import modelo.Nino;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZonaHawkins {

    private final String nombre;
    private final List<Nino> ninos = new ArrayList<>();
    private final Object lock = new Object();

    public ZonaHawkins(String nombre) {
        this.nombre = nombre;
    }

    public void entrar(Nino nino) {
        synchronized (lock) {
            ninos.add(nino);
        }
    }

    public void salir(Nino nino) {
        synchronized (lock) {
            ninos.remove(nino);
        }
    }

    public List<Nino> getNinos() {
        synchronized (lock) {
            return Collections.unmodifiableList(new ArrayList<>(ninos));
        }
    }

    public int getNumNinos() {
        synchronized (lock) {
            return ninos.size();
        }
    }

    public String getNombre() { return nombre; }
}