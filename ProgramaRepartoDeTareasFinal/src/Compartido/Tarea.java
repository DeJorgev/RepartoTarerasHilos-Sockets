package Compartido;

import java.io.Serializable;

public class Tarea implements Serializable{
    private int tiempoTarea , ID;
    
    public Tarea(int ID,int tiempoTarea){
        this.ID = ID;
        this.tiempoTarea = tiempoTarea;
    }

    public int getTiempoTarea() {
        return tiempoTarea;
    }

    public int getID() {
        return ID;
    }    
}