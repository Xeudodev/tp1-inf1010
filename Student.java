public class Student implements Contact {
    private String firstName;
    private String lastName;
    private Category category;
    private String email;
    private String studentId;
    private String domain;

    public Student(String firstName, String lastName, String studentId, String email, String domain) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.category = Category.STUDENT;
        this.studentId = studentId;
        this.email = email;
        this.domain = domain;
    }

    @Override
    public String getFirstName() { return firstName; }

    @Override
    public void setFirstName(String firstName) { this.firstName = firstName; }

    @Override
    public String getLastName() { return lastName; }

    @Override
    public void setLastName(String lastName) { this.lastName = lastName; }

    @Override
    public Category getCategory() { return category; }

    @Override
    public void setCategory(Category category) { this.category = category; }

    @Override
    public String getStudentId() { return studentId; }

    @Override
    public void setStudentId(String studentId) { this.studentId = studentId; }

    @Override
    public String getEmail() { return email; }

    @Override
    public void setEmail(String email) { this.email = email; }

    @Override
    public String getOfficePhone() { return null; }

    @Override
    public void setOfficePhone(String officePhone) {
        throw new UnsupportedOperationException("Un étudiant ne possède pas de téléphone de bureau");
    }

    @Override
    public String getDomain() { return domain; }

    @Override
    public void setDomain(String domain) { this.domain = domain; }

    @Override
    public String toString() {
        return "Student{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", studentId='" + studentId + '\'' +
                ", email='" + email + '\'' +
                ", domain='" + domain + '\'' +
                "}";
    }
}
