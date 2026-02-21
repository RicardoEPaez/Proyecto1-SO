/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

import estructuras.ColaPrioridad;
/**
 * Algoritmo Earliest Deadline First (EDF)
 * Planificación dinámica: La prioridad es la cercanía del tiempo límite.
 * El proceso con el deadline más cercano se ejecuta primero.
 * Si llega un proceso con un deadline menor al actual, toma control del CPU.
 * @author Ramon-Carrasquel
 */
public class AlgoritmoEDF implements AlgoritmoPlanificacion{
    
    @Override
    public void encolar(ColaPrioridad<PCB> cola, PCB proceso) {
        // La "prioridad" en EDF es el Deadline.
        // Mientras menor sea el deadline (más cercano al ciclo actual o 0), más rápido será atendido ese proceso (se pondrá de primero en la fila).
        cola.encolar(proceso, proceso.getDeadline());
    }

    @Override
    public boolean debeExpropiar(PCB procesoEnCPU, PCB procesoNuevo) {
        // Si llega un proceso cuyo deadline vence es más urgente (es decir, caduca más pronto) 
        // que el del proceso que está actualmente en CPU, debemos atender al nuevo para evitar fallos.
        return procesoNuevo.getDeadline() < procesoEnCPU.getDeadline();
    }

    @Override
    public String toString() {
        return "Earliest Deadline First (EDF)";
    }
}
