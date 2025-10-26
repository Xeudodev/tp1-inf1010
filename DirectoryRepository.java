import java.util.*;

public class DirectoryRepository {
    private static final DirectoryRepository INSTANCE = new DirectoryRepository();

    private final List<Student> students = new ArrayList<>();
    private final List<Professor> professors = new ArrayList<>();

    private final Set<String> redlist = new HashSet<>();

    private boolean seeded = false;

    public static DirectoryRepository getInstance() {
        return INSTANCE;
    }

    public static String identifierOf(Contact c) {
        if (c instanceof Student s)
            return s.getStudentId();
        if (c instanceof Professor p)
            return p.getOfficePhone();
        return null;
    }

    private DirectoryRepository() {
        System.err.println("[INFO] DirectoryRepository initialized.");
    }

    public synchronized Contact searchByIdentifier(String ident) {
        for (Student s : students) {
            if (s.getStudentId() != null && s.getStudentId().equalsIgnoreCase(ident))
                return s;
        }
        for (Professor p : professors) {
            if (p.getOfficePhone() != null && p.getOfficePhone().equalsIgnoreCase(ident))
                return p;
        }
        return null;
    }

    public synchronized boolean addStudent(Student s) {
        if (s == null || s.getStudentId() == null)
            return false;
        if (searchByIdentifier(s.getStudentId()) != null)
            return false;
        students.add(s);
        return true;
    }

    public synchronized boolean addProfessor(Professor p) {
        if (p == null || p.getOfficePhone() == null)
            return false;
        if (searchByIdentifier(p.getOfficePhone()) != null)
            return false;
        professors.add(p);
        return true;
    }

    public synchronized void seedUQTR() {
        if (seeded || (!students.isEmpty() || !professors.isEmpty()))
            return;

        Professor t1 = new Professor(
                "Pierre", "Lemaire", Category.PROFESSOR,
                "pierre.lemaire@uqtr.ca", "8193765011", "Informatique");
        addProfessor(t1);

        Professor t2 = new Professor(
                "Francine", "Rousseau", Category.ASSISTANT,
                "francine.rousseau@uqtr.ca", "8198874447", "Informatique");
        addProfessor(t2);

        Student s1 = new Student(
                "William", "Beaudoin", "BEAW92070107",
                "william.beaudoin@uqtr.ca", "Informatique");
        addStudent(s1);

        Student s2 = new Student(
                "Mathilde", "Côté", "COTM93020256",
                "mathilde.cote@uqtr.ca", "Musique");
        addStudent(s2);
        redlistAdd(s2.getStudentId());

        Student s3 = new Student(
                "Étienne", "Courschene", "COUE15070203",
                "etienne.courschene@uqtr.ca", "Informatique");
        addStudent(s3);

        seeded = true;
    }

    public synchronized List<Contact> listByCategory(Category category) {
        List<Contact> out = new ArrayList<>();
        switch (category) {
            case STUDENT -> out.addAll(students);
            case PROFESSOR, ASSISTANT -> out.addAll(professors);
            default -> {
            }
        }
        return out;
    }

    public synchronized boolean isRedlisted(Contact c) {
        String id = identifierOf(c);
        return id != null && redlist.contains(id);
    }

    public synchronized boolean redlistAdd(String id) {
        if (searchByIdentifier(id) == null)
            return false;
        return redlist.add(id);
    }

    public synchronized boolean redlistRemove(String id) {
        return redlist.remove(id);
    }
}