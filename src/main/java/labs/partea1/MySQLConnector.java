package labs.partea1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//docker run --name mysql-container -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=lab -p 3306:3306 -d mysql:latest

//create database lab;
//USE lab;
//
//CREATE TABLE employees (
//        id INT PRIMARY KEY,
//        name VARCHAR(255)
//);

public class MySQLConnector {
    private static Connection connection;

    public static void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = "jdbc:mysql://localhost:3306/lab";
            String user = "root";
            String password = "root"; // schimbă dacă ai altă parolă
            connection = DriverManager.getConnection(url, user, password);
        }
    }

    public static Connection getConnection() throws SQLException {
        connect();
        return connection;
    }

    public static void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
