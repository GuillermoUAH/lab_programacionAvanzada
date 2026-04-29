package test.sistema;

import org.junit.jupiter.api.*;
import sistema.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del Logger: singleton, escritura atómica, formato de línea
 * y resistencia bajo carga concurrente.
 */
class LoggerTest {

    @Test
    @DisplayName("Singleton: getInstancia() devuelve siempre la misma instancia")
    void testSingleton() {
        Logger a = Logger.getInstancia();
        Logger b = Logger.getInstancia();
        assertSame(a, b, "Logger debería ser singleton");
    }

    @Test
    @DisplayName("Singleton bajo concurrencia: todas las llamadas devuelven la misma instancia")
    void testSingletonConcurrente() throws Exception {
        int N = 50;
        ExecutorService ex = Executors.newFixedThreadPool(N);
        Set<Logger> instancias = ConcurrentHashMap.newKeySet();
        CountDownLatch start = new CountDownLatch(1);

        for (int i = 0; i < N; i++) {
            ex.submit(() -> {
                try {
                    start.await();
                    instancias.add(Logger.getInstancia());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        start.countDown();
        ex.shutdown();
        assertTrue(ex.awaitTermination(5, TimeUnit.SECONDS));

        assertEquals(1, instancias.size(),
                "Todas las llamadas concurrentes deberían devolver la misma instancia");
    }

    @Test
    @DisplayName("Formato: cada línea empieza con [yyyy-MM-dd HH:mm:ss]")
    void testFormatoLinea() throws IOException {
        Logger log = Logger.getInstancia();
        log.log("Mensaje de prueba del formato");

        // Dar tiempo al flush
        try { Thread.sleep(50); } catch (InterruptedException e) {}

        List<String> lineas = Files.readAllLines(Paths.get("hawkins.txt"));
        assertFalse(lineas.isEmpty(), "hawkins.txt debería tener al menos una línea");

        String ultima = lineas.get(lineas.size() - 1);
        assertTrue(ultima.matches("^\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\] .*"),
                "La línea debería empezar con [yyyy-MM-dd HH:mm:ss]: " + ultima);
    }

    @Test
    @DisplayName("Escritura concurrente: 100 hilos × 20 mensajes = 2000 líneas en hawkins.txt")
    void testEscrituraConcurrente() throws Exception {
        Logger log = Logger.getInstancia();
        int NHILOS = 100;
        int MENSAJES_POR_HILO = 20;
        int total = NHILOS * MENSAJES_POR_HILO;

        // Marca de inicio para distinguir líneas de este test
        String marca = "LOG_STRESS_" + System.nanoTime() + "_";
        AtomicInteger contador = new AtomicInteger(0);

        ExecutorService ex = Executors.newFixedThreadPool(NHILOS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(NHILOS);

        for (int i = 0; i < NHILOS; i++) {
            ex.submit(() -> {
                try {
                    start.await();
                    for (int j = 0; j < MENSAJES_POR_HILO; j++) {
                        log.log(marca + contador.incrementAndGet());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertTrue(done.await(20, TimeUnit.SECONDS), "Todos los hilos deben terminar");
        ex.shutdown();

        // Dar tiempo al último flush
        Thread.sleep(100);

        List<String> lineas = Files.readAllLines(Paths.get("hawkins.txt"));
        long contieneMarca = lineas.stream().filter(l -> l.contains(marca)).count();
        assertEquals(total, contieneMarca,
                "Deberían aparecer exactamente " + total + " líneas con la marca");

        // Verificar que ninguna línea está entrelazada (el separador [ - ] debe aparecer al principio)
        lineas.stream().filter(l -> l.contains(marca)).forEach(l ->
                assertTrue(l.matches("^\\[\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\] .*"),
                        "Línea corrupta (entrelazada): " + l));
    }
}