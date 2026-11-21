public class Librarian extends User {

    public Librarian() {
        super(0, "Librarian");
    }

    @Override
    public void showProfile() {
        System.out.println("ID: " + getId() + " | Name: " + getName() + " | Role: Librarian");
    }
}