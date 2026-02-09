/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package estructuras;

/**
 *
 * @author Ramon-Carrasquel
 * @param <T>
 */
public class ColaPrioridad<T> {
    private NodoPrioridad<T> frente;
    private int tamano;
    
    public ColaPrioridad() {
        this.frente = null;
        this.tamano = 0;
    }
    
    // Inserta un elemento manteniendo el orden segun el valor 'prioridad'
    // Menor valor = Mayor prioridad (sale antes)
    public void encolar(T dato, int prioridad){
        NodoPrioridad<T> nuevo = new NodoPrioridad<>(dato, prioridad);
        
        //Caso 1: Insertar al inicio si esta vacia o si el nuevo tiene "mejor" prioridad
        if (estaVacia() || prioridad < frente.getPrioridad()){
            nuevo.setSiguiente(frente);
            frente = nuevo;
        } else{
            // Caso 2: Buscar la posicion correcta
            NodoPrioridad<T> actual = frente;
            
            // Analizamos mientras el siguiente exista y tenga una prioridad "mejor" o igual
            while (actual.getSiguiente() != null && actual.getSiguiente().getPrioridad() <= prioridad){
                actual = actual.getSiguiente();
            }
            // Insertamos el nodo
            nuevo.setSiguiente(actual.getSiguiente());
            actual.setSiguiente(nuevo);
        }
        tamano++;
    }
    
    public T desencolar() {
        if (estaVacia()) return null;
        
        T dato = frente.getContenido();
        frente = frente.getSiguiente();
        tamano--;
        return dato;
    }
    
    public boolean estaVacia(){
        return frente == null;
    }
    
    public int getTamano(){
        return tamano;
    }
    
    // Método para la GUI: Devuelve un arreglo simple con los elementos
    public Object[] toArray() {
        Object[] arreglo = new Object[tamano];
        NodoPrioridad<T> actual = frente;
        
        for (int i = 0; i < tamano; i++) {
            if (actual != null) {
                arreglo[i] = actual.getContenido();
                actual = actual.getSiguiente();
            }
        }
        return arreglo;
    }
}
