package com.demo.library.dao;

import com.demo.library.config.DBConfig;
import com.demo.library.model.ActiveRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowDao {

    /**
     * 借书：整个流程在一个事务里完成，防止并发时超借。
     */
    public boolean borrowBook(long readerId, long bookId, int days) throws SQLException {
        if (days <= 0 || days > 180) {
            throw new SQLException("借阅天数必须在 1-180 之间");
        }
        String lockSql = "SELECT available FROM book WHERE id = ? FOR UPDATE";
        String insertSql = "INSERT INTO borrow_record (book_id, reader_id, borrow_date, due_date) "
                + "VALUES (?, ?, ?, ?)";
        String updateSql = "UPDATE book SET available = available - 1 WHERE id = ? AND available > 0";

        try (Connection conn = DBConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int available;
                try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
                    ps.setLong(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("图书不存在");
                        }
                        available = rs.getInt("available");
                    }
                }
                if (available <= 0) {
                    throw new SQLException("库存不足，无法借出");
                }

                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setLong(1, bookId);
                    ps.setLong(2, readerId);
                    ps.setDate(3, Date.valueOf(LocalDate.now()));
                    ps.setDate(4, Date.valueOf(LocalDate.now().plusDays(days)));
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setLong(1, bookId);
                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("扣减库存失败");
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * 还书：把 return_date 写回当天，并恢复库存。
     */
    public boolean returnBook(long recordId) throws SQLException {
        String lockSql = "SELECT book_id FROM borrow_record WHERE id = ? AND return_date IS NULL FOR UPDATE";
        String updateRecordSql = "UPDATE borrow_record SET return_date = ? WHERE id = ?";
        String updateStockSql = "UPDATE book SET available = available + 1 WHERE id = ?";

        try (Connection conn = DBConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long bookId;
                try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
                    ps.setLong(1, recordId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("借阅记录不存在或已归还");
                        }
                        bookId = rs.getLong("book_id");
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(updateRecordSql)) {
                    ps.setDate(1, Date.valueOf(LocalDate.now()));
                    ps.setLong(2, recordId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(updateStockSql)) {
                    ps.setLong(1, bookId);
                    ps.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<ActiveRecord> listActiveRecords() throws SQLException {
        String sql = "SELECT br.id AS record_id, br.book_id, br.reader_id, "
                + "b.title AS book_title, r.name AS reader_name, "
                + "DATE_FORMAT(br.borrow_date, '%Y-%m-%d') AS borrow_date, "
                + "DATE_FORMAT(br.due_date, '%Y-%m-%d') AS due_date "
                + "FROM borrow_record br "
                + "JOIN book b ON br.book_id = b.id "
                + "JOIN reader r ON br.reader_id = r.id "
                + "WHERE br.return_date IS NULL ORDER BY br.id";
        List<ActiveRecord> result = new ArrayList<ActiveRecord>();
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ActiveRecord row = new ActiveRecord();
                row.setRecordId(rs.getLong("record_id"));
                row.setBookId(rs.getLong("book_id"));
                row.setReaderId(rs.getLong("reader_id"));
                row.setBookTitle(rs.getString("book_title"));
                row.setReaderName(rs.getString("reader_name"));
                row.setBorrowDate(rs.getString("borrow_date"));
                row.setDueDate(rs.getString("due_date"));
                result.add(row);
            }
        }
        return result;
    }
}
