/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.proyecto.so;

import clases.CPU;
import clases.PCB;
import clases.Planificador;
import java.awt.BorderLayout;

/**
 * Interfaz Grafica que controla y visualiza el estado del CPU, memoria y procesos en tiempo real
 * @author ricar
 */
public class ProyectoSO extends javax.swing.JFrame {

    // Referencias globales para que no se pierdan
    private CPU cpu;
    private Planificador planificador;
    private PanelProcesador panelVisual;

    /**
     * Creates new form ProyectoSO
     */
    public ProyectoSO() {
        // 1. Inicializar componentes básicos de la ventana (NetBeans)
        initComponents();
        
        // 2. CONFIGURACIÓN DE LA VENTANA
        this.setTitle("Simulador SO - NASA Mode");
        this.setSize(1100, 700); // Tamaño grande
        this.setLocationRelativeTo(null); // Centrar en pantalla
        this.setLayout(new BorderLayout()); // Para que el panel ocupe todo el espacio
        
        // 3. INICIALIZAR EL BACKEND (Cerebro)
        iniciarSistemaOperativo();
        
        // 4. INICIALIZAR EL FRONTEND (Tu Panel)
        panelVisual = new PanelProcesador(cpu, planificador);
        
        // 5. AGREGAR EL PANEL A LA VENTANA
        this.add(panelVisual, BorderLayout.CENTER);
        
        // 6. Refrescar para asegurar que se vea
        this.revalidate();
        this.repaint();
    }
    
    private void iniciarSistemaOperativo() {
        // Creamos el planificador (Kernel)
        planificador = new Planificador();
        
        // Creamos el CPU con un Quantum por defecto de 3
        cpu = new CPU(3, planificador);
        
        // Conectamos ambos
        planificador.setCPU(cpu);
        
        // Arrancamos el hilo del CPU (IMPORTANTE: Si no, no funciona "isAlive")
        cpu.start();
        
        // (Opcional) Agregamos un proceso de prueba para que no salga vacío al inicio
        PCB prueba = new PCB("System Check", 50, 1, 0, 0, 0, 32);
        planificador.agregarProceso(prueba);
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1121, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 639, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

 

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
