package programarepartodetareas.ServidorCreadorDeTareas;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class CreadorDeTareas {
    public static void main(String[] args) {
        DatagramSocket dSocket = getDatagramSocket();
        
        if(dSocket != null){
            InetAddress direccionReceptor;
            if((direccionReceptor = getIAddress()) != null){
                int tarea , id = 0, puertoReceptor = 6000;
                ByteBuffer bBufferTarea = ByteBuffer.allocate(4), bBufferID = ByteBuffer.allocate(4);
                
                while(ByteBuffer.wrap(recibirSenial(dSocket)).getInt() != -1){
                    tarea = (int)(Math.random()*(4-1) + 1);
                    id++;
                    
                    bBufferTarea.clear();bBufferTarea.putInt(tarea);
                    bBufferID.clear();bBufferID.putInt(id);
                    
                    byte bufferTarea [] = bBufferTarea.array(), bufferID [] = bBufferID.array();
                    
                    DatagramPacket paqueteTarea = new DatagramPacket(bufferTarea,bufferTarea.length,direccionReceptor,puertoReceptor);
                    enviarPaquete(paqueteTarea, dSocket);
                    
                    DatagramPacket paqueteId = new DatagramPacket(bufferID,bufferID.length,direccionReceptor,puertoReceptor);
                    enviarPaquete(paqueteId, dSocket);
                }
            }
        }
        
        dSocket.close(); 
    }
    
//Metodos generales
    private static DatagramSocket getDatagramSocket(){
        DatagramSocket dSocket = null;
        
        try {
            dSocket = new DatagramSocket(6001);
        } catch (SocketException ex) {ex.printStackTrace();}
        
        return dSocket;
    }
    
    private static InetAddress getIAddress(){
        InetAddress direccionInet = null;
        
        try {
            direccionInet = InetAddress.getByName("localhost");
        } catch (UnknownHostException ex) {ex.printStackTrace();}
        
        return direccionInet;
    }
//Metodos de trabajo   
    //Capta una señal con un valor de 1 que indica la peticion de una nueva tarea o -1 que indica el cierre de la conexion y el final.
    private static byte [] recibirSenial(DatagramSocket dSocket){
        byte[] buffer = new byte[4];
        DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
        
        try {
            dSocket.receive(paquete);
            buffer = paquete.getData();
        } catch (IOException ex) {ex.printStackTrace();}
        
        return buffer;
    }
    
    //Envia el paquete elegido por el socket elegido.
    private static void enviarPaquete(DatagramPacket paquete,DatagramSocket dSocket){
        try {
            dSocket.send(paquete);
        } catch (IOException ex) {ex.printStackTrace();}
    }
}
