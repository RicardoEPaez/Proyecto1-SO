/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.proyecto.so;

import clases.CPU;
import clases.Memoria;
import clases.PCB;
import clases.Planificador;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

/**
 * Interfaz Grafica que controla y visualiza el estado del CPU, memoria y procesos en tiempo real
 * @author ricar
 */
public class ProyectoSO extends javax.swing.JFrame {

    // Referencias globales para que no se pierdan
    private CPU cpu;
    private Memoria memoria;
    private Planificador planificador;
    private PanelProcesador panelVisual;
    private PanelCreador panelCreador;   // Sur
    private PanelColas panelColas;       // Oeste (Izquierda)
    private PanelMemoria panelMemoria;   // Este (Derecha)
    
    public ProyectoSO() {
        // 1. Iniciar componentes visuales
        initComponents();

        // 2. Configuración Básica de la Ventana
        this.setTitle("NASA Mission Control - Process Simulator");
        this.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH); // Pantalla completa
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        // USAMOS BORDERLAYOUT (Norte, Sur, Este, Oeste, Centro)
        this.setLayout(new java.awt.BorderLayout(0, 0)); 

        // Iniciar lógica del SO (CPU, Memoria, etc)
        iniciarSistemaOperativo();
        
      
        
        // =========================================================
        // A. ZONA NORTE: PROCESADOR (Panel Gris)
        // =========================================================
        panelVisual = new PanelProcesador(cpu, planificador);
        // Altura de 290px: Suficiente para ver datos, pero deja espacio abajo
        panelVisual.setPreferredSize(new java.awt.Dimension(0, 290)); 
        this.add(panelVisual, java.awt.BorderLayout.NORTH);

        // =========================================================
        // B. ZONA CENTRO: COLAS Y MEMORIA
        // =========================================================
        // 1. Usamos un "Box Vertical" para apilar: Arriba las Tablas, Abajo el Creador
    // Esto evita que el Creador se vaya al fondo de la pantalla.
    javax.swing.Box contenedorVertical = javax.swing.Box.createVerticalBox();
    
    // --- NIVEL 1: LAS TABLAS (Colas + Memoria) ---
    javax.swing.JPanel filaTablas = new javax.swing.JPanel();
    // FlowLayout a la IZQUIERDA para pegar Memoria con Colas
    filaTablas.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
    
    // Agregar Panel de Colas
    panelColas = new PanelColas();
    filaTablas.add(panelColas);
    
    // Agregar Panel de Memoria
    panelMemoria = new PanelMemoria();
    // AUMENTAMOS LA ALTURA: De 350 a 430
    // Esto soluciona que se corte la barra de porcentaje al final
    panelMemoria.setPreferredSize(new java.awt.Dimension(360, 430)); 
    filaTablas.add(panelMemoria);
    
    // Alineamos la fila de tablas a la izquierda
    filaTablas.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
    contenedorVertical.add(filaTablas);
    
    // --- NIVEL 2: EL CREADOR (Nombre, Botones, etc.) ---
    // Espacio pequeño de separación (10px) en lugar del hueco gigante
    contenedorVertical.add(javax.swing.Box.createVerticalStrut(10));
    
    panelCreador = new PanelCreador();
    panelCreador.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
    contenedorVertical.add(panelCreador);
    
    // --- 3. AGREGAR TODO AL CENTRO ---
    // Usamos un panel contenedor final para que todo fluya desde arriba-izquierda
    javax.swing.JPanel panelFinal = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
    panelFinal.add(contenedorVertical);
    
    this.add(panelFinal, java.awt.BorderLayout.CENTER);

        // =========================================================
        // C. ZONA SUR: CREADOR DE PROCESOS (Barra de abajo)
        // =========================================================
        PanelCreador panelCreador = new PanelCreador();
        panelCreador.setPlanificador(planificador);

        // Altura fija de 80px para la barra de botones
        panelCreador.setPreferredSize(new java.awt.Dimension(0, 80));

        this.add(panelCreador, java.awt.BorderLayout.SOUTH);

        iniciarRefrescoTablas();
        
        // Finalizar
        this.setVisible(true);
    }
    
    private void iniciarSistemaOperativo() {
        
        // Instanciamos la memoria
        memoria = new Memoria();
        
        // Creamos el planificador (Kernel)
        planificador = new Planificador();
        
        // Conectamos la memoria con el planificador
        planificador.setMemoria(memoria);
        
        // Creamos el CPU con un Quantum por defecto de 3
        cpu = new CPU(3, planificador);
        
        // Conectamos ambos
        planificador.setCPU(cpu);
        
        // Arrancamos el hilo del CPU (IMPORTANTE: Si no, no funciona "isAlive")
        cpu.start();
        
    }
    
 
    private void iniciarRefrescoTablas() {
        javax.swing.Timer timerVisual = new javax.swing.Timer(500, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (panelColas != null && planificador != null) {
                    panelColas.actualizarColas(
                        planificador.getColaListos(), 
                        planificador.getColaBloqueados()
                    );
                }
                // Si tu panel de memoria necesita refresco explícito, hazlo aquí también
                if (panelMemoria != null && memoria != null) {
                    panelMemoria.actualizarMemoria(memoria);
                } 
            }
        });
        timerVisual.start();
    }
    
    public void actualizarTablas() {
        // Si los paneles existen y hay datos en el planificador...
        if (panelColas != null && planificador != null) {
            // ... MANDAMOS LOS DATOS A LA TABLA
            panelColas.actualizarColas(
                planificador.getColaListos(), 
                planificador.getColaBloqueados()
            );
        }
        // Refrescar pantalla
        this.repaint();
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
