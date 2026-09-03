package com.demo.library.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConfig {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/book_manager"
                    + "?useUnicode=true&characterEncoding=utf8&useSSL=false"
                    + "&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static String url = DEFAULT_URL;
    private static String username = DEFAULT_USER;
    private static String password = DEFAULT_PASSWORD;

    static {
        loadProperties();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                System.err.println("未找到 MySQL 驱动，请把 mysql-connector jar 放到 classpath 中。");
            }
        }
    }

    private DBConfig() {
    }

    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream in = new FileInputStream("db.properties")) {
            props.load(in);
            url = props.getProperty("db.url", url);
            username = props.getProperty("db.username", username);
            password = props.getProperty("db.password", password);
        } catch (IOException ignored) {
            // 没有 db.properties 时使用默认值，方便首次运行调试。
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
