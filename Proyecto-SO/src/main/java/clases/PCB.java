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
    
    //Atributos PCB
    private int id;
    private String nombre;
    private Estado estado;
    private int programCounter;
    private int memoryAddressRegister;
    
    //Planificacion
    private int instruccionesTotales;
    private int instruccionesEjecutadas;
    private int prioridad;
    private int deadline;
    
    public PCB(String nombre, int instruccionesTotales, int prioridad, int deadline){
        this.id = contadorIds++;
        this.nombre = nombre;
        this.instruccionesTotales = instruccionesTotales;
        this.prioridad = prioridad;
        this.deadline = deadline;
        this.estado = Estado.NUEVO;
        this.programCounter = 0;
        this.memoryAddressRegister = 0;
        this.instruccionesEjecutadas = 0;
    }
           
    //Metodo para simular trabajo de CPU
    
    public void ejecutar(){
        if (instruccionesEjecutadas < instruccionesTotales) {
            programCounter++;
            instruccionesEjecutadas++;
        }
    }
    
    public boolean haTerminado() {
        return instruccionesEjecutadas >= instruccionesTotales;
    }
    
    // Getters y Setters
    public int getId(){ 
        return id; 
    }
    
    public String getNombre(){ 
        return nombre; 
    }
    
    public Estado getEstado(){ 
        return estado; 
    }
    
    public void setEstado(Estado estado){ 
        this.estado = estado; 
    }
    
    public int getPrioridad(){ 
        return prioridad; 
    }
    
    public int getDeadline(){ 
        return deadline; 
    }
    
    @Override
    public String toString() {
        return nombre + " (ID:" + id + ")";
    }
}
