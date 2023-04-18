package tcpclient;
import java.net.*;
import java.util.concurrent.TimeoutException;
import java.io.*;

public class TCPClient {
    public boolean shutdown = false; 
    public Integer timeout = null;
    public Integer limit = null;  

    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {

        //setting the instances 
        this.shutdown = shutdown; 
        this.timeout = timeout; 
        this.limit = limit; 

    }

    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {
        
        Socket socket = null;  
        //recieve data from server 
        ByteArrayOutputStream bStream = new ByteArrayOutputStream(); 
        try {

        socket = new Socket(hostname, port); 
        InputStream inStream = (socket.getInputStream()); 
        byte [] data = new byte[2048]; 

        //setting limit case
        if(this.timeout != null) socket.setSoTimeout(this.timeout);
        //setting timeout case 
        if(this.limit != null) socket.setReceiveBufferSize(this.limit); 

        //write the toServerBytes
        socket.getOutputStream().write(toServerBytes); 

        //setting shutdown case
        if(shutdown) socket.shutdownOutput();

        /*recieving 3000, Round 1 = 2048, Round 2 = 2048 Round 3 = 952 which leads to .read = -1 and we exit */
        int size =0; //initial size of data
        while(size != -1){
            bStream.write(data, 0,size);
            size = inStream.read(data);      
    }

        inStream.close();
        socket.close(); 
        return bStream.toByteArray();
    
        }catch(Exception e){
        System.out.println(e);
        return bStream.toByteArray();
        }
    }
}