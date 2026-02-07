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
public class ListaEnlazada<T> {
    public Nodo<T> inicio;
    private int tamano;
    
    public ListaEnlazada(){
        this.inicio = null;
        this.tamano = 0;
    }
    
    // Para insertar al final de la lista
    public void agregar(T dato){
        Nodo<T> nuevo = new Nodo <>(dato);
        if (estaVacia()) {
            inicio = nuevo;
        } else {
            Nodo<T> aux = inicio;
            while (aux.getSiguiente() != null){
                aux = aux.getSiguiente();
            }
            aux.setSiguiente(nuevo);
        }
        tamano++;
    }
    
    // Metodo para eliminar un objeto especifico, lo cual es util para mover procesos entre colas
    public void eliminar(T dato) {
        if (estaVacia()) return;
        
        if (inicio.getContenido().equals(dato)){
            inicio = inicio.getSiguiente();
            tamano--;
            return; 
        }
        
        Nodo<T> actual = inicio;
        // Verificamos que actual no sea null antes de empezar el ciclo
        if (actual == null) return; 

        while (actual.getSiguiente() != null && !actual.getSiguiente().getContenido().equals(dato)){
            actual = actual.getSiguiente();
        }
        
        // Si encontramos el nodo (es decir, el ciclo paró porque encontró el dato, no porque llegó al final)
        if (actual.getSiguiente() != null){
            actual.setSiguiente(actual.getSiguiente().getSiguiente());
            tamano--;
        }
    }
    
    // Obtener elementos por indice para recorrer la lista en bucles
    public T get(int indice){
        if (indice < 0 || indice >= tamano) return null;
        
        Nodo<T> actual = inicio;
        for (int i = 0; i < indice; i++){
            actual = actual.getSiguiente();
        }
        return actual.getContenido();
    }
    
    // Para determinar si la lista esta vacia
    public boolean estaVacia(){
        return inicio == null;
    }
    
    public int getTamano(){
        return tamano;
    }
    
    // Getter del nodo inicio, el cual es util si se necesita iterar manualmente fuera de la clase
    public Nodo<T> getInicio(){
        return inicio;
    }
}