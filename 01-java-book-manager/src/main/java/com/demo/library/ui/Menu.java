package com.demo.library.ui;

import com.demo.library.model.ActiveRecord;
import com.demo.library.model.Book;
import com.demo.library.model.Reader;
import com.demo.library.service.LibraryService;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Menu {
    private final LibraryService service = new LibraryService();
    private final Scanner scanner = new Scanner(System.in);

    public void start() {
        while (true) {
            printMainMenu();
            String input = scanner.nextLine().trim();
            switch (input) {
                case "1":
                    addBook();
                    break;
                case "2":
                    listBooks();
                    break;
                case "3":
                    addReader();
                    break;
                case "4":
                    listReaders();
                    break;
                case "5":
                    borrow();
                    break;
                case "6":
                    giveBack();
                    break;
                case "7":
                    listActiveRecords();
                    break;
                case "8":
                    deleteBook();
                    break;
                case "0":
                    System.out.println("已退出，再见！");
                    return;
                default:
                    System.out.println("输入无效，请重新选择。");
            }
            pause();
        }
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("================ 图书管理系统 ================");
        System.out.println("  1. 新增图书");
        System.out.println("  2. 图书查询（支持关键字）");
        System.out.println("  3. 新增读者");
        System.out.println("  4. 读者查询");
        System.out.println("  5. 借书");
        System.out.println("  6. 还书");
        System.out.println("  7. 在借记录");
        System.out.println("  8. 删除图书");
        System.out.println("  0. 退出");
        System.out.print("请选择：");
    }

    private void addBook() {
        System.out.print("ISBN：");
        String isbn = scanner.nextLine().trim();
        System.out.print("书名：");
        String title = scanner.nextLine().trim();
        System.out.print("作者：");
        String author = scanner.nextLine().trim();
        System.out.print("分类：");
        String category = scanner.nextLine().trim();
        System.out.print("馆藏数量：");
        int total = readInt();
        if (total <= 0) {
            System.out.println("馆藏数量必须大于 0");
            return;
        }
        try {
            service.addBook(new Book(0, isbn, title, author, category, total, total));
            System.out.println("新增成功");
        } catch (SQLException e) {
            System.out.println("新增失败：" + friendlyError(e));
        }
    }

    private void listBooks() {
        System.out.print("输入关键字（直接回车查看全部）：");
        String keyword = scanner.nextLine().trim();
        try {
            List<Book> books = keyword.isEmpty()
                    ? service.listBooks()
                    : service.searchBooks(keyword);
            if (books.isEmpty()) {
                System.out.println("没有找到图书");
                return;
            }
            System.out.printf("%-4s %-16s %-20s %-12s %-8s %-6s %-6s%n",
                    "ID", "ISBN", "书名", "作者", "分类", "总数", "可借");
            for (Book b : books) {
                System.out.printf("%-4d %-16s %-20s %-12s %-8s %-6d %-6d%n",
                        b.getId(), b.getIsbn(), b.getTitle(), b.getAuthor(),
                        b.getCategory(), b.getTotal(), b.getAvailable());
            }
        } catch (SQLException e) {
            System.out.println("查询失败：" + friendlyError(e));
        }
    }

    private void addReader() {
        System.out.print("姓名：");
        String name = scanner.nextLine().trim();
        System.out.print("手机号：");
        String phone = scanner.nextLine().trim();
        System.out.print("读者证号：");
        String cardNo = scanner.nextLine().trim();
        try {
            service.addReader(new Reader(0, name, phone, cardNo));
            System.out.println("新增成功");
        } catch (SQLException e) {
            System.out.println("新增失败：" + friendlyError(e));
        }
    }

    private void listReaders() {
        System.out.print("输入关键字（直接回车查看全部）：");
        String keyword = scanner.nextLine().trim();
        try {
            List<Reader> readers = keyword.isEmpty()
                    ? service.listReaders()
                    : service.searchReaders(keyword);
            if (readers.isEmpty()) {
                System.out.println("没有找到读者");
                return;
            }
            System.out.printf("%-4s %-10s %-14s %-10s%n", "ID", "姓名", "手机号", "证号");
            for (Reader r : readers) {
                System.out.printf("%-4d %-10s %-14s %-10s%n",
                        r.getId(), r.getName(), r.getPhone(), r.getCardNo());
            }
        } catch (SQLException e) {
            System.out.println("查询失败：" + friendlyError(e));
        }
    }

    private void borrow() {
        System.out.print("读者 ID：");
        long readerId = readLong();
        System.out.print("图书 ID：");
        long bookId = readLong();
        System.out.print("借阅天数（默认 30）：");
        String dayInput = scanner.nextLine().trim();
        int days = dayInput.isEmpty() ? 30 : parsePositiveInt(dayInput);
        if (days <= 0) {
            System.out.println("借阅天数无效");
            return;
        }
        try {
            service.borrow(readerId, bookId, days);
            System.out.println("借书成功");
        } catch (SQLException e) {
            System.out.println("借书失败：" + friendlyError(e));
        }
    }

    private void giveBack() {
        System.out.print("输入在借记录 ID：");
        long recordId = readLong();
        try {
            service.giveBack(recordId);
            System.out.println("还书成功");
        } catch (SQLException e) {
            System.out.println("还书失败：" + friendlyError(e));
        }
    }

    private void listActiveRecords() {
        try {
            List<ActiveRecord> rows = service.listActiveRecords();
            if (rows.isEmpty()) {
                System.out.println("当前没有在借记录");
                return;
            }
            System.out.printf("%-6s %-10s %-24s %-12s %-12s%n",
                    "记录ID", "读者", "书名", "借出日期", "应还日期");
            for (ActiveRecord row : rows) {
                System.out.printf("%-6d %-10s %-24s %-12s %-12s%n",
                        row.getRecordId(), row.getReaderName(), row.getBookTitle(),
                        row.getBorrowDate(), row.getDueDate());
            }
        } catch (SQLException e) {
            System.out.println("查询失败：" + friendlyError(e));
        }
    }

    private void deleteBook() {
        System.out.print("输入要删除的图书 ID：");
        long bookId = readLong();
        try {
            boolean ok = service.deleteBook(bookId);
            System.out.println(ok ? "删除成功" : "删除失败：该书存在未还记录或不存在");
        } catch (SQLException e) {
            System.out.println("删除失败：" + friendlyError(e));
        }
    }

    private int readInt() {
        String line = scanner.nextLine().trim();
        return parsePositiveInt(line);
    }

    private long readLong() {
        String line = scanner.nextLine().trim();
        try {
            return Long.parseLong(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int parsePositiveInt(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String friendlyError(SQLException e) {
        String message = e.getMessage();
        if (message != null && message.contains("Duplicate entry")) {
            return "数据重复（ISBN / 手机号 / 证号可能已存在）";
        }
        if (message != null && message.contains("Communications link failure")) {
            return "无法连接数据库，请检查 MySQL 是否启动";
        }
        if (message != null && message.contains("Access denied")) {
            return "数据库账号或密码错误，请检查 db.properties";
        }
        return message == null ? e.toString() : message;
    }

    private void pause() {
        System.out.println();
        System.out.print("按回车键继续...");
        scanner.nextLine();
    }
}
