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

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    out.println("[ERROR] Empty request");
                    out.println("END");
                    continue;
                }

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
                    case "auth" -> handleAuthentication(arguments, out);
                    case "QUIT" -> {
                        out.println("[INFO] Bye");
                        out.println("END");
                        return;
                    }
                    default -> {
                        out.println("[ERROR] Unknown command: " + command);
                        out.println("END");
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Client handler error: " + e.getMessage());
        }
    }

    private boolean handleAuthentication(String[] arguments, PrintWriter out) {
        String password = arguments[1];
        boolean isAdmin = "admin123".equals(password);
        System.out.println("[INFO] Authentication " + (isAdmin ? "successful." : "failed."));
        out.println(isAdmin ? "[INFO] Authentication successful." : "[ERROR] Authentication failed.");
        out.println("END");
        return isAdmin;
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
        String categoryStr = arguments[1].toUpperCase();
        try {
            Category category = Category.valueOf(categoryStr);
            var repo = DirectoryRepository.getInstance();
            var list = repo.listByCategory(category);
            if (list.isEmpty()) {
                out.println("[INFO] No members found.");
            } else {
                for (Contact contact : list) {
                    if (repo.isRedlisted(contact)) {
                        out.println(contact.getFirstName() + " " + contact.getLastName() + " [RED LIST]");
                    } else {
                        out.println(contact.toString());
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            out.println("[ERROR] Invalid category: " + categoryStr);
        }
        out.println("END");
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
        String id = arguments[1];
        var repo = DirectoryRepository.getInstance();
        Contact contact = repo.searchByIdentifier(id);

        if (contact == null) {
            out.println("[ERROR] Member not found: " + id);
            out.println("END");
            return;
        }

        boolean isRedlisted = repo.isRedlisted(contact);
        if (add) {
            if (isRedlisted) {
                out.println("[WARN] Member is already on the redlist.");
            } else {
                boolean operationSucceeded = repo.redlistAdd(id);
                out.println(operationSucceeded ? "[INFO] Member added to the redlist." : "[ERROR] Failed to add to the redlist.");
            }
        } else {
            if (!isRedlisted) {
                out.println("[WARN] Member is not on the redlist.");
            } else {
                boolean operationSucceeded = repo.redlistRemove(id);
                out.println(operationSucceeded ? "[INFO] Member removed from the redlist." : "[ERROR] Failed to remove from the redlist.");
            }
        }
        out.println("END");
    }
}