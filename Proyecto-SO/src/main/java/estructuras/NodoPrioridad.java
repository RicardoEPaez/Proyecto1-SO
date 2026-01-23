/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

/**
 * Nodo especial para la Cola de Prioridad
 * @author Home
 * @param <T> Tipo de objeto a almacenar
 */
public class NodoPrioridad<T> {
    private T contenido;
    private int prioridad; // El valor numerico para ordenar (prioridad, deadline, etc)
    private NodoPrioridad<T> siguiente;
    
    // Constructor
    public NodoPrioridad(T contenido, int prioridad){
        this.contenido = contenido;
        this.prioridad = prioridad;
        this.siguiente = null;
    }
    
    // Getters y Setters
    public T getContenido(){
        return contenido;
    }
    
    public void setContenido(T contenido){
        this.contenido = contenido;
    }
    
    public int getPrioridad(){
        return prioridad;
    }
    
    public void setPrioridad(int prioridad){
        this.prioridad = prioridad;
    }
    
    public NodoPrioridad<T> getSiguiente(){
        return siguiente;
    }
    
    public void setSiguiente(NodoPrioridad<T> siguiente){
        this.siguiente = siguiente;
    }
}
