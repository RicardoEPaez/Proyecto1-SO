/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

import java.util.Random;

/**
 * Simula el entorno externo del sistema generando interrupciones asíncronas.
 * Este hilo actúa como un generador de eventos aleatorios (Hardware Interrupts).
 * @author Ramon-Carrasquel
 */
public class GeneradorInterrupciones extends Thread {
    
    private CPU cpu;
    private boolean activo = true;
    private Random random = new Random();

    // Fíjate que solo necesitamos la CPU, ¡nada de planificador!
    public GeneradorInterrupciones(CPU cpu) {
        this.cpu = cpu;
    }

    @Override
    public void run() {
        while (activo) {
            try {
                // 1. Esperar un tiempo aleatorio (ej. cada 10 a 20 segundos)
                int tiempoEspera = (random.nextInt(10) + 10) * 1000; 
                Thread.sleep(tiempoEspera);

                // 2. Disparar el HILO DE INTERRUPCIÓN INDEPENDIENTE
                System.out.println("\n>>> [HARDWARE] Señal de interrupción por impacto de micro-meteoritos detectada...");
                
                // Calculamos que la ISR tarde entre 2 y 4 segundos en resolver el problema
                int tiempoResolucion = (random.nextInt(3) + 2) * 1000;
                
                // 3. NACE EL HILO ISR (Solo le pasamos la CPU y el tiempo)
                HiloISR rutina = new HiloISR(cpu, tiempoResolucion);
                rutina.start(); 

            } catch (InterruptedException e) {
                System.out.println("Generador de Interrupciones detenido.");
                activo = false; // Rompemos el ciclo
            }
        }
    }
    
    public void detener() {
        this.activo = false;
    }
}
