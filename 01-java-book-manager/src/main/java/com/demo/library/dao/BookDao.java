package com.demo.library.dao;

import com.demo.library.config.DBConfig;
import com.demo.library.model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BookDao {

    public boolean addBook(Book book) throws SQLException {
        String sql = "INSERT INTO book (isbn, title, author, category, total, available) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getCategory());
            ps.setInt(5, book.getTotal());
            ps.setInt(6, book.getTotal());
            return ps.executeUpdate() > 0;
        }
    }

    public List<Book> searchBooks(String keyword) throws SQLException {
        String sql = "SELECT id, isbn, title, author, category, total, available "
                + "FROM book WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? "
                + "ORDER BY id";
        String like = "%" + keyword.trim() + "%";
        List<Book> result = new ArrayList<Book>();
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapBook(rs));
                }
            }
        }
        return result;
    }

    public List<Book> listAllBooks() throws SQLException {
        String sql = "SELECT id, isbn, title, author, category, total, available "
                + "FROM book ORDER BY id";
        List<Book> result = new ArrayList<Book>();
        try (Connection conn = DBConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapBook(rs));
            }
        }
        return result;
    }

    public boolean deleteBook(long bookId) throws SQLException {
        // 还有未还记录的书不允许删除，保护借阅历史。
        String checkSql = "SELECT COUNT(*) FROM borrow_record WHERE book_id = ? AND return_date IS NULL";
        String deleteSql = "DELETE FROM book WHERE id = ?";
        try (Connection conn = DBConfig.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setLong(1, bookId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false;
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setLong(1, bookId);
                return ps.executeUpdate() > 0;
            }
        }
    }

    private Book mapBook(ResultSet rs) throws SQLException {
        return new Book(
                rs.getLong("id"),
                rs.getString("isbn"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("category"),
                rs.getInt("total"),
                rs.getInt("available")
        );
    }
}
