/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

/**
 *
 * @author Home
 * @param <T>
 */
public class Cola<T> {
    private Nodo<T> frente;
    private Nodo<T> finalCola;
    private int tamano;
    
    public Cola(){
        this.frente = null;
        this.finalCola = null;
        this.tamano = 0;
    }
    
    // Definimos el metodo para agregar un elemento al final de la cola (encolar)
    public void encolar(T dato){
        Nodo<T> nuevoNodo = new Nodo<>(dato);
        if (estaVacia()){
            frente = nuevoNodo;
            finalCola = nuevoNodo;
        }else{
            finalCola.setSiguiente(nuevoNodo);
            finalCola = nuevoNodo;
        }
        tamano++;
    }
    
    // Definimos el metodo para sacar el elemento del frente de la cola (desencolar)
    public T desencolar(){
        if (estaVacia()){
            return null;
        }
        T dato = frente.getContenido();
        frente = frente.getSiguiente();
        if (frente == null){
            finalCola = null;
        }
        tamano--;
        return dato;
    }
    
    // Para ver el primer elemento sin sacarlo de la cola
    public T obtenerFrente(){
        return estaVacia() ? null : frente.getContenido();
    }
    
    // Para determinar si la cola esta vacia
    public boolean estaVacia(){
        return frente == null;
    }
    
    // Para oonocer el tamano de la cola
    public int getTamano(){
        return tamano;
    }
}
