/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

/**
 *
 * @author ricar
 */
public enum Estado {
    NUEVO,
    LISTO,
    EJECUCION,
    BLOQUEADO,
    TERMINADO,
    
    //Para el Swapping (Memoria Virtual)
    LISTO_SUSPENDIDO,
    BLOQUEADO_SUSPENDIDO,
}
