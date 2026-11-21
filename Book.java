
public class Book {
    private static final String[] CATEGORIES = {
        "Programming", "Science", "Literature", "Other"
    };

    private final int id;
    private String title;
    private String author;
    private String category;

    public Book(int id, String title, String author, String category) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required.");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author is required.");
        }
        if (!isValidCategory(category)) {
            throw new IllegalArgumentException("Invalid category.");
        }
        this.id = id;
        this.title = title.trim();
        this.author = author.trim();
        this.category = category;
    }

    public static String[] getCategories() {
        return CATEGORIES.clone();
    }

    public static boolean isValidCategory(String value) {
        for (String c : CATEGORIES) {
            if (c.equals(value)) return true;
        }
        return false;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }

    public void showDetails() {
        System.out.println(id + " | " + title + " | " + author + " | " + category);
    }
}