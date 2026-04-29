// zonas/ZonaHawkins.java
package zonas;

import modelo.Nino;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ZonaHawkins {

    private final String nombre;
    private final List<Nino> ninos = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public ZonaHawkins(String nombre) {
        this.nombre = nombre;
    }

    public void entrar(Nino nino) {
        lock.lock();
        try { ninos.add(nino); }
        finally { lock.unlock(); }
    }

    public void salir(Nino nino) {
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

    public String getNombre() { return nombre; }
}