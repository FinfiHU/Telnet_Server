package utb.fai;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class App {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar server.jar <port> <max_threads>");
            return;
        }

        final int port;
        final int maxThreads;
        try {
            port = Integer.parseInt(args[0]);
            maxThreads = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Port and maximum thread count must be integers.");
            return;
        }

        if (port < 1 || port > 65535) {
            System.err.println("Port must be an integer between 1 and 65535.");
            return;
        }
        if (maxThreads < 1) {
            System.err.println("Maximum thread count must be greater than zero.");
            return;
        }

        ThreadPoolExecutor clientPool = new ThreadPoolExecutor(
                maxThreads,
                maxThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new SynchronousQueue<>());

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                try {
                    clientPool.execute(new ClientThread(clientSocket));
                } catch (RejectedExecutionException e) {
                    System.err.println("Maximum number of clients reached; rejecting connection.");
                    closeSocket(clientSocket);
                }
            }
        } catch (IOException e) {
            System.err.println("Server failed: " + e.getMessage());
        } finally {
            clientPool.shutdownNow();
        }
    }

    private static void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Failed to close rejected client connection: " + e.getMessage());
        }
    }
}
