
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class LibraryService {
    private static final int MAX_ISSUED_PER_MEMBER = 2;

    private final FileManager files;
    private final ArrayList<Member> members = new ArrayList<>();
    private final ArrayList<Book> books = new ArrayList<>();
    private final ArrayList<Loan> loans = new ArrayList<>();
    private int nextMemberId = 1;
    private int nextBookId = 1;
    private int nextLoanId = 1;

    public LibraryService(FileManager files) {
        this.files = files;
    }

    // ---------- startup ----------

    public void load() throws IOException {
        files.ensureDataFolder();
        List<Member> loadedMembers = files.loadMembers();
        List<Book> loadedBooks = files.loadBooks();
        List<Loan> loadedLoans = files.loadLoans(loadedMembers, loadedBooks);

        members.clear();
        members.addAll(loadedMembers);
        books.clear();
        books.addAll(loadedBooks);
        loans.clear();
        loans.addAll(loadedLoans);

        nextMemberId = largestMemberId() + 1;
        nextBookId = largestBookId() + 1;
        nextLoanId = largestLoanId() + 1;
    }

    private int largestMemberId() {
        int max = 0;
        for (Member m : members) max = Math.max(max, m.getId());
        return max;
    }

    private int largestBookId() {
        int max = 0;
        for (Book b : books) max = Math.max(max, b.getId());
        return max;
    }

    private int largestLoanId() {
        int max = 0;
        for (Loan l : loans) max = Math.max(max, l.getId());
        return max;
    }

    // ---------- member records ----------

    public Member registerMember(String name, String department) throws IOException {
        requireText("Name", name);
        requireText("Department", department);
        Member member = new Member(nextMemberId, name, department);
        members.add(member);
        try {
            files.saveMembers(members);
        } catch (IOException e) {
            members.remove(member);
            throw e;
        }
        nextMemberId++;
        return member;
    }

    public Member findMember(int id) {
        for (Member m : members) {
            if (m.getId() == id) return m;
        }
        return null;
    }

    // ---------- book catalogue ----------

    public Book addBook(String title, String author, String category) throws IOException {
        requireText("Title", title);
        requireText("Author", author);
        Book book = new Book(nextBookId, title, author, category);
        books.add(book);
        try {
            files.saveBooks(books);
        } catch (IOException e) {
            books.remove(book);
            throw e;
        }
        nextBookId++;
        return book;
    }

    public Book findBook(int id) {
        for (Book b : books) {
            if (b.getId() == id) return b;
        }
        return null;
    }

    public boolean isBookIssued(int bookId) {
        for (Loan l : loans) {
            if (l.getBookId() == bookId && l.getStatus() == LoanStatus.ISSUED) return true;
        }
        return false;
    }

    private int countIssued(int memberId) {
        int count = 0;
        for (Loan l : loans) {
            if (l.getMemberId() == memberId && l.getStatus() == LoanStatus.ISSUED) count++;
        }
        return count;
    }

    public void showBooks() {
        if (books.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        for (Book b : books) {
            System.out.println(b.getId() + " | " + b.getTitle() + " | " + b.getAuthor()
                    + " | " + b.getCategory() + " | "
                    + (isBookIssued(b.getId()) ? "Currently issued" : "Available"));
        }
    }

    // ---------- issue and return ----------

    public Loan issueBook(int memberId, int bookId) throws IOException {
        if (findMember(memberId) == null) {
            throw new IllegalArgumentException("Member not found.");
        }
        if (findBook(bookId) == null) {
            throw new IllegalArgumentException("Book not found.");
        }
        if (isBookIssued(bookId)) {
            throw new IllegalArgumentException("Book " + bookId + " is already issued.");
        }
        if (countIssued(memberId) >= MAX_ISSUED_PER_MEMBER) {
            throw new IllegalArgumentException("Member " + memberId + " already has "
                    + MAX_ISSUED_PER_MEMBER + " issued books.");
        }
        Loan loan = new Loan(nextLoanId, memberId, bookId, LoanStatus.ISSUED);
        loans.add(loan);
        try {
            files.saveLoans(loans);
        } catch (IOException e) {
            loans.remove(loan);
            throw e;
        }
        nextLoanId++;
        return loan;
    }

    public Loan returnBook(int loanId) throws IOException {
        int index = -1;
        for (int i = 0; i < loans.size(); i++) {
            if (loans.get(i).getId() == loanId) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new IllegalArgumentException("Loan not found.");
        }
        Loan old = loans.get(index);
        if (old.getStatus() != LoanStatus.ISSUED) {
            throw new IllegalArgumentException("Loan " + loanId + " is already returned.");
        }
        Loan updated = new Loan(old.getId(), old.getMemberId(), old.getBookId(), old.getStatus());
        updated.markReturned();
        loans.set(index, updated);
        try {
            files.saveLoans(loans);
        } catch (IOException e) {
            loans.set(index, old);
            throw e;
        }
        return updated;
    }

    public void showLoans() {
        if (loans.isEmpty()) {
            System.out.println("No records found.");
            return;
        }
        for (Loan l : loans) {
            System.out.println(describeLoan(l));
        }
    }

    public void showMemberHistory(int memberId) {
        boolean found = false;
        for (Loan l : loans) {
            if (l.getMemberId() == memberId) {
                System.out.println(describeLoan(l));
                found = true;
            }
        }
        if (!found) {
            System.out.println("No records found.");
        }
    }

    private String describeLoan(Loan l) {
        Book b = findBook(l.getBookId());
        String title = (b == null) ? "?" : b.getTitle();
        return "Loan " + l.getId() + " | Member " + l.getMemberId()
                + " | Book " + l.getBookId() + " (" + title + ") | " + l.getStatus();
    }

    // ---------- reports ----------

    public List<String> buildReport() {
        int issued = 0;
        int returned = 0;
        for (Loan l : loans) {
            if (l.getStatus() == LoanStatus.ISSUED) issued++;
            else returned++;
        }
        HashSet<String> authors = new HashSet<>();
        for (Book book : books) {
            authors.add(book.getAuthor().trim().toUpperCase(Locale.ROOT));
        }

        List<String> lines = new ArrayList<>();
        lines.add("Total members: " + members.size());
        lines.add("Total book copies: " + books.size());
        lines.add("Books currently issued: " + issued);
        lines.add("Available copies: " + (books.size() - issued));
        lines.add("Total loans: " + loans.size());
        lines.add("Returned loans: " + returned);
        lines.add("Distinct authors: " + authors.size());
        return lines;
    }

    public void showReport() {
        for (String line : buildReport()) {
            System.out.println(line);
        }
    }

    public void saveReport() throws IOException {
        files.saveReport(buildReport());
    }

    // ---------- validation ----------

    private void requireText(String label, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        if (value.contains("|") || value.contains("\n") || value.contains("\r")) {
            throw new IllegalArgumentException(label + " cannot contain | or line breaks.");
        }
    }
}