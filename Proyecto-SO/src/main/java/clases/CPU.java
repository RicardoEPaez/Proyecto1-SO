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
    private int cicloGlobal;     // Reloj del sistema
    private int quantum;         // Tiempo máximo por turno
    private int contadorQuantum; // Tiempo que lleva el proceso actual
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

    public void interrumpir() {
        this.interrupcionInmediata = true;
    }

    
    public boolean estaLibre() {
        return procesoActual == null;
    }
    
    
    

    @Override
    public void run() {
        while (activo) {
            try {
                // 1. Simulamos el paso del tiempo (1 segundo = 1 ciclo)
                Thread.sleep(1000); 
                cicloGlobal++;

                // IMPORTANTE: Llamamos al "despertador" del planificador
                // para que revise si algún proceso terminó su I/O y debe volver a Listos.
                planificador.verificarBloqueados(); 

                // 2. Si hay un proceso cargado, ejecutamos
                if (procesoActual != null) {
                    
                    // Ejecuta una instrucción
                    procesoActual.ejecutar();
                    contadorQuantum++;

                    System.out.println("[CPU Reloj:" + cicloGlobal + "] Ejecutando " + procesoActual.getNombre() 
                            + " | Instr: " + procesoActual.getInstruccionesEjecutadas() 
                            + "/" + procesoActual.getInstruccionesTotales());

                    // --- TOMA DE DECISIONES (JERARQUÍA) ---

                    // A. ¿El proceso terminó todas sus instrucciones?
                    if (procesoActual.haTerminado()) {
                        System.out.println("--> [CPU] FIN DE PROCESO: " + procesoActual.getNombre());
                        procesoActual.setEstado(Estado.TERMINADO);
                        
                        // Avisamos al planificador (opcional) y limpiamos
                        planificador.terminarProceso(procesoActual);
                        liberarCPU();
                    }
                    
                    // B. ¿El proceso necesita hacer I/O justo ahora?
                    else if (procesoActual.necesitaIO()) {
                        System.out.println("--> [CPU] INTERRUPCIÓN I/O: " + procesoActual.getNombre() + " va a bloquearse.");
                        procesoActual.setEstado(Estado.BLOQUEADO);
                        
                        // Enviamos el proceso a la cola de bloqueados
                        planificador.bloquearProceso(procesoActual);
                        liberarCPU();
                    }

                    // C. (NUEVO) ¿Me mandaron a interrumpir por SRT?
                    else if (this.interrupcionInmediata) {
                        System.out.println("--> [CPU] DESALOJO POR ALGORITMO (SRT): " + procesoActual.getNombre() + " vuelve a la cola.");
                        
                        // Lo devolvemos a la cola de listos
                        planificador.expulsarProceso(procesoActual);
                        
                        // Reseteamos la bandera y liberamos
                        this.interrupcionInmediata = false;
                        liberarCPU();
                    }
                    
                    // D. ¿Se acabó el tiempo asignado (Quantum)?
                    // Solo aplica si el quantum no es infinito (RR)
                    else if (contadorQuantum >= quantum) {
                        System.out.println("--> [CPU] FIN DE QUANTUM: " + procesoActual.getNombre() + " vuelve a la cola.");
                        planificador.expulsarProceso(procesoActual);
                        liberarCPU();
                    }
                }

                // 3. Si el CPU está libre, intentamos cargar el siguiente proceso
                if (procesoActual == null) {
                    PCB siguiente = planificador.obtenerSiguiente();
                    
                    if (siguiente != null) {
                        asignarProceso(siguiente);
                        System.out.println("[CPU] Cargando proceso: " + siguiente.getNombre());
                    } 
                }

            } catch (InterruptedException e) {
                System.err.println("Error en hilo CPU: " + e.getMessage());
            }
        }
    }

    
    public void asignarProceso(PCB proceso) {
        this.procesoActual = proceso;
        this.procesoActual.setEstado(Estado.EJECUCION);
        this.contadorQuantum = 0; 
        this.interrupcionInmediata = false;
    }

    
    private void liberarCPU() {
        this.procesoActual = null;
        this.contadorQuantum = 0;
        this.interrupcionInmediata = false;
    }

    // --- GETTERS (Útiles para Interfaz o Debug) ---

    public PCB getProcesoActual() {
        return procesoActual;
    }

    public int getCicloGlobal() {
        return cicloGlobal;
    }
    
    public void detenerCPU() {
        this.activo = false;
    }
}