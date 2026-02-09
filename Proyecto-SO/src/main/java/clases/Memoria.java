/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

/**
 * Gestor de Memoria (Simulación de Particiones Fijas)
 * @author Ramon-Carrasquel
 */
public class Memoria {
    
    // Configuración
    private final int MAX_MEMORIA = 1024; // 1024 MB Totales
    private int memoriaDisponible;
    
    // Representación de la RAM: 
    // Para simplificar, diremos que la RAM puede aguantar máximo N procesos simultáneos
    // o podemos hacerlo por bloques. Haremos una simplificación de "Slots".
    private PCB[] particiones;
    
    public Memoria() {
        this.memoriaDisponible = MAX_MEMORIA;
        this.particiones = new PCB[10]; // Digamos que el satélite soporta 10 procesos en RAM simultáneos
    }

    /**
     * Intenta cargar un proceso en memoria (RAM).
     * @param proceso El objeto PCB que intenta entrar a la RAM.
     * @return true si se pudo cargar, false si la memoria está llena o no hay slots.
     */
    public boolean cargarEnMemoria(PCB proceso) {
        if (proceso.getTamano() > memoriaDisponible) {
            return false; // No hay espacio físico suficiente
        }

        // Buscamos un slot vacío en el arreglo
        for (int i = 0; i < particiones.length; i++) {
            if (particiones[i] == null) {
                particiones[i] = proceso;
                proceso.setDireccionMemoria(i); // Guardamos dónde quedó
                memoriaDisponible -= proceso.getTamano();
                return true; // Éxito
            }
        }
        return false; // No hay slots (aunque haya MB libres, no hay particiones)
    }

    /**
     * Saca un proceso de memoria (Swap Out o Terminado).
     * @param proceso El proceso que libera su espacio.
     */
    public void liberarMemoria(PCB proceso) {
        int idx = proceso.getDireccionMemoria();
        
        if (idx != -1 && particiones[idx] == proceso) {
            particiones[idx] = null; // Liberamos el slot
            memoriaDisponible += proceso.getTamano(); // Recuperamos los MB
            proceso.setDireccionMemoria(-1);
        }
    }
    
    // --- MÉTODOS PARA LA INTERFAZ GRÁFICA (GUI) ---

    // Permite a la GUI ver el estado de los bloques de memoria
    public PCB[] getParticiones() {
        return particiones;
    }
    
    // Permite a la GUI saber cuánto espacio libre queda para mostrarlo en un Label
    public int getMemoriaDisponible() { return memoriaDisponible; }
}
