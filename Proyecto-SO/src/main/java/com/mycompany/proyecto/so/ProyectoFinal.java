/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.mycompany.proyecto.so;
import clases.*;
/**
 *
 * @author ricar
 */
public class ProyectoFinal {

    public static void main(String[] args) {
        
        // --- CONFIGURACIÓN ---
        // 1 = FCFS (Cola simple)
        // 2 = SRT (Expropiativo / Interrupción)
        // 3 = Round Robin (Quantum / Turnos)
        int PRUEBA_A_EJECUTAR = 3; 
        // ---------------------

        Planificador planificador = new Planificador();
        CPU cpu;

        try {
            switch (PRUEBA_A_EJECUTAR) {
                
                case 1: // FCFS
                    System.out.println("=== INICIANDO MODO FCFS ===");
                    cpu = new CPU(9999, planificador); // Sin Quantum (infinito)
                    planificador.setCPU(cpu);
                    
                    // Usamos la clase específica FCFS
                    planificador.setAlgoritmo(new AlgoritmoFCFS());
                    
                    cpu.start();
                    
                    // Prueba: El rápido espera al lento
                    planificador.agregarProceso(new PCB("Proceso_Lento", 5, 100, 1, 0, 0));
                    Thread.sleep(1000); 
                    System.out.println("--> Llegó proceso rápido (deberá esperar)");
                    planificador.agregarProceso(new PCB("Proceso_Rapido", 2, 101, 1, 0, 0));
                    break;

                case 2: // SRT
                    System.out.println("=== INICIANDO MODO SRT (Shortest Remaining Time) ===");
                    cpu = new CPU(9999, planificador); // Sin Quantum
                    planificador.setCPU(cpu);
                    
                    // Usamos la clase específica SRT
                    planificador.setAlgoritmo(new AlgoritmoSRT()); 
                    
                    cpu.start();

                    // Prueba: El corto interrumpe al largo
                    planificador.agregarProceso(new PCB("Proceso_Largo", 20, 100, 1, 0, 0));
                    Thread.sleep(1000); // Dejar que arranque
                    System.out.println("--> (!!!) LLEGADA DE PROCESO URGENTE");
                    planificador.agregarProceso(new PCB("Proceso_Corto", 3, 101, 1, 0, 0));
                    break;

                case 3: // Round Robin
                    System.out.println("=== INICIANDO MODO ROUND ROBIN ===");
                    
                    int QUANTUM = 3;
                    cpu = new CPU(QUANTUM, planificador); // Configuramos Quantum en CPU
                    planificador.setCPU(cpu);
                    
                    // ¡AQUÍ EL CAMBIO! Usamos tu nueva clase AlgoritmoRoundRobin
                    planificador.setAlgoritmo(new AlgoritmoRoundRobin(QUANTUM)); 
                    
                    cpu.start();

                    // Prueba: Pelea de turnos (A: 5 instr, B: 4 instr)
                    planificador.agregarProceso(new PCB("Proceso_A", 5, 100, 1, 0, 0));
                    planificador.agregarProceso(new PCB("Proceso_B", 4, 101, 1, 0, 0));
                    break;
            }

            // Tiempo suficiente para ver la simulación completa
            Thread.sleep(10000);
            
            System.out.println("\n=== FIN DE LA SIMULACIÓN ===");
            System.exit(0); // Forzamos el cierre de hilos

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}