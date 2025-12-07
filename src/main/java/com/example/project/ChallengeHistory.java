package com.example.project;


import java.io.Serializable;
import java.util.List;

public class ChallengeHistory implements Serializable {
    private String startDate;
    private String endDate;
    private int targetCount;
    private List<Book> books; // 그 당시 읽었던 책 목록

    public ChallengeHistory(String startDate, String endDate, int targetCount, List<Book> books) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.targetCount = targetCount;
        this.books = books;
    }

    public String getPeriod() {
        return startDate + " ~ " + endDate;
    }

    public String getResultSummary() {
        return "목표 " + targetCount + "권 중 " + books.size() + "권 달성 (" + getPercent() + "%)";
    }

    public int getPercent() {
        if (targetCount == 0) return 0;
        return (int) ((double) books.size() / targetCount * 100);
    }

    public List<Book> getBooks() {
        return books;
    }
}