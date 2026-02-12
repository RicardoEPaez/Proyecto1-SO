/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

/**
 * El Reloj Maestro del Sistema.
 * Sincroniza el paso del tiempo para I/O y CPU.
 * @author Ramon-Carrasquel
 */
public class Reloj extends Thread{
    private Planificador planificador;
    private CPU cpu;
    private volatile boolean activo = true;
    private int cicloActual = 0;

    public Reloj(Planificador planificador, CPU cpu) {
        this.planificador = planificador;
        this.cpu = cpu;
    }

    @Override
    public void run() {
        while (activo) {
            try {
                // 1. EL RELOJ MARCA EL RITMO (1 segundo real)
                Thread.sleep(1000); 
                cicloActual++;
                System.out.println(">>> [RELOJ] Ciclo Global: " + cicloActual);

                // 2. ACTUALIZAR PROCESOS BLOQUEADOS (I/O)
                // Esto reduce el contador de espera de los procesos en E/S
                planificador.verificarBloqueados(); 

                // 3. AVISAR AL CPU (Wait/Notify)
                // Despierta al CPU para que ejecute una instrucción en este ciclo
                synchronized(cpu) {
                    cpu.notify(); 
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void detener() {
        this.activo = false;
    }
    
    public int getCicloActual() { return cicloActual; }
}
