// main/Main.java
package main;

import modelo.Demogorgon;
import modelo.Nino;
import eventos.GestorEventos;
import gui.VentanaPrincipal;
import sistema.EstadoGlobal;
import sistema.Logger;

import javax.swing.*;
import static java.lang.Thread.sleep;

public class Main {

    private static final int NUM_NINOS = 1500;

    public static void main(String[] args) {
        EstadoGlobal eg = EstadoGlobal.getInstancia();
        Logger log = Logger.getInstancia();

        //Lanzador de la interfaz grafica
        SwingUtilities.invokeLater(() -> {
            new VentanaPrincipal().setVisible(true);
        });

        //Creacion del demogorgon Alpha
        Demogorgon alpha = new Demogorgon("D0000");
        eg.registrarDemogorgon(alpha);
        alpha.start();
        log.log("El Demogorgon Alpha (D0000) ha aparecido");

        // Creacion del gestor de eventod
        GestorEventos gestor = new GestorEventos();
        gestor.start();
        log.log("Sistema de eventos globales iniciado");

        // Creamos niños de forma escalonada
        for (int i = 1; i <= NUM_NINOS; i++) {
            try {
                String idNino = String.format("N%04d", i);
                new Nino(idNino).start();
                log.log("El niño " + idNino + " ha llegado a Hawkins");
                sleep(500 + (int)(Math.random() * 1500));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}