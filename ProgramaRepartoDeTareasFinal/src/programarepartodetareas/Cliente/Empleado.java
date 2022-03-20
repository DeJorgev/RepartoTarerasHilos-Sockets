package programarepartodetareas.Cliente;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import Compartido.Tarea;

public class Empleado {    
    
    public static void main(String[] args) {
        int horasLibres = 40;
        
        Socket socketEmpleado = getSocket();
        
        ObjectInputStream ois = getObjectInputStream(socketEmpleado);
        BufferedWriter bWriter = getBufferedWriter(socketEmpleado);
        Tarea ultimaTarea;
        
        while ((ultimaTarea = recibirObjetoTarea(ois))!= null) {
            if(horasLibres - ultimaTarea.getTiempoTarea() >= 0){
                horasLibres -= ultimaTarea.getTiempoTarea();
                enviarRespuesta(bWriter, true);
            }else
                enviarRespuesta(bWriter, false);
        }
        cerrarComunicacionesEmpleado(socketEmpleado, ois, bWriter);
    }

//Metodos para manejar los canales de comunicacion del Empleado.    
    private static Socket getSocket() {
        Socket socket = null;
        
        try {
            socket = new Socket("localhost", 5000);
        } catch (UnknownHostException e) {e.printStackTrace();
        } catch(IOException e){e.printStackTrace();}
        
        return socket;
    }
  
    private static ObjectInputStream getObjectInputStream(Socket socketEmpleado) {
        ObjectInputStream ois = null;
        
	try {
            ois = new ObjectInputStream(socketEmpleado.getInputStream());
	} catch (IOException e) {e.printStackTrace();}
        
        return ois;
    }

    private static BufferedWriter getBufferedWriter(Socket socketEmpleado) {
        BufferedWriter bWriter = null;
        
	try {
		bWriter = new BufferedWriter(new OutputStreamWriter(socketEmpleado.getOutputStream()));
	} catch (IOException e) {e.printStackTrace();}
        
        return bWriter;
    }
    
    private static void cerrarComunicacionesEmpleado(Socket socket, ObjectInputStream ois, BufferedWriter bWriter){
        try {
            ois.close();
            bWriter.close();
            socket.close();
        } catch (IOException e) {e.printStackTrace();}        
    }

//Metodos de trabajo.  
    //Espera a recibir una tarea de jefe, si recibe cualquier otra cosa devuelve nulo. 
    private static Tarea recibirObjetoTarea(ObjectInputStream ois){
        Tarea tareaNueva = null;
        try {
            Object objetoRecibido = ois.readObject();
            
            if(objetoRecibido instanceof Tarea)
                tareaNueva = (Tarea) objetoRecibido;            
        } 
        catch (IOException e) {e.printStackTrace();}
        catch (ClassNotFoundException e){e.printStackTrace();}
                
        return tareaNueva;
    }
        
    //Envia una respuesta al sistema dependiendo de si puede o no aceptar la ultima carga de trabajo.
    private static void enviarRespuesta(BufferedWriter bWritter,boolean Aceptado){
        try {
            if (Aceptado) 
                bWritter.write("OK" + "\n");
            else
                bWritter.write("NO" + "\n");
            bWritter.flush();
        } catch (IOException e) {e.printStackTrace();}
    }
    
}