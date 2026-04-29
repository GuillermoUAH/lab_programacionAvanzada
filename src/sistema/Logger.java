// sistema/Logger.java
package sistema;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

public class Logger {

    private static Logger instancia;
    private static final ReentrantLock lockInstancia = new ReentrantLock();

    private BufferedWriter writer;
    private final ReentrantLock lock = new ReentrantLock();
    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Logger() {
        try {
            writer = new BufferedWriter(new FileWriter("hawkins.txt", false));
        } catch (IOException e) {
            System.err.println("Error al abrir hawkins.txt: " + e.getMessage());
        }
    }

    public static Logger getInstancia() {
        if (instancia == null) {
            lockInstancia.lock();
            try {
                if (instancia == null) instancia = new Logger();
            } finally {
                lockInstancia.unlock();
            }
        }
        return instancia;
    }

    public void log(String mensaje) {
        String linea = "[" + LocalDateTime.now().format(fmt) + "] " + mensaje;
        System.out.println(linea);
        lock.lock();
        try {
            writer.write(linea);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error al escribir log: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void cerrar() {
        lock.lock();
        try {
            if (writer != null) writer.close();
        } catch (IOException e) {
            System.err.println("Error al cerrar hawkins.txt: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }
}