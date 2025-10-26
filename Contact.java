public interface Contact {
    String getFirstName();
    void setFirstName(String firstName);

    String getLastName();
    void setLastName(String lastName);

    Category getCategory();
    void setCategory(Category category);

    String getStudentId();
    void setStudentId(String studentId);

    String getEmail();
    void setEmail(String email);

    String getOfficePhone();
    void setOfficePhone(String officePhone);

    String getDomain();
    void setDomain(String domain);

    String toString();
}
