import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ClientHandler implements Runnable {
    private final Socket socket;

    ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (Socket s = socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {

            // Pour l'instant, lit une seule ligne, la traite, puis quitte.
            String line = in.readLine();
            if (line == null) {
                out.println("[ERROR] No input was received");
                return;
            }

            line = line.trim();
            if (line.isEmpty()) {
                out.println("[ERROR] The message is empty");
                return;
            }

            // On affiche le message reçu et envoie une confirmation de réception.
            System.out.println("[INFO] Received from " + s.getRemoteSocketAddress() + ": " + line);
            out.println("Le message a bien été reçu. (" + line + ")");

        } catch (IOException e) {
            System.err.println("[ERROR] Client handler error: " + e.getMessage());
        }
    }
}