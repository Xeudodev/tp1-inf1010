import java.util.Scanner;

public class Util {
    
     public static String prompt(Scanner sc, String label) {
        while (true) {
            System.out.print(label);
            String line;
            try {
                line = sc.nextLine();
            } catch (Exception e) {
                return "";
            }
            String value = line == null ? "" : line.trim();
            if (value.isEmpty()) {
                System.out.println("[ERROR] Value cannot be empty. Please try again.");
                continue;
            }
            return value;
        }
    }
}
