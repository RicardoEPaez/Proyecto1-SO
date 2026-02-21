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
    private clases.GeneradorInterrupciones generadorInterrupciones;
    private clases.HiloEntorno hiloEntorno;
    private org.jfree.data.xy.XYSeries serieUsoCPU;
    private int ultimoCicloGraficado = 0;
    private int ciclosCPUActivo = 0;
    
    /**
     * Creates new form PanelProcesador
     */
    public PanelProcesador(CPU cpu, Planificador planificador) {
        this.cpu = cpu;
        this.planificador = planificador;
        initComponents();
        actualizarLabelVelocidad();
        inicializarGrafico();
        // En el Constructor:
        timerSimulacion = new javax.swing.Timer(100, new java.awt.event.ActionListener() {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            actualizarLabelsCPU(); // <--- ¡AQUÍ!
            actualizarMetricas();
            actualizarDatosGrafico();
        
        repaint();
    }
    });
        timerSimulacion.start();
    }

    
    
   
    private void detenerTodo() {
        // 1. Detener Backend (Reloj)
        if (relojSistema != null) {
            relojSistema.detener(); 
            relojSistema = null;
        }
        
        // 2. Detener Generador de Interrupciones (NUEVO)
        if (generadorInterrupciones != null) {
            generadorInterrupciones.detener();
            generadorInterrupciones.interrupt(); // Lo despertamos por si estaba durmiendo (Thread.sleep)
            generadorInterrupciones = null;
        }
        
        // 3. Detener Hilo del Entorno
        if (hiloEntorno != null){
            hiloEntorno.detener();
            hiloEntorno = null;
        }

        // 4. Detener Frontend (Timer Visual)
        if (timerSimulacion != null && timerSimulacion.isRunning()) {
            timerSimulacion.stop();
        }
        
        // 5. CERRAR LAS PUERTAS DE LA INTERFAZ
        planificador.setSistemaCorriendo(false);
    }
      
    
    /**
     * Actualiza los textos del Panel CPU usando tu clase PCB.
     */
    private void actualizarLabelsCPU() {
        
        // ---------------------------------------------------------
        // 0. ACTUALIZAR TIEMPO GLOBAL DE MISIÓN
        // ---------------------------------------------------------
        if (planificador != null) {
            int tiempoGlobal = (relojSistema != null) ? relojSistema.getCicloActual() : 0;
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
        
        // CHEQUEO DE RUTINA DE INTERRUPCIÓN (ISR)
        if (cpu.isEnRutinaISR()) {
            lblCpuNombre.setText("RUTINA ISR");
            lblCpuId.setText("ID: SYSTEM");
            lblCpuPC.setText("Resolviendo...");
            
            lblCpuEstado.setText(":: INTERRUMPIDO ::");
            lblCpuEstado.setForeground(java.awt.Color.cyan); 
            return; // Salimos para no evaluar los demás casos
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

        // Si llegamos aquí, SÍ hay proceso en el CPU. 
        lblCpuNombre.setText(proceso.getNombre());
        lblCpuId.setText("ID del Proceso: " + proceso.getId());
        lblCpuPC.setText("Contador: " + proceso.getProgramCounter() + " / " + proceso.getInstruccionesTotales());

        // CASO B: PROCESO EXISTE PERO RELOJ DETENIDO (Pausa manual)
        if (relojSistema == null) {
            if (!lblCpuEstado.getText().equals("PASO EJECUTADO")) {
                lblCpuEstado.setText(":: PAUSADO ::");
                lblCpuEstado.setForeground(java.awt.Color.ORANGE);
            }
            return; 
        }

        // ---------------------------------------------------------
        // 3. ESTADO DINÁMICO (MIENTRAS CORRE EL RELOJ)
        // ---------------------------------------------------------
        // Leemos el estado interno del proceso
        clases.Estado estadoActual = proceso.getEstado();

        // CASO C: (Este ya no se disparará mucho por las ISR externas, pero es útil mantenerlo por si el planificador hace expropiaciones internas)
        if (estadoActual == clases.Estado.BLOQUEADO) {
            lblCpuEstado.setText(":: BLOQUEADO ::");
            lblCpuEstado.setForeground(java.awt.Color.cyan);
        } 
        // CASO D: EJECUCIÓN NORMAL
        else {
            lblCpuEstado.setText("ESTADO: EJECUTANDO");
            lblCpuEstado.setForeground(java.awt.Color.GREEN);
        }
    }
  
   
    // --- 📊 MÉTODO PARA ACTUALIZAR LA TELEMETRÍA EN PANTALLA ---
    private void actualizarMetricas() {
        if (planificador != null) {
            // Obtenemos los valores y los formateamos a 2 decimales para que se vea profesional
            String tasaExito = String.format("%.2f %%", planificador.getTasaExitoMision());
            String esperaProm = String.format("%.2f Ciclos", planificador.getTiempoEsperaPromedio());
            String throughput = String.format("%.4f Proc/Ciclo", planificador.getThroughput());
            
            // Textos para los JLabels (Cambia los nombres si les pusiste otro diferente en NetBeans)
            lblTasaExito.setText("Tasa de Éxito: " + tasaExito);
            lblEsperaPromedio.setText("Espera Promedio: " + esperaProm);
            lblThroughput.setText("Throughput: " + throughput);
            
            // Muestra el total y cuántos fueron exitosos
            int totales = planificador.getTotalProcesosTerminados();
            int exitos = planificador.getMisionesExitosas();
            lblProcesosTerminados.setText("Finalizados: " + totales + " (" + exitos + " a tiempo)");
            
            // Opcional: Cambiar de color la tasa de éxito si baja mucho (Simulación de riesgo espacial)
            if (planificador.getTasaExitoMision() < 50.0 && totales > 0) {
                lblTasaExito.setForeground(java.awt.Color.RED);
            } else {
                lblTasaExito.setForeground(java.awt.Color.GREEN);
            }
        }
    }
    
    // --- 📈 MÉTODO PARA INICIALIZAR EL GRÁFICO ---
    private void inicializarGrafico() {
        // 1. Crear la serie de datos (Línea del gráfico)
        serieUsoCPU = new org.jfree.data.xy.XYSeries("Uso del CPU (%)");
        org.jfree.data.xy.XYSeriesCollection dataset = new org.jfree.data.xy.XYSeriesCollection(serieUsoCPU);

        // 2. Crear el gráfico XY
        org.jfree.chart.JFreeChart chart = org.jfree.chart.ChartFactory.createXYLineChart(
            "Utilización del Procesador en Tiempo Real", // Título
            "Tiempo (Ciclos Globales)",                  // Eje X
            "Uso de CPU (%)",                            // Eje Y
            dataset,
            org.jfree.chart.plot.PlotOrientation.VERTICAL,
            false, true, false
        );

        // 3. Estilo Visual (Modo oscuro / Espacial)
        chart.setBackgroundPaint(java.awt.Color.DARK_GRAY);
        chart.getTitle().setPaint(java.awt.Color.WHITE);
        
        org.jfree.chart.plot.XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(java.awt.Color.BLACK);
        plot.getDomainAxis().setTickLabelPaint(java.awt.Color.WHITE);
        plot.getDomainAxis().setLabelPaint(java.awt.Color.WHITE);
        plot.getRangeAxis().setTickLabelPaint(java.awt.Color.WHITE);
        plot.getRangeAxis().setLabelPaint(java.awt.Color.WHITE);
        
        // Fjar el eje Y de 0% a 105% para que no ande saltando
        plot.getRangeAxis().setRange(0.0, 105.0); 
        plot.getRenderer().setSeriesPaint(0, java.awt.Color.CYAN); // Línea color Cyan

        // 1. Creamos el panel del gráfico
        org.jfree.chart.ChartPanel chartPanel = new org.jfree.chart.ChartPanel(chart);
        
        // 2. Creamos la ventana flotante
        javax.swing.JFrame ventanaGrafico = new javax.swing.JFrame("Telemetría en Vivo - Uso de CPU");
        ventanaGrafico.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
        ventanaGrafico.setSize(800, 450); // Un buen tamaño panorámico
        
        // ====================================================================
        // 3. ¡LA LÍNEA MÁGICA! 
        // Esto obliga a la ventana a quedarse SIEMPRE por encima de la principal
        ventanaGrafico.setAlwaysOnTop(true); 
        // ====================================================================

        // 4. Metemos el gráfico
        ventanaGrafico.setLayout(new java.awt.BorderLayout());
        ventanaGrafico.add(chartPanel, java.awt.BorderLayout.CENTER);
        
        // 5. Evitamos que quite el foco a la ventana principal al aparecer
        ventanaGrafico.setFocusableWindowState(false); 
        
        // 6. La mostramos
        ventanaGrafico.setLocationRelativeTo(null); // Centrada
        ventanaGrafico.setVisible(true);
    }
    
    
    
    private void actualizarDatosGrafico() {
        if (cpu == null || relojSistema == null) return;
        
        int cicloActual = relojSistema.getCicloActual();
        
        // Solo añadimos un punto si el Reloj Global avanzó un ciclo nuevo
        if (cicloActual > ultimoCicloGraficado) {
            ultimoCicloGraficado = cicloActual;
            
            // Si el CPU tiene un proceso asignado, sumamos un ciclo de actividad
            if (cpu.getProcesoActual() != null) {
                ciclosCPUActivo++;
            }
            
            // Calculamos el porcentaje histórico de uso (Ciclos Trabajando / Ciclos Totales)
            double porcentajeUso = ((double) ciclosCPUActivo / cicloActual) * 100.0;
            
            // Añadimos el nuevo punto al gráfico
            serieUsoCPU.add(cicloActual, porcentajeUso);
        }
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
        jPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        lblEsperaPromedio = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        lblThroughput = new javax.swing.JLabel();
        lblProcesosTerminados = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        lblTasaExito = new javax.swing.JLabel();
        sliderVelocidad = new javax.swing.JSlider();
        lblValorVelocidad = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "CONTROL DE MISION", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        setForeground(new java.awt.Color(255, 255, 255));
        setToolTipText("");
        setLayout(null);

        comboAlgoritmos.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FCFS", "RoundRobin", "SPN", "SRT", "EDF", "" }));
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
        jLabel1.setBounds(20, 40, 220, 40);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("ALGORITMO");
        add(jLabel2);
        jLabel2.setBounds(20, 90, 90, 16);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("QUANTUM");
        add(jLabel3);
        jLabel3.setBounds(290, 60, 80, 20);

        btnAbortar.setBackground(new java.awt.Color(153, 0, 0));
        btnAbortar.setText("EMERGENCY ABORT");
        btnAbortar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAbortarActionPerformed(evt);
            }
        });
        add(btnAbortar);
        btnAbortar.setBounds(760, 70, 180, 50);

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
                .addContainerGap(135, Short.MAX_VALUE))
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
        jPanel1.setBounds(10, 140, 420, 140);

        jPanel2.setBackground(new java.awt.Color(51, 51, 51));
        jPanel2.setForeground(new java.awt.Color(255, 255, 255));

        jLabel8.setText("Tasa Exito");

        lblEsperaPromedio.setForeground(new java.awt.Color(255, 255, 255));

        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Espera Promedio");

        jLabel10.setText("Throughtput");
        jLabel10.setToolTipText("");

        lblThroughput.setForeground(new java.awt.Color(255, 255, 255));
        lblThroughput.setToolTipText("");

        lblProcesosTerminados.setForeground(new java.awt.Color(255, 255, 255));
        lblProcesosTerminados.setToolTipText("");

        jLabel11.setText("Procesos Terminados");
        jLabel11.setToolTipText("");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblThroughput, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblProcesosTerminados, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblTasaExito, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblEsperaPromedio, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 87, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblTasaExito, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblEsperaPromedio, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblThroughput, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblProcesosTerminados, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(7, 7, 7))
        );

        add(jPanel2);
        jPanel2.setBounds(440, 140, 380, 140);

        sliderVelocidad.setMaximum(2000);
        sliderVelocidad.setMinimum(50);
        sliderVelocidad.setValue(1000);
        sliderVelocidad.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderVelocidadStateChanged(evt);
            }
        });
        add(sliderVelocidad);
        sliderVelocidad.setBounds(170, 20, 200, 22);

        lblValorVelocidad.setText("1000 ms");
        add(lblValorVelocidad);
        lblValorVelocidad.setBounds(370, 20, 50, 16);

        jLabel12.setText("DURACION DEL CICLO");
        add(jLabel12);
        jLabel12.setBounds(20, 20, 130, 16);
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
            case "SPN":
                nuevoAlgoritmo = new clases.AlgoritmoPrioridad(); 
                break;
            case "SRT":
                nuevoAlgoritmo = new clases.AlgoritmoSRT(); 
                break;
            case "EDF":
                nuevoAlgoritmo = new clases.AlgoritmoEDF();
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
            
            // Le decimos al nuevo reloj que empiece donde se quedó la gráfica
            relojSistema.setCicloActual(ultimoCicloGraficado);
            
           // Sincronizar velocidad con el Slider antes de arrancar
                int msIniciales = sliderVelocidad.getValue();
                relojSistema.setTiempoCiclo(msIniciales);

                // Aplicamos la misma regla visual aquí al arrancar:
                if (msIniciales >= 1000) {
                    lblValorVelocidad.setText(String.format("%.1f s", msIniciales / 1000.0));
                } else {
                    lblValorVelocidad.setText(msIniciales + " ms");
                }
    
            relojSistema.start();
        }
        
        // 3. Arrancar el Reloj Visual (Frontend)
        if (timerSimulacion != null && !timerSimulacion.isRunning()) {
            timerSimulacion.start();
        }
        
        // 4. Arrancar generador de interrupciones asíncronas
        if (generadorInterrupciones == null || !generadorInterrupciones.isAlive()) {
            generadorInterrupciones = new clases.GeneradorInterrupciones(cpu);
            generadorInterrupciones.start();
        }
        
        // 5. Arrancar el entorno (llegada aleatoria de procesos normales)
        if (hiloEntorno == null || !hiloEntorno.isAlive()){
            hiloEntorno = new clases.HiloEntorno(planificador);
            hiloEntorno.start();
        }
        
        // 6. ABRIR LAS PUERTAS DE LA INTERFAZ
        // Avisamos que la simulación ya arrancó para que los botones funcionen
        planificador.setSistemaCorriendo(true);
    }//GEN-LAST:event_btnIniciarActionPerformed
       
    
    private void actualizarLabelVelocidad() {
        int ms = sliderVelocidad.getValue();
        if (ms >= 1000) {
         double segundos = ms / 1000.0;
        lblValorVelocidad.setText(String.format("%.1f s", segundos)); 
    } else {
        lblValorVelocidad.setText(ms + " ms");
    }
    }
    
    
    private void btnDetenerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDetenerActionPerformed
        // Llamamos al método que creamos arriba
        detenerTodo();
        
        // Forzamos actualización visual inmediata
        lblCpuEstado.setText(":: PAUSADO ::");
        lblCpuEstado.setForeground(java.awt.Color.ORANGE);
        repaint();
    }//GEN-LAST:event_btnDetenerActionPerformed

    private void btnAbortarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAbortarActionPerformed
       // 1. Activar bloqueo visual (para que el Timer no moleste)
        sistemaAbortado = true; 

        // 2. Matar el reloj del sistema
        detenerTodo();

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

    private void sliderVelocidadStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderVelocidadStateChanged
       actualizarLabelVelocidad(); // Actualiza el texto con ms o s
    
        // Actualiza el reloj en tiempo real
        if (relojSistema != null) {
            relojSistema.setTiempoCiclo(sliderVelocidad.getValue());
        }
    }//GEN-LAST:event_sliderVelocidadStateChanged

   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAbortar;
    private javax.swing.JButton btnDetener;
    private javax.swing.JButton btnIniciar;
    private javax.swing.JComboBox<String> comboAlgoritmos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblCpuEstado;
    private javax.swing.JLabel lblCpuId;
    private javax.swing.JLabel lblCpuNombre;
    private javax.swing.JLabel lblCpuPC;
    private javax.swing.JLabel lblEsperaPromedio;
    private javax.swing.JLabel lblProcesosTerminados;
    private javax.swing.JLabel lblTasaExito;
    private javax.swing.JLabel lblThroughput;
    private javax.swing.JLabel lblValorVelocidad;
    private javax.swing.JSlider sliderVelocidad;
    private javax.swing.JSpinner spinnerQuantum;
    // End of variables declaration//GEN-END:variables


}


