package test.sistema;

import eventos.EventoGlobal;
import eventos.TipoEvento;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de los tipos de evento y el EventoGlobal.
 * El GestorEventos no se testea aquí porque es un hilo daemon
 * con tiempos de 30-60s, inviable para tests unitarios.
 */
class EventoGlobalTest {

    @Test
    @DisplayName("TipoEvento: los 4 tipos del enunciado están definidos")
    void testTiposEvento() {
        assertEquals(4, TipoEvento.values().length);
        assertNotNull(TipoEvento.valueOf("APAGON_LABORATORIO"));
        assertNotNull(TipoEvento.valueOf("TORMENTA_UPSIDE_DOWN"));
        assertNotNull(TipoEvento.valueOf("INTERVENCION_ELEVEN"));
        assertNotNull(TipoEvento.valueOf("RED_MENTAL"));
    }

    @Test
    @DisplayName("EventoGlobal: tiempo restante decreciente")
    void testTiempoRestante() throws InterruptedException {
        EventoGlobal e = new EventoGlobal(TipoEvento.TORMENTA_UPSIDE_DOWN, 2000);
        long t1 = e.getTiempoRestanteMs();
        assertTrue(t1 > 1500 && t1 <= 2000);

        Thread.sleep(300);
        long t2 = e.getTiempoRestanteMs();
        assertTrue(t2 < t1);
        assertFalse(e.haTerminado());
    }

    @Test
    @DisplayName("EventoGlobal: haTerminado() cuando se agota la duración")
    void testHaTerminado() throws InterruptedException {
        EventoGlobal e = new EventoGlobal(TipoEvento.RED_MENTAL, 100);
        assertFalse(e.haTerminado());
        Thread.sleep(200);
        assertTrue(e.haTerminado());
        assertEquals(0, e.getTiempoRestanteMs());
    }

    @Test
    @DisplayName("EventoGlobal: toString contiene el tipo")
    void testToString() {
        EventoGlobal e = new EventoGlobal(TipoEvento.APAGON_LABORATORIO, 5000);
        assertTrue(e.toString().contains("APAGON_LABORATORIO"));
    }
}