/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

import java.util.Random;
/**
 * Simula el entorno externo del sistema generando interrupciones asíncronas.
 * Este hilo actúa como un generador de eventos aleatorios (Hardware Interrupts) para poner a prueba la capacidad del CPU de realizar desalojos (expropiación) y manejar prioridades ante situaciones de emergencia.
 * @author Ramon-Carrasquel
 */
public class GeneradorInterrupciones extends Thread {
    private CPU cpu;
    private boolean activo = true;
    private Random random = new Random();

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

                // 2. Disparar la interrupción
                System.out.println(">>> [EVENTO] Interrumpiendo CPU...");
                
                // Usamos el método que ya tenías en CPU.java
                cpu.interrumpir(); 

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
