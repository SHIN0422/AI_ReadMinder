package com.example.project;

import java.util.List;

//api에서 가져온 데이터 변수
public class BookSearchResponse {
    public List<Item> items;

    public static class Item {
        public String title;
        public String author;
        public String image;
        public String description; // [추가] 줄거리 정보
    }
}