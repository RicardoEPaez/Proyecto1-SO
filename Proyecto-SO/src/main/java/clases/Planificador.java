/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

import estructuras.ColaPrioridad;
import estructuras.ListaEnlazada;
import java.util.concurrent.Semaphore; // <--- 1. IMPORTAMOS ESTO

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
    
    // Mutex = Mutual Exclusion. Permiso único.
    private final Semaphore mutex = new Semaphore(1); 
    private int tiempoSistema = 0;
    
    
    // --- 📊 VARIABLES PARA MÉTRICAS DEL SISTEMA (NUEVO) ---
    private int totalProcesosTerminados = 0;
    private int misionesExitosas = 0;
    private int sumaTiempoEsperaTotal = 0;
    // ------------------------------------------------------
    
    private boolean sistemaCorriendo = false; // Bandera para bloquear UI

    public Planificador() {
        this.colaListos = new ColaPrioridad<>();
        this.listaBloqueados = new ListaEnlazada<>();
        this.algoritmoActual = new AlgoritmoFCFS(); // Default
        
        this.memoria = new Memoria();
        this.colaSwap = new ListaEnlazada<>();
    }

    public void setAlgoritmo(AlgoritmoPlanificacion nuevoAlgoritmo) {
        // Este es un cambio de configuración, idealmente protegemos también
        try {
            mutex.acquire();
            this.algoritmoActual = nuevoAlgoritmo;
            System.out.println("--- [SISTEMA] Algoritmo cambiado a: " + nuevoAlgoritmo.toString() + " ---");
        } catch (InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            mutex.release();
        }
    }

    public void setCPU(CPU cpu) {
        this.cpu = cpu;
    }
    
    public void setMemoria(Memoria memoria) {
        this.memoria = memoria;
    }
    
    public boolean isSistemaCorriendo() {
        return sistemaCorriendo;
    }

    public void setSistemaCorriendo(boolean estado) {
        this.sistemaCorriendo = estado;
    }

    // --- MÉTODOS PRINCIPALES (Protegidos con Semáforo) ---

    public boolean agregarProceso(PCB proceso) {
        boolean resultado = false;
        try {
            mutex.acquire(); // <--- BLOQUEO (ENTRADA)
            
            // 1. Intentamos cargar en RAM
            if (memoria.cargarEnMemoria(proceso)) {
                System.out.println("--- [MEMORIA] Proceso " + proceso.getNombre() + " cargado en RAM ---");
                
                // 2. Si entra, va a la cola de LISTOS
                proceso.setEstado(Estado.LISTO);
                encolarEnListos(proceso); 

                // 3. Verificamos si debe expropiar al CPU
                verificarExpropiacion(proceso);
                
                resultado = true;

            } else {
                // 4. Si no cabe, va al Disco (Swap)
                System.out.println("--- [MEMORIA FULL] No cabe " + proceso.getNombre() + ". Enviando a Swap... ---");
                gestionarSwapping(proceso);
                
                resultado = true;
            }
            // ----------------------------------
            
        } catch (InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            mutex.release(); // <--- DESBLOQUEO (SALIDA)
        }
        return resultado;
    }

    // Método PRIVADO (Auxiliar): NO lleva semáforo porque lo llama 'agregarProceso' que YA tiene el permiso
    private void gestionarSwapping(PCB proceso) {
        colaSwap.agregar(proceso);
        System.out.println("    -> [SWAP] Proceso " + proceso.getNombre() + " encolado en disco virtual.");
    }

    // --- MANEJO DE ESTADOS ---

    public PCB obtenerSiguiente() {
        PCB siguiente = null;
        try {
            mutex.acquire(); // <--- BLOQUEO
            if (!colaListos.estaVacia()) {
                siguiente = colaListos.desencolar();
            }
        } catch (InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            mutex.release(); // <--- DESBLOQUEO
        }
        return siguiente;
    }

    // Usado por PanelProcesador (botón abortar) o interrupciones
    public void expulsarProceso(PCB proceso) {
        try {
            mutex.acquire();
            proceso.setEstado(Estado.LISTO);
            encolarEnListos(proceso);
        } catch (InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            mutex.release();
        }
    }

    public void bloquearProceso(PCB proceso) {
        try {
            mutex.acquire();
            proceso.setEstado(Estado.BLOQUEADO);
            listaBloqueados.agregar(proceso);
        } catch (InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            mutex.release();
        }
    }

    public void terminarProceso(PCB proceso) {
        try {
            mutex.acquire();
            terminarProcesoInterno(proceso); // Llamamos a la lógica interna
        } catch (InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            mutex.release();
        }
    }
    
    // Lógica interna sin semáforo (para poder reusarla)
    private void terminarProcesoInterno(PCB proceso) {
        System.out.println("[Planificador] Proceso finalizado: " + proceso.getNombre());
        
        // --- 1. REGISTRAR TELEMETRÍA ---
        totalProcesosTerminados++;
        sumaTiempoEsperaTotal += proceso.getTiempoEspera();
        
        // El PCB calcula si cumplió o no el deadline basándose en el tiempo actual
        proceso.verificarExitoMision(this.tiempoSistema);
        
        if (proceso.isCumplioDeadline()) {
            misionesExitosas++;
        }
        // -------------------------------
        
        // 2. Liberar RAM
        memoria.liberarMemoria(proceso);
        
        // 3. Revisar si alguien del Swap puede entrar
        revisarColaSwap();
    }
    
    // --- GESTIÓN DE MEMORIA VIRTUAL (SWAP) ---

    // Privado: No necesita semáforo propio, usa el del llamante
    private void revisarColaSwap() {
        if (!colaSwap.estaVacia()) {
            PCB candidato = colaSwap.get(0); 
            
            // Intentamos subirlo a RAM
            if (memoria.cargarEnMemoria(candidato)) {
                colaSwap.eliminar(candidato); 
                System.out.println("    <- [SWAP IN] Proceso " + candidato.getNombre() + " movido de Disco a RAM.");
                
                candidato.setEstado(Estado.LISTO);
                encolarEnListos(candidato);
                
                verificarExpropiacion(candidato);
            }
        }
    }

    // --- CÓDIGO DEL RELOJ Y BLOQUEOS ---

    public void verificarBloqueados() {
        try {
            mutex.acquire(); // <--- BLOQUEO COMPLETO DE LA VERIFICACIÓN
            
            if (!listaBloqueados.estaVacia()) {
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

                    p.setEstado(Estado.LISTO);
                    encolarEnListos(p);

                    verificarExpropiacion(p);
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            mutex.release();
        }
    }
    
    // --- MÉTODO CENTRALIZADO DE EXPROPIACIÓN ---
    // Privado: Hereda el permiso del semáforo
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
    public void manejarInterrupcion(TipoInterrupcion tipo, PCB proceso) {
        if (proceso == null) return;
        
        try {
            mutex.acquire(); // Protegemos la decisión
            
            switch (tipo) {
                case FIN_PROCESO:
                    proceso.setEstado(Estado.TERMINADO);
                    terminarProcesoInterno(proceso); // Usamos el interno para no bloquear 2 veces
                    break;

                case TIEMPO_AGOTADO: // Fin de Quantum
                case DESALOJO_POR_PRIORIDAD: // SRT / Prioridad Apropiativa
                    proceso.setEstado(Estado.LISTO);
                    encolarEnListos(proceso);
                    break;

                case SOLICITUD_IO:
                    proceso.setEstado(Estado.BLOQUEADO);
                    listaBloqueados.agregar(proceso);
                    break;
            }
            
            // Liberamos ANTES de despachar para que el dispatcher pueda trabajar si necesita el semáforo
            // Siempre intentamos llenar el CPU si quedó vacío
            despacharSiguiente();
            
        } catch (InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            mutex.release();
        }
    }

    // Privado, llamado dentro de la sección crítica
    private void despacharSiguiente() {
        if (!colaListos.estaVacia()) {
             PCB siguiente = colaListos.desencolar();
             if (siguiente != null) {
                cpu.asignarProceso(siguiente);
             }
        }
    }
    
    public boolean hayProcesosListos() {
        // Lectura rápida, a veces se permite sin semáforo, pero por seguridad:
        boolean hay = false;
        try {
            mutex.acquire();
            hay = !colaListos.estaVacia();
        } catch (InterruptedException e) { System.err.println("Error: " + e.getMessage()); } 
        finally { mutex.release(); }
        return hay;
    }
    
    // Helper privado
    private void encolarEnListos(PCB proceso) {
        algoritmoActual.encolar(this.colaListos, proceso);
    }
    
    // GETTERS PARA GUI (CON SEMÁFORO)
    
    public Object[] getColaListosParaTabla() {
        Object[] datos = null;
        try {
            mutex.acquire();
            datos = colaListos.toArray();
        } catch(InterruptedException e) { System.err.println("Error: " + e.getMessage()); }
        finally { mutex.release(); }
        return datos;
    }

    public Object[] getColaBloqueadosParaTabla() {
        Object[] datos = null;
        try {
            mutex.acquire();
            datos = listaBloqueados.toArray(); // Asumiendo que ListaEnlazada tiene un toArray()
        } catch(InterruptedException e) { System.err.println("Error: " + e.getMessage()); }
        finally { mutex.release(); }
        return datos;
    }
    
    
    public ColaPrioridad<PCB> getColaListos() { return this.colaListos; }
    public ListaEnlazada<PCB> getColaBloqueados() { return this.listaBloqueados; }
    
    
    // --- ⏱️ BLOQUE 1: ACTUALIZADOR DE TIEMPOS ---
    public void actualizarTiemposMision(int cicloActual) {
        try {
            mutex.acquire();
            this.tiempoSistema = cicloActual; // Guardamos el ciclo actual del reloj
            
            // A todos los que están esperando en la cola de listos, les sumamos 1 de espera
            Object[] listosEnEspera = colaListos.toArray();
            if (listosEnEspera != null) {
                for (Object obj : listosEnEspera) {
                    if (obj instanceof PCB) {
                        ((PCB) obj).aumentarTiempoEspera();
                    }
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            mutex.release();
        }
    }
    
    // --- 📈 BLOQUE 2: FÓRMULAS DE RENDIMIENTO ---
    public double getTasaExitoMision() {
        if (totalProcesosTerminados == 0) return 100.0; // Evita división por cero
        return ((double) misionesExitosas / totalProcesosTerminados) * 100.0;
    }

    public double getTiempoEsperaPromedio() {
        if (totalProcesosTerminados == 0) return 0.0;
        return (double) sumaTiempoEsperaTotal / totalProcesosTerminados;
    }

    public double getThroughput() {
        if (tiempoSistema == 0) return 0.0;
        return (double) totalProcesosTerminados / tiempoSistema;
    }
    
    public int getTotalProcesosTerminados() { return totalProcesosTerminados; }
    public int getMisionesExitosas() { return misionesExitosas; }
    
}