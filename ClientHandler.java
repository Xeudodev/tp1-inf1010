import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
                    case "update" -> handleUpdate(arguments, out);
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
        String domain = arguments[1].trim();
        var repo = DirectoryRepository.getInstance();
        var list = repo.listProfessorsByDomain(domain);
        if (list.isEmpty()) {
            out.println("[INFO] No professors or assistants found in domain: " + domain);
        } else {
            for (Professor p : list) {
                if (repo.isRedlisted(p)) {
                    out.println(p.getFirstName() + " " + p.getLastName() + " [RED LIST]");
                } else {
                    out.println(p.toString());
                }
            }
        }
        out.println("END");
    }

    private void handleSearch(String[] arguments, PrintWriter out) {
        String id = arguments[1].trim();
        var repo = DirectoryRepository.getInstance();
        Contact contact = repo.searchByIdentifier(id);
        if (contact == null) {
            out.println("[INFO] Member not found: " + id);
        } else if (repo.isRedlisted(contact)) {
            out.println(contact.getFirstName() + " " + contact.getLastName() + " [RED LIST]");
        } else {
            out.println(contact.toString());
        }
        out.println("END");
    }

    private void handleDelete(String[] arguments, PrintWriter out) {
        String id = arguments[1].trim();
        var repo = DirectoryRepository.getInstance();
        boolean operationSucceeded = repo.deleteByIdentifier(id);
        if (operationSucceeded) {
            out.println("[INFO] Member deleted: " + id);
        } else {
            out.println("[ERROR] Member not found: " + id);
        }
        out.println("END");
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

    private void handleUpdate(String[] arguments, PrintWriter out) {
        String type = arguments[1].trim().toUpperCase();
        String id = arguments[2].trim();

        var repo = DirectoryRepository.getInstance();
        Contact contact = repo.searchByIdentifier(id);
        if (contact == null) {
            out.println("[ERROR] Member not found: " + id);
            out.println("END");
            return;
        }

        boolean typeAllowed = ("STUDENT".equals(type) && contact instanceof Student) ||
                         ("PROFESSOR".equals(type) && contact instanceof Professor);
        if (!typeAllowed) {
            out.println("[ERROR] Type invalid for the identifier: " + id);
            out.println("END");
            return;
        }

        Map<String, String> changes = new HashMap<>();
        for (int i = 3; i < arguments.length; i++) {
            String arg = arguments[i];
            int identifier = arg.indexOf('=');
            if (identifier <= 0) {
                out.println("[WARN] Ignoring invalid token: " + arg);
                continue;
            }
            String key = arg.substring(0, identifier).trim();
            String value = arg.substring(identifier + 1).trim();
            changes.put(key, value);
        }

        if (changes.isEmpty()) {
            out.println("[ERROR] No valid changes provided.");
            out.println("END");
            return;
        }

        Set<String> allowed;
        if ("STUDENT".equals(type)) {
            allowed = new HashSet<>(Arrays.asList(
                "firstName","lastName","studentId","email","domain"
            ));
        } else if ("PROFESSOR".equals(type)) {
            allowed = new HashSet<>(Arrays.asList(
                "firstName","lastName","category","email","officePhone","domain"
            ));
        } else {
            out.println("[ERROR] Unknown type: " + type);
            out.println("END");
            return;
        }

        for (String k : new ArrayList<>(changes.keySet())) {
            if (!allowed.contains(k)) {
                out.println("[WARN] Ignoring unknown field: " + k);
                changes.remove(k);
            }
        }

        boolean operationSucceeded;
        if ("STUDENT".equals(type)) {
            operationSucceeded = repo.updateStudent(id, changes);
        } else {
            operationSucceeded = repo.updateProfessor(id, changes);
        }

        if (operationSucceeded) {
            out.println("[INFO] Member updated.");
        } else {
            out.println("[ERROR] Update failed.");
        }
        out.println("END");
    }
}