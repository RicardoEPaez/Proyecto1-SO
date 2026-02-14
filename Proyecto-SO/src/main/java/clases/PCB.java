/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package clases;

/**
 *
 * @author ricar
 */
public class PCB {
    private static int contadorIds = 1;
    
    // Atributos PCB
    private int id;
    private String nombre;
    private Estado estado;
    private int programCounter;
    private int memoryAddressRegister;
    private int cicloGeneracionIO;
    private int longitudIO;
    
    private boolean yaHizoIO;
// NUEVO: Para contar el tiempo de espera
    private int contadorIO; 
    
    // Planificacion
    private int instruccionesTotales;
    private int instruccionesEjecutadas;
    private int prioridad;
    private int deadline;
    
    private int tamano; // Nuevo atributo: Tamaño en MB o Páginas
    private int direccionMemoria; // Simulación de puntero (índice en el array de RAM)
    
    public PCB(String nombre, int instruccionesTotales, int prioridad, int deadline, int cicloGeneracion, int longitudIO, int tamano){
        this.id = contadorIds++;
        this.nombre = nombre;
        this.instruccionesTotales = instruccionesTotales;
        this.prioridad = prioridad;
        this.deadline = deadline;
        this.estado = Estado.NUEVO;
        
        // CORRECCIÓN IMPORTANTE: Asignar el parámetro correctamente
        this.cicloGeneracionIO = cicloGeneracion; 
        this.longitudIO = longitudIO;
        this.contadorIO = 0; // Inicializar contador de espera
        
        this.programCounter = 0;
        this.memoryAddressRegister = 0;
        this.instruccionesEjecutadas = 0;
        
        this.yaHizoIO = false;
        
        this.tamano = tamano;
        this.direccionMemoria = -1; // -1 significa que no está en RAM
        
    }
            
    // Metodo para simular trabajo de CPU
    public void ejecutar(){
        if (instruccionesEjecutadas < instruccionesTotales) {
            programCounter++;
            memoryAddressRegister++;
            instruccionesEjecutadas++;
        }
    }
    
    // LÓGICA DE I/O: Detecta si toca hacer I/O en este ciclo
    public boolean necesitaIO() {
         if (longitudIO > 0 && instruccionesEjecutadas == cicloGeneracionIO && !yaHizoIO) {
            yaHizoIO = true; // Marcamos que ya solicitó el bloqueo
            return true;
        }
        return false;
    }
    
    public boolean haTerminado() {
        return instruccionesEjecutadas >= instruccionesTotales;
    }

    // Métodos para el manejo de espera (bloqueados)
    public void aumentarContadorIO() {
        this.contadorIO++;
    }
    
    public void reiniciarContadorIO() {
        this.contadorIO = 0;
    }
    
    public int getContadorIO() {
        return contadorIO;
    }

    // Getters y Setters
    public int getId(){ return id; }
    public String getNombre(){ return nombre; }
    public Estado getEstado(){ return estado; }
    public void setEstado(Estado estado){ this.estado = estado; }
    public int getPrioridad(){ return prioridad; }
    public int getDeadline(){ return deadline; }
    public int getProgramCounter() { return programCounter; }
    public int getInstruccionesTotales() { return instruccionesTotales; }
    public int getCicloGeneracionIO() { return cicloGeneracionIO; }
    public int getLongitudIO() { return longitudIO; }
    
    // Getter útil para monitoreo
    public int getInstruccionesEjecutadas() { return instruccionesEjecutadas; }
    
    // GETTERS Y SETTERS NUEVOS
    public int getTamano() { return tamano; }
    public void setTamano(int tamano) { this.tamano = tamano; }
    
    public int getDireccionMemoria() { return direccionMemoria; }
    public void setDireccionMemoria(int direccionMemoria) { this.direccionMemoria = direccionMemoria; }
    
    @Override
    public String toString() {
        return nombre + " (ID:" + id + ")";
    }
    
    public void reiniciar() {
        this.programCounter = 0;
        this.instruccionesEjecutadas = 0;
        this.memoryAddressRegister = 0;
        this.yaHizoIO = false; // Importante: Reseteamos esto para que pueda volver a pedir I/O
        this.contadorIO = 0;
    }
    
}
