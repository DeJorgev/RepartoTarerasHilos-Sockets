package programarepartodetareas.servidor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import Compartido.HorarioEmpleado;

public class Jefe {
    private static ServerSocket sSocket = getServerSockect();
    private static boolean limiteDeTiempoAlcanzado = false;
    
    //Constructor sobreescrito de la clase TimerTask
    //Proporciona al objeto timer en el metodo aceptarConexiones() lineas que ejecutar tras el tiempo indicado. 
    static TimerTask tTask = new TimerTask() {
        public void run() {
            try {
                sSocket.close();
                limiteDeTiempoAlcanzado = true;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    };
    
    public static void main(String[] args) {     
        if(sSocket != null){    
            ArrayList <Socket> socketsEmpleados = aceptarConexiones();
                        
            Datos datos = new Datos(socketsEmpleados.size());
            HorarioEmpleado [] horariosEmpleados = crearHorariosEmpleados(socketsEmpleados);
            
            if (!socketsEmpleados.isEmpty()) {
                lanzaHilos(socketsEmpleados, datos, horariosEmpleados);
                imprimirHorario(horariosEmpleados);            
            }
            //se manda una señal de apagado al creador de tareas y se cierran todos los sockets antes de finalizar.
            datos.desconectarServidorTareas();
            cerrarSockets(socketsEmpleados, sSocket);
        }        
    }
    
//Metodos para manejar los canales de comunicacion del jefe/servidor.    
    private static ServerSocket getServerSockect() {
        ServerSocket sSocket = null;
        try {
            sSocket = new ServerSocket(5000);
        } catch (IOException e) {e.printStackTrace();}
        
        return  sSocket;
    }
       
    //Recibe un arrayList de sockets que rellena estableciendo conexiones a peticion de clientes
    //durante la duraccion indicada en el timer tras la primera conexion realizada.
    private static ArrayList<Socket> aceptarConexiones() {
        ArrayList<Socket> socketsEmpleados = new ArrayList<>();
        int contadorEmpleados = 0;
        
        Timer timer = new Timer();
        
        while (!limiteDeTiempoAlcanzado) {                
           socketsEmpleados.add(establecerComunicacion(sSocket));  

            if (socketsEmpleados.get(contadorEmpleados) == null) 
                socketsEmpleados.remove(contadorEmpleados);
            
            //Tras la primera conexion se inicia un temporizador de 10 segundos que ejecuta tasktimer.
            if(contadorEmpleados == 0)
                    timer.schedule(tTask, 10 * 1000/*1 sec*/);

            contadorEmpleados++;           
        }    
        timer.cancel();
        return socketsEmpleados;
    }
    
    private static Socket establecerComunicacion(ServerSocket sSocket) {
       Socket socket = null;
        try {
            socket = sSocket.accept();           
        } catch (IOException e) {}
              
       return socket;
    }
    
    private static void cerrarSockets(ArrayList<Socket> sockets, ServerSocket serverSocket){
        try {
            if (!sockets.isEmpty()) 
                for (int i = 0; i < sockets.size(); i++)
                    sockets.get(i).close();
            
            if(serverSocket != null)
                serverSocket.close();

        } catch (IOException e) {e.printStackTrace();}
    }

//Varios
    //Lanza los hilos encargados de cada empleado y espera hasta que terminen todos.
    private static Thread [] lanzaHilos(ArrayList<Socket> socketsEmpleados,Datos datos, HorarioEmpleado [] horariosEmpleado){
        Thread [] hilos = new  Thread[socketsEmpleados.size()];
        
        for (int i = 0; i < socketsEmpleados.size(); i++) {
            EncargadoEmpleado hiloEncargadoEmpleado = new EncargadoEmpleado(socketsEmpleados.get(i), datos, horariosEmpleado[i]);
            hilos[i] = new Thread(hiloEncargadoEmpleado);                
            hilos[i].start();
        }

        for (int i = 0; i < socketsEmpleados.size(); i++) {
            try {hilos[i].join();
            } catch (InterruptedException ex) {ex.printStackTrace();}
        }
        return hilos;
}
    
    //Genera un array de horarios para guardar el horario de cada empleado.
    private static HorarioEmpleado[] crearHorariosEmpleados(ArrayList<Socket> socketsEmpleados){
        HorarioEmpleado[] horariosEmpleados = new HorarioEmpleado[socketsEmpleados.size()]; 
            
         for (int i = 0; i < horariosEmpleados.length; i++) {
                horariosEmpleados[i] = new HorarioEmpleado(i+1);
            }
         
         return horariosEmpleados;
    }
    
    //Imprime en un fichero de texto el resultado del reparto de tareas.
    private static void imprimirHorario(HorarioEmpleado[] horarios){
        try {
            FileWriter fw = new FileWriter(new File("Horario.txt"));
            BufferedWriter bWriter = new BufferedWriter(fw);
            
            for (int i = 0; i < horarios.length; i++){
                for (int j = 0; j < horarios[i].getHorario().size(); j++) {
                    bWriter.write("Empleado: " + horarios[i].getNumEmpleado() + " ");
                    bWriter.write("ID Tarea: " + horarios[i].getHorario().get(j).getID() + " Duracion: " + horarios[i].getHorario().get(j).getTiempoTarea());
                    bWriter.newLine();
                }
            bWriter.newLine();
            }
            
           
            bWriter.close(); fw.close();
        } catch (IOException e) {e.printStackTrace();}        
    }   

}