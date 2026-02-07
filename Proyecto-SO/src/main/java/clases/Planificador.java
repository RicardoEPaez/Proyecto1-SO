/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

import estructuras.ColaPrioridad;
import estructuras.ListaEnlazada;

/**
 * El Planificador actua como el gestor de procesos del Sistema Operativo
 * Mantiene las colas de estados y decide qué proceso va al CPU
 * @author Ramon-Carrasquel
 */
public class Planificador {

    // CAMBIO 1: Referencia a la estrategia (el algoritmo actual)
    private AlgoritmoPlanificacion algoritmoActual;
    
    // CAMBIO 2: Referencia al CPU (Necesaria para interrumpir en SRT)
    private CPU cpu; 

    private ColaPrioridad<PCB> colaListos;
    private ListaEnlazada<PCB> listaBloqueados;

    public Planificador() {
        this.colaListos = new ColaPrioridad<>();
        this.listaBloqueados = new ListaEnlazada<>();
        this.algoritmoActual = new AlgoritmoFCFS();
    }

    public void setAlgoritmo(AlgoritmoPlanificacion nuevoAlgoritmo) {
        this.algoritmoActual = nuevoAlgoritmo;
        System.out.println("--- [SISTEMA] Algoritmo cambiado a: " + nuevoAlgoritmo.toString() + " ---");
    }

    public void setCPU(CPU cpu) {
        this.cpu = cpu;
    }
    
    public synchronized void agregarProceso(PCB proceso) {
        // 1. Cambiamos estado y lo metemos en la cola según el algoritmo actual
        proceso.setEstado(Estado.LISTO);
         
        algoritmoActual.encolar(colaListos, proceso);
        
        // Log para ver qué pasa en consola
        System.out.println("--> [Planificador] Proceso agregado: " + proceso.getNombre() + " | Algoritmo: " + algoritmoActual.toString());

        // 2. Verificación de Expropiación 
        // Solo intentamos expropiar si el CPU está asignado y trabajando
        if (cpu != null && !cpu.estaLibre()) {
            PCB enEjecucion = cpu.getProcesoActual();
            
            // Preguntamos al algoritmo: "¿El nuevo es más importante que el actual?"
            if (enEjecucion != null && algoritmoActual.debeExpropiar(enEjecucion, proceso)) {
                System.out.println("    !!! [Planificador] EXPROPIACIÓN: " + proceso.getNombre() + " desplaza a " + enEjecucion.getNombre());
                cpu.interrumpir(); // Esto requiere el método interrumpir() en tu CPU
            }
        }
    }

    public synchronized PCB obtenerSiguiente() {
        if (colaListos.estaVacia()) {
            return null;
        }
        // Simplemente sacamos el primero. El algoritmo ya se encargó de ordenarlos.
        return colaListos.desencolar();
    }
    
    public synchronized void expulsarProceso(PCB proceso) {
        proceso.setEstado(Estado.LISTO);
       
        algoritmoActual.encolar(colaListos, proceso);
    }

    public synchronized void bloquearProceso(PCB proceso) {
        proceso.setEstado(Estado.BLOQUEADO);
        listaBloqueados.agregar(proceso);
    }

    public synchronized void terminarProceso(PCB proceso) {
        System.out.println("[Planificador] Proceso finalizado y desalojado: " + proceso.getNombre());
    }

    //Este método se llama en cada ciclo del reloj del CPU 
    // Revisa la lista de bloqueados, aumenta sus contadores y despierta a los que terminaron.
    public synchronized void verificarBloqueados() {
        if (listaBloqueados.estaVacia()) {
            return;
        }

        // Usamos una lista auxiliar para guardar los que deben salir
        ListaEnlazada<PCB> procesosParaDespertar = new ListaEnlazada<>();

        // 1. Recorremos todos los bloqueados
        for (int i = 0; i < listaBloqueados.getTamano(); i++) {
            PCB p = listaBloqueados.get(i);
            
            // Aumentamos su tiempo esperando I/O
            p.aumentarContadorIO();

            // Verificamos si ya cumplio su tiempo de espera
            if (p.getContadorIO() >= p.getLongitudIO()) {
                procesosParaDespertar.agregar(p);
            }
        }

        // 2. Movemos los procesos listos de Bloqueados a la Cola de Listos
        for (int i = 0; i < procesosParaDespertar.getTamano(); i++) {
            PCB p = procesosParaDespertar.get(i);
            
            // Lo sacamos de bloqueados
            listaBloqueados.eliminar(p);
            
            // Reseteamos su contador de I/O para futuras interrupciones
            p.reiniciarContadorIO();
            p.setEstado(Estado.LISTO);
            
            System.out.println("--> [Planificador] I/O Completado: " + p.getNombre());
            
            this.agregarProceso(p);
        }
    }
    
    public boolean hayProcesosListos() {
        return !colaListos.estaVacia();
    }
    
    public synchronized void manejarInterrupcion(TipoInterrupcion tipo, PCB proceso) {
        
        if (proceso == null) return;

        switch (tipo) {
            case FIN_PROCESO:
                System.out.println("--- [PLANIFICADOR] Interrupción: Fin de Proceso (" + proceso.getNombre() + ") ---");
                proceso.setEstado(Estado.TERMINADO);
                this.terminarProceso(proceso);
                break;

            case TIEMPO_AGOTADO:
                System.out.println("--- [PLANIFICADOR] Interrupción: Tiempo Agotado (" + proceso.getNombre() + ") ---");
                // Vuelve a la cola de listos
                this.expulsarProceso(proceso);
                break;

            case SOLICITUD_IO:
                System.out.println("--- [PLANIFICADOR] Interrupción: Solicitud de E/S (" + proceso.getNombre() + ") ---");
                proceso.setEstado(Estado.BLOQUEADO);
                this.bloquearProceso(proceso);
                break;
                
            case DESALOJO_POR_PRIORIDAD:
                System.out.println("--- [PLANIFICADOR] Interrupción: Desalojo por Prioridad (" + proceso.getNombre() + ") ---");
                // Vuelve a la cola de listos
                this.expulsarProceso(proceso);
                break;
        }
        // Al final, siempre intentamos cargar el siguiente proceso en el CPU
        despacharSiguiente();
    }
    
    private void despacharSiguiente() {
         PCB siguiente = this.obtenerSiguiente();
         if (siguiente != null) {
             System.out.println("[PLANIFICADOR] Asignando CPU a: " + siguiente.getNombre());
             cpu.asignarProceso(siguiente);
         } else {
             System.out.println("[PLANIFICADOR] CPU en espera (Idle)...");
         }
    }
    
}