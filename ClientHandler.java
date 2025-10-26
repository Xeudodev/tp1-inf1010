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

            String line = in.readLine();
            if (line == null || line.trim().isEmpty()) {
                out.println("[ERROR] Empty request");
                return;
            }

            line = line.trim();
            System.out.println("[INFO] Received from " + s.getRemoteSocketAddress() + ": " + line);

            String[] arguments = line.split("\\|", -1);
            String command = arguments[0];
            switch (command) {
                case "getMembersFromCategory" -> handleListByCategory(arguments, out);
                case "getTeachersByDomain" -> handleListProfessorsByDomain(arguments, out);
                case "search" -> handleSearch(arguments, out);
                case "add" -> handleAdd(arguments, out);
                case "delete" -> handleDelete(arguments, out);
                case "addRedlist" -> handleRedlist(arguments, out, true);
                case "removeRedlist" -> handleRedlist(arguments, out, false);
                default -> {
                    out.println("[ERROR] Unknown command: " + command);
                    out.println("END");
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Client handler error: " + e.getMessage());
        }
    }

    private void handleAdd(String[] arguments, PrintWriter out) {
        String type = arguments[1];
        var repo = DirectoryRepository.getInstance();
        boolean operationSucceeded = false;
        if ("STUDENT".equalsIgnoreCase(type)) {
            String student = arguments[2], last = arguments[3], sid = arguments[4], email = arguments[5],
                    domain = arguments[6];
            operationSucceeded = repo.addStudent(new Student(student, last, sid, email, domain));
        } else if ("PROFESSOR".equalsIgnoreCase(type)) {
            String first = arguments[2], last = arguments[3], cat = arguments[4], email = arguments[5],
                    phone = arguments[6], domain = arguments[7];

            Category category;
            try {
                category = Category.valueOf(cat.toUpperCase());
            } catch (Exception e) {
                out.println("[ERROR] Invalid professor category: " + cat);
                out.println("END");
                return;
            }

            operationSucceeded = repo.addProfessor(new Professor(first, last, category, email, phone, domain));
        } else {
            out.println("[ERROR] Unknown type: " + type);
            out.println("END");
            return;
        }
        out.println(operationSucceeded ? "[INFO] Member added." : "[ERROR] Duplicate or invalid data.");
        out.println("END");
    }

    private void handleListByCategory(String[] arguments, PrintWriter out) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private void handleListProfessorsByDomain(String[] arguments, PrintWriter out) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private void handleSearch(String[] arguments, PrintWriter out) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private void handleDelete(String[] arguments, PrintWriter out) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private void handleRedlist(String[] arguments, PrintWriter out, boolean add) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}