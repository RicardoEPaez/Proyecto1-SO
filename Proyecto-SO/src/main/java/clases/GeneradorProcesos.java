/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

import java.util.Random;

/**
 * Clase utilitaria para la generación automática de procesos
 * Simula la carga de trabajo del satélite.
 * @author Ramon-Carrasquel
 */
public class GeneradorProcesos {
    private static final Random random = new Random();
    private static int consecutivo = 1; // Para nombres únicos (P1, P2...)

    // Rangos de configuración (puedes ajustarlos según la dificultad deseada)
    private static final int MIN_INSTRUCCIONES = 10;
    private static final int MAX_INSTRUCCIONES = 100;
    
    private static final int MIN_PRIORIDAD = 1; // 1 = Más alta
    private static final int MAX_PRIORIDAD = 3; // 3 = Más baja
    
    // Probabilidad de que un proceso tenga operaciones de I/O (70%)
    private static final double PROBABILIDAD_IO = 0.7; 
    
    // NUEVAS CONSTANTES DE TAMAÑO (En MB)
    private static final int MIN_TAMANO = 64;  // Mínimo 64 MB
    private static final int MAX_TAMANO = 512; // Máximo 512 MB

    /**
     * Genera un único proceso con parámetros aleatorios.
     * @return Un objeto PCB listo para ser planificado.
     */
    public static PCB generarProcesoAleatorio() {
        // 1. Nombre único
        String nombre = "Proceso_" + consecutivo++;
        
        // 2. Instrucciones Totales (Duración del proceso)
        int instruccionesTotales = random.nextInt(MAX_INSTRUCCIONES - MIN_INSTRUCCIONES + 1) + MIN_INSTRUCCIONES;
        
        // 3. Prioridad (1, 2 o 3)
        int prioridad = random.nextInt(MAX_PRIORIDAD - MIN_PRIORIDAD + 1) + MIN_PRIORIDAD;
        
        // 4. Configuración de I/O (Entrada/Salida)
        int cicloGeneracionIO = 0;
        int longitudIO = 0;
        
        // Decidimos si este proceso tendrá I/O
        if (random.nextDouble() < PROBABILIDAD_IO) {
            // El ciclo de I/O debe ocurrir antes de que termine el proceso (entre 1 y total-1)
            if (instruccionesTotales > 1) {
                cicloGeneracionIO = random.nextInt(instruccionesTotales - 1) + 1;
                // La duración del bloqueo será entre 2 y 10 ciclos
                longitudIO = random.nextInt(9) + 2; 
            }
        }

        // 5. Deadline (Tiempo límite)
        // El deadline debe ser al menos igual a las instrucciones totales + un margen.
        // Simulamos procesos urgentes (margen pequeño) y relajados (margen amplio).
        int margen = random.nextInt(50) + 10; // Margen entre 10 y 60 ciclos extra
        int deadline = instruccionesTotales + margen;

        // 6. GENERAR TAMAÑO ALEATORIO
        // Fórmula: Random entre Min y Max
        int tamano = random.nextInt(MAX_TAMANO - MIN_TAMANO + 1) + MIN_TAMANO;

        // 7. PASARLO AL CONSTRUCTOR
        // Nota que agregamos 'tamano' al final de los paréntesis
        return new PCB(nombre, instruccionesTotales, prioridad, deadline, cicloGeneracionIO, longitudIO, tamano);
    }

    /**
     * Genera un arreglo de N procesos aleatorios.
     * Útil para la carga masiva inicial o el botón de "20 procesos".
     * * CORRECCIÓN 2: Agregados los tags @param y @return
     * @param cantidad Número de procesos a generar.
     * @return Un arreglo de objetos PCB generados.
     */
    public static PCB[] generarMasivos(int cantidad) {
        PCB[] procesos = new PCB[cantidad];
        for (int i = 0; i < cantidad; i++) {
            procesos[i] = generarProcesoAleatorio();
        }
        return procesos;
    }
}
