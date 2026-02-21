/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

import java.util.concurrent.Semaphore; // <--- IMPORTAR

/**
 * Gestor de Memoria con SEMÁFOROS (Exclusión Mutua)
 * @author Ramon-Carrasquel
 */
public class Memoria {
    
    // Configuración
    private final int MAX_MEMORIA = 1024; 
    private int memoriaDisponible;
    private PCB[] particiones;
    
    // SEMÁFORO (MUTEX)
    private final Semaphore mutex = new Semaphore(1); // Permiso único

    public Memoria() {
        this.memoriaDisponible = MAX_MEMORIA;
        this.particiones = new PCB[10]; 
    }

    public boolean cargarEnMemoria(PCB proceso) {
        boolean exito = false;
        try {
            mutex.acquire(); // <--- BLOQUEO
            
            // --- SECCIÓN CRÍTICA ---
            if (proceso.getTamano() <= memoriaDisponible) {
                // Buscamos slot vacío
                for (int i = 0; i < particiones.length; i++) {
                    if (particiones[i] == null) {
                        particiones[i] = proceso;
                        proceso.setDireccionMemoria(i); 
                        memoriaDisponible -= proceso.getTamano();
                        exito = true;
                        break; // Salimos del for
                    }
                }
            }
            // -----------------------
            
        } catch (InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            mutex.release(); // <--- DESBLOQUEO
        }
        return exito;
    }

    public void liberarMemoria(PCB proceso) {
        try {
            mutex.acquire(); // <--- BLOQUEO
            
            int idx = proceso.getDireccionMemoria();
            if (idx != -1 && particiones[idx] == proceso) {
                particiones[idx] = null; 
                memoriaDisponible += proceso.getTamano(); 
                proceso.setDireccionMemoria(-1);
            }
            
        } catch (InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            mutex.release(); // <--- DESBLOQUEO
        }
    }
    
    // --- MÉTODOS PARA LA GUI ---
    // Es buena práctica proteger también la lectura para que la GUI no lea un estado "a medias".

    public PCB[] getParticiones() {
        PCB[] copia = null;
        try {
            mutex.acquire();
            // Retornamos el array
            copia = particiones; 
        } catch (InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            mutex.release();
        }
        return copia;
    }
    
    public int getMemoriaDisponible() { 
        int mem = 0;
        try {
            mutex.acquire();
            mem = memoriaDisponible;
        } catch (InterruptedException e) { System.err.println("Error: " + e.getMessage()); } 
        finally { mutex.release(); }
        return mem;
    }
}
