/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

import java.util.concurrent.Semaphore; 

/**
 * CPU simula la ejecución de procesos ciclo a ciclo.
 * Ahora sincronizado mediante SEMÁFOROS.
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

    // --- 2. SEMÁFORO DE SINCRONIZACIÓN ---
    // Inicia en 0 (ROJO/CERRADO). El CPU se bloqueará al intentar pasar.
    // Solo el Reloj puede ponerlo en VERDE/ABIERTO.
    private final Semaphore semaforoCiclo = new Semaphore(0);
    
    // Control de la Rutina de Interrupción (ISR)
    private boolean enRutinaISR = false;
    
    // Inicia en 1 (VERDE). El CPU fluye normal hasta que la ISR lo pone en 0.
    private final Semaphore semaforoISR = new Semaphore(1);
    
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
        
        if (this.procesoActual != null) {
            this.procesoActual.setEstado(Estado.BLOQUEADO); // (O Estado.INTERRUMPIDO si lo tienes)
        }
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

    // --- MÉTODO AUXILIAR CRÍTICO ---
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

    // --- 3. NUEVO MÉTODO PARA EL RELOJ ---
    // Este método sustituye al 'notify()'. El Reloj lo llama cada segundo.
    public void enviarPulsoReloj() {
        semaforoCiclo.release(); // Incrementa el permiso (+1) y despierta al hilo
    }

    @Override
    public void run() {
        while (activo) {
            try {
                // 1. ESPERA ACTIVA: Esperamos el pulso del Reloj
                semaforoCiclo.acquire();
                
                // === INICIO DE SECCIÓN CRÍTICA PROTEGIDA ===
                // El CPU toma la puerta. Si una ISR intenta interrumpir AHORA, 
                // tendrá que esperar a que el CPU termine este pequeño ciclo.
                semaforoISR.acquire(); 
                
                try {
                    // 2. EJECUCIÓN (Si hay proceso cargado)
                    if (procesoActual != null) {
                        
                        // Ejecuta una instrucción de forma segura
                        procesoActual.ejecutar();
                        contadorQuantum++;

                        System.out.println("[CPU] Ejecutando " + procesoActual.getNombre() 
                                + " | Instr: " + procesoActual.getInstruccionesEjecutadas() 
                                + "/" + procesoActual.getInstruccionesTotales()
                                + " | Quantum: " + contadorQuantum);

                        // --- DETECCIÓN DE EVENTOS NORMALES ---
                        if (procesoActual.haTerminado()) {
                            liberarYNotificar(TipoInterrupcion.FIN_PROCESO);
                        }
                        else if (procesoActual.necesitaIO()) {
                            liberarYNotificar(TipoInterrupcion.SOLICITUD_IO);
                        }
                        else if (this.interrupcionInmediata) {
                            liberarYNotificar(TipoInterrupcion.DESALOJO_POR_PRIORIDAD);
                        }
                        else if (quantum < 9999 && contadorQuantum >= quantum) {
                            liberarYNotificar(TipoInterrupcion.TIEMPO_AGOTADO);
                        }
                    } 
                    
                    // 3. IDLE / CARGA DE TRABAJO
                    if (procesoActual == null) {
                        if (planificador.hayProcesosListos()) {
                            PCB siguiente = planificador.obtenerSiguiente();
                            if (siguiente != null) { 
                                 asignarProceso(siguiente);
                                 System.out.println("[CPU] Dispatcher cargó: " + siguiente.getNombre());
                            }
                        }
                    }

                } finally {
                    // === FIN DE SECCIÓN CRÍTICA ===
                    // El CPU suelta la puerta. 
                    // Si el HiloISR estaba esperando para secuestrar el CPU, entra AHORA MISMO
                    // y hará el desalojo antes de que llegue el siguiente tick del reloj.
                    semaforoISR.release(); 
                }

            } catch (InterruptedException e) {
                System.err.println("Error en hilo CPU: " + e.getMessage());
            }
        }
    }
    
    public void detenerCPU() {
        this.activo = false;
    }
    
    // NUEVOS MÉTODOS PARA LA ISR
    
    /**
     * Congela el CPU, desaloja de emergencia el proceso actual y lo devuelve a la cola.
     * @throws InterruptedException Si el hilo es interrumpido mientras espera adquirir el semáforo.
     */
    public void suspenderParaISR() throws InterruptedException {
        // Cerramos la puerta: El ciclo run() se congelará en el próximo tick
        semaforoISR.acquire();
        this.enRutinaISR = true;
        
        // Desalojo forzoso e inmediato del proceso actual
        if (this.procesoActual != null) {
            System.out.println("[CPU-URGENCIA] Expropiando " + procesoActual.getNombre() + " por ISR.");
            
            // Lo ponemos como LISTO
            this.procesoActual.setEstado(Estado.LISTO);
            
            // Lo devolvemos a la cola del planificador
            planificador.agregarProceso(this.procesoActual);
            
            // Limpiamos la CPU para que quede vacía
            this.procesoActual = null;
            this.contadorQuantum = 0;
            this.interrupcionInmediata = false;
        }
    }
    
    /**
     * Libera el CPU para que vuelva a pedir procesos al Dispatcher.
     */
    public void reanudarDeISR() {
        this.enRutinaISR = false;
        // Abrimos la puerta: El CPU vuelve a funcionar
        semaforoISR.release();
    }
    
    /**
     * Útil por si quieres que tu Panel visual (GUI) muestre la CPU en rojo cuando hay emergencia
     * @return true si el CPU está actualmente en una Rutina de Servicio (ISR), false de lo contrario.
     */
    public boolean isEnRutinaISR() {
        return enRutinaISR;
    }
}