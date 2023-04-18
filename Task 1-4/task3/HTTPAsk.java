import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import tcpclient.TCPClient;

public class HTTPAsk {

    static String hostname = "";
    static byte[] data = new byte[2048];
    static Boolean shutdown = false;
    static Integer limit = null;
    static Integer timeout = null;

    static String BadRequest = "HTTP/1.1 400 Bad Request\r\n";
    static String NotFound = "HTTP/1.1 404 Not Found\r\n";
    static String ok = "HTTP/1.1 200 OK\r\n\r\n";

    static String status = "";

    public static void main(String[] args) throws IOException {
        try {
            ServerSocket CurrentServer = new ServerSocket(Integer.parseInt(args[0]));
            while (true) {
                int clientPort = 0;
                Socket socket = CurrentServer.accept();

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
                    continue;

                } else if (!version.startsWith("HTTP/1.1")) {
                    status = BadRequest;
                    sb.append(status);
                    System.out.println("bad request");
                    outPut.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                    socket.close();
                    continue;
                } else if (!row[0].equals("GET")) {
                    status = BadRequest;
                    sb.append(status);
                    System.out.println("bad request");
                    outPut.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                    socket.close();
                    continue;
                }

                String[] fin = sections.split("&");

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
                                data = stringArray[1].getBytes();
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
                    continue;
                }
                sb.append(ok);
                TCPClient client = new TCPClient(shutdown, timeout, limit);
                byte[] temp = null;
                try {
                    temp = client.askServer(hostname, clientPort, data);
                } catch (IOException e) {
                    System.out.println(e);
                }

                sb.append(new String(temp));
                bStream.write(sb.toString().getBytes());
                outPut.write(sb.toString().getBytes(StandardCharsets.UTF_8));

                socket.close();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}