package test.eventos;

import eventos.TipoEvento;
import org.junit.jupiter.api.*;
import sistema.EstadoGlobal;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del EstadoGlobal: inicialización del mundo, atomicidad
 * del contador de sangre, flags de evento y protocolo de pausa.
 */
class EstadoGlobalTest {

    @Test
    @DisplayName("Singleton consistente")
    void testSingleton() {
        EstadoGlobal a = EstadoGlobal.getInstancia();
        EstadoGlobal b = EstadoGlobal.getInstancia();
        assertSame(a, b);
    }

    @Test
    @DisplayName("Inicialización: 3 zonas Hawkins + 4 Upside Down + 4 portales + colmena")
    void testInicializacion() {
        EstadoGlobal eg = EstadoGlobal.getInstancia();

        assertNotNull(eg.getCallePrincipal());
        assertNotNull(eg.getSotanoByers());
        assertNotNull(eg.getRadioWSQK());
        assertEquals("CALLE PRINCIPAL", eg.getCallePrincipal().getNombre());
        assertEquals("SÓTANO BYERS", eg.getSotanoByers().getNombre());
        assertEquals("RADIO WSQK", eg.getRadioWSQK().getNombre());

        assertEquals(4, eg.getZonasUpsideDown().length);
        assertEquals(4, eg.getPortales().length);

        // Verificar tamaños de grupo del enunciado: Bosque=2, Lab=3, CC=4, Alc=2
        assertEquals("BOSQUE",            eg.getPortal(0).getNombre());
        assertEquals("LABORATORIO",       eg.getPortal(1).getNombre());
        assertEquals("CENTRO COMERCIAL",  eg.getPortal(2).getNombre());
        assertEquals("ALCANTARILLADO",    eg.getPortal(3).getNombre());

        assertNotNull(eg.getColmena());
    }

    @Test
    @DisplayName("Sangre: incrementos concurrentes sin pérdidas")
    void testSangreAtomica() throws Exception {
        EstadoGlobal eg = EstadoGlobal.getInstancia();
        int inicial = eg.getSangreTotal();

        int NHILOS = 50;
        int INCREMENTOS = 100;
        ExecutorService ex = Executors.newFixedThreadPool(NHILOS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(NHILOS);

        for (int i = 0; i < NHILOS; i++) {
            ex.submit(() -> {
                try {
                    start.await();
                    for (int j = 0; j < INCREMENTOS; j++) {
                        eg.aniadirSangre(1);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));
        ex.shutdown();

        assertEquals(inicial + NHILOS * INCREMENTOS, eg.getSangreTotal(),
                "AtomicInteger no debe perder incrementos");
    }

    @Test
    @DisplayName("consumirSangreParaEleven deja el contador a 0")
    void testConsumirSangre() {
        EstadoGlobal eg = EstadoGlobal.getInstancia();
        // Poner el contador a un valor conocido
        eg.consumirSangreParaEleven(); // limpia
        eg.aniadirSangre(42);

        int consumida = eg.consumirSangreParaEleven();
        assertEquals(42, consumida);
        assertEquals(0, eg.getSangreTotal());
    }

    @Test
    @DisplayName("Activar evento: flags consistentes (APAGON)")
    void testActivarApagon() {
        EstadoGlobal eg = EstadoGlobal.getInstancia();
        eg.desactivarEvento(); // estado limpio

        assertFalse(eg.isPortalesBloqueados());
        eg.activarEvento(TipoEvento.APAGON_LABORATORIO, 5000);

        assertTrue(eg.isPortalesBloqueados(), "Apagón debe bloquear portales");
        assertEquals(TipoEvento.APAGON_LABORATORIO, eg.getEventoActivo());

        eg.desactivarEvento();
        assertFalse(eg.isPortalesBloqueados());
        assertNull(eg.getEventoActivo());
    }

    @Test
    @DisplayName("Activar evento: flags consistentes (ELEVEN)")
    void testActivarEleven() {
        EstadoGlobal eg = EstadoGlobal.getInstancia();
        eg.desactivarEvento();

        eg.activarEvento(TipoEvento.INTERVENCION_ELEVEN, 5000);
        assertTrue(eg.isElevenActiva());
        assertTrue(eg.isDemogorgonsParalizados(), "Eleven debe paralizar a los demogorgons");

        eg.desactivarEvento();
        assertFalse(eg.isElevenActiva());
        assertFalse(eg.isDemogorgonsParalizados());
    }

    @Test
    @DisplayName("Tiempo restante del evento es decreciente")
    void testTiempoRestante() throws InterruptedException {
        EstadoGlobal eg = EstadoGlobal.getInstancia();
        eg.desactivarEvento();

        eg.activarEvento(TipoEvento.TORMENTA_UPSIDE_DOWN, 2000);
        long t1 = eg.getTiempoRestanteEventoMs();
        Thread.sleep(200);
        long t2 = eg.getTiempoRestanteEventoMs();

        assertTrue(t2 < t1, "El tiempo restante debe ser decreciente");
        assertTrue(t1 <= 2000 && t1 > 1500);

        eg.desactivarEvento();
        assertEquals(0, eg.getTiempoRestanteEventoMs());
    }

    @Test
    @DisplayName("Pausa: un hilo en checkPausa() se bloquea hasta reanudar")
    void testPausaBloquea() throws Exception {
        EstadoGlobal eg = EstadoGlobal.getInstancia();
        eg.pausar();

        AtomicBoolean haPasado = new AtomicBoolean(false);
        Thread t = new Thread(() -> {
            try {
                eg.checkPausa();
                haPasado.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        t.start();

        // Durante la pausa el hilo no debe pasar
        Thread.sleep(200);
        assertFalse(haPasado.get(), "El hilo no debe haber cruzado checkPausa() estando pausado");

        // Reanudar → el hilo desbloquea
        eg.reanudar();
        t.join(1000);
        assertTrue(haPasado.get(), "El hilo debe cruzar checkPausa() tras reanudar");
    }

    @Test
    @DisplayName("Pausa: signalAll despierta a todos los hilos bloqueados")
    void testPausaMultiplesHilos() throws Exception {
        EstadoGlobal eg = EstadoGlobal.getInstancia();
        eg.reanudar(); // estado limpio
        eg.pausar();

        int N = 20;
        CountDownLatch todosPasaron = new CountDownLatch(N);
        for (int i = 0; i < N; i++) {
            new Thread(() -> {
                try {
                    eg.checkPausa();
                    todosPasaron.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        Thread.sleep(300);
        assertEquals(N, todosPasaron.getCount(), "Todos deben estar bloqueados");

        eg.reanudar();
        assertTrue(todosPasaron.await(2, TimeUnit.SECONDS),
                "signalAll debe despertar a todos los hilos");
    }
}