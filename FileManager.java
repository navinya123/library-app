
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FileManager {

    public static class DataException extends IOException {
        public DataException(String message) { super(message); }
    }

    private static final String MEMBERS_FILE = "members.txt";
    private static final String BOOKS_FILE = "books.txt";
    private static final String LOANS_FILE = "loans.txt";
    private static final String REPORT_FILE = "report.txt";
    private static final int MAX_ISSUED_PER_MEMBER = 2;

    private final Path dir;

    public FileManager(String folder) {
        this.dir = Paths.get(folder);
    }

    public void ensureDataFolder() throws IOException {
        Files.createDirectories(dir);
    }

    // ---------- loading ----------

    public List<Member> loadMembers() throws IOException {
        List<Member> result = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        int lineNo = 0;
        for (String line : readLines(MEMBERS_FILE)) {
            lineNo++;
            String[] f = split(line, 3, MEMBERS_FILE, lineNo);
            int id = parseId(f[0], MEMBERS_FILE, lineNo);
            if (!ids.add(id)) {
                throw bad(MEMBERS_FILE, lineNo, "duplicate ID " + id);
            }
            try {
                result.add(new Member(id, f[1], f[2]));
            } catch (IllegalArgumentException e) {
                throw bad(MEMBERS_FILE, lineNo, e.getMessage());
            }
        }
        return result;
    }

    public List<Book> loadBooks() throws IOException {
        List<Book> result = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        int lineNo = 0;
        for (String line : readLines(BOOKS_FILE)) {
            lineNo++;
            String[] f = split(line, 4, BOOKS_FILE, lineNo);
            int id = parseId(f[0], BOOKS_FILE, lineNo);
            if (!ids.add(id)) {
                throw bad(BOOKS_FILE, lineNo, "duplicate ID " + id);
            }
            try {
                result.add(new Book(id, f[1], f[2], f[3]));
            } catch (IllegalArgumentException e) {
                throw bad(BOOKS_FILE, lineNo, e.getMessage());
            }
        }
        return result;
    }

    public List<Loan> loadLoans(List<Member> members, List<Book> books) throws IOException {
        Set<Integer> memberIds = new HashSet<>();
        for (Member m : members) memberIds.add(m.getId());
        Set<Integer> bookIds = new HashSet<>();
        for (Book b : books) bookIds.add(b.getId());

        List<Loan> result = new ArrayList<>();
        Set<Integer> ids = new HashSet<>();
        Set<Integer> issuedBooks = new HashSet<>();
        Map<Integer, Integer> issuedPerMember = new HashMap<>();
        int lineNo = 0;
        for (String line : readLines(LOANS_FILE)) {
            lineNo++;
            String[] f = split(line, 4, LOANS_FILE, lineNo);
            int id = parseId(f[0], LOANS_FILE, lineNo);
            int memberId = parseId(f[1], LOANS_FILE, lineNo);
            int bookId = parseId(f[2], LOANS_FILE, lineNo);
            if (!ids.add(id)) {
                throw bad(LOANS_FILE, lineNo, "duplicate ID " + id);
            }
            if (!memberIds.contains(memberId)) {
                throw bad(LOANS_FILE, lineNo, "unknown member ID " + memberId);
            }
            if (!bookIds.contains(bookId)) {
                throw bad(LOANS_FILE, lineNo, "unknown book ID " + bookId);
            }
            LoanStatus status;
            try {
                status = LoanStatus.valueOf(f[3]);
            } catch (IllegalArgumentException e) {
                throw bad(LOANS_FILE, lineNo, "invalid status '" + f[3] + "'");
            }
            if (status == LoanStatus.ISSUED) {
                if (!issuedBooks.add(bookId)) {
                    throw bad(LOANS_FILE, lineNo, "book " + bookId + " has two ISSUED loans");
                }
                int count = issuedPerMember.merge(memberId, 1, Integer::sum);
                if (count > MAX_ISSUED_PER_MEMBER) {
                    throw bad(LOANS_FILE, lineNo, "member " + memberId + " has more than "
                            + MAX_ISSUED_PER_MEMBER + " ISSUED loans");
                }
            }
            result.add(new Loan(id, memberId, bookId, status));
        }
        return result;
    }

    // ---------- saving ----------

    public void saveMembers(List<Member> members) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Member m : members) {
            lines.add(m.getId() + "|" + clean(m.getName()) + "|" + clean(m.getDepartment()));
        }
        writeLines(MEMBERS_FILE, lines);
    }

    public void saveBooks(List<Book> books) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Book b : books) {
            lines.add(b.getId() + "|" + clean(b.getTitle()) + "|" + clean(b.getAuthor())
                    + "|" + b.getCategory());
        }
        writeLines(BOOKS_FILE, lines);
    }

    public void saveLoans(List<Loan> loans) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Loan l : loans) {
            lines.add(l.getId() + "|" + l.getMemberId() + "|" + l.getBookId() + "|" + l.getStatus());
        }
        writeLines(LOANS_FILE, lines);
    }

    public void saveReport(List<String> lines) throws IOException {
        writeLines(REPORT_FILE, lines);
    }

    // ---------- helpers ----------

    private List<String> readLines(String fileName) throws IOException {
        Path file = dir.resolve(fileName);
        List<String> lines = new ArrayList<>();
        if (!Files.exists(file)) {
            return lines;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new IOException("Cannot read " + fileName + ": " + e.getMessage(), e);
        }
        return lines;
    }

    private void writeLines(String fileName, List<String> lines) throws IOException {
        Files.createDirectories(dir);
        Path target = dir.resolve(fileName);
        Path temp = Files.createTempFile(dir, fileName, ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            Files.deleteIfExists(temp);
            throw new IOException("Cannot save " + fileName + ": " + e.getMessage(), e);
        }
    }

    private String[] split(String line, int expected, String file, int lineNo) throws DataException {
        String[] fields = line.split("\\|", -1);
        if (fields.length != expected) {
            throw bad(file, lineNo, "expected " + expected + " fields but found " + fields.length);
        }
        return fields;
    }

    private int parseId(String text, String file, int lineNo) throws DataException {
        try {
            int id = Integer.parseInt(text.trim());
            if (id < 1) {
                throw bad(file, lineNo, "ID must be 1 or more");
            }
            return id;
        } catch (NumberFormatException e) {
            throw bad(file, lineNo, "'" + text + "' is not a valid number");
        }
    }

    private DataException bad(String file, int lineNo, String reason) {
        return new DataException("Invalid data in " + file + " at line " + lineNo + ": " + reason);
    }

    private String clean(String text) throws IOException {
        if (text.contains("|") || text.contains("\n") || text.contains("\r")) {
            throw new IOException("Text cannot contain | or line breaks.");
        }
        return text;
    }
}