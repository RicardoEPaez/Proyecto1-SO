/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

import java.util.Random;
/**
 * Hilo que simula el entorno del satélite.
 * Genera interrupciones (llegada de nuevos procesos) de forma aleatoria mientras el sistema está corriendo.
 * @author Ramon-Carrasquel
 */
public class HiloEntorno extends Thread{
    private Planificador planificador;
    private boolean activo;
    private Random random;

    public HiloEntorno(Planificador planificador) {
        this.planificador = planificador;
        this.activo = true;
        this.random = new Random();
    }

    @Override
    public void run() {
        while (activo) {
            try {
                // 1. Esperar un tiempo aleatorio antes de crear otro proceso
                // Por ejemplo: entre 3 y 8 segundos
                int tiempoEspera = (random.nextInt(6) + 3) * 1000;
                Thread.sleep(tiempoEspera);

                // 2. Usar tu clase existente para crear el dato
                PCB nuevoProceso = GeneradorProcesos.generarProcesoAleatorio();
                
                System.out.println("!!! [EVENTO EXTERNO] Ha llegado un nuevo proceso: " + nuevoProceso.getNombre() + " (Prioridad: " + nuevoProceso.getPrioridad() + ")");
                
                // 3. Inyectarlo al sistema (esto activará la lógica de Planificador.agregarProceso)
                planificador.agregarProceso(nuevoProceso);

            } catch (InterruptedException e) {
                System.err.println("HiloEntorno interrumpido.");
            }
        }
    }
    
    public void detener() {
        this.activo = false;
    }
}
