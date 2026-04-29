package test.modelo;

import modelo.Nino;
import org.junit.jupiter.api.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del protocolo captura/liberación de un Nino.
 * No ejecutamos el run() completo (requeriría EstadoGlobal+GUI),
 * probamos únicamente el mecanismo de sincronización.
 */
class NinoTest {

    @Test
    @DisplayName("Estado inicial: no capturado")
    void testEstadoInicial() {
        Nino n = new Nino("N0001");
        assertFalse(n.isCapturado());
    }

    @Test
    @DisplayName("capturar() cambia la flag")
    void testCapturar() {
        Nino n = new Nino("N0001");
        n.capturar();
        assertTrue(n.isCapturado());
    }

    @Test
    @DisplayName("liberar() restaura la flag y despierta wait()")
    void testLiberarDespiertaWait() throws Exception {
        Nino n = new Nino("N0001");
        n.capturar();
        assertTrue(n.isCapturado());

        AtomicBoolean liberado = new AtomicBoolean(false);
        CountDownLatch enWait = new CountDownLatch(1);

        // Hilo que simula el bucle del niño: wait() hasta que se libere
        Thread hiloNino = new Thread(() -> {
            synchronized (getLockCaptura(n)) {
                enWait.countDown();
                try {
                    while (n.isCapturado()) getLockCaptura(n).wait();
                    liberado.set(true);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        hiloNino.start();

        assertTrue(enWait.await(2, TimeUnit.SECONDS));
        Thread.sleep(100); // asegurar que está en wait()
        assertFalse(liberado.get(), "El hilo no debe liberarse antes de llamar a liberar()");

        n.liberar();
        hiloNino.join(2000);
        assertFalse(n.isCapturado());
        assertTrue(liberado.get(), "Tras liberar(), el wait() debe desbloquearse");
    }

    /**
     * Helper para acceder al lockCaptura privado mediante reflexión.
     * En una implementación real este test tocaría la API pública.
     */
    private Object getLockCaptura(Nino n) {
        try {
            var f = Nino.class.getDeclaredField("lockCaptura");
            f.setAccessible(true);
            return f.get(n);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("capturar/liberar múltiples veces no corrompe el estado")
    void testCiclosCapturaLiberar() {
        Nino n = new Nino("N0001");
        for (int i = 0; i < 100; i++) {
            n.capturar();
            assertTrue(n.isCapturado());
            n.liberar();
            assertFalse(n.isCapturado());
        }
    }

    @Test
    @DisplayName("Captura concurrente desde N demogorgons: la flag queda en true")
    void testCapturaConcurrente() throws Exception {
        Nino n = new Nino("N0001");
        int N = 50;
        ExecutorService ex = Executors.newFixedThreadPool(N);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(N);

        for (int i = 0; i < N; i++) {
            ex.submit(() -> {
                try {
                    start.await();
                    n.capturar();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertTrue(done.await(5, TimeUnit.SECONDS));
        ex.shutdown();

        assertTrue(n.isCapturado(), "Tras múltiples capturas concurrentes, capturado=true");
    }
}