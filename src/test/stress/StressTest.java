package test.stress;

import eventos.TipoEvento;
import modelo.Nino;
import org.junit.jupiter.api.*;
import portales.Portal;
import sistema.EstadoGlobal;
import zonas.Colmena;
import zonas.ZonaUpsideDown;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de estrés calibrados para que cada uno termine en menos de 40s.
 * El cruce del Portal es de 1s fijo (Thread.sleep del enunciado), por lo
 * que los números de niños están ajustados para caber en ese presupuesto.
 */
class StressTest {

    @AfterEach
    void cleanup() {
        EstadoGlobal.getInstancia().desactivarEvento();
        EstadoGlobal.getInstancia().reanudar();
    }

    @Test
    @Timeout(40)
    @DisplayName("ESTRÉS 1: 20 niños cruzan y vuelven sin deadlock")
    void testIdaYVueltaMasiva() throws Exception {
        ZonaUpsideDown zona = new ZonaUpsideDown("TEST_ZONA");
        Portal portal = new Portal("STRESS", zona, 4);

        int N = 20; // 5 grupos de 4
        CountDownLatch done = new CountDownLatch(N);

        for (int i = 0; i < N; i++) {
            Nino nino = new Nino(String.format("N%04d", i));
            new Thread(() -> {
                try {
                    portal.cruzarHaciaUpsideDown(nino);
                    Thread.sleep(ThreadLocalRandom.current().nextInt(50, 200));
                    portal.cruzarHaciaHawkins(nino);
                    done.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        assertTrue(done.await(38, TimeUnit.SECONDS),
                "Los 20 niños deben completar ida y vuelta sin deadlock");

        assertEquals(0, portal.getEsperandoIda());
        assertEquals(0, portal.getEsperandoVuelta());
        assertEquals(0, zona.getNumNinos(),
                "Todos los niños deben haber salido de la zona");
    }

    @Test
    @Timeout(40)
    @DisplayName("ESTRÉS 2: 12 niños con apagones intermitentes")
    void testApagonesIntermitentes() throws Exception {
        EstadoGlobal eg = EstadoGlobal.getInstancia();
        ZonaUpsideDown zona = new ZonaUpsideDown("APAGONES");
        Portal portal = new Portal("STRESS_APAGON", zona, 3);

        int N = 12; // 4 grupos
        CountDownLatch done = new CountDownLatch(N);

        AtomicInteger seguir = new AtomicInteger(1);
        Thread gestor = new Thread(() -> {
            try {
                while (seguir.get() == 1) {
                    eg.activarEvento(TipoEvento.APAGON_LABORATORIO, 600);
                    Thread.sleep(600);
                    eg.desactivarEvento();
                    portal.desbloquear();
                    Thread.sleep(400);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        gestor.setDaemon(true);
        gestor.start();

        for (int i = 0; i < N; i++) {
            Nino nino = new Nino(String.format("N%04d", i));
            new Thread(() -> {
                try {
                    portal.cruzarHaciaUpsideDown(nino);
                    Thread.sleep(100);
                    portal.cruzarHaciaHawkins(nino);
                    done.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        assertTrue(done.await(38, TimeUnit.SECONDS),
                "Todos los niños deben completar el ciclo pese a los apagones");

        seguir.set(0);
        eg.desactivarEvento();
        portal.desbloquear();
    }

    @Test
    @Timeout(30)
    @DisplayName("ESTRÉS 3: pausa/reanuda no deja hilos colgados")
    void testPausaReanudaEstres() throws Exception {
        EstadoGlobal eg = EstadoGlobal.getInstancia();

        int N = 50;
        CountDownLatch iteraciones = new CountDownLatch(N * 10);
        AtomicInteger seguir = new AtomicInteger(1);

        for (int i = 0; i < N; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < 10 && seguir.get() == 1; j++) {
                        eg.checkPausa();
                        Thread.sleep(10);
                        iteraciones.countDown();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        for (int i = 0; i < 5; i++) {
            Thread.sleep(50);
            eg.pausar();
            Thread.sleep(50);
            eg.reanudar();
        }

        assertTrue(iteraciones.await(20, TimeUnit.SECONDS),
                "Ningún hilo debe quedarse colgado tras pausar/reanudar");
    }

    @Test
    @Timeout(45)
    @DisplayName("ESTRÉS 4: Colmena con depósitos y liberaciones concurrentes")
    void testColmenaConcurrente() throws Exception {
        Colmena colmena = new Colmena();
        int NDEPOSITOS = 500;
        int NLIBERACIONES = 20;

        CountDownLatch deposDone = new CountDownLatch(NDEPOSITOS);

        ExecutorService exDepositar = Executors.newFixedThreadPool(20);
        for (int i = 0; i < NDEPOSITOS; i++) {
            final int id = i;
            exDepositar.submit(() -> {
                Nino n = new Nino(String.format("N%04d", id));
                n.capturar();
                colmena.depositar(n);
                deposDone.countDown();
            });
        }

        ExecutorService exLiberar = Executors.newFixedThreadPool(5);
        for (int i = 0; i < NLIBERACIONES; i++) {
            exLiberar.submit(() -> {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(10, 100));
                    colmena.liberarNinos(ThreadLocalRandom.current().nextInt(10, 50));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        assertTrue(deposDone.await(30, TimeUnit.SECONDS));
        exDepositar.shutdown();
        exLiberar.shutdown();
        assertTrue(exLiberar.awaitTermination(10, TimeUnit.SECONDS));

        int restantes = colmena.getNumCapturados();
        assertTrue(restantes >= 0 && restantes <= NDEPOSITOS);
    }

    @Test
    @Timeout(15)
    @DisplayName("ESTRÉS 5: 4 demogorgons simultáneos atacando en la misma zona")
    void testMultiplesDemogorgons() throws Exception {
        ZonaUpsideDown zona = new ZonaUpsideDown("MASS_ATTACK");
        int NNINOS = 30;
        List<Nino> ninos = new ArrayList<>();
        for (int i = 0; i < NNINOS; i++) {
            Nino n = new Nino(String.format("N%04d", i));
            ninos.add(n);
            zona.entrarNino(n);
        }

        AtomicInteger capturados = new AtomicInteger(0);
        AtomicInteger dobleCaptura = new AtomicInteger(0);

        int NDEMO = 4;
        CountDownLatch done = new CountDownLatch(NDEMO);

        for (int i = 0; i < NDEMO; i++) {
            new Thread(() -> {
                try {
                    for (int j = 0; j < 10; j++) { // 10 intentos por demogorgon
                        Nino objetivo = zona.getNinoAleatorio();
                        if (objetivo == null) break;

                        if (!objetivo.isCapturado()) {
                            Thread.sleep(ThreadLocalRandom.current().nextInt(5, 20));

                            synchronized (objetivo) {
                                if (!objetivo.isCapturado()) {
                                    zona.salirNino(objetivo);
                                    objetivo.capturar();
                                    capturados.incrementAndGet();
                                } else {
                                    dobleCaptura.incrementAndGet();
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }

        assertTrue(done.await(10, TimeUnit.SECONDS),
                "Los 4 demogorgons deben terminar sus intentos");

        assertEquals(0, dobleCaptura.get(),
                "No debe haber capturas dobles");
        assertTrue(capturados.get() > 0, "Debe haber capturas");
        assertTrue(capturados.get() <= NNINOS,
                "No se pueden capturar más niños de los que hay");
    }

    @Test
    @Timeout(10)
    @DisplayName("ESTRÉS 6: sangre suma 1500 sin pérdidas bajo carga máxima")
    void testSangreBajoCarga() throws Exception {
        EstadoGlobal eg = EstadoGlobal.getInstancia();
        eg.consumirSangreParaEleven();

        int N = 1500;
        ExecutorService ex = Executors.newFixedThreadPool(100);
        CountDownLatch done = new CountDownLatch(N);

        for (int i = 0; i < N; i++) {
            ex.submit(() -> {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(1, 5));
                    eg.aniadirSangre(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        assertTrue(done.await(8, TimeUnit.SECONDS));
        ex.shutdown();

        assertEquals(N, eg.getSangreTotal(),
                "1500 incrementos atómicos = 1500");
    }
}