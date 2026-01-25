/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;
/**
 *
 * @author ricar
 */
public class CPU extends Thread {
    
    private PCB procesoActual;
    private int cicloGlobal;
    private boolean activo; 
    private int tiempoCiclo; // En milisegundos

    public CPU() {
        this.procesoActual = null;
        this.cicloGlobal = 0;
        this.activo = true;
        this.tiempoCiclo = 1000; // 1 segundo por ciclo para ver la simulación
    }

    @Override
    public void run() {
        while (activo) {
            try {
                // 1. Simular reloj
                Thread.sleep(tiempoCiclo);
                cicloGlobal++; 

                // 2. Ejecutar proceso si existe
                if (procesoActual != null) {
                    // Usamos TU método ejecutar()
                    procesoActual.ejecutar(); 
                    
                    System.out.println("[Reloj: " + cicloGlobal + "] Ejecutando: " 
                            + procesoActual.getNombre() 
                            + " | PC: " + procesoActual.getProgramCounter()
                            + "/" + procesoActual.getInstruccionesTotales());
                    
                    // Verificar si terminó usando TU método haTerminado()
                    if (procesoActual.haTerminado()) {
                        procesoActual.setEstado(Estado.TERMINADO); // Asegúrate que en Estado.java tengas EXIT o TERMINADO
                        System.out.println("--> Proceso " + procesoActual.getNombre() + " FINALIZADO.");
                        procesoActual = null; // Liberar CPU
                    }
                } else {
                    System.out.println("[Reloj: " + cicloGlobal + "] CPU Esperando...");
                }
                
            } catch (InterruptedException e) {
                System.err.println("Error en CPU: " + e.getMessage());
            }
        }
    }

    // Método para recibir procesos del Planificador
    public void asignarProceso(PCB proceso) {
        this.procesoActual = proceso;
        if (proceso != null) {
            // Asegúrate que en Estado.java tengas EJECUCION o RUNNING
            proceso.setEstado(Estado.EJECUCION); 
        }
    }
    
    // Getters
    public int getCicloGlobal() { return cicloGlobal; }
}
