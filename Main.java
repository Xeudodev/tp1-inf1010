public class Main {
    public static void main(String[] args) {
        Student student = new Student("William", "Beaudoin", "BEAW92070107", "william.beaudoin@uqtr.qc.ca", "Informatique");
        Professor professor = new Professor("Pierre", "Lemaire", Category.PROFESSOR, "pierre.lemaire@uqtr.qc.ca", "5144475521", "Informatique");
        
        System.out.println(student);
        System.out.println(professor);
    }
}
