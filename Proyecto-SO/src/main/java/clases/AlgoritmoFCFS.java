/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

import estructuras.ColaPrioridad;

/**
 *
 * @author ricar
 */

/**
 * Algoritmo First Come First Served (FCFS)
 */
public class AlgoritmoFCFS implements AlgoritmoPlanificacion {

    // Contador FIFO para simular orden de llegada
    private static int ordenLlegada = 0;

    @Override
    public void encolar(ColaPrioridad<PCB> cola, PCB proceso) {
        // Usamos orden de llegada como prioridad
        cola.encolar(proceso, ordenLlegada++);
    }

    @Override
    public boolean debeExpropiar(PCB procesoEnCPU, PCB procesoNuevo) {
        return false;
    }

    @Override
    public String toString() {
        return "FCFS";
    }
}
