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

    // Referencias
    private Planificador planificador;
    private PCB procesoActual;

    // Estado del CPU
    private boolean activo;
    
    // Contadores de tiempo
    private int cicloGlobal;     
    private int quantum;         
    private int contadorQuantum; 
    private boolean interrupcionInmediata = false;

    // Constructor
    public CPU(int quantum, Planificador planificador) {
        this.quantum = quantum;
        this.planificador = planificador;
        this.cicloGlobal = 0;
        this.contadorQuantum = 0;
        this.activo = true;
    }
    
    
    public void setQuantum(int quantum) {
        this.quantum = quantum;
    }

    // Método para forzar una interrupción externa
    public void interrumpir() {
        this.interrupcionInmediata = true;
    }
    
    
    public boolean estaLibre() {
        return procesoActual == null;
    }
    
    public PCB getProcesoActual() {
        return procesoActual;
    }
    
    // Método llamado por el Planificador (Dispatcher) para cargar un proceso
    public void asignarProceso(PCB proceso) {
        this.procesoActual = proceso;
        this.procesoActual.setEstado(Estado.EJECUCION);
        this.contadorQuantum = 0; 
        this.interrupcionInmediata = false;
    }

    // --- NUEVO MÉTODO AUXILIAR CRÍTICO ---
    // Libera el CPU y dispara la interrupción hacia el Planificador
    private void liberarYNotificar(TipoInterrupcion tipo) {
        PCB procesoSaliente = this.procesoActual;
        
        // 1. Limpiamos el CPU
        this.procesoActual = null;
        this.contadorQuantum = 0;
        this.interrupcionInmediata = false;
        
        // 2. Avisamos al Kernel (Planificador)
        if (procesoSaliente != null) {
            planificador.manejarInterrupcion(tipo, procesoSaliente);
        }
    }

    @Override
    public void run() {
        while (activo) {
            try {
                // 1. Reloj del Sistema (1 ciclo)
                Thread.sleep(1000); 
                cicloGlobal++;
                
                // Mantenemos la verificación de I/O sincronizada con el reloj
                planificador.verificarBloqueados(); 

                // 2. Ejecución
                if (procesoActual != null) {
                    
                    // Ejecuta una instrucción
                    procesoActual.ejecutar();
                    contadorQuantum++;

                    System.out.println("[CPU Reloj:" + cicloGlobal + "] Ejecutando " + procesoActual.getNombre() 
                            + " | Instr: " + procesoActual.getInstruccionesEjecutadas() 
                            + "/" + procesoActual.getInstruccionesTotales());

                    // --- DETECCIÓN DE EVENTOS (INTERRUPCIONES) ---
                    // Fíjate cómo ahora el código es mucho más limpio.
                    // Solo detectamos el "QUÉ" pasó, no decidimos el "CÓMO" solucionarlo.

                    // A. Fin de Proceso
                    if (procesoActual.haTerminado()) {
                        liberarYNotificar(TipoInterrupcion.FIN_PROCESO);
                    }
                    
                    // B. Solicitud de I/O
                    else if (procesoActual.necesitaIO()) {
                        liberarYNotificar(TipoInterrupcion.SOLICITUD_IO);
                    }

                    // C. Desalojo por Prioridad (Algoritmos Expropiativos)
                    else if (this.interrupcionInmediata) {
                        liberarYNotificar(TipoInterrupcion.DESALOJO_POR_PRIORIDAD);
                    }
                    
                    // D. Tiempo Agotado (Quantum)
                    else if (quantum < 9999 && contadorQuantum >= quantum) {
                        liberarYNotificar(TipoInterrupcion.TIEMPO_AGOTADO);
                    }
                } 
                
                // 3. Si el CPU está libre, intentamos pedir trabajo al Dispatcher
                // (Esto cubre el caso de arranque o cuando la cola se vació y llegó algo nuevo)
                if (procesoActual == null) {
                    if (planificador.hayProcesosListos()) {
                        PCB siguiente = planificador.obtenerSiguiente();
                        asignarProceso(siguiente); // El Dispatcher carga el proceso
                        System.out.println("[CPU] Dispatcher cargó: " + siguiente.getNombre());
                    }
                }

            } catch (InterruptedException e) {
                System.err.println("Error en hilo CPU: " + e.getMessage());
            }
        }
    }
    
    public void detenerCPU() {
        this.activo = false;
    }
}