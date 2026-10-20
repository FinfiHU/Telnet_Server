package utb.fai;

import java.io.*;
import java.net.*;

public class ClientThread extends Thread {

    private final Socket clientSocket;

    public ClientThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[4096];
        try (Socket socket = clientSocket) {
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                output.flush();
            }
        } catch (IOException e) {
            System.err.println("Client connection failed: " + e.getMessage());
        }
    }
}
