import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.*;

public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;
    private static final String MENU = """
            ===== Remote Directory =====
            1) List members of a category
            2) List professors by domain
            3) Search for a member
            4) Add a member
            5) Delete a member
            6) Modify a member
            7) Put a member on the red list
            8) Remove a member from the red list
            9) Quit
            Choice: """;

    public static void main(String[] args) {

        Socket server = connectWithRetry(HOST, PORT, 3, 15000, 3000);
        if (server == null) {
            System.err.println("[ERROR] Unable to connect to server after 3 attempts.");
            System.out.print("Press Enter to quit...");
            try (Scanner sc = new Scanner(System.in)) {
                sc.nextLine();
            } catch (Exception ignored) {
            }
            return;
        }

        try (server;
                PrintWriter out = new PrintWriter(server.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
                Scanner scanner = new Scanner(System.in)) {

            System.out.println("[INFO] Connected to " + HOST + ":" + PORT);
            boolean running = true;
            while (running) {
                System.out.print(MENU);
                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1" -> handleListCategory(out, in, scanner);
                    case "2" -> handleListProfessorsByDomain(out, in, scanner);
                    case "3" -> handleSearch(out, in, scanner);
                    case "4" -> handleAdd(out, in, scanner);
                    case "5" -> handleDelete(out, in, scanner);
                    case "6" -> handleUpdate(out, in, scanner);
                    case "7" -> handleRedlist(out, in, scanner, true);
                    case "8" -> handleRedlist(out, in, scanner, false);
                    case "9" -> {
                        running = false;
                        out.println("QUIT");
                        System.out.println("[INFO] Exiting...");
                    }
                    default -> System.out.println("[ERROR] Invalid choice, please try again.");
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("[ERROR] Unknown host: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[ERROR] IO error: " + e.getMessage());
        }
    }

    private static Socket connectWithRetry(String host, int port, int attempts, int timeout, int delay) {
        for (int i = 1; i <= attempts; i++) {
            try {
                Socket s = new Socket();
                s.connect(new InetSocketAddress(host, port), timeout);
                if (i > 1) {
                    System.out.println("[INFO] Connected to " + host + ":" + port + " on attempt " + i + "/" + attempts);
                }
                return s;
            } catch (IOException e) {
                System.err.println("[WARN] Connection attempt " + i + "/" + attempts + " failed: " + e.getMessage());
                if (i < attempts) {
                    System.out.println("[INFO] Retrying in " + (delay / 1000.0) + "s...");
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        return null;
    }

    private static void handleListCategory(PrintWriter out, BufferedReader in, Scanner scanner) throws IOException {
        String label = """
                Choose a category:
                1) PROFESSOR
                2) ASSISTANT
                3) STUDENT
                Your choice: """;

        String category;
        do {
            String choice = Util.prompt(scanner, label).trim();
            category = switch (choice) {
                case "1" -> Category.PROFESSOR.name();
                case "2" -> Category.ASSISTANT.name();
                case "3" -> Category.STUDENT.name();
                default -> {
                    System.out.println("[ERROR] Invalid choice, please enter 1, 2 or 3.");
                    yield null;
                }
            };
        } while (category == null);
        sendAndPrint(out, in, "getMembersFromCategory|" + category);
    }

    private static void handleListProfessorsByDomain(PrintWriter out, BufferedReader in, Scanner scanner)
            throws IOException {
        String label = "Enter domain : ";
        String query = Util.prompt(scanner, label);
        sendAndPrint(out, in, "getTeachersByDomain|" + query);
    }

    private static void handleSearch(PrintWriter out, BufferedReader in, Scanner scanner) throws IOException {
        String label = "Search by student ID for student or phone number if teacher: ";
        String query = Util.prompt(scanner, label);
        sendAndPrint(out, in, "search|" + query);
    }

    private static void handleDelete(PrintWriter out, BufferedReader in, Scanner scanner) throws IOException {
        if (!isAdmin(out, in, scanner)) {
            return;
        }

        String label = "Delete by student ID for student or phone number if teacher: ";
        String query = Util.prompt(scanner, label);
        sendAndPrint(out, in, "delete|" + query);
    }

    private static void handleRedlist(PrintWriter out, BufferedReader in, Scanner scanner, boolean add)
            throws IOException {
        if (!isAdmin(out, in, scanner)) {
            return;
        }

        String action = add ? "add" : "remove";
        String label = (add ? "Add to" : "Remove from")
                + " red list by student ID for student or phone number if teacher: ";
        String query = Util.prompt(scanner, label);
        sendAndPrint(out, in, action + "Redlist|" + query);
    }

    private static void handleAdd(PrintWriter out, BufferedReader in, Scanner scanner) throws IOException {
        if (!isAdmin(out, in, scanner)) {
            return;
        }

        String type = askType(scanner);

        if ("STUDENT".equals(type)) {
            String first = Util.prompt(scanner, "First name: ");
            String last = Util.prompt(scanner, "Last name: ");
            String sid = Util.prompt(scanner, "Student ID: ");
            String email = Util.prompt(scanner, "Email: ");
            String domain = Util.prompt(scanner, "Domain: ");
            String req = String.join("|", "add", type, first, last, sid, email, domain);
            sendAndPrint(out, in, req);
        } else {
            String first = Util.prompt(scanner, "First name: ");
            String last = Util.prompt(scanner, "Last name: ");
            String category = askCategory(scanner);
            String email = Util.prompt(scanner, "Email: ");
            String phone = Util.prompt(scanner, "Office phone: ");
            String domain = Util.prompt(scanner, "Domain: ");
            String req = String.join("|", "add", type, first, last, category, email, phone, domain);
            sendAndPrint(out, in, req);
        }
    }

    private static void handleUpdate(PrintWriter out, BufferedReader in, Scanner scanner) throws IOException {
        if (!isAdmin(out, in, scanner)) {
            return;
        }

        String type = askType(scanner);

        String label = "STUDENT".equals(type)
                ? "Enter student ID associated with the record to modify: "
                : "Enter the office phone associated with the record to modify: ";
        String identifier = Util.prompt(scanner, label);

        System.out.println("[INFO] Current record:");
        sendAndPrint(out, in, "search|" + identifier);

        List<String> changes = new ArrayList<>();
        boolean done = false;
        while (!done) {
            printUpdateMenu(type);
            String choice = Util.prompt(scanner, "Your choice: ");
            if ("9".equalsIgnoreCase(choice)) {
                done = true;
                break;
            }
            String field = fieldKeyFromChoice(type, choice);
            if (field == null) {
                System.out.println("[ERROR] Invalid choice.");
                continue;
            }
            String prompt = switch (field) {
                case "firstName" -> "New first name: ";
                case "lastName" -> "New last name: ";
                case "studentId" -> "New student ID: ";
                case "email" -> "New email: ";
                case "domain" -> "New domain: ";
                case "category" -> "New category (PROFESSOR or ASSISTANT): ";
                case "officePhone" -> "New office phone: ";
                default -> "New value: ";
            };
            String value = Util.prompt(scanner, prompt);
            if ("category".equals(field)) {
                if (!"PROFESSOR".equalsIgnoreCase(value) && !"ASSISTANT".equalsIgnoreCase(value)) {
                    System.out.println("[ERROR] Category must be PROFESSOR or ASSISTANT.");
                    continue;
                }
                value = value.toUpperCase();
            }
            changes.add(field + "=" + value);
        }

        if (changes.isEmpty()) {
            System.out.println("[INFO] No changes to apply.");
            return;
        }

        StringBuilder req = new StringBuilder();
        req.append("update|").append(type).append("|").append(identifier);
        for (String c : changes) {
            req.append("|").append(c);
        }
        sendAndPrint(out, in, req.toString());
    }

    private static String askType(Scanner scanner) {
        String label = """
                Choose type:
                1) STUDENT
                2) PROFESSOR
                Your choice: """;
        while (true) {
            String c = Util.prompt(scanner, label);
            switch (c) {
                case "1" -> {
                    return "STUDENT";
                }
                case "2" -> {
                    return "PROFESSOR";
                }
                default -> System.out.println("[ERROR] Please enter 1 or 2.");
            }
        }
    }

    private static String askCategory(Scanner scanner) {
        String label = """
                Choose category:
                1) PROFESSOR
                2) ASSISTANT
                Your choice: """;
        while (true) {
            String c = Util.prompt(scanner, label);
            switch (c) {
                case "1" -> {
                    return Category.PROFESSOR.name();
                }
                case "2" -> {
                    return Category.ASSISTANT.name();
                }
                default -> System.out.println("[ERROR] Please enter 1 or 2.");
            }
        }
    }

    private static boolean isAdmin(PrintWriter out, BufferedReader in, Scanner scanner) throws IOException {
        String query = Util.prompt(scanner, "Enter admin password: ");
        boolean isAuthorized = sendAndValidate(out, in, "auth|" + query);

        if (!isAuthorized) {
            System.out.println("[ERROR] Authorization failed.");
            return false;
        }
        return true;
    }

    private static void printUpdateMenu(String type) {
        System.out.println("Select a field to update: ");
        if ("STUDENT".equals(type)) {
            System.out.println("1) firstName");
            System.out.println("2) lastName");
            System.out.println("3) studentId");
            System.out.println("4) email");
            System.out.println("5) domain");
            System.out.println("9) Done");
        } else {
            System.out.println("1) firstName");
            System.out.println("2) lastName");
            System.out.println("3) category");
            System.out.println("4) email");
            System.out.println("5) officePhone");
            System.out.println("6) domain");
            System.out.println("9) Done");
        }
    }

    private static String fieldKeyFromChoice(String type, String choice) {
        if ("STUDENT".equals(type)) {
            return switch (choice) {
                case "1" -> "firstName";
                case "2" -> "lastName";
                case "3" -> "studentId";
                case "4" -> "email";
                case "5" -> "domain";
                default -> null;
            };
        } else {
            return switch (choice) {
                case "1" -> "firstName";
                case "2" -> "lastName";
                case "3" -> "category";
                case "4" -> "email";
                case "5" -> "officePhone";
                case "6" -> "domain";
                default -> null;
            };
        }
    }

    private static void sendAndPrint(PrintWriter out, BufferedReader in, String req) throws IOException {
        out.println(req);
        String line;
        System.out.println("[INFO] Start of the server response");
        while ((line = in.readLine()) != null) {
            if ("END".equals(line)) break;
            System.out.println(line);
        }
        System.out.println("[INFO] End of the server response");
    }

    private static boolean sendAndValidate(PrintWriter out, BufferedReader in, String req) throws IOException {
        out.println(req);
        String line;
        boolean success = false;
        while ((line = in.readLine()) != null) {
            if ("END".equals(line)) break;
            if (line.contains("successful")) {
                System.out.println(line);
                success = true;
            } else {
                System.out.println(line);
            }
        }
        return success;
    }
}
