public class Professor implements Contact {
    private String firstName;
    private String lastName;
    private Category category;
    private String email;
    private String officePhone;
    private String domain;

    public Professor(String firstName, String lastName, Category category, String email, String officePhone, String domain) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.category = category;
        this.email = email;
        this.officePhone = officePhone;
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
    public String getStudentId() { return null; }

    @Override
    public void setStudentId(String studentId) {
        throw new UnsupportedOperationException("Un professeur ne possède pas d'identifiant étudiant");
    }

    @Override
    public String getEmail() { return email; }

    @Override
    public void setEmail(String email) { this.email = email; }

    @Override
    public String getOfficePhone() { return officePhone; }

    @Override
    public void setOfficePhone(String officePhone) { this.officePhone = officePhone; }

    @Override
    public String getDomain() { return domain; }

    @Override
    public void setDomain(String domain) { this.domain = domain; }

    @Override
    public String toString() {
        return "Professor{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", category=" + category +
                ", email='" + email + '\'' +
                ", officePhone='" + officePhone + '\'' +
                ", domain='" + domain + '\'' +
                "}";
    }
}
