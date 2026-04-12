// sistema/Logger.java
package sistema;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static Logger instancia;
    private static final Object lockInstancia = new Object();

    private BufferedWriter writer;
    private final Object lock = new Object();
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
            synchronized (lockInstancia) {
                if (instancia == null) {
                    instancia = new Logger();
                }
            }
        }
        return instancia;
    }

    public void log(String mensaje) {
        String linea = "[" + LocalDateTime.now().format(fmt) + "] " + mensaje;
        System.out.println(linea);
        synchronized (lock) {
            try {
                writer.write(linea);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                System.err.println("Error al escribir log: " + e.getMessage());
            }
        }
    }

    public void cerrar() {
        synchronized (lock) {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar hawkins.txt: " + e.getMessage());
            }
        }
    }
}