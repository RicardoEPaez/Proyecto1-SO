/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.proyecto.so;

import clases.AlgoritmoPlanificacion;
import clases.CPU;
import clases.Planificador;


/**
 *
 * @author ricar
 */
public class PanelProcesador extends javax.swing.JPanel {

    private final CPU cpu;
    private final Planificador planificador;
    private boolean sistemaAbortado = false;
    private clases.Reloj relojSistema;
    private javax.swing.Timer timerSimulacion;
    
    /**
     * Creates new form PanelProcesador
     */
    public PanelProcesador(CPU cpu, Planificador planificador) {
        this.cpu = cpu;
        this.planificador = planificador;
        initComponents();
        // En el Constructor:
        timerSimulacion = new javax.swing.Timer(100, new java.awt.event.ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            actualizarLabelsCPU(); // <--- ¡AQUÍ!
        
        repaint();
    }
    });
        timerSimulacion.start();
    }

    
    
        // --- MÉTODO AUXILIAR PARA NO REPETIR CÓDIGO ---
    private void detenerTodo() {
        // 1. Detener Backend
        if (relojSistema != null) {
            relojSistema.detener(); // Asumiendo que tu clase Reloj tiene este método
            relojSistema = null;
        }
        // 2. Detener Frontend (Opcional: Si quieres que se congele la pantalla)
        if (timerSimulacion != null && timerSimulacion.isRunning()) {
            timerSimulacion.stop();
        }
    }
    /**
     * Actualiza los textos del Panel CPU usando tu clase PCB.
     */
    /**
     * Actualiza los textos del Panel CPU usando tu clase PCB.
     */
    private void actualizarLabelsCPU() {
        
        // ---------------------------------------------------------
        // 0. ACTUALIZAR TIEMPO GLOBAL DE MISIÓN
        // ---------------------------------------------------------
        if (planificador != null) {
            // Obtenemos el tiempo total acumulado desde el Planificador o el Reloj
            int tiempoGlobal = (relojSistema != null) ? relojSistema.getCicloActual() : 0;
            
            // Si el reloj está null (pausa), intentamos mantener el último valor conocido si es posible,
            // pero por simplicidad aquí mostraremos el del ciclo actual o 0.
            // (Nota: Si tu clase Planificador guarda el tiempo total, úsalo aquí: planificador.getTiempoTotal())
            
            jLabel1.setText("TIEMPO DE MISION: T+ " + tiempoGlobal + " CICLOS");
        }

        // ---------------------------------------------------------
        // 1. CHEQUEO DE ABORTADO (Emergencia)
        // ---------------------------------------------------------
        if (sistemaAbortado) {
            lblCpuNombre.setText("---");
            lblCpuId.setText("--");
            lblCpuPC.setText("0 / 0");
            lblCpuEstado.setText("!! ABORTADO !!");
            lblCpuEstado.setForeground(java.awt.Color.RED);
            return;
        }
        
        // ---------------------------------------------------------
        // 2. OBTENER ESTADO DEL CPU
        // ---------------------------------------------------------
        clases.PCB proceso = cpu.getProcesoActual();

        // CASO A: CPU VACÍA (No hay procesos o acabaron todos)
        if (proceso == null) {
            lblCpuNombre.setText("Nombre del Proceso: ---");
            lblCpuId.setText("ID del Proceso: --");
            lblCpuPC.setText("Contador: 0 / 0");
            
            lblCpuEstado.setText("ESTADO: ESPERANDO");
            lblCpuEstado.setForeground(java.awt.Color.WHITE);
            return; 
        }

        // CASO B: PROCESO EXISTE PERO RELOJ DETENIDO (Pausa manual)
        if (relojSistema == null) {
            lblCpuNombre.setText(proceso.getNombre());
            lblCpuId.setText("ID del Proceso: " + proceso.getId());
            lblCpuPC.setText("Contador: " + proceso.getProgramCounter() + " / " + proceso.getInstruccionesTotales());
            
            if (!lblCpuEstado.getText().equals("PASO EJECUTADO")) {
                lblCpuEstado.setText(":: PAUSADO ::");
                lblCpuEstado.setForeground(java.awt.Color.ORANGE);
            }
            return; 
        }

        // --- SI HAY PROCESO Y RELOJ CORRIENDO (ESTADO EJECUTANDO) ---
        lblCpuNombre.setText(proceso.getNombre());
        lblCpuId.setText("ID del Proceso: " + proceso.getId());
        lblCpuPC.setText("Contador: " + proceso.getProgramCounter() + " / " + proceso.getInstruccionesTotales());
        
        lblCpuEstado.setText("ESTADO: EJECUTANDO");
        lblCpuEstado.setForeground(java.awt.Color.GREEN);
    }

  
   
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        comboAlgoritmos = new javax.swing.JComboBox<>();
        spinnerQuantum = new javax.swing.JSpinner();
        btnIniciar = new javax.swing.JButton();
        btnDetener = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        btnPaso = new javax.swing.JButton();
        btnAbortar = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        lblCpuEstado = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        lblCpuNombre = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        lblCpuId = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        lblCpuPC = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "CONTROL DE MISION", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        setForeground(new java.awt.Color(255, 255, 255));
        setToolTipText("");
        setLayout(null);

        comboAlgoritmos.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FCFS", "RoundRobin", "SPN", "SRT" }));
        add(comboAlgoritmos);
        comboAlgoritmos.setBounds(100, 80, 160, 30);

        spinnerQuantum.setModel(new javax.swing.SpinnerNumberModel(3, 1, null, 1));
        add(spinnerQuantum);
        spinnerQuantum.setBounds(280, 80, 100, 30);

        btnIniciar.setBackground(new java.awt.Color(0, 153, 51));
        btnIniciar.setText("INICIAR");
        btnIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIniciarActionPerformed(evt);
            }
        });
        add(btnIniciar);
        btnIniciar.setBounds(400, 70, 170, 50);

        btnDetener.setBackground(new java.awt.Color(204, 0, 0));
        btnDetener.setText("DETENER");
        btnDetener.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDetenerActionPerformed(evt);
            }
        });
        add(btnDetener);
        btnDetener.setBounds(580, 70, 170, 50);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel1.setText("TIEMPO DE MISION: T+ 0 CICLOS");
        add(jLabel1);
        jLabel1.setBounds(20, 40, 190, 40);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("ALGORITMO");
        add(jLabel2);
        jLabel2.setBounds(20, 90, 90, 16);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("QUANTUM");
        add(jLabel3);
        jLabel3.setBounds(290, 60, 80, 20);

        btnPaso.setBackground(new java.awt.Color(255, 204, 0));
        btnPaso.setText("PASO A PASO");
        btnPaso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPasoActionPerformed(evt);
            }
        });
        add(btnPaso);
        btnPaso.setBounds(760, 70, 160, 50);

        btnAbortar.setBackground(new java.awt.Color(153, 0, 0));
        btnAbortar.setText("EMERGENCY ABORT");
        btnAbortar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAbortarActionPerformed(evt);
            }
        });
        add(btnAbortar);
        btnAbortar.setBounds(930, 70, 180, 50);

        jPanel1.setBackground(new java.awt.Color(51, 51, 51));
        jPanel1.setForeground(new java.awt.Color(255, 255, 255));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Estado CPU: ");

        lblCpuEstado.setText("Esperando");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Nombre del Proceso");

        lblCpuNombre.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCpuNombre.setForeground(new java.awt.Color(255, 255, 255));
        lblCpuNombre.setText("---");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("ID del Proceso");

        lblCpuId.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCpuId.setForeground(new java.awt.Color(255, 255, 255));
        lblCpuId.setText("--");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Contador");

        lblCpuPC.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblCpuPC.setForeground(new java.awt.Color(255, 255, 255));
        lblCpuPC.setText("0");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addComponent(lblCpuPC, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(lblCpuEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblCpuId, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblCpuNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(165, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(lblCpuNombre, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(lblCpuId, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(lblCpuPC, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(lblCpuEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(159, 159, 159))
        );

        add(jPanel1);
        jPanel1.setBounds(10, 140, 450, 140);
    }// </editor-fold>//GEN-END:initComponents

    private void btnIniciarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIniciarActionPerformed
        sistemaAbortado = false;
        
        String algoritmoSeleccionado = comboAlgoritmos.getSelectedItem().toString();
        int quantumSeleccionado = (int) spinnerQuantum.getValue();
        
        
        System.out.println("Configurando simulacion -> Algoritmo: " + algoritmoSeleccionado + " | Quantum: " + quantumSeleccionado);
        
        
        
         cpu.setQuantum(quantumSeleccionado);
        
      
         
         clases.AlgoritmoPlanificacion nuevoAlgoritmo = null; 
        
        switch (algoritmoSeleccionado) {
            case "FCFS":
                nuevoAlgoritmo = new clases.AlgoritmoFCFS(); 
                break;
            case "RoundRobin":
                nuevoAlgoritmo = new clases.AlgoritmoRoundRobin(quantumSeleccionado); 
                break;
            //case "SPN":
                //nuevoAlgoritmo = new clases.AlgoritmoEDF(); 
                //break;
            case "SRT":
                nuevoAlgoritmo = new clases.AlgoritmoSRT(); 
                break;
        }

        // Enviamos el objeto al Planificador usando tu método seguro con Mutex
        if (nuevoAlgoritmo != null) {
            planificador.setAlgoritmo(nuevoAlgoritmo);
        }
        // =========================================================
         
        // 1. Cargar Procesos (Solo si está vacío o es el inicio)
        if (planificador.getColaListos().estaVacia()&& cpu.getProcesoActual() == null) {
            System.out.println(">>> Generando carga inicial de 20 procesos...");
            for (int i = 0; i < 20; i++) {
                clases.PCB proceso = clases.GeneradorProcesos.generarProcesoAleatorio();
                planificador.agregarProceso(proceso);
            }
        }
        
        // 2. Arrancar el Reloj Lógico (Backend)
        if (relojSistema == null || !relojSistema.isAlive()) {
            relojSistema = new clases.Reloj(planificador, cpu);
            relojSistema.start();
        }
        
        // 3. Arrancar el Reloj Visual (Frontend)
        if (timerSimulacion != null && !timerSimulacion.isRunning()) {
            timerSimulacion.start();
        }
    }//GEN-LAST:event_btnIniciarActionPerformed
       
    
    private void btnDetenerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetenerActionPerformed
        // Llamamos al método que creamos arriba
        detenerTodo();
        
        // Forzamos actualización visual inmediata
        lblCpuEstado.setText(":: PAUSADO ::");
        lblCpuEstado.setForeground(java.awt.Color.ORANGE);
        repaint();
    }//GEN-LAST:event_btnDetenerActionPerformed

    private void btnPasoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPasoActionPerformed
    // 1. Detener automático
        detenerTodo();

        // 2. Dar un empujón al CPU
        synchronized(cpu) {
            cpu.notify();
        }
        
        // 3. Actualizar visualmente una vez
        actualizarLabelsCPU();
        lblCpuEstado.setText("PASO EJECUTADO");
        lblCpuEstado.setForeground(java.awt.Color.YELLOW);
    }//GEN-LAST:event_btnPasoActionPerformed

    private void btnAbortarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAbortarActionPerformed
       // 1. Activar bloqueo visual (para que el Timer no moleste)
        sistemaAbortado = true; 

        // 2. Matar el reloj del sistema
        if (relojSistema != null) {
            relojSistema.detener();
            relojSistema = null;
        }

        // 3. REINICIAR EL PROCESO ACTUAL
        clases.PCB proceso = cpu.getProcesoActual();
        
        if (proceso != null) {
            // Ahora sí funciona este método porque lo acabamos de crear en el PCB
            proceso.reiniciar(); 
        }
        
        // 4. Mensaje visual de confirmación
        lblCpuEstado.setText("!! ABORTADO !!");
        lblCpuEstado.setForeground(java.awt.Color.RED);
        
    }//GEN-LAST:event_btnAbortarActionPerformed

   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAbortar;
    private javax.swing.JButton btnDetener;
    private javax.swing.JButton btnIniciar;
    private javax.swing.JButton btnPaso;
    private javax.swing.JComboBox<String> comboAlgoritmos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblCpuEstado;
    private javax.swing.JLabel lblCpuId;
    private javax.swing.JLabel lblCpuNombre;
    private javax.swing.JLabel lblCpuPC;
    private javax.swing.JSpinner spinnerQuantum;
    // End of variables declaration//GEN-END:variables


}


