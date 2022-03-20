package programarepartodetareas.servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import Compartido.HorarioEmpleado;
import Compartido.Tarea;

public class EncargadoEmpleado implements Runnable{
    private Socket socket;
    private Datos datos;
    private HorarioEmpleado horarioEmpleado;

    public EncargadoEmpleado(Socket socket, Datos datos, HorarioEmpleado horarioEmpleado) {
        this.socket = socket;
        this.datos = datos;
        this.horarioEmpleado = horarioEmpleado;
    }
    
    @Override
    public void run() {
        BufferedReader bReader = getBufferedReader(socket);
        ObjectOutputStream oos = getObjectOutputStream(socket);
        
        String respuestaEmpleado;
        datos.arrancarHilo();
        Tarea tarea = null;
               
        //Se inicia un bucle que intercambia con el cliente tareas por respuesta, guardanod la informacion de la tarea en horario empleado y 
        //que acaba cuando el cliente rechaza una tarea de tiempo igual a 1.
            do {
                tarea = datos.getTarea();
                EnviarTarea(oos,tarea);
                respuestaEmpleado = recibirRespuestaEmpleado(bReader);
                
                gestionarRespuestaEmpleado(respuestaEmpleado, tarea);

            } while (respuestaEmpleado.equals("OK") || tarea.getTiempoTarea() != 1);
            
        //Para indicar al empleado el fin de su vida se manda un null al cliente y se notifica a datos.
        EnviarTarea(oos, null);
        datos.marcarMuerteHilo();
        cerrarComunicaciones(socket, bReader, oos);
    }

    //
    private void gestionarRespuestaEmpleado(String respuestaEmpleado, Tarea tarea) {
        if (respuestaEmpleado.equals("OK")){
            horarioEmpleado.getHorario().add(tarea);
            datos.marcarTareaAceptada();
        }else if(tarea.getTiempoTarea() != 1 ){
            int ultimoIDTareaRechazada = tarea.getID();
            datos.marcarTareaSuperiorAUnoRechazada();
            //En caso de que se le envie una tarea que ya ha rechazado, la volvera a rechazar inmediatamente y se pondra en espera las veces necesarias.
            while ((tarea = datos.getTarea()).getID() == ultimoIDTareaRechazada){
                System.out.println("Tarea ya rechazada "+ tarea.getID() + " , a mimir " + Thread.currentThread());
                datos.marcarTareaYaRechazada();
            }
        }
    }
    
//Metodos de control de canales de comunicacion con el cliente
    private static BufferedReader getBufferedReader(Socket socketCliente) {
        BufferedReader bReader=null;
        try {
            bReader = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
	} catch (IOException e) {e.printStackTrace();}
        
        return bReader;
    }

    private static ObjectOutputStream getObjectOutputStream(Socket socketCliente) {
        ObjectOutputStream ois = null;
	try {
		ois = new ObjectOutputStream(socketCliente.getOutputStream());
	} catch (IOException e) {e.printStackTrace();}
        
        return ois;
    }
    
    private static void cerrarComunicaciones(Socket socket, BufferedReader bReader, ObjectOutputStream oos){
        try {
            bReader.close();
            oos.close();
            socket.close();
        } catch (IOException e) {e.printStackTrace();}
    }
    
//Metodos de comunicacion con el cliente    
    private static void EnviarTarea(ObjectOutputStream oos, Tarea nuevaTarea){
        try {
            oos.writeObject(nuevaTarea);oos.flush();
        } catch (IOException e) {e.printStackTrace();}
    }
    
    private static String recibirRespuestaEmpleado(BufferedReader bReader){
        String mensaje = null;
        try {
            mensaje = bReader.readLine();
        } catch (IOException e) {e.printStackTrace();}
        
        return mensaje;
    }
}
