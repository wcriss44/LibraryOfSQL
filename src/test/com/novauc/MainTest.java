package com.novauc;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;

public class MainTest {
    public Connection startConnection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:mem:test");
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS users(id IDENTITY, name VARCHAR, password VARCHAR, interest VARCHAR)");
        statement.execute("CREATE TABLE IF NOT EXISTS books(id IDENTITY, title VARCHAR, genre VARCHAR, author VARCHAR, user_id INTEGER)");
        return connection;
    }
    @Test
    public void testInsertSelectUser() throws Exception {
        Connection connection = startConnection();
        Main.insertUser(connection, "Foo", "Bar", "Shoe");
        User user = Main.selectUser(connection, "Foo");
        connection.close();
        assertTrue(user != null);
    }

    @Test
    public void testInsertSelectBook() throws Exception {
        Connection connection = startConnection();
        int bookId = Main.insertBook(connection, "foo", "bar","shoe", 1);
        Book book = Main.selectBook(connection,bookId, 1);
        connection.close();
        assertTrue(book != null);

        //This method SHOULD work but it shows as failed. The issue has to
        // do with getting the bookId. The current build of my website does
        // not need the Id unless the user has selected an existing book.
        // if the user does select a book, that book is displayed from an array
        // and the id is there.
    }

    @Test
    public void updateBook() throws Exception {

    }

    @Test
    public void removeBook() throws Exception {

    }

    @Test
    public void selectBook() throws Exception {

    }

    @Test
    public void selectUserBooks() throws Exception {

    }

}