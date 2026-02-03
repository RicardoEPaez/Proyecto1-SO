/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

import estructuras.ColaPrioridad;
/**
 * Algoritmo de Prioridad Estática Preemptiva
 * El proceso con el valor de prioridad más bajo (por ejemplo, 1) se ejecuta primero.
 * Si llega un proceso con mayor prioridad que el actual, toma control del CPU.
 * @author Ramon-Carrasquel
 */
public class AlgoritmoPrioridad implements AlgoritmoPlanificacion{
    
    @Override
    public void encolar(ColaPrioridad<PCB> cola, PCB proceso) {
        // Encolamos usando la prioridad del proceso como criterio de ordenamiento.
        // ColaPrioridad pone al principio los valores enteros más bajos.
        cola.encolar(proceso, proceso.getPrioridad());
    }

    @Override
    public boolean debeExpropiar(PCB procesoEnCPU, PCB procesoNuevo) {
        // En un esquema preemptivo, si el nuevo proceso tiene mayor prioridad que el que está corriendo, debemos sacarlo del CPU.
        return procesoNuevo.getPrioridad() < procesoEnCPU.getPrioridad();
    }

    @Override
    public String toString() {
        return "Prioridad Estática Preemptiva";
    }
}
