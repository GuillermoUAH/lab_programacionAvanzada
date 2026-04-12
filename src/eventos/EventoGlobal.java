// eventos/EventoGlobal.java
package eventos;

public class EventoGlobal {

    private final TipoEvento tipo;
    private final long duracionMs;
    private final long inicioMs;

    public EventoGlobal(TipoEvento tipo, long duracionMs) {
        this.tipo      = tipo;
        this.duracionMs = duracionMs;
        this.inicioMs  = System.currentTimeMillis();
    }

    public TipoEvento getTipo()     { return tipo; }
    public long getDuracionMs()     { return duracionMs; }
    public long getInicioMs()       { return inicioMs; }

    public long getTiempoRestanteMs() {
        return Math.max(0, (inicioMs + duracionMs) - System.currentTimeMillis());
    }

    public boolean haTerminado() {
        return System.currentTimeMillis() >= inicioMs + duracionMs;
    }

    @Override
    public String toString() {
        return tipo.name() + " (" + (getTiempoRestanteMs() / 1000) + "s restantes)";
    }
}