// eventos/GestorEventos.java
package eventos;

import sistema.EstadoGlobal;
import sistema.Logger;
import portales.Portal;
import static java.lang.Thread.sleep;

public class GestorEventos extends Thread {

    public GestorEventos() {
    }

    @Override
    public void run() {
        EstadoGlobal eg = EstadoGlobal.getInstancia();
        Logger log = Logger.getInstancia();

        try {
            while (true) {
                eg.checkPausa();

                // Esperar entre 30 y 60 segundos antes del siguiente evento
                int espera = 30000 + (int)(Math.random() * 30000);
                sleep(espera);

                eg.checkPausa();

                // Elegir evento aleatorio
                TipoEvento[] tipos = TipoEvento.values();
                TipoEvento tipo = tipos[(int)(Math.random() * tipos.length)];

                // Duración entre 5 y 10 segundos
                long duracion = 5000 + (int)(Math.random() * 5000);

                eg.activarEvento(tipo, duracion);

                log.log("EVENTO GLOBAL: " + nombreEvento(tipo) + " iniciado");

                // Para Eleven: anotar cuánta sangre había ANTES de que empiece el evento.
                // Al terminar se librarán tantos niños como sangre se haya recogido DURANTE el evento.
                int sangreAntesEleven = (tipo == TipoEvento.INTERVENCION_ELEVEN) ? eg.getSangreTotal() : 0;

                // Esperar a que termine el evento
                sleep(duracion);

                eg.desactivarEvento();
                log.log("EVENTO GLOBAL: " + nombreEvento(tipo) + " finalizado");

                // Si era apagón, desbloquear portales
                if (tipo == TipoEvento.APAGON_LABORATORIO) {
                    for (Portal p : eg.getPortales()) {
                        p.desbloquear();
                    }
                }

                // Eleven: contar solo la sangre recogida DURANTE el evento (no la anterior)
                if (tipo == TipoEvento.INTERVENCION_ELEVEN) {
                    int sangreRecogida = eg.getSangreTotal() - sangreAntesEleven;
                    if (sangreRecogida > 0) {
                        eg.getColmena().liberarNinos(sangreRecogida);
                        log.log("Eleven libera " + sangreRecogida + " niños de la COLMENA");
                    } else {
                        log.log("Eleven interviene pero no se recogió sangre durante el evento");
                    }
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String nombreEvento(TipoEvento tipo) {
        return switch (tipo) {
            case APAGON_LABORATORIO  -> "Apagón del Laboratorio";
            case TORMENTA_UPSIDE_DOWN -> "Tormenta del Upside Down";
            case INTERVENCION_ELEVEN  -> "Intervención de Eleven";
            case RED_MENTAL           -> "La Red Mental";
        };
    }
}