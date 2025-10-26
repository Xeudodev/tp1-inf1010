import java.net.*;
import java.io.*;

public class Server {

    public static void main(String[] args) {
        int port = 8080;
        System.out.println("[INFO] The server is starting on port " + port);

        // Seed repo
        try {
            DirectoryRepository.getInstance().seedUQTR();
            System.out.println("[INFO] Repository seeded with UQTR sample data.");
        } catch (Throwable t) {
            System.err.println("[WARN] Failed to seed repository: " + t.getMessage());
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[INFO] The server is listening on port " + port);
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[INFO] New client connected from: " + clientSocket.getRemoteSocketAddress());

                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("[ERROR] The server failed: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
