/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

import estructuras.ColaPrioridad;
import estructuras.ListaEnlazada;

/**
 * El Planificador actua como el gestor de procesos del Sistema Operativo.
 * Mantiene las colas de estados y decide qué proceso va al CPU.
 * Sincronizado con el Reloj del sistema.
 * @author Ramon-Carrasquel
 */
public class Planificador {

    // Referencias
    private AlgoritmoPlanificacion algoritmoActual;
    private CPU cpu;

    // Colas de Planificación
    private ColaPrioridad<PCB> colaListos;
    private ListaEnlazada<PCB> listaBloqueados;

    // Gestión de Memoria
    private Memoria memoria;
    private ListaEnlazada<PCB> colaSwap; // Disco virtual

    public Planificador() {
        this.colaListos = new ColaPrioridad<>();
        this.listaBloqueados = new ListaEnlazada<>();
        this.algoritmoActual = new AlgoritmoFCFS(); // Default
        
        this.memoria = new Memoria();
        this.colaSwap = new ListaEnlazada<>();
    }

    public void setAlgoritmo(AlgoritmoPlanificacion nuevoAlgoritmo) {
        this.algoritmoActual = nuevoAlgoritmo;
        // Reordenamos la cola actual con el nuevo criterio
        System.out.println("--- [SISTEMA] Algoritmo cambiado a: " + nuevoAlgoritmo.toString() + " ---");
    }

    public void setCPU(CPU cpu) {
        this.cpu = cpu;
    }

    // --- MÉTODOS PRINCIPALES ---

    public synchronized void agregarProceso(PCB proceso) {
        // 1. Intentamos cargar en RAM
        if (memoria.cargarEnMemoria(proceso)) {
            System.out.println("--- [MEMORIA] Proceso " + proceso.getNombre() + " cargado en RAM ---");
            
            // 2. Si entra, va a la cola de LISTOS
            proceso.setEstado(Estado.LISTO);
            encolarEnListos(proceso); // Usamos el wrapper

            // 3. Verificamos si debe expropiar al CPU (Context Switch)
            verificarExpropiacion(proceso);

        } else {
            // 4. Si no cabe, va al Disco (Swap)
            System.out.println("--- [MEMORIA FULL] No cabe " + proceso.getNombre() + ". Enviando a Swap... ---");
            gestionarSwapping(proceso);
        }
    }

    private void gestionarSwapping(PCB proceso) {
        colaSwap.agregar(proceso);
        System.out.println("    -> [SWAP] Proceso " + proceso.getNombre() + " encolado en disco virtual.");
    }

    // --- MANEJO DE ESTADOS ---

    public synchronized PCB obtenerSiguiente() {
        if (colaListos.estaVacia()) return null;
        return colaListos.desencolar();
    }

    public synchronized void expulsarProceso(PCB proceso) {
        proceso.setEstado(Estado.LISTO);
        encolarEnListos(proceso);
    }

    public synchronized void bloquearProceso(PCB proceso) {
        proceso.setEstado(Estado.BLOQUEADO);
        listaBloqueados.agregar(proceso);
    }

    public synchronized void terminarProceso(PCB proceso) {
        System.out.println("[Planificador] Proceso finalizado: " + proceso.getNombre());
        
        // 1. Liberar RAM
        memoria.liberarMemoria(proceso);
        
        // 2. Revisar si alguien del Swap puede entrar
        revisarColaSwap();
    }
    
    // --- GESTIÓN DE MEMORIA VIRTUAL (SWAP) ---

    private void revisarColaSwap() {
        if (!colaSwap.estaVacia()) {
            PCB candidato = colaSwap.get(0); // Miramos el primero
            
            // Intentamos subirlo a RAM
            if (memoria.cargarEnMemoria(candidato)) {
                colaSwap.eliminar(candidato); // Lo sacamos del disco
                System.out.println("    <- [SWAP IN] Proceso " + candidato.getNombre() + " movido de Disco a RAM.");
                
                // Lo ponemos listo
                candidato.setEstado(Estado.LISTO);
                encolarEnListos(candidato);
                
                // IMPORTANTE: Al volver del swap, también podría ser urgente.
                // Verificamos si debe expropiar al actual.
                verificarExpropiacion(candidato);
            }
        }
    }

    // --- CÓDIGO DEL RELOJ Y BLOQUEOS ---

    public synchronized void verificarBloqueados() {
        if (listaBloqueados.estaVacia()) return;

        ListaEnlazada<PCB> procesosParaDespertar = new ListaEnlazada<>();

        // 1. Aumentar contadores
        for (int i = 0; i < listaBloqueados.getTamano(); i++) {
            PCB p = listaBloqueados.get(i);
            p.aumentarContadorIO();
            if (p.getContadorIO() >= p.getLongitudIO()) {
                procesosParaDespertar.agregar(p);
            }
        }

        // 2. Despertar procesos
        for (int i = 0; i < procesosParaDespertar.getTamano(); i++) {
            PCB p = procesosParaDespertar.get(i);
            
            listaBloqueados.eliminar(p);
            p.reiniciarContadorIO();
            System.out.println("--> [Planificador] I/O Completado: " + p.getNombre());

            // Regresa a la cola de listos
            p.setEstado(Estado.LISTO);
            encolarEnListos(p);

            // Verificamos si este proceso que despertó es más importante que el actual
            verificarExpropiacion(p);
        }
    }
    
    // --- MÉTODO CENTRALIZADO DE EXPROPIACIÓN ---
    private void verificarExpropiacion(PCB nuevoProceso) {
        if (cpu != null && !cpu.estaLibre()) {
            PCB enEjecucion = cpu.getProcesoActual();
            if (enEjecucion != null && algoritmoActual.debeExpropiar(enEjecucion, nuevoProceso)) {
                System.out.println("    !!! [Planificador] EXPROPIACIÓN: " + nuevoProceso.getNombre() + " desplaza a " + enEjecucion.getNombre());
                cpu.interrumpir(); 
            }
        }
    }

    // --- MANEJO DE INTERRUPCIONES DEL CPU ---

    public synchronized void manejarInterrupcion(TipoInterrupcion tipo, PCB proceso) {
        if (proceso == null) return;

        switch (tipo) {
            case FIN_PROCESO:
                proceso.setEstado(Estado.TERMINADO);
                this.terminarProceso(proceso);
                break;

            case TIEMPO_AGOTADO: // Fin de Quantum
            case DESALOJO_POR_PRIORIDAD: // SRT / Prioridad Apropiativa
                this.expulsarProceso(proceso);
                break;

            case SOLICITUD_IO:
                this.bloquearProceso(proceso);
                break;
        }
        
        // Siempre intentamos llenar el CPU si quedó vacío
        despacharSiguiente();
    }

    private void despacharSiguiente() {
        PCB siguiente = this.obtenerSiguiente();
        if (siguiente != null) {
            // Nota: Aquí solo asignamos la referencia. 
            // El CPU no ejecutará nada hasta que el Reloj haga notify()
            cpu.asignarProceso(siguiente);
        }
    }
    
    public boolean hayProcesosListos() {
        return !colaListos.estaVacia();
    }
    
    // --- WRAPPER ÚTIL ---
    public void encolarEnListos(PCB proceso) {
        algoritmoActual.encolar(this.colaListos, proceso);
    }
}