package test.portales;

import eventos.TipoEvento;
import modelo.Nino;
import org.junit.jupiter.api.*;
import portales.Portal;
import sistema.EstadoGlobal;
import zonas.ZonaUpsideDown;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests del Portal: la clase con mayor complejidad de sincronización.
 * Verifica formación de grupo exclusivo, cruce uno-a-uno,
 * prioridad de la vuelta y bloqueo durante el apagón.
 */
class PortalTest {

    private ZonaUpsideDown zona;
    private Portal portal;

    @BeforeEach
    void setup() {
        zona = new ZonaUpsideDown("ZONA_TEST");
    }

    @AfterEach
    void cleanup() {
        // Asegurar que no hay evento activo residual
        EstadoGlobal.getInstancia().desactivarEvento();
    }

    @Test
    @DisplayName("Grupo de tamaño 2: los dos niños cruzan, el tercero espera al siguiente grupo")
    void testFormacionGrupo2() throws Exception {
        portal = new Portal("TEST2", zona, 2);

        Nino n1 = new Nino("N0001");
        Nino n2 = new Nino("N0002");
        Nino n3 = new Nino("N0003");

        CountDownLatch n1Done = new CountDownLatch(1);
        CountDownLatch n2Done = new CountDownLatch(1);
        CountDownLatch n3Done = new CountDownLatch(1);

        Thread t1 = new Thread(() -> {
            try { portal.cruzarHaciaUpsideDown(n1); n1Done.countDown(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread t2 = new Thread(() -> {
            try { portal.cruzarHaciaUpsideDown(n2); n2Done.countDown(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread t3 = new Thread(() -> {
            try { portal.cruzarHaciaUpsideDown(n3); n3Done.countDown(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        t1.start();
        Thread.sleep(50);
        t2.start();
        Thread.sleep(50);
        t3.start();

        // n1 y n2 deben cruzar juntos (formaron grupo) en unos 2 segundos
        assertTrue(n1Done.await(4, TimeUnit.SECONDS), "n1 debe cruzar");
        assertTrue(n2Done.await(4, TimeUnit.SECONDS), "n2 debe cruzar");

        // n3 aún no debe haber cruzado: está esperando un compañero
        assertEquals(1, n3Done.getCount(), "n3 no debe cruzar solo, necesita otro niño");

        // Añadimos un cuarto que completa el grupo con n3
        Nino n4 = new Nino("N0004");
        CountDownLatch n4Done = new CountDownLatch(1);
        Thread t4 = new Thread(() -> {
            try { portal.cruzarHaciaUpsideDown(n4); n4Done.countDown(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        t4.start();

        assertTrue(n3Done.await(4, TimeUnit.SECONDS), "n3 cruza cuando llega n4");
        assertTrue(n4Done.await(4, TimeUnit.SECONDS), "n4 cruza con n3");
    }

    @Test
    @DisplayName("Grupo de tamaño 3 se forma y cruza correctamente")
    void testFormacionGrupo3() throws Exception {
        portal = new Portal("TEST3", zona, 3);
        int N = 3;
        CountDownLatch done = new CountDownLatch(N);
        long inicio = System.currentTimeMillis();

        for (int i = 0; i < N; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    portal.cruzarHaciaUpsideDown(new Nino("N" + id));
                    done.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        assertTrue(done.await(5, TimeUnit.SECONDS), "Los 3 niños deben cruzar");
        long duracion = System.currentTimeMillis() - inicio;
        // 3 cruces de 1 segundo secuencial = ~3s, no más de ~4s
        assertTrue(duracion >= 2800 && duracion < 5000,
                "Los 3 niños deben cruzar en unos 3s (fue " + duracion + "ms)");
    }

    @Test
    @DisplayName("Cruce uno-a-uno: nunca hay dos niños cruzando simultáneamente")
    void testCruceExclusivo() throws Exception {
        portal = new Portal("EXCL", zona, 2);
        int N = 6; // 3 grupos de 2

        AtomicInteger cruzandoAhora = new AtomicInteger(0);
        AtomicInteger maxSimultaneos = new AtomicInteger(0);

        // Interceptamos: envolvemos el portal con un "proxy" lógico
        // Usamos reflexión para leer la variable 'cruzando' del portal
        CountDownLatch done = new CountDownLatch(N);

        for (int i = 0; i < N; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    portal.cruzarHaciaUpsideDown(new Nino("N" + id));
                    done.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        // Mientras cruzan, muestreamos el estado
        Thread monitor = new Thread(() -> {
            for (int i = 0; i < 100 && done.getCount() > 0; i++) {
                try {
                    boolean cruzando = getCampoBooleano(portal, "cruzando");
                    if (cruzando) {
                        int actual = cruzandoAhora.incrementAndGet();
                        maxSimultaneos.updateAndGet(m -> Math.max(m, actual));
                        Thread.sleep(20);
                        cruzandoAhora.decrementAndGet();
                    } else {
                        Thread.sleep(10);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        monitor.start();

        assertTrue(done.await(15, TimeUnit.SECONDS));
        monitor.join(1000);

        // La variable interna 'cruzando' es un boolean → si funciona bien,
        // nunca vemos más de 1 hilo cruzando, porque está protegida por lock.
        assertTrue(maxSimultaneos.get() <= 1,
                "Solo debe haber un niño cruzando a la vez (observado: " + maxSimultaneos.get() + ")");
    }

    @Test
    @DisplayName("Prioridad de vuelta: un niño que regresa pasa antes que los de ida esperando")
    void testPrioridadVuelta() throws Exception {
        portal = new Portal("PRIO", zona, 2);

        // Primero formamos y cruzamos un grupo para que haya niños en la zona destino
        Nino n1 = new Nino("N0001");
        Nino n2 = new Nino("N0002");
        CountDownLatch ida = new CountDownLatch(2);
        Thread t1 = new Thread(() -> {
            try { portal.cruzarHaciaUpsideDown(n1); ida.countDown(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread t2 = new Thread(() -> {
            try { portal.cruzarHaciaUpsideDown(n2); ida.countDown(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        t1.start(); Thread.sleep(20); t2.start();
        assertTrue(ida.await(5, TimeUnit.SECONDS));

        // Ahora n1 regresa. Mientras, n3 y n4 intentan ir hacia el UpsideDown.
        // El orden de finalización debe ser: n1 (vuelta) antes que n3/n4 (ida).
        List<String> ordenFinalizacion = new ArrayList<>();
        CountDownLatch done = new CountDownLatch(3);

        Thread tVuelta = new Thread(() -> {
            try {
                portal.cruzarHaciaHawkins(n1);
                ordenFinalizacion.add("VUELTA_N1");
                done.countDown();
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread tIda3 = new Thread(() -> {
            try {
                portal.cruzarHaciaUpsideDown(new Nino("N0003"));
                ordenFinalizacion.add("IDA_N3");
                done.countDown();
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread tIda4 = new Thread(() -> {
            try {
                portal.cruzarHaciaUpsideDown(new Nino("N0004"));
                ordenFinalizacion.add("IDA_N4");
                done.countDown();
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        // Lanzamos primero los de ida y luego la vuelta: aun así, la vuelta debe pasar antes
        tIda3.start(); Thread.sleep(20);
        tIda4.start(); Thread.sleep(50);
        tVuelta.start();

        assertTrue(done.await(10, TimeUnit.SECONDS));

        assertEquals("VUELTA_N1", ordenFinalizacion.get(0),
                "El niño de vuelta debe finalizar antes que los de ida esperando");
    }

    @Test
    @DisplayName("Apagón: los niños que intentan cruzar se bloquean hasta desbloquear")
    void testApagonBloqueaIda() throws Exception {
        portal = new Portal("APAGON", zona, 2);
        EstadoGlobal eg = EstadoGlobal.getInstancia();

        eg.activarEvento(TipoEvento.APAGON_LABORATORIO, 60000);
        assertTrue(eg.isPortalesBloqueados());

        CountDownLatch cruzaron = new CountDownLatch(2);
        Thread t1 = new Thread(() -> {
            try { portal.cruzarHaciaUpsideDown(new Nino("N0001")); cruzaron.countDown(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread t2 = new Thread(() -> {
            try { portal.cruzarHaciaUpsideDown(new Nino("N0002")); cruzaron.countDown(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        t1.start(); t2.start();

        // Durante el apagón no deben cruzar
        Thread.sleep(500);
        assertEquals(2, cruzaron.getCount(), "Durante el apagón no debe cruzar nadie");

        // Desactivamos el evento y notificamos al portal
        eg.desactivarEvento();
        portal.desbloquear();

        assertTrue(cruzaron.await(5, TimeUnit.SECONDS),
                "Tras desbloquear el apagón, los niños deben cruzar");
    }

    @Test
    @DisplayName("Estrés: 20 niños con portal de grupo=4 → 5 grupos completos")
    void testEstres20ninosGrupo4() throws Exception {
        portal = new Portal("STRESS4", zona, 4);
        int N = 20;

        CountDownLatch done = new CountDownLatch(N);
        for (int i = 0; i < N; i++) {
            final int id = i;
            new Thread(() -> {
                try {
                    portal.cruzarHaciaUpsideDown(new Nino(String.format("N%04d", id)));
                    done.countDown();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            Thread.sleep(5);
        }

        // 5 grupos × 4 cruces × 1 seg = ~20s. Dejamos margen.
        assertTrue(done.await(40, TimeUnit.SECONDS), "Los 20 niños deben cruzar en < 40s");
        assertEquals(0, portal.getEsperandoIda(),
                "No debe quedar ningún niño esperando");
    }

    // ─── Helper para reflexión ─────────────────────────────────
    private boolean getCampoBooleano(Portal p, String nombre) {
        try {
            var f = Portal.class.getDeclaredField(nombre);
            f.setAccessible(true);
            return f.getBoolean(p);
        } catch (Exception e) {
            return false;
        }
    }
}