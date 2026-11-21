public abstract class User {
    private final int id;
    private String name;

    protected User(int id, String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        this.id = id;
        this.name = name.trim();
    }

    public int getId() { return id; }
    public String getName() { return name; }

    public abstract void showProfile();
}