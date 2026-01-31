/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.proyecto.so;

import clases.CPU;
import clases.PCB;
import clases.Planificador;

/**
 *
 * @author ricar
 */
public class ProyectoFinal {

    public static void main(String[] args) {
        System.out.println("=== PROYECTO SO: INICIO DE SIMULACIÓN ===");

        try {
            // 1. Instanciamos el Planificador (El de tu amigo)
            Planificador planificador = new Planificador();

            // 2. Instanciamos el CPU (Tu código)
            // Quantum de 4 ciclos
            CPU cpu = new CPU(4, planificador);

            // 3. Creamos Procesos de Prueba
            // PCB(nombre, totalInst, prioridad, deadline, cicloInicioIO, duracionIO)
            
            // Proceso 1: Word (Largo, saldrá por Quantum varias veces)
            PCB p1 = new PCB("Word", 10, 1, 100, 0, 0);
            
            // Proceso 2: Music (Pide I/O en la instrucción 2, tarda 5 ciclos esperando)
            PCB p2 = new PCB("Music", 6, 2, 100, 2, 5);
            
            // Proceso 3: Notas (Corto, termina rápido)
            PCB p3 = new PCB("Notas", 2, 3, 100, 0, 0);

            // 4. Cargamos los procesos al sistema
            System.out.println("--- Cargando procesos ---");
            planificador.agregarProceso(p1);
            planificador.agregarProceso(p2);
            planificador.agregarProceso(p3);

            // 5. Encendemos el CPU
            System.out.println("--- Iniciando CPU ---");
            cpu.start();
            
        } catch (Exception e) {
            System.out.println("Ocurrió un error en la inicialización: " + e.getMessage());
            e.printStackTrace();
        }
    }
}