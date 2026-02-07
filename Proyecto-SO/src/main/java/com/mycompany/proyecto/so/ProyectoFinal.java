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
        // 1 = FCFS
        // 2 = SRT
        // 3 = Round Robin
        // 5 = PRUEBA MAESTRA (Interrupciones + I/O + Expropiación)
        int PRUEBA_A_EJECUTAR = 5; 
        // ---------------------

        Planificador planificador = new Planificador();
        CPU cpu;

        try {
            switch (PRUEBA_A_EJECUTAR) {
                
                case 1: // FCFS
                    System.out.println("=== INICIANDO MODO FCFS ===");
                    cpu = new CPU(9999, planificador);
                    planificador.setCPU(cpu);
                    planificador.setAlgoritmo(new AlgoritmoFCFS());
                    cpu.start();
                    
                    planificador.agregarProceso(new PCB("Proceso_Lento", 5, 100, 0, 0, 0));
                    Thread.sleep(1000); 
                    planificador.agregarProceso(new PCB("Proceso_Rapido", 2, 101, 0, 0, 0));
                    break;

                case 2: // SRT
                    System.out.println("=== INICIANDO MODO SRT ===");
                    cpu = new CPU(9999, planificador);
                    planificador.setCPU(cpu);
                    planificador.setAlgoritmo(new AlgoritmoSRT()); 
                    cpu.start();

                    planificador.agregarProceso(new PCB("Proceso_Largo", 20, 100, 0, 0, 0));
                    Thread.sleep(1000);
                    System.out.println("--> (!!!) LLEGADA DE PROCESO CORTO");
                    planificador.agregarProceso(new PCB("Proceso_Corto", 3, 101, 0, 0, 0));
                    break;

                case 3: // Round Robin
                    System.out.println("=== INICIANDO MODO ROUND ROBIN ===");
                    int QUANTUM = 3;
                    cpu = new CPU(QUANTUM, planificador);
                    planificador.setCPU(cpu);
                    planificador.setAlgoritmo(new AlgoritmoRoundRobin(QUANTUM)); 
                    cpu.start();

                    planificador.agregarProceso(new PCB("Proceso_A", 5, 100, 0, 0, 0));
                    planificador.agregarProceso(new PCB("Proceso_B", 4, 101, 0, 0, 0));
                    break;
                    
                case 5: // SISTEMA DE INTERRUPCIONES (NUEVO)
                    System.out.println("=== TEST DE SISTEMA DE INTERRUPCIONES Y KERNEL ===");
                    
                    // Usamos Prioridad para que sea evidente quién debe mandar
                    cpu = new CPU(9999, planificador); 
                    planificador.setCPU(cpu);
                    planificador.setAlgoritmo(new AlgoritmoPrioridad());
                    cpu.start();

                    // 1. LANZAMOS UN PROCESO QUE HACE I/O
                    // Parametros: Nombre, Total=20, Prio=10, Deadline=10, InicioIO=3, DuracionIO=5
                    PCB pLento = new PCB("Proceso_IO", 20, 10, 10, 3, 5);
                    
                    System.out.println("--> [USER] Agregando Proceso que pedirá I/O en breve...");
                    planificador.agregarProceso(pLento);
                    
                    // Esperamos a que pida I/O y vuelva (aprox 8 seg)
                    Thread.sleep(8000); 

                    // 2. LANZAMOS UN PROCESO URGENTE PARA CAUSAR EXPROPIACIÓN
                    // Parametros: Nombre, Total=5, Prio=1, Deadline=1, InicioIO=0, DuracionIO=0
                    System.out.println("\n--> [USER] !!! LANZANDO PROCESO URGENTE !!!");
                    PCB pUrgente = new PCB("Proceso_Urgente", 5, 1, 1, 0, 0);
                    planificador.agregarProceso(pUrgente);
                    
                    break;
            }

            // Damos tiempo para ver toda la simulación antes de cerrar
            Thread.sleep(15000);
            
            System.out.println("\n=== FIN DE LA SIMULACIÓN ===");
            System.exit(0);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}