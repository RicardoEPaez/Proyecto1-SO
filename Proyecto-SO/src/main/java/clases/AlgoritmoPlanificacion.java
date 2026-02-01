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

public interface AlgoritmoPlanificacion {
    
    // Define cómo se guarda el proceso en la cola (al principio, al final, ordenado...)
    void encolar(ColaPrioridad<PCB> cola, PCB proceso);
    
    // Define si hay que sacar al proceso del CPU (solo para SRT)
    boolean debeExpropiar(PCB procesoEnCPU, PCB procesoNuevo);
}