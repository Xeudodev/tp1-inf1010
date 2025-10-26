import java.net.Socket;
import java.net.UnknownHostException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Client {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;

        try (Socket serveur = new Socket(host, port);
             PrintWriter out = new PrintWriter(serveur.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(serveur.getInputStream()))) {

            // message minimal envoyé au serveur
            out.println("Bonjour serveur!");

            // lire la première ligne de réponse
            String recevoir = in.readLine();
            System.out.println("[INFO] Server response: " + recevoir);

        } catch (UnknownHostException e) {
            System.err.println("[ERROR] Unknown host: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[ERROR] IO error: " + e.getMessage());
        }
    }
}
