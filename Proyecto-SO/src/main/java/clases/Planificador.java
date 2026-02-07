/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

import estructuras.ColaPrioridad;
import estructuras.ListaEnlazada;

/**
 * El Planificador actua como el gestor de procesos del Sistema Operativo
 * Mantiene las colas de estados y decide qué proceso va al CPU
 * @author Ramon-Carrasquel
 */
public class Planificador {

    // Referencia a la estrategia (el algoritmo actual)
    private AlgoritmoPlanificacion algoritmoActual;

    // Referencia al CPU (Necesaria para interrumpir en SRT)
    private CPU cpu;

    // Colas de Planificación
    private ColaPrioridad<PCB> colaListos;
    private ListaEnlazada<PCB> listaBloqueados;

    // Gestión de Memoria
    private Memoria memoria;
    private ListaEnlazada<PCB> colaSwap; // Disco virtual (Cola de espera para RAM)

    public Planificador() {
        this.colaListos = new ColaPrioridad<>();
        this.listaBloqueados = new ListaEnlazada<>();
        this.algoritmoActual = new AlgoritmoFCFS(); // Algoritmo por defecto
        
        // Inicialización de Memoria
        this.memoria = new Memoria();
        this.colaSwap = new ListaEnlazada<>();
    }

    public void setAlgoritmo(AlgoritmoPlanificacion nuevoAlgoritmo) {
        this.algoritmoActual = nuevoAlgoritmo;
        System.out.println("--- [SISTEMA] Algoritmo cambiado a: " + nuevoAlgoritmo.toString() + " ---");
    }

    public void setCPU(CPU cpu) {
        this.cpu = cpu;
    }

    public synchronized void agregarProceso(PCB proceso) {
        // --- PASO 1: GESTIÓN DE MEMORIA ---
        // Intentamos meter el proceso a la RAM
        boolean pudoCargar = memoria.cargarEnMemoria(proceso);

        if (pudoCargar) {
            // EL PROCESO ESTÁ EN RAM -> PASA A PLANIFICACIÓN DE CPU
            
            System.out.println("--- [MEMORIA] Proceso " + proceso.getNombre() + " cargado en RAM ---");
            
            // 1. Cambiamos estado y lo metemos en la cola de listos
            proceso.setEstado(Estado.LISTO);
            algoritmoActual.encolar(colaListos, proceso);

            System.out.println("--> [Planificador] Encolado: " + proceso.getNombre() + " | Algoritmo: " + algoritmoActual.toString());

            // 2. Verificación de Expropiación
            // Solo intentamos expropiar si el CPU está ocupado
            if (cpu != null && !cpu.estaLibre()) {
                PCB enEjecucion = cpu.getProcesoActual();

                // Preguntamos al algoritmo: "¿El nuevo es más importante que el actual?"
                if (enEjecucion != null && algoritmoActual.debeExpropiar(enEjecucion, proceso)) {
                    System.out.println("    !!! [Planificador] EXPROPIACIÓN: " + proceso.getNombre() + " desplaza a " + enEjecucion.getNombre());
                    cpu.interrumpir(); // Forzamos el context switch
                }
            }

        } else {
            // --- PASO 2: MEMORIA LLENA (SWAPPING) ---
            System.out.println("--- [MEMORIA FULL] No cabe " + proceso.getNombre() + ". Iniciando protocolo de Swapping... ---");

            // Llamamos al método que faltaba
            gestionarSwapping(proceso);
        }
    }

    // Maneja los procesos que no caben en la RAM enviándolos al "Disco" (Swap).
    private void gestionarSwapping(PCB proceso) {
        // Por ahora, simplemente lo agregamos a la cola de espera del disco.
        colaSwap.agregar(proceso);
        
        // Lo marcamos como "Suspendido" o simplemente esperamos.
        // Nota: No cambiamos a BLOQUEADO porque no está esperando I/O, está esperando MEMORIA.
        System.out.println("    -> [SWAP] Proceso " + proceso.getNombre() + " encolado en disco virtual.");
    }

    public synchronized PCB obtenerSiguiente() {
        if (colaListos.estaVacia()) {
            return null;
        }
        // Simplemente sacamos el primero. El algoritmo ya se encargó de ordenarlos.
        return colaListos.desencolar();
    }

    public synchronized void expulsarProceso(PCB proceso) {
        proceso.setEstado(Estado.LISTO);
        algoritmoActual.encolar(colaListos, proceso);
    }

    public synchronized void bloquearProceso(PCB proceso) {
        proceso.setEstado(Estado.BLOQUEADO);
        listaBloqueados.agregar(proceso);
    }

    // Ahora debe liberar memoria cuando termina
    public synchronized void terminarProceso(PCB proceso) {
        System.out.println("[Planificador] Proceso finalizado: " + proceso.getNombre());
        
        // 1. Liberamos el espacio en RAM
        memoria.liberarMemoria(proceso);
        
        // 2. Revisamos si alguien de la cola de Swap puede entrar ahora que hay espacio
        revisarColaSwap();
    }
    
    // Revisa si hay procesos esperando en disco y trata de meterlos a RAM
    private void revisarColaSwap() {
        if (!colaSwap.estaVacia()) {
            // Intentamos cargar el primero de la fila
            PCB candidato = colaSwap.get(0); // Asumiendo que tu lista tiene get(0)
            
            // Intentamos meterlo de nuevo (recursividad indirecta segura)
            if (memoria.cargarEnMemoria(candidato)) {
                colaSwap.eliminar(candidato); // Lo sacamos del disco
                System.out.println("    <- [SWAP IN] Proceso " + candidato.getNombre() + " movido de Disco a RAM.");
                
                // Lo agregamos a la lógica normal de CPU
                // Llamamos a agregarProceso para que pase por la lógica de expropiación
                // Pero como ya lo cargamos en memoria manualmente arriba, podemos simplificar:
                candidato.setEstado(Estado.LISTO);
                algoritmoActual.encolar(colaListos, candidato);
            }
        }
    }

    // Este método se llama en cada ciclo del reloj del CPU 
    // Revisa la lista de bloqueados, aumenta sus contadores y despierta a los que terminaron.
    public synchronized void verificarBloqueados() {
        if (listaBloqueados.estaVacia()) {
            return;
        }

        // Usamos una lista auxiliar para guardar los que deben salir
        ListaEnlazada<PCB> procesosParaDespertar = new ListaEnlazada<>();

        // 1. Recorremos todos los bloqueados
        for (int i = 0; i < listaBloqueados.getTamano(); i++) {
            PCB p = listaBloqueados.get(i);

            // Aumentamos su tiempo esperando I/O
            p.aumentarContadorIO();

            // Verificamos si ya cumplio su tiempo de espera
            if (p.getContadorIO() >= p.getLongitudIO()) {
                procesosParaDespertar.agregar(p);
            }
        }

        // 2. Movemos los procesos listos de Bloqueados a la Cola de Listos
        for (int i = 0; i < procesosParaDespertar.getTamano(); i++) {
            PCB p = procesosParaDespertar.get(i);

            // Lo sacamos de bloqueados
            listaBloqueados.eliminar(p);

            // Reseteamos su contador de I/O para futuras interrupciones
            p.reiniciarContadorIO();
            
            System.out.println("--> [Planificador] I/O Completado: " + p.getNombre());
            
            // Verificamos si sigue teniendo memoria asignada (casi siempre sí, a menos que implementes swap agresivo)
            if (p.getDireccionMemoria() != -1) {
                 
                 // --- Lógica de Expropiación al volver de I/O ---
                 p.setEstado(Estado.LISTO);
                 algoritmoActual.encolar(colaListos, p);
                 
                 // Verificamos si este proceso que despertó es más importante que el actual
                 if (cpu != null && !cpu.estaLibre()) {
                    PCB enEjecucion = cpu.getProcesoActual();
                    if (enEjecucion != null && algoritmoActual.debeExpropiar(enEjecucion, p)) {
                        System.out.println("    !!! [Planificador] EXPROPIACIÓN POR I/O: " + p.getNombre() + " desplaza a " + enEjecucion.getNombre());
                        cpu.interrumpir(); 
                    }
                 }

            } else {
                 // Si por alguna razón perdió la memoria, tratamos de agregarlo como nuevo
                 this.agregarProceso(p); 
            }
        }
    }

    public boolean hayProcesosListos() {
        return !colaListos.estaVacia();
    }

    public synchronized void manejarInterrupcion(TipoInterrupcion tipo, PCB proceso) {

        if (proceso == null) return;

        switch (tipo) {
            case FIN_PROCESO:
                System.out.println("--- [PLANIFICADOR] Interrupción: Fin de Proceso (" + proceso.getNombre() + ") ---");
                proceso.setEstado(Estado.TERMINADO);
                this.terminarProceso(proceso);
                break;

            case TIEMPO_AGOTADO:
                System.out.println("--- [PLANIFICADOR] Interrupción: Tiempo Agotado (" + proceso.getNombre() + ") ---");
                // Vuelve a la cola de listos
                this.expulsarProceso(proceso);
                break;

            case SOLICITUD_IO:
                System.out.println("--- [PLANIFICADOR] Interrupción: Solicitud de E/S (" + proceso.getNombre() + ") ---");
                proceso.setEstado(Estado.BLOQUEADO);
                this.bloquearProceso(proceso);
                break;

            case DESALOJO_POR_PRIORIDAD:
                System.out.println("--- [PLANIFICADOR] Interrupción: Desalojo por Prioridad (" + proceso.getNombre() + ") ---");
                // Vuelve a la cola de listos
                this.expulsarProceso(proceso);
                break;
        }
        // Al final, siempre intentamos cargar el siguiente proceso en el CPU
        despacharSiguiente();
    }

    private void despacharSiguiente() {
        PCB siguiente = this.obtenerSiguiente();
        if (siguiente != null) {
            System.out.println("[PLANIFICADOR] Asignando CPU a: " + siguiente.getNombre());
            cpu.asignarProceso(siguiente);
        } else {
            System.out.println("[PLANIFICADOR] CPU en espera (Idle)...");
        }
    }
}