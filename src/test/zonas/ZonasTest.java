package test.zonas;

import modelo.Nino;
import modelo.Demogorgon;
import org.junit.jupiter.api.*;
import zonas.Colmena;
import zonas.ZonaHawkins;
import zonas.ZonaUpsideDown;

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de zonas: inserción/extracción thread-safe y consistencia
 * del estado observable.
 */
class ZonasTest {

    @Test
    @DisplayName("ZonaHawkins: entrar/salir coherente")
    void testZonaHawkins() {
        ZonaHawkins zona = new ZonaHawkins("TEST");
        Nino n1 = new Nino("N0001");
        Nino n2 = new Nino("N0002");

        zona.entrar(n1);
        zona.entrar(n2);
        assertEquals(2, zona.getNumNinos());
        assertTrue(zona.getNinos().contains(n1));

        zona.salir(n1);
        assertEquals(1, zona.getNumNinos());
        assertFalse(zona.getNinos().contains(n1));
    }

    @Test
    @DisplayName("ZonaHawkins: getNinos devuelve vista inmutable")
    void testGetNinosInmutable() {
        ZonaHawkins zona = new ZonaHawkins("TEST");
        zona.entrar(new Nino("N0001"));
        List<Nino> ninos = zona.getNinos();
        assertThrows(UnsupportedOperationException.class,
                () -> ninos.add(new Nino("N9999")),
                "La lista devuelta debe ser inmutable");
    }

    @Test
    @DisplayName("ZonaHawkins: entrar/salir concurrente mantiene contador correcto")
    void testZonaHawkinsConcurrente() throws Exception {
        ZonaHawkins zona = new ZonaHawkins("CALLE");
        int N = 500;

        List<Nino> ninos = new ArrayList<>();
        for (int i = 0; i < N; i++) ninos.add(new Nino(String.format("N%04d", i)));

        ExecutorService ex = Executors.newFixedThreadPool(50);
        CountDownLatch done = new CountDownLatch(N);

        for (Nino n : ninos) {
            ex.submit(() -> {
                zona.entrar(n);
                done.countDown();
            });
        }
        assertTrue(done.await(10, TimeUnit.SECONDS));
        assertEquals(N, zona.getNumNinos());

        CountDownLatch doneSalir = new CountDownLatch(N);
        for (Nino n : ninos) {
            ex.submit(() -> {
                zona.salir(n);
                doneSalir.countDown();
            });
        }
        assertTrue(doneSalir.await(10, TimeUnit.SECONDS));
        ex.shutdown();

        assertEquals(0, zona.getNumNinos());
    }

    @Test
    @DisplayName("ZonaUpsideDown: contenedor doble niños + demogorgons")
    void testZonaUpsideDown() {
        ZonaUpsideDown z = new ZonaUpsideDown("BOSQUE");
        Nino n = new Nino("N0001");
        Demogorgon d = new Demogorgon("D0001");

        z.entrarNino(n);
        z.entrarDemogorgon(d);
        assertEquals(1, z.getNumNinos());
        assertEquals(1, z.getNumDemogorgons());

        Nino aleatorio = z.getNinoAleatorio();
        assertNotNull(aleatorio);
        assertEquals("N0001", aleatorio.getIdHawkins());
    }

    @Test
    @DisplayName("ZonaUpsideDown: getNinoAleatorio devuelve null si está vacía")
    void testNinoAleatorioVacia() {
        ZonaUpsideDown z = new ZonaUpsideDown("LAB");
        assertNull(z.getNinoAleatorio());
    }

    @Test
    @DisplayName("ZonaUpsideDown: distribución de getNinoAleatorio no degenera")
    void testNinoAleatorioDistribucion() {
        ZonaUpsideDown z = new ZonaUpsideDown("LAB");
        for (int i = 0; i < 10; i++) z.entrarNino(new Nino("N" + i));

        // 500 extracciones aleatorias deben elegir al menos 5 distintos
        Set<String> distintos = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            distintos.add(z.getNinoAleatorio().getIdHawkins());
        }
        assertTrue(distintos.size() >= 5,
                "La selección aleatoria no debe degenerar (solo eligió " + distintos.size() + ")");
    }

    @Test
    @DisplayName("Colmena: depositar/liberar niños y liberación saturada")
    void testColmenaDepositar() {
        Colmena c = new Colmena();
        Nino n1 = new Nino("N0001");
        Nino n2 = new Nino("N0002");
        c.depositar(n1);
        c.depositar(n2);
        assertEquals(2, c.getNumCapturados());

        // Liberar 5 cuando solo hay 2 → libera solo 2
        c.liberarNinos(5);
        assertEquals(0, c.getNumCapturados());
    }

    @Test
    @DisplayName("Colmena: depósito concurrente")
    void testColmenaDepositoConcurrente() throws Exception {
        Colmena c = new Colmena();
        int N = 200;

        ExecutorService ex = Executors.newFixedThreadPool(20);
        CountDownLatch done = new CountDownLatch(N);

        for (int i = 0; i < N; i++) {
            final int id = i;
            ex.submit(() -> {
                c.depositar(new Nino(String.format("N%04d", id)));
                done.countDown();
            });
        }
        assertTrue(done.await(10, TimeUnit.SECONDS));
        ex.shutdown();
        assertEquals(N, c.getNumCapturados());
    }
}