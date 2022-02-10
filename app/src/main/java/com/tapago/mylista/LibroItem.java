package com.tapago.mylista;

public class LibroItem {
    public final String id;
    public final String author;
    public final String description;
    public final String publication_date;
    public final String title;
    public final String url_image;
    public LibroItem(String id, String author, String description, String publication_date, String title, String url_image) {
        this.id = id;
        this.author = author;
        this.description = description;
        this.publication_date = publication_date;
        this.title = title;
        this.url_image = url_image;
    }
    @Override
    public String toString() {return this.author + " " + this.title;}
}
