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
        // 6 = PRUEBA DE GENERADOR (Carga Masiva + Dinámica + Memoria)
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
                    
                    // CORREGIDO: Agregado tamaño 128MB y 64MB al final
                    planificador.agregarProceso(new PCB("Proceso_Lento", 5, 100, 0, 0, 0, 128));
                    Thread.sleep(1000); 
                    planificador.agregarProceso(new PCB("Proceso_Rapido", 2, 101, 0, 0, 0, 64));
                    break;

                case 2: // SRT
                    System.out.println("=== INICIANDO MODO SRT ===");
                    cpu = new CPU(9999, planificador);
                    planificador.setCPU(cpu);
                    planificador.setAlgoritmo(new AlgoritmoSRT()); 
                    cpu.start();

                    // CORREGIDO: Agregado tamaño 256MB
                    planificador.agregarProceso(new PCB("Proceso_Largo", 20, 100, 0, 0, 0, 256));
                    Thread.sleep(1000);
                    System.out.println("--> (!!!) LLEGADA DE PROCESO CORTO");
                    // CORREGIDO: Agregado tamaño 64MB
                    planificador.agregarProceso(new PCB("Proceso_Corto", 3, 101, 0, 0, 0, 64));
                    break;

                case 3: // Round Robin
                    System.out.println("=== INICIANDO MODO ROUND ROBIN ===");
                    int QUANTUM = 3;
                    cpu = new CPU(QUANTUM, planificador);
                    planificador.setCPU(cpu);
                    planificador.setAlgoritmo(new AlgoritmoRoundRobin(QUANTUM)); 
                    cpu.start();

                    // CORREGIDO: Agregados tamaños estándar 128MB
                    planificador.agregarProceso(new PCB("Proceso_A", 5, 100, 0, 0, 0, 128));
                    planificador.agregarProceso(new PCB("Proceso_B", 4, 101, 0, 0, 0, 128));
                    break;
                    
                case 5: // SISTEMA DE INTERRUPCIONES
                    System.out.println("=== TEST DE SISTEMA DE INTERRUPCIONES Y KERNEL ===");
                    
                    cpu = new CPU(9999, planificador); 
                    planificador.setCPU(cpu);
                    planificador.setAlgoritmo(new AlgoritmoPrioridad());
                    cpu.start();

                    // 1. LANZAMOS UN PROCESO QUE HACE I/O
                    // Parametros: Nombre, Total=20, Prio=10, Deadline=10, InicioIO=3, DuracionIO=5, TAMAÑO=200MB
                    PCB pLento = new PCB("Proceso_IO", 20, 10, 10, 3, 5, 200);
                    
                    System.out.println("--> [USER] Agregando Proceso que pedirá I/O en breve...");
                    planificador.agregarProceso(pLento);
                    
                    // Esperamos a que pida I/O y vuelva (aprox 8 seg)
                    Thread.sleep(8000); 

                    // 2. LANZAMOS UN PROCESO URGENTE PARA CAUSAR EXPROPIACIÓN
                    // Parametros: ..., TAMAÑO=64MB
                    System.out.println("\n--> [USER] !!! LANZANDO PROCESO URGENTE !!!");
                    PCB pUrgente = new PCB("Proceso_Urgente", 5, 1, 1, 0, 0, 64);
                    planificador.agregarProceso(pUrgente);
                    break;
                    
                case 6: 
                    System.out.println("=== TEST DE CARGA AUTOMÁTICA (GENERADOR) ===");
                    
                    int Q_AUTO = 3;
                    cpu = new CPU(Q_AUTO, planificador);
                    planificador.setCPU(cpu);
                    planificador.setAlgoritmo(new AlgoritmoRoundRobin(Q_AUTO));
                    cpu.start();

                    // PASO 1: Carga Masiva Inicial
                    System.out.println("--> [SISTEMA] Generando carga inicial de 5 procesos...");
                    // Nota: GeneradorProcesos ya lo actualizaste para que incluya tamaños aleatorios
                    PCB[] iniciales = GeneradorProcesos.generarMasivos(5);
                    
                    for (PCB p : iniciales) {
                        System.out.println("    + Cargando: " + p.getNombre() + " (Inst: " + p.getInstruccionesTotales() + ", MB: " + p.getTamano() + ")");
                        planificador.agregarProceso(p);
                    }
                    
                    Thread.sleep(5000);

                    // PASO 2: Llegada de Procesos Dinámicos
                    for (int i = 0; i < 3; i++) {
                        System.out.println("\n--> [TIEMPO REAL] Generando proceso entrante aleatorio...");
                        PCB nuevo = GeneradorProcesos.generarProcesoAleatorio();
                        
                        System.out.println("    + NUEVO PROCESO DETECTADO: " + nuevo.getNombre() + (nuevo.getCicloGeneracionIO() > 0 ? " [CON I/O]" : ""));
                        planificador.agregarProceso(nuevo);
                        
                        Thread.sleep(3000); 
                    }
                    break;
            }

            Thread.sleep(15000);
            
            System.out.println("\n=== FIN DE LA SIMULACIÓN ===");
            System.exit(0);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}