public class Member extends User {
    private String department;

    public Member(int id, String name, String department) {
        super(id, name);
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department is required.");
        }
        this.department = department.trim();
    }

    public String getDepartment() { return department; }

    @Override
    public void showProfile() {
        System.out.println("ID: " + getId() + " | Name: " + getName()
                + " | Department: " + department + " | Role: Member");
    }
}