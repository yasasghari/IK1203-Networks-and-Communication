import java.net.*;
import java.io.*;

public class ConcHTTPAsk {
    public static void main(String[] args) throws IOException {
        ServerSocket CurrentServer = new ServerSocket(Integer.parseInt(args[0]));
        try {
            while (true) {
                Thread thread = new Thread(new MyRunnable(CurrentServer.accept()));
                thread.start();
            }

        } catch (Exception e) {
            System.out.println(e);

        }

    }

}
