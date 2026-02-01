/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

import estructuras.ColaPrioridad;

/**
 * Algoritmo Round Robin
 * Planificación expropiativa basada en quantum
 */
public class AlgoritmoRoundRobin implements AlgoritmoPlanificacion {

    private int quantum;

    // Contador FIFO para mantener orden justo
    private static int ordenLlegada = 0;

    public AlgoritmoRoundRobin(int quantum) {
        this.quantum = quantum;
    }

    @Override
    public void encolar(ColaPrioridad<PCB> cola, PCB proceso) {
        // En Round Robin se respeta el orden de llegada
        cola.encolar(proceso, ordenLlegada++);
    }

    @Override
    public boolean debeExpropiar(PCB procesoEnCPU, PCB procesoNuevo) {
        // RR NO expropia por llegada, solo por fin de quantum
        return false;
    }

    @Override
    public String toString() {
        return "Round Robin (q=" + quantum + ")";
    }
}

