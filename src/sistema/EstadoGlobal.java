// sistema/EstadoGlobal.java
package sistema;

import modelo.Demogorgon;
import zonas.*;
import portales.Portal;
import eventos.TipoEvento;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class EstadoGlobal {

    private static EstadoGlobal instancia;
    private static final Object lockInstancia = new Object();

    // ── Zonas Hawkins ──────────────────────────────────────────────
    private final ZonaHawkins callePrincipal;
    private final ZonaHawkins sotanoByers;
    private final ZonaHawkins radioWSQK;

    // ── Zonas Upside Down ──────────────────────────────────────────
    private final ZonaUpsideDown bosque;
    private final ZonaUpsideDown laboratorio;
    private final ZonaUpsideDown centroComercial;
    private final ZonaUpsideDown alcantarillado;

    // ── Colmena ────────────────────────────────────────────────────
    private final Colmena colmena;

    // ── Portales (índices: 0=Bosque, 1=Lab, 2=CC, 3=Alc) ──────────
    private final Portal[] portales;

    // ── Sangre de Vecna recolectada ────────────────────────────────
    private final AtomicInteger sangreTotal = new AtomicInteger(0);

    // ── Demogorgons activos ────────────────────────────────────────
    private final List<Demogorgon> demogorgons = new ArrayList<>();
    private final ReentrantLock lockDemogorgons = new ReentrantLock();

    // ── Evento global activo ───────────────────────────────────────
    private TipoEvento eventoActivo = null;
    private long eventoFinMs = 0;
    private final ReentrantLock lockEvento = new ReentrantLock();

    // ── Pausa global ───────────────────────────────────────────────
    private boolean pausado = false;
    private final ReentrantLock lockPausa = new ReentrantLock();
    private final Condition condPausa = lockPausa.newCondition();

    // ── Flags de evento (consultados por hilos) ────────────────────
    private volatile boolean portalesBloqueados = false;   // Apagón
    private volatile boolean tormentaActiva = false;        // Tormenta
    private volatile boolean elevenActiva = false;          // Eleven
    private volatile boolean redMentalActiva = false;       // Red Mental
    private volatile boolean demogorgonsParalizados = false;

    // ── Contador para generar nuevos demogorgons ───────────────────
    private final AtomicInteger capturasSinNuevoDemogorgon = new AtomicInteger(0);

    // ── ID autoincrementable para demogorgons ──────────────────────
    private final AtomicInteger nextDemogorgonId = new AtomicInteger(1);

    // ──────────────────────────────────────────────────────────────
    private EstadoGlobal() {
        callePrincipal  = new ZonaHawkins("CALLE PRINCIPAL");
        sotanoByers     = new ZonaHawkins("SÓTANO BYERS");
        radioWSQK       = new ZonaHawkins("RADIO WSQK");

        bosque          = new ZonaUpsideDown("BOSQUE");
        laboratorio     = new ZonaUpsideDown("LABORATORIO");
        centroComercial = new ZonaUpsideDown("CENTRO COMERCIAL");
        alcantarillado  = new ZonaUpsideDown("ALCANTARILLADO");

        colmena = new Colmena();

        // Tamaños de grupo: Bosque=2, Lab=3, CC=4, Alc=2
        portales = new Portal[]{
                new Portal("BOSQUE",            bosque,          2),
                new Portal("LABORATORIO",       laboratorio,     3),
                new Portal("CENTRO COMERCIAL",  centroComercial, 4),
                new Portal("ALCANTARILLADO",    alcantarillado,  2)
        };
    }

    public static EstadoGlobal getInstancia() {
        if (instancia == null) {
            synchronized (lockInstancia) {
                if (instancia == null) instancia = new EstadoGlobal();
            }
        }
        return instancia;
    }

    // ── Pausa global ───────────────────────────────────────────────
    /**
     * Llamar al inicio de cada "paso" de un hilo.
     * Si el sistema está pausado, el hilo se bloquea aquí hasta que se reanude.
     */
    public void checkPausa() throws InterruptedException {
        lockPausa.lock();
        try {
            while (pausado) condPausa.await();
        } finally {
            lockPausa.unlock();
        }
    }

    public void pausar() {
        lockPausa.lock();
        try { pausado = true; }
        finally { lockPausa.unlock(); }
    }

    public void reanudar() {
        lockPausa.lock();
        try {
            pausado = false;
            condPausa.signalAll();
        } finally {
            lockPausa.unlock();
        }
    }

    public boolean isPausado() {
        lockPausa.lock();
        try { return pausado; }
        finally { lockPausa.unlock(); }
    }

    // ── Sangre ────────────────────────────────────────────────────
    public void añadirSangre(int cantidad) { sangreTotal.addAndGet(cantidad); }
    public int getSangreTotal() { return sangreTotal.get(); }

    /**
     * Evento Eleven: consume toda la sangre acumulada y devuelve cuánta había.
     */
    public int consumirSangreParaEleven() { return sangreTotal.getAndSet(0); }

    // ── Demogorgons ───────────────────────────────────────────────
    public void registrarDemogorgon(Demogorgon d) {
        lockDemogorgons.lock();
        try { demogorgons.add(d); }
        finally { lockDemogorgons.unlock(); }
    }

    public List<Demogorgon> getDemogorgons() {
        lockDemogorgons.lock();
        try { return new ArrayList<>(demogorgons); }
        finally { lockDemogorgons.unlock(); }
    }

    /**
     * Llamado cuando un demogorgon deposita un niño en la colmena.
     * Cada 8 capturas genera un nuevo demogorgon.
     */
    public void notificarCaptura() {
        int total = capturasSinNuevoDemogorgon.incrementAndGet();
        if (total % 8 == 0) {
            generarNuevoDemogorgon();
        }
    }

    private void generarNuevoDemogorgon() {
        int id = nextDemogorgonId.getAndIncrement();
        String idStr = String.format("D%04d", id);
        Demogorgon nuevo = new Demogorgon(idStr);
        registrarDemogorgon(nuevo);
        nuevo.start();
        Logger.getInstancia().log("Vecna genera un nuevo demogorgon: " + idStr);
    }

    public String getSiguienteDemogorgonId() {
        return String.format("D%04d", nextDemogorgonId.getAndIncrement());
    }

    // ── Evento global ─────────────────────────────────────────────
    public void activarEvento(TipoEvento tipo, long duracionMs) {
        lockEvento.lock();
        try {
            eventoActivo = tipo;
            eventoFinMs  = System.currentTimeMillis() + duracionMs;
            aplicarEfectosEvento(tipo, true);
        } finally {
            lockEvento.unlock();
        }
    }

    public void desactivarEvento() {
        lockEvento.lock();
        try {
            if (eventoActivo != null) {
                aplicarEfectosEvento(eventoActivo, false);
                eventoActivo = null;
                eventoFinMs  = 0;
            }
        } finally {
            lockEvento.unlock();
        }
    }

    private void aplicarEfectosEvento(TipoEvento tipo, boolean activar) {
        switch (tipo) {
            case APAGON_LABORATORIO:
                portalesBloqueados      = activar;
                demogorgonsParalizados  = false;
                break;
            case TORMENTA_UPSIDE_DOWN:
                tormentaActiva = activar;
                break;
            case INTERVENCION_ELEVEN:
                elevenActiva            = activar;
                demogorgonsParalizados  = activar;
                break;
            case RED_MENTAL:
                redMentalActiva = activar;
                break;
        }
    }

    public TipoEvento getEventoActivo() {
        lockEvento.lock();
        try { return eventoActivo; }
        finally { lockEvento.unlock(); }
    }

    public long getTiempoRestanteEventoMs() {
        lockEvento.lock();
        try { return Math.max(0, eventoFinMs - System.currentTimeMillis()); }
        finally { lockEvento.unlock(); }
    }

    // ── Getters de flags de evento ────────────────────────────────
    public boolean isPortalesBloqueados()     { return portalesBloqueados; }
    public boolean isTormentaActiva()         { return tormentaActiva; }
    public boolean isElevenActiva()           { return elevenActiva; }
    public boolean isRedMentalActiva()        { return redMentalActiva; }
    public boolean isDemogorgonsParalizados() { return demogorgonsParalizados; }

    // ── Getters de zonas y portales ───────────────────────────────
    public ZonaHawkins  getCallePrincipal()  { return callePrincipal; }
    public ZonaHawkins  getSotanoByers()     { return sotanoByers; }
    public ZonaHawkins  getRadioWSQK()       { return radioWSQK; }

    public ZonaUpsideDown getBosque()          { return bosque; }
    public ZonaUpsideDown getLaboratorio()     { return laboratorio; }
    public ZonaUpsideDown getCentroComercial() { return centroComercial; }
    public ZonaUpsideDown getAlcantarillado()  { return alcantarillado; }

    public ZonaUpsideDown[] getZonasUpsideDown() {
        return new ZonaUpsideDown[]{ bosque, laboratorio, centroComercial, alcantarillado };
    }

    public Colmena  getColmena()         { return colmena; }
    public Portal[] getPortales()        { return portales; }
    public Portal   getPortal(int i)     { return portales[i]; }
}