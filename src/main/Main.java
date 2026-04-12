// main/Main.java
package main;

import modelo.Demogorgon;
import modelo.Nino;
import eventos.GestorEventos;
import gui.VentanaPrincipal;
import sistema.EstadoGlobal;
import sistema.Logger;

import javax.swing.*;
import java.util.Random;

public class Main {

    private static final int NUM_NINOS = 1500;
    private static final Random rnd = new Random();

    public static void main(String[] args) {
        EstadoGlobal eg = EstadoGlobal.getInstancia();
        Logger log = Logger.getInstancia();

        // ── GUI ───────────────────────────────────────────────────────
        SwingUtilities.invokeLater(() -> {
            new VentanaPrincipal().setVisible(true);
        });

        // ── Demogorgon Alpha ──────────────────────────────────────────
        Demogorgon alpha = new Demogorgon("D0000");
        eg.registrarDemogorgon(alpha);
        alpha.start();
        log.log("El Demogorgon Alpha (D0000) ha aparecido");

        // ── Gestor de eventos globales ────────────────────────────────
        GestorEventos gestor = new GestorEventos();
        gestor.start();
        log.log("Sistema de eventos globales iniciado");

        // ── Crear niños de forma escalonada ───────────────────────────
        Thread creadorNinos = new Thread(() -> {
            for (int i = 1; i <= NUM_NINOS; i++) {
                try {
                    String idNino = String.format("N%04d", i);
                    Nino nino = new Nino(idNino);
                    nino.start();
                    log.log("El niño " + idNino + " ha llegado a Hawkins");

                    // Intervalo aleatorio entre 0.5 y 2 segundos
                    Thread.sleep(500 + rnd.nextInt(1500));

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            log.log("Todos los niños han sido creados");
        });
        creadorNinos.setDaemon(true);
        creadorNinos.start();
    }
}