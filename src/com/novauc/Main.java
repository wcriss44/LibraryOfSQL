package com.novauc;

import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.SQLException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) throws SQLException{
        Server.createWebServer().start();
        Connection connection = DriverManager.getConnection("jdbc:h2:./main");
        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE IF NOT EXISTS users(id IDENTITY, name VARCHAR, password VARCHAR, interest VARCHAR)");
        statement.execute("CREATE TABLE IF NOT EXISTS books(id IDENTITY, title VARCHAR, genre VARCHAR, author VARCHAR, user_id INTEGER)");

        Spark.staticFileLocation("/styles");
        Spark.init();

        Spark.get(
                "/",//FINISHED
                ((request, response) -> {
                    HashMap m = new HashMap<>();
                    Session session = request.session();

                    String userName = session.attribute("userName");
                    if (userName == null){
                        return new ModelAndView(m, "registration.html");
                    } else {
                        int id = Integer.valueOf(session.attribute("id"));
                        return new ModelAndView(m, "home.html");
                    }
                }),
                new MustacheTemplateEngine()
        );
        Spark.get(
                "/home.html",
                ((request, response) -> {
                    HashMap m = new HashMap<>();
                    Session session = request.session();

                    String userName = session.attribute("userName");
                    if (userName == null){
                        return new ModelAndView(m, "registration.html");
                    } else {
                        User user = selectUser(connection,userName);

                        ArrayList<Book> books = selectUserBooks(connection, user.getId());
                        m.put("books", books);
                        return new ModelAndView(m, "home.html");
                    }
                }),
                new MustacheTemplateEngine()
        );
        Spark.get( //should be the same as default
                "/index.html",
                ((request, response) -> {
                    HashMap m = new HashMap<>();
                    Session session = request.session();

                    String userName = session.attribute("userName");
                    if (userName != null){//redirect to homepage if you are logged in
                        User user = selectUser(connection,userName);

                        ArrayList<Book> books = selectUserBooks(connection, user.getId());
                        m.put("books", books);
                        return new ModelAndView(m, "home.html");
                    } else {
                        return new ModelAndView(m, "registration.html");
                    }
                }),
                new MustacheTemplateEngine()
        );
        Spark.get(
                "/registration.html",
                ((request, response) -> {
                    HashMap m = new HashMap<>();
                    Session session = request.session();

                    String userName = session.attribute("userName");
                    if (userName != null){//redirect to homepage if you are logged in
                        User user = selectUser(connection,userName);

                        ArrayList<Book> books = selectUserBooks(connection, user.getId());
                        m.put("books", books);
                        return new ModelAndView(m, "home.html");
                    } else {
                        String userExists = session.attribute("userExist");
                        if (userExists != null){
                            m.put("userExists", "userExists");
                        }
                        String noInterest = session.attribute("noInterest");
                        if (noInterest != null){
                            m.put("noInterest", noInterest);
                        }
                        return new ModelAndView(m, "registration.html");
                    }
                }),
                new MustacheTemplateEngine()
        );
        Spark.get(
                "/viewall.html",
                ((request, response) -> {
                    HashMap m = new HashMap<>();
                    Session session = request.session();

                    String userName = session.attribute("userName");
                    if (userName == null){
                        return new ModelAndView(m, "registration.html");
                    } else {
                        ArrayList<Book> books = selectBooks(connection);
                        User user = selectUser(connection,userName);
                        m.put("books", books);
                        return new ModelAndView(m, "viewall.html");
                    }
                }),
                new MustacheTemplateEngine()
        );
        Spark.get(//FINISHED
                "/logout.html",
                ((request, response) -> {
                    HashMap m = new HashMap<>();
                    Session session = request.session();
                    session.invalidate();
                    return new ModelAndView(m, "registration.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/register",
                ((request, response) -> {
                    Session session = request.session();
                    String userName = session.attribute("userName");
                    session.removeAttribute("userExists");
                    session.removeAttribute("noInterest");
                    //TODO: add to login

                    if (userName != null){
                        response.redirect("/home.html");
                        return "";
                    } else {
                        userName = request.queryParams("userName");
                        String password = request.queryParams("password");
                        String interest = request.queryParams("bookInterest");
                        if (interest == null){
                            session.attribute("noInterest", "noInterest");
                            response.redirect("/registration.html");
                            return "";
                        }
                        User user = selectUser(connection, userName);
                        if (user != null){
                            session.attribute("userExists", "userExists");
                            response.redirect("/registration.html");
                            return "";
                        } else {
                            int id = insertUser(connection,userName, password, interest);
                            session.attribute("userName", userName);
                            session.attribute("id", id);
                            session.removeAttribute("userExists");
                            session.removeAttribute("noInterest");

                            response.redirect("/home.html");
                            return "";
                        }
                    }
                })
        );
        Spark.post(
                "/login",
                ((request, response) -> {
                    Session session = request.session();
                    String userName = session.attribute("userName");

                    if (userName != null){
                        response.redirect("/registration.html");
                        return "";
                    } else {
                        userName = request.queryParams("userName");
                        User user = selectUser(connection, userName);
                        if (user != null){
                            if (user.getPassword().equals(request.queryParams("password"))){
                                session.removeAttribute("userExists");
                                session.removeAttribute("noInterest");

                                session.attribute("userName", userName);
                                response.redirect("/home.html");
                                return "";
                            }
                        }
                        response.redirect("/registration.html");
                        return "";

                    }
                })
        );
        Spark.post(
                "/edit",
                ((request, response) -> {
                    Session session = request.session();
                    String userName = session.attribute("userName");

                    if (userName == null){
                        response.redirect("/registration.html");
                        return "";
                    } else {
                        User user = selectUser(connection, userName);
                        int id = Integer.valueOf(request.queryParams("bookId"));
                        String title = request.queryParams("title");
                        String genre = request.queryParams("genre");
                        String author = request.queryParams("author");
                        updateBook(connection, id, title, genre, author, user.getId());
                        response.redirect("/home.html");
                        return "";
                    }
                })
        );
        Spark.post(
                "/delete",
                ((request, response) -> {
                    Session session = request.session();
                    String userName = session.attribute("userName");

                    if (userName == null){
                        response.redirect("/registration.html");
                        return "";
                    } else {
                        int bookId = Integer.valueOf(request.queryParams("id"));
                        User user = selectUser(connection, userName);
                        removeBook(connection, bookId, user.getId());
                        response.redirect("/home.html");
                        return "";
                    }
                })
        );
        Spark.post(
                "/add-book",
                ((request, response) -> {
                    Session session = request.session();;
                    String userName = session.attribute("userName");

                    if (userName == null){
                        response.redirect("/registration.html");
                        return "";
                    } else {
                        String title = request.queryParams("title");
                        String author = request.queryParams("author");
                        String genre = request.queryParams("genre");
                        User user = selectUser(connection, userName);

                        insertBook(connection,title, genre, author,user.getId());
                        response.redirect("/home.html");
                        return "";
                    }
                })
        );
//        Spark.post(
//                "/add-current",
//                ((request, response) -> {
//                    Session session = request.session();
//                    String userName = session.attribute("userName");
//
//                    if (userName == null){
//                        response.redirect("/registration.html");
//                        return "";
//                    } else {
//                        String title = request.queryParams("title");
//                        String author = request.queryParams("author");
//                        String genre = request.queryParams("genre");
//                        User user = selectUser(connection, userName);
//
//                        insertBook(connection,title, genre, author,user.getId());
//                        response.redirect("/home.html");
//                        return "";
//                    }
//                })
//        );
    }
    public static int insertUser(Connection connection, String userName, String password, String interest) throws SQLException{
        PreparedStatement statement = connection.prepareStatement("INSERT INTO users VALUES(NULL, ?, ?, ?)");
        statement.setString(1, userName);
        statement.setString(2, password);
        statement.setString(3, interest);
        statement.execute();

        PreparedStatement getId = connection.prepareStatement("SELECT id FROM users WHERE name = ?");
        getId.setString(1, userName);

        ResultSet results = getId.executeQuery();

        int id = 0;
        while(results.next()){
            id = results.getInt("id");
        }
        return id;
    }
    public static User selectUser(Connection connection, String userName) throws SQLException{
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE name = ?");
        statement.setString(1, userName);
        ResultSet results = statement.executeQuery();
        int id = 0;
        String interest = null;
        String password = null;
        while(results.next()){
            id = results.getInt("id");
            interest = results.getString("interest");
            password = results.getString("password");

        }
        if (interest == null){
            return null;
        } else {
            return new User(userName, password, interest, id);
        }
    }
    public static void insertBook(Connection connection, String title, String genre, String author,  int userId) throws SQLException{
        PreparedStatement statement = connection.prepareStatement("INSERT INTO books VALUES(NULL, ?, ?, ?, ?)");
        statement.setString(1, title);
        statement.setString(2, genre);
        statement.setString(3, author);
        statement.setInt(4, userId);
        statement.execute();
    }
    public static void updateBook(Connection connection,int bookId, String title, String genre, String author, int userId) throws SQLException{
        PreparedStatement statement = connection.prepareStatement("UPDATE books SET title = ?, genre = ?, author = ?  WHERE (user_id = ? AND id = ?)");
        statement.setString(1, title);
        statement.setString(2, genre);
        statement.setString(3, author);
        statement.setInt(4, userId);
        statement.setInt(5, bookId);
        statement.execute();
    }
    public static void removeBook(Connection connection, int bookId, int userId) throws SQLException{
        PreparedStatement statement = connection.prepareStatement("DELETE FROM books WHERE (id = ? AND user_id = ?)");
        statement.setInt(1, bookId);
        statement.setInt(2, userId);
        statement.execute();
    }
    public static Book selectBook(Connection connection, int bookId, int userId) throws SQLException{
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM books INNER JOIN users ON users.id = books.user_id WHERE (users.id = ? AND books.id = ?) ");
        statement.setInt(1, userId);
        statement.setInt(2, bookId);
        //May need to remove above statement and the ? in SQL

        Book book = null;
        String title = null;
        String genre = null;
        String author = null;
        int id = 0;
        ResultSet results = statement.executeQuery();
        while(results.next()){
            title = results.getString("title");
            genre = results.getString("title");
            author = results.getString("title");
            id = results.getInt("id");
        }
        if (title != null){
            book = new Book(title, genre, author, id);

        }
        return book;
    }
    public static ArrayList<Book> selectBooks(Connection connection) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT users.name, books.title, books.genre, books.author FROM books INNER JOIN users ON users.id = books.user_id");
        ResultSet results = statement.executeQuery();


        ArrayList<Book> allBooks = new ArrayList<>();
        while (results.next()) {
            String title = results.getString("title");
            String genre = results.getString("genre");
            String author = results.getString("author");
            String reader = results.getString("name");
            allBooks.add(new Book(title, genre, author,  reader));
        }
        return allBooks;
    }
    public static ArrayList<Book> selectUserBooks(Connection connection, int userId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM books WHERE user_id = ?");
        statement.setInt(1, userId);
        ResultSet results = statement.executeQuery();
        ArrayList<Book> userBooks = new ArrayList<>();

        while (results.next()) {
            String title = results.getString("title");
            String genre = results.getString("genre");
            String author = results.getString("author");
            int id = results.getInt("id");
            userBooks.add(new Book(title, genre, author, id));
        }
        return userBooks;
    }
}
