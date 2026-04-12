// eventos/GestorEventos.java
package eventos;

import sistema.EstadoGlobal;
import sistema.Logger;
import portales.Portal;

import java.util.Random;

public class GestorEventos extends Thread {

    private static final Random rnd = new Random();

    public GestorEventos() {
        setDaemon(true); // muere cuando muere el programa
    }

    @Override
    public void run() {
        EstadoGlobal eg = EstadoGlobal.getInstancia();
        Logger log = Logger.getInstancia();

        try {
            while (true) {
                eg.checkPausa();

                // Esperar entre 30 y 60 segundos antes del siguiente evento
                int espera = 30000 + rnd.nextInt(30000);
                Thread.sleep(espera);

                eg.checkPausa();

                // Elegir evento aleatorio
                TipoEvento[] tipos = TipoEvento.values();
                TipoEvento tipo = tipos[rnd.nextInt(tipos.length)];

                // Duración entre 5 y 10 segundos
                long duracion = 5000 + rnd.nextInt(5000);

                EventoGlobal evento = new EventoGlobal(tipo, duracion);
                eg.activarEvento(tipo, duracion);

                log.log("EVENTO GLOBAL: " + nombreEvento(tipo) + " iniciado");

                // Acción inmediata para Eleven
                if (tipo == TipoEvento.INTERVENCION_ELEVEN) {
                    int sangre = eg.consumirSangreParaEleven();
                    if (sangre > 0) {
                        eg.getColmena().liberarNinos(sangre);
                        log.log("Eleven libera " + sangre + " niños de la COLMENA");
                    } else {
                        log.log("Eleven interviene pero no hay sangre acumulada");
                    }
                }

                // Esperar a que termine el evento
                Thread.sleep(duracion);

                eg.desactivarEvento();
                log.log("EVENTO GLOBAL: " + nombreEvento(tipo) + " finalizado");

                // Si era apagón, desbloquear portales
                if (tipo == TipoEvento.APAGON_LABORATORIO) {
                    for (Portal p : eg.getPortales()) {
                        p.desbloquear();
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