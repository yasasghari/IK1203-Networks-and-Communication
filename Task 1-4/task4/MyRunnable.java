import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import tcpclient.TCPClient;

public class MyRunnable implements Runnable {

    private String hostname = "";
    private byte[] data = new byte[2048];
    private Boolean shutdown = false;
    private Integer limit = null;
    private Integer timeout = null;

    private String BadRequest = "HTTP/1.1 400 Bad Request\r\n";
    private String NotFound = "HTTP/1.1 404 Not Found\r\n";
    private String ok = "HTTP/1.1 200 OK\r\n\r\n";

    private String status = "";
    private Socket socket;

    public MyRunnable(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {

            int clientPort = 0;
            InputStream inStream = socket.getInputStream();
            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            int size = 0; // initial size of data
            while (size != -1) {
                size = inStream.read(data);
                bStream.write(data, 0, size);
                if (bStream.toString().endsWith("\n")) {
                    break;
                }
            }

            String ask = "";
            String sections = "";
            String version = "";
            StringBuilder sb = new StringBuilder();

            String string = bStream.toString("UTF-8");
            String[] splits = string.split("\r\n");
            String[] row = splits[0].split(" ");

            for (String s : row) {
                if (s.startsWith("/ask?")) {
                    ask = s;
                    sections = ask.substring(5);
                } else if (s.startsWith("HTTP/1.1")) {
                    version = s;
                }
            }
            OutputStream outPut = socket.getOutputStream();

            if (!ask.startsWith("/ask?")) {
                status = NotFound;
                sb.append(status);
                System.out.println("not found");
                outPut.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                socket.close();
                return;

            } else if (!version.startsWith("HTTP/1.1")) {
                status = BadRequest;
                sb.append(status);
                System.out.println("bad request");
                outPut.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                socket.close();
                return;

            } else if (!row[0].equals("GET")) {
                status = BadRequest;
                sb.append(status);
                System.out.println("bad request");
                outPut.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                socket.close();
                return;

            }

            String[] fin = sections.split("&");
            String stringData = "";

            for (int i = 0; i < fin.length; i++) {
                String[] stringArray = fin[i].split("=");
                if (string.length() >= 2) {
                    switch (stringArray[0]) {
                        case "hostname":
                            hostname = stringArray[1];
                            break;
                        case "port":
                            clientPort = Integer.parseInt(stringArray[1]);
                            break;
                        case "string":
                            stringData = stringArray[1];
                            break;
                        case "shutdown":
                            shutdown = Boolean.parseBoolean(stringArray[1]);
                            break;
                        case "limit":
                            limit = Integer.parseInt(stringArray[1]);
                            break;
                        case "timeout":
                            timeout = Integer.parseInt(stringArray[1]);
                            break;
                    }
                }
            }

            if (0 > clientPort || clientPort > 65536) {
                status = BadRequest;
                sb.append(status);
                System.out.println("bad request");
                outPut.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                socket.close();
                return;

            }
            sb.append(ok);
            TCPClient client = new TCPClient(shutdown, timeout, limit);
            byte[] temp = null;
            try {
                temp = client.askServer(hostname, clientPort, stringData.getBytes());
            } catch (IOException e) {
                System.out.println(e);
            }

            sb.append(new String(temp));
            bStream.write(sb.toString().getBytes());
            outPut.write(sb.toString().getBytes(StandardCharsets.UTF_8));

            socket.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}