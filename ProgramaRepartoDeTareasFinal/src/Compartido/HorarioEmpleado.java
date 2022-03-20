package Compartido;

import java.util.ArrayList;

public class HorarioEmpleado {
    private  ArrayList<Tarea> horario = new ArrayList<>();
    private int numEmpleado;

    public HorarioEmpleado(int numEmpleado) {
        this.numEmpleado = numEmpleado;
    }

    public int getNumEmpleado() {
        return numEmpleado;
    }
    
    public ArrayList<Tarea> getHorario() {
        return horario;
    }
}