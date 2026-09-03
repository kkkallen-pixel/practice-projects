package com.demo.library.service;

import com.demo.library.dao.BookDao;
import com.demo.library.dao.BorrowDao;
import com.demo.library.dao.ReaderDao;
import com.demo.library.model.ActiveRecord;
import com.demo.library.model.Book;
import com.demo.library.model.Reader;

import java.sql.SQLException;
import java.util.List;

public class LibraryService {
    private final BookDao bookDao = new BookDao();
    private final ReaderDao readerDao = new ReaderDao();
    private final BorrowDao borrowDao = new BorrowDao();

    public void addBook(Book book) throws SQLException {
        if (!bookDao.addBook(book)) {
            throw new SQLException("新增图书失败");
        }
    }

    public List<Book> listBooks() throws SQLException {
        return bookDao.listAllBooks();
    }

    public List<Book> searchBooks(String keyword) throws SQLException {
        return bookDao.searchBooks(keyword);
    }

    public boolean deleteBook(long bookId) throws SQLException {
        return bookDao.deleteBook(bookId);
    }

    public void addReader(Reader reader) throws SQLException {
        if (!readerDao.addReader(reader)) {
            throw new SQLException("新增读者失败");
        }
    }

    public List<Reader> listReaders() throws SQLException {
        return readerDao.listAllReaders();
    }

    public List<Reader> searchReaders(String keyword) throws SQLException {
        return readerDao.searchReaders(keyword);
    }

    public void borrow(long readerId, long bookId, int days) throws SQLException {
        borrowDao.borrowBook(readerId, bookId, days);
    }

    public void giveBack(long recordId) throws SQLException {
        borrowDao.returnBook(recordId);
    }

    public List<ActiveRecord> listActiveRecords() throws SQLException {
        return borrowDao.listActiveRecords();
    }
}
