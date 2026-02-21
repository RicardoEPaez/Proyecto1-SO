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

public class AlgoritmoSRT implements AlgoritmoPlanificacion {

    @Override
    public void encolar(ColaPrioridad<PCB> cola, PCB proceso) {
        // En SRT, la prioridad es el tiempo restante (menor es mejor)
        int tiempoRestante = proceso.getInstruccionesTotales()
                           - proceso.getInstruccionesEjecutadas();
        cola.encolar(proceso, tiempoRestante);
    }

    @Override
    public boolean debeExpropiar(PCB procesoEnCPU, PCB procesoNuevo) {
        // Calculamos correctamente el tiempo restante de ambos procesos
        int restanteActual = procesoEnCPU.getInstruccionesTotales()
                             - procesoEnCPU.getInstruccionesEjecutadas();

        int restanteNuevo = procesoNuevo.getInstruccionesTotales()
                            - procesoNuevo.getInstruccionesEjecutadas();

        // Expropiamos si el nuevo proceso termina antes
        return restanteNuevo < restanteActual;
    }

    @Override
    public String toString() {
        return "SRT (Expropiativo)";
    }
}
