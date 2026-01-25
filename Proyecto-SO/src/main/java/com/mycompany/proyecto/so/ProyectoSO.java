/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.proyecto.so;

import clases.CPU;
import clases.PCB;
/**
 *
 * @author ricar
 */
public class ProyectoSO {

    public static void main(String[] args) {
        System.out.println("----Iniciando Simulacion----!");
        // 1. Crear el CPU
        CPU cpu = new CPU();
        
        // 2. Crear un proceso de prueba 
        // (Nombre, Instr, Prio, Deadline, CicloIO, LenIO)
        PCB procesoPrueba = new PCB("Proceso_Test_1", 5, 1, 10, 2, 3);
        
        // 3. Arrancar el CPU
        cpu.start(); 
        
        // 4. A los 2 segundos, le asignamos el proceso
        try { 
            Thread.sleep(2000); 
            cpu.asignarProceso(procesoPrueba);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
