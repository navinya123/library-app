# Library Book Management System

A Java console-based application for a small college library. It stores members and books, records which member borrowed a book, and marks a book as returned. Records are saved in text files and loaded again the next time the program starts.

## Features

- Two roles: **Librarian** (built-in, ID 0) and **Member** (enters their member ID)
- **Librarian** can register members, add books, issue a book, return a book, and view loans and reports
- **Member** can view their own profile, view books with availability, and view their own borrowing history
- Member and book IDs are generated automatically, starting at 1
- Each book record is one physical copy, so two copies of a title have different IDs
- A book can have only one active loan at a time
- A member can hold at most 2 books at once
- Returned loans stay in the history, and a book can be issued again as a new loan
- Report with total members, total book copies, books issued, available copies, total loans, returned loans and distinct authors, which can be saved to `report.txt`
- Input is checked, so wrong menu choices and non-numeric input show a message and ask again

## Java Concepts Used

| Concept | Where it is used |
| --- | --- |
| Encapsulation | Private fields with validation in constructors and methods |
| Inheritance | `Librarian` and `Member` extend `User` |
| Abstraction | `User` is abstract and declares `showProfile()` |
| Polymorphism | `User user = member; user.showProfile();` runs the Member version |
| Composition | `LibraryService` contains the lists of records |
| Array | `String[]` of 4 book categories |
| ArrayList | Stores members, books and loans |
| HashSet | Counts each author once in the report |
| Enum | `LoanStatus` with `ISSUED` and `RETURNED` |
| File handling | `BufferedReader` and `BufferedWriter` with try-with-resources |

## Project Structure

```
LibraryApp/
├── src/
│   ├── Main.java             Menus and Scanner input
│   ├── LibraryService.java   Business rules and lists of records
│   ├── FileManager.java      Reading and writing text files
│   ├── User.java             Abstract parent class
│   ├── Librarian.java        Built-in librarian (extends User)
│   ├── Member.java           Library member (extends User)
│   ├── Book.java             One physical copy of a book
│   ├── Loan.java             Borrowing record
│   └── LoanStatus.java       Enum: ISSUED, RETURNED
├── data/                     Created automatically on the first run
├── README.md
└── .gitignore
```

## Data Files

The program creates a `data` folder and stores one record per line, with fields separated by `|`.

| File | Line format | Example |
| --- | --- | --- |
| `members.txt` | `id\|name\|department` | `1\|Ananya Rao\|CSE` |
| `books.txt` | `id\|title\|author\|category` | `1\|Java Basics\|R Kumar\|Programming` |
| `loans.txt` | `id\|memberId\|bookId\|status` | `1\|1\|1\|ISSUED` |
| `report.txt` | Readable totals, one per line | Created only when you choose to save the report |

At startup every line is checked (field count, valid values, duplicate IDs and references). If a line is invalid, the program shows the file name and line number and stops, so the file can be corrected.

## Requirements

- JDK 17 or newer
- Any Java IDE such as Eclipse or IntelliJ IDEA, or just the command line

## How to Run

**Command line** (from the project folder):

```
mkdir out
javac -d out src/*.java
java -cp out Main
```

**Eclipse:** create a Java project, copy the 9 files into `src`, and run `Main` (Run As > Java Application).

Run the program from the project folder so the `data` folder is created there.

## Menus

```
MAIN MENU
1. Librarian menu
2. Member menu
0. Exit

LIBRARIAN MENU
1. Register member
2. Add book
3. View books
4. Issue book
5. Return book
6. View loans and reports
0. Back

MEMBER MENU
1. View my profile
2. View books
3. View my borrowing history
0. Back
```

## Sample Run

```
Librarian > Add book
Title: Java Basics
Author: R Kumar
Category choice: 1
Book 1 saved.

Librarian > Issue book
Member ID: 1
Book ID: 1
Loan 1 saved. Status: ISSUED

Librarian > Return book
Return loan ID: 1
Loan 1 saved. Status: RETURNED
Book 1 is now available.
```

## Business Rules

- Both the member name and department are required
- The text fields cannot contain the `|` character or line breaks
- A book cannot be issued if it already has an ISSUED loan
- A member cannot be issued a third book while holding two
- A loan can be returned only once
- There are no due dates or fines in this version
- The classroom version uses role selection without passwords

## Author

Your Name
