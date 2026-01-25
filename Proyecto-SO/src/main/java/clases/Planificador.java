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

    // Cola de Listos: Usamos ColaPrioridad para respetar la jerarquía del RTOS
    private ColaPrioridad<PCB> colaListos;
    
    // Cola de Bloqueados: Usamos ListaEnlazada para poder recorrerlos y actualizar sus tiempos
    private ListaEnlazada<PCB> listaBloqueados;

    public Planificador() {
        this.colaListos = new ColaPrioridad<>();
        this.listaBloqueados = new ListaEnlazada<>();
    }

    /**
     * Metodo para agregar un proceso nuevo al sistema.
     * @param proceso El PCB del proceso nuevo
     */
    public synchronized void agregarProceso(PCB proceso) {
        proceso.setEstado(Estado.LISTO);
        // Encolamos según su prioridad (menor valor = mayor prioridad)
        colaListos.encolar(proceso, proceso.getPrioridad());
        System.out.println("[Planificador] Proceso agregado a Listos: " + proceso.getNombre());
    }

    /**
     * El CPU llama a este método cuando está libre y necesita un proceso.
     * @return El siguiente PCB a ejecutar o null si no hay nadie.
     */
    public synchronized PCB obtenerSiguiente() {
        if (colaListos.estaVacia()) {
            return null;
        }
        // Extraemos el de mayor prioridad (frente de la cola)
        return colaListos.desencolar();
    }
    
    // El CPU llama a este método cuando se le acaba el Quantum a un proceso
    // Lo devolvemos a la cola de listos (Round Robin)
    public synchronized void expulsarProceso(PCB proceso) {
        proceso.setEstado(Estado.LISTO);
        // Al encolarlo de nuevo, si hay otros con misma prioridad, este queda detrás
        colaListos.encolar(proceso, proceso.getPrioridad());
    }

    // El CPU llama a este método cuando un proceso solicita I/O (Entrada/Salida)
    public synchronized void bloquearProceso(PCB proceso) {
        proceso.setEstado(Estado.BLOQUEADO);
        listaBloqueados.agregar(proceso);
    }

    // El CPU llama a este método cuando un proceso termina su ejecución.
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
            
            // Lo mandamos a la cola de prioridad
            colaListos.encolar(p, p.getPrioridad());
            
            System.out.println("--> [Planificador] I/O Completado: " + p.getNombre() + " vuelve a Listos.");
        }
    }
    
    public boolean hayProcesosListos() {
        return !colaListos.estaVacia();
    }
}