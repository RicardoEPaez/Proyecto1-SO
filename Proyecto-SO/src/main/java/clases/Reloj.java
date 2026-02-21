/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

/**
 * Reloj Maestro para el Ciclo de Reloj.
 * @author Ramon-Carrasquel
 */
public class Reloj extends Thread {
    private Planificador planificador;
    private CPU cpu;
    private volatile boolean activo = true;
    private int cicloActual = 0;
    
    
    private volatile int tiempoCicloMs = 1000;
    
    public Reloj(Planificador planificador, CPU cpu) {
        this.planificador = planificador;
        this.cpu = cpu;
    }

    public void setTiempoCiclo(int ms){
        this.tiempoCicloMs = ms;
    }
    
    @Override
    public void run() {
        while (activo) {
            try {
                // 1. RITMO (1 segundo real)
                Thread.sleep(tiempoCicloMs); 
                cicloActual++;
                System.out.println(">>> [RELOJ] Ciclo Global: " + cicloActual);
                
                planificador.actualizarTiemposMision(cicloActual);
                // 2. ACTUALIZAR I/O (Ya es seguro porque Planificador usa semáforos dentro)
                planificador.verificarBloqueados(); 

                // 3. AVISAR AL CPU (Reemplazo de notify())
                // En lugar de synchronized/notify, liberamos un "ticket" para el CPU.
                cpu.enviarPulsoReloj(); 
                
            } catch (InterruptedException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
    
    public void detener() {
        this.activo = false;
    }
    
    public int getCicloActual() { return cicloActual; }
    
    public void setCicloActual(int ciclo) {
        this.cicloActual = ciclo;
    }
}
