package tcpclient;
import java.net.*;
import java.io.*;


public class TCPClient {
    
    public TCPClient() {

    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {

        Socket socket = null;  
        try {
             socket = new Socket(hostname, port); 
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        }

        try{
        //recieve data from server 
        ByteArrayOutputStream bStream = new ByteArrayOutputStream(); 
        InputStream inStream = (socket.getInputStream()); 
        byte [] data = new byte[2048]; 

        socket.getOutputStream().write(toServerBytes); 

        /*recieving 3000, Round 1 = 2048, Round 2 = 2048 Round 3 = 952 which leads to .read = -1 and we exit */
        int size =0; //initial size of data
        while(size != -1){ 
            bStream.write(data, 0,size);
            size = inStream.read(data); 
        }

        inStream.close();
        socket.close(); 
        return bStream.toByteArray();
    }catch(IOException e){
        System.out.println(e);
    }
    return null; 
    }
}
