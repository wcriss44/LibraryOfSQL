package com.novauc;

public class Book {
    private String title, genre, author, reader;
    private int id;

    public Book() {

    }

    public Book(String title, String genre, String author, String reader) {
        this.title = title;
        this.genre = genre;
        this.author = author;
        this.reader = reader;
    }

    public Book(String title, String genre, String author, int id) {
        this.title = title;
        this.genre = genre;
        this.author = author;
        this.id = id;
    }

    public String getReader() {
        return reader;
    }

    public void setReader(String reader) {
        this.reader = reader;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getid() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
