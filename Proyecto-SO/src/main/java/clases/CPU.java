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
    private int quantum;         
    private int contadorQuantum; 
    private boolean interrupcionInmediata = false;

    // Constructor
    public CPU(int quantum, Planificador planificador) {
        this.quantum = quantum;
        this.planificador = planificador;
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
                // 1. ESPERA PASIVA (Sincronización con Reloj)
                // El CPU se queda "congelado" aquí.
                // No consume recursos ni tiempo hasta que Reloj diga: cpu.notify()
                synchronized (this) {
                    wait(); 
                }

                // Cuando despierta, es porque pasó 1 segundo en el Reloj.

                // 2. EJECUCIÓN (Si hay proceso cargado)
                if (procesoActual != null) {
                    
                    // Ejecuta una instrucción
                    procesoActual.ejecutar();
                    contadorQuantum++;

                    System.out.println("[CPU] Ejecutando " + procesoActual.getNombre() 
                            + " | Instr: " + procesoActual.getInstruccionesEjecutadas() 
                            + "/" + procesoActual.getInstruccionesTotales()
                            + " | Quantum: " + contadorQuantum);

                    // --- DETECCIÓN DE EVENTOS (INTERRUPCIONES) ---

                    // A. Fin de Proceso
                    if (procesoActual.haTerminado()) {
                        liberarYNotificar(TipoInterrupcion.FIN_PROCESO);
                    }
                    
                    // B. Solicitud de I/O
                    else if (procesoActual.necesitaIO()) {
                        liberarYNotificar(TipoInterrupcion.SOLICITUD_IO);
                    }

                    // C. Desalojo por Prioridad (Algoritmos Expropiativos)
                    // Esto ocurre si el Planificador activó la bandera "interrumpir()"
                    else if (this.interrupcionInmediata) {
                        liberarYNotificar(TipoInterrupcion.DESALOJO_POR_PRIORIDAD);
                    }
                    
                    // D. Tiempo Agotado (Quantum)
                    else if (quantum < 9999 && contadorQuantum >= quantum) {
                        liberarYNotificar(TipoInterrupcion.TIEMPO_AGOTADO);
                    }
                } 
                
                // 3. IDLE / CARGA DE TRABAJO
                // Si el CPU está libre, intentamos pedir trabajo al Dispatcher
                if (procesoActual == null) {
                    // Solo logueamos si está vacío para no llenar la consola
                    // System.out.println("[CPU] Estado IDLE (Esperando procesos...)");
                    
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