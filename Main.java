
import java.io.IOException;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static LibraryService service;

    public static void main(String[] args) {
        service = new LibraryService(new FileManager("data"));
        try {
            service.load();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Fix the file in the data folder and start again.");
            return;
        }

        while (true) {
            System.out.println();
            System.out.println("MAIN MENU");
            System.out.println("1. Librarian menu");
            System.out.println("2. Member menu");
            System.out.println("0. Exit");
            int choice = readInt("Choice: ");
            if (choice == 1) {
                showLibrarianMenu();
            } else if (choice == 2) {
                showMemberMenu();
            } else if (choice == 0) {
                System.out.println("Goodbye.");
                return;
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // ---------- librarian ----------

    private static void showLibrarianMenu() {
        User user = new Librarian();
        user.showProfile();
        while (true) {
            System.out.println();
            System.out.println("LIBRARIAN MENU");
            System.out.println("1. Register member");
            System.out.println("2. Add book");
            System.out.println("3. View books");
            System.out.println("4. Issue book");
            System.out.println("5. Return book");
            System.out.println("6. View loans and reports");
            System.out.println("0. Back");
            int choice = readInt("Choice: ");
            try {
                switch (choice) {
                    case 1 -> registerMember();
                    case 2 -> addBook();
                    case 3 -> service.showBooks();
                    case 4 -> issueBook();
                    case 5 -> returnBook();
                    case 6 -> showLoansAndReports();
                    case 0 -> {
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage() + " The change was cancelled.");
            }
        }
    }

    private static void registerMember() throws IOException {
        String name = readLine("Member name: ");
        String department = readLine("Department: ");
        Member member = service.registerMember(name, department);
        System.out.println("Record saved. Member ID: " + member.getId());
    }

    private static void addBook() throws IOException {
        String title = readLine("Title: ");
        String author = readLine("Author: ");
        String[] categories = Book.getCategories();
        System.out.println("Categories:");
        for (int i = 0; i < categories.length; i++) {
            System.out.println((i + 1) + ". " + categories[i]);
        }
        int choice;
        while (true) {
            choice = readInt("Category choice: ");
            if (choice >= 1 && choice <= categories.length) {
                break;
            }
            System.out.println("Invalid choice. Enter 1 to " + categories.length + ".");
        }
        String category = categories[choice - 1];
        Book book = service.addBook(title, author, category);
        System.out.println("Book " + book.getId() + " saved.");
    }

    private static void issueBook() throws IOException {
        int memberId = readInt("Member ID: ");
        int bookId = readInt("Book ID: ");
        Loan loan = service.issueBook(memberId, bookId);
        System.out.println("Loan " + loan.getId() + " saved. Status: " + loan.getStatus());
    }

    private static void returnBook() throws IOException {
        int loanId = readInt("Return loan ID: ");
        Loan loan = service.returnBook(loanId);
        System.out.println("Loan " + loan.getId() + " saved. Status: " + loan.getStatus());
        System.out.println("Book " + loan.getBookId() + " is now available.");
    }

    private static void showLoansAndReports() throws IOException {
        System.out.println("--- Loans ---");
        service.showLoans();
        System.out.println("--- Report ---");
        service.showReport();
        String answer = readLine("Save report to report.txt? (y/n): ");
        if (answer.equalsIgnoreCase("y")) {
            service.saveReport();
            System.out.println("Record saved.");
        }
    }

    // ---------- member ----------

    private static void showMemberMenu() {
        int id = readInt("Enter member ID: ");
        Member member = service.findMember(id);
        if (member == null) {
            System.out.println("Member not found.");
            return;
        }
        User user = member; // polymorphism
        while (true) {
            System.out.println();
            System.out.println("MEMBER MENU");
            System.out.println("1. View my profile");
            System.out.println("2. View books");
            System.out.println("3. View my borrowing history");
            System.out.println("0. Back");
            int choice = readInt("Choice: ");
            switch (choice) {
                case 1 -> user.showProfile();
                case 2 -> service.showBooks();
                case 3 -> service.showMemberHistory(member.getId());
                case 0 -> {
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    // ---------- input helpers ----------

    private static int readInt(String prompt) {
        while (true) {
            String text = readLine(prompt);
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }
}
