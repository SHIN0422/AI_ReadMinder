package com.example.project;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Book implements Serializable {
    private String title;       //제목
    private String author;      //저자
    private String category;    //카테고리
    private float rating;       //점수
    private String image;       //책 이미지(이미지가 들어있는 주소)
    private String description; //설명
    private String review;      //메모
    private String readDate;    //읽은 날짜(yyyy-MM-dd)

    public Book(String title, String author, String category, float rating, String image, String description, String review) {
        this.title = title;
        this.author = author;
        this.category = category;
        this.rating = rating;
        this.image = image;
        this.description = description;
        this.review = review;
        this.readDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()); //읽은 날짜
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public float getRating() { return rating; }
    public String getImage() { return image; }
    public String getDescription() { return description; }
    public String getReview() { return review; }
    public String getReadDate() { return readDate; }
    //public void setReadDate(String readDate) { this.readDate = readDate; }
}