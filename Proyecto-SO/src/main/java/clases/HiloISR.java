/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

/**
 * Hilo independiente que maneja la Rutina de Servicio de Interrupción (ISR).
 * @author Ramon-Carrasquel
 */
public class HiloISR extends Thread {
    
    private CPU cpu;
    private int tiempoResolucion; // Cuánto tarda en resolverse el problema

    public HiloISR(CPU cpu, int tiempoResolucion) {
        this.cpu = cpu;
        this.tiempoResolucion = tiempoResolucion;
    }

    @Override
    public void run() {
        try {
            System.out.println("\n[ISR] ¡EMERGENCIA DETECTADA! Iniciando Rutina de Servicio...");
            
            // 1. SECUESTRAR EL CPU
            cpu.suspenderParaISR();
            
            // 2. ATENDER LA EMERGENCIA
            System.out.println("[ISR] Atendiendo interrupción (Tiempo estimado: " + (tiempoResolucion/1000) + "s)...");
            Thread.sleep(tiempoResolucion);
            
            // 3. DEVOLVER EL CONTROL
            System.out.println("[ISR] Emergencia resuelta. Devolviendo control al procesador...\n");
            cpu.reanudarDeISR();

        } catch (InterruptedException e) {
            System.err.println("[ISR] Error crítico en rutina de servicio: " + e.getMessage());
            cpu.reanudarDeISR(); // Siempre liberar la CPU en caso de error
        }
    }
}
