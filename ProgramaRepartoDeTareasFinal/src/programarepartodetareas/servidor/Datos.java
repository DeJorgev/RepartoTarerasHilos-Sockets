package programarepartodetareas.servidor;

import Compartido.Tarea;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;


public  class Datos {
    private int hilosTotales, hilosAArrancar ,hilosPorProbarTarea;
    private Tarea tarea;
    
    //Variables conexion con servidor creador de información de tareas.
    private final DatagramSocket dSocket = getDatagramSocket();
    private final InetAddress direccionInet = getIAddress();
    
    public Datos(int hilosTotales) {
        this.hilosTotales = hilosTotales;
        hilosAArrancar = hilosTotales;
        hilosPorProbarTarea = hilosTotales;
    }
    
//Metodos generales    
    public Tarea getTarea() {
        return tarea;
    }
      
//Metodos ciclo de vida hilo
    //Pone en pausa los hilos al inicio de su vida, salvo el ultimo para asi trabajar de 1 en uno.
    public synchronized void arrancarHilo(){
        if (hilosAArrancar > 1) {
            hilosAArrancar--;
            try { wait();
            } catch (InterruptedException ex) {ex.printStackTrace();}              
        }else
            crearTarea();
    }
    
    //Si hay mas de un hilo vivo se crea una nueva tarea, se notifica a otro hilo y duerme el actual.
    public synchronized void marcarTareaAceptada(){
        tarea = crearTarea();        
        
        //Si solo queda un hilo vivo se siguen creando para el hasta que muera.
        if (hilosTotales > 1) {
                try {
                    notify();
                    wait();
                }catch (InterruptedException ex) {ex.printStackTrace();}
            }
    }
    
    //Si hay mas de un hilo vivo se mantiene la tarea, se notifica a otro hilo y duerme el actual hasta que todos la rechacen o uno la acepte.
    public synchronized void marcarTareaSuperiorAUnoRechazada(){
        //Si solo hay un hilo vivo se crean tareas nuevas hasta que muera 
        if (hilosTotales > 1) {
            hilosPorProbarTarea--;

            //Solo si todos los hilos rechazan la tarea se crea una nueva
            if (hilosPorProbarTarea > 0)
                try {
                    notify();
                    wait();
                } catch (InterruptedException ex) {ex.printStackTrace();}   
            else
                try {
                    crearTarea();
                    notify();
                    wait();
                } catch (InterruptedException ex) {ex.printStackTrace();}
        }
        else
            crearTarea();
    }
    
    //Si al hilo se le devuelve una tarea que ya habia rechazado despierta a otro y se duerme.
    public synchronized void marcarTareaYaRechazada(){
        try {
            notify();
            wait();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
        
    //Al llegar el hilo al fin de su vida despierta a uno al azar para que continue.
    public synchronized void marcarMuerteHilo(){
        hilosTotales--;
        notify();
    }

//Metodos conexion con servidor creador de tareas.
    private DatagramSocket getDatagramSocket(){
        DatagramSocket dSocket = null;
        
        try {
            dSocket = new DatagramSocket(6000);
        } catch (SocketException ex) {ex.printStackTrace();}
        
        return dSocket;
    }
    
    private InetAddress getIAddress(){
        InetAddress direccionInet = null;
        
        try {
            direccionInet = InetAddress.getByName("localhost");
        } catch (UnknownHostException ex) {ex.printStackTrace();}
        
        return direccionInet;
    }
    
//Metodos trabajo con servidor creador de tareas.    
    //Envia una señal al sevidor creador de tareas para que envie tareas o acabe, al acabar cierra la conexion.
    private void enviarSenial(boolean acabar){
        ByteBuffer bBufferSenial = ByteBuffer.allocate(4);
        
        if (!acabar) 
            bBufferSenial.putInt(1);
        else
            bBufferSenial.putInt(-1);
        
        byte [] bufferSenial = bBufferSenial.array();
        
        DatagramPacket paqueteSenial = new DatagramPacket(bufferSenial, bufferSenial.length, direccionInet, 6001);
        
        try { dSocket.send(paqueteSenial);
        } catch (IOException ex) {ex.printStackTrace();}
        
        if (acabar) dSocket.close();
    } 
    
    //Usa el metodo enviarSenial() para enviar una señal de final al servidor en un código mas seguro.
    public void desconectarServidorTareas(){
        enviarSenial(true);
    }
    
    //Recibe y procesa en una tarea la informacion recibida del servidor.
    private Tarea recibirInformacionTarea(){
        byte[] bufferTarea = new byte[4],bufferID = new byte[4];
        DatagramPacket paqueteTarea = new DatagramPacket(bufferTarea, bufferTarea.length), paqueteID = new DatagramPacket(bufferID, bufferTarea.length);
        
        enviarSenial(false);
        try {
            dSocket.receive(paqueteTarea);
            bufferTarea = paqueteTarea.getData();
            dSocket.receive(paqueteID);
            bufferID = paqueteID.getData();
        } catch (IOException ex) {ex.printStackTrace();}
        
        Tarea tareaSuministrada = new Tarea(ByteBuffer.wrap(bufferID).getInt(),ByteBuffer.wrap(bufferTarea).getInt());
        
        return tareaSuministrada;
    }    
    
//Varios
    //Crea una tarea nueva y resetea el contador de hilos por probarla.
    private Tarea crearTarea(){
        tarea = recibirInformacionTarea();
        hilosPorProbarTarea = hilosTotales;
        
        return tarea;
    }
}
