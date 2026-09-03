package com.demo.library.dao;

import com.demo.library.config.DBConfig;
import com.demo.library.model.Reader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReaderDao {

    public boolean addReader(Reader reader) throws SQLException {
        String sql = "INSERT INTO reader (name, phone, card_no) VALUES (?, ?, ?)";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reader.getName());
            ps.setString(2, reader.getPhone());
            ps.setString(3, reader.getCardNo());
            return ps.executeUpdate() > 0;
        }
    }

    public List<Reader> searchReaders(String keyword) throws SQLException {
        String sql = "SELECT id, name, phone, card_no FROM reader "
                + "WHERE name LIKE ? OR phone LIKE ? OR card_no LIKE ? ORDER BY id";
        String like = "%" + keyword.trim() + "%";
        List<Reader> result = new ArrayList<Reader>();
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reader reader = new Reader();
                    reader.setId(rs.getLong("id"));
                    reader.setName(rs.getString("name"));
                    reader.setPhone(rs.getString("phone"));
                    reader.setCardNo(rs.getString("card_no"));
                    result.add(reader);
                }
            }
        }
        return result;
    }

    public List<Reader> listAllReaders() throws SQLException {
        String sql = "SELECT id, name, phone, card_no FROM reader ORDER BY id";
        List<Reader> result = new ArrayList<Reader>();
        try (Connection conn = DBConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Reader reader = new Reader();
                reader.setId(rs.getLong("id"));
                reader.setName(rs.getString("name"));
                reader.setPhone(rs.getString("phone"));
                reader.setCardNo(rs.getString("card_no"));
                result.add(reader);
            }
        }
        return result;
    }
}
