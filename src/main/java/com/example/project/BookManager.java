package com.example.project;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class BookManager {
    private static BookManager instance;            //싱글톤 변수
    private Context context;                        //시스템 기능 문맥
    private Gson gson;                              //자바 객체, json 변환기

    private List<Book> bookList;                    //현재 읽은 책 목록
    private List<Book> wishList;                    //위시리스트
    private List<ChallengeHistory> historyList;     //지난 챌린지 기록

    private int challengeTarget;                    //목표 권수
    private String startDate;                       //시작일
    private String endDate;                         //종료일

    private static final String FILE_NAME = "books.json";
    private static final String FILE_WISH = "wishlist.json";
    private static final String FILE_HISTORY = "history.json";
    private static final String FILE_CONFIG = "config.json";

    private BookManager(Context context) {
        this.context = context;
        this.gson = new Gson();
        this.bookList = new ArrayList<>();
        this.wishList = new ArrayList<>();
        this.historyList = new ArrayList<>();

        this.challengeTarget = 50;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        this.startDate = sdf.format(cal.getTime());
        cal.add(Calendar.YEAR, 1);
        this.endDate = sdf.format(cal.getTime());

        loadBooksFromJson();
        loadWishListFromJson();
        loadHistoryFromJson();
        loadConfigFromJson();
    }

    public static BookManager getInstance(Context context) {
        if (instance == null) {
            instance = new BookManager(context.getApplicationContext());
        }
        return instance;
    }

    //책 기록이 있는 모든 년도 가져오기
    public List<Integer> getAvailableYears() {
        Set<Integer> years = new HashSet<>();       //중복제거 set
        List<Book> allBooks = getAllReadBooks();    //현재 + 과거 모든 책

        // 현재 년도도 기본적으로 포함 (데이터가 없어도 현재는 보여야 하므로)
        years.add(Calendar.getInstance().get(Calendar.YEAR));

        for (Book book : allBooks) {
            String date = book.getReadDate();
            if (date != null && date.length() >= 4) {
                try {
                    int year = Integer.parseInt(date.substring(0, 4));
                    years.add(year);
                } catch (NumberFormatException e) {}
            }
        }

        // 내림차순 정렬 (최신 년도가 위로 오게)
        List<Integer> sortedYears = new ArrayList<>(years);
        Collections.sort(sortedYears, Collections.reverseOrder());

        return sortedYears;
    }

    public List<Book> getAllReadBooks() {
        List<Book> allBooks = new ArrayList<>(bookList);
        for (ChallengeHistory history : historyList) {
            allBooks.addAll(history.getBooks());
        }
        return allBooks;
    }

    //챌린지 끝났는지 검사
    public boolean isChallengeExpired() {
        if (endDate == null) return false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date end = sdf.parse(endDate);
            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);
            return end.before(todayCal.getTime());  // 종료일이 오늘보다 이전이면 true
        } catch (Exception e) { return false; }
    }

    // --- 파일 저장/로드 ---
    private void saveBooksToJson() { saveListToFile(bookList, FILE_NAME); }
    private void loadBooksFromJson() {
        bookList = loadListFromFile(FILE_NAME, new TypeToken<ArrayList<Book>>(){}.getType());
        if(bookList == null) bookList = new ArrayList<>();
    }
    private void saveWishListToJson() { saveListToFile(wishList, FILE_WISH); }
    private void loadWishListFromJson() {
        wishList = loadListFromFile(FILE_WISH, new TypeToken<ArrayList<Book>>(){}.getType());
        if(wishList == null) wishList = new ArrayList<>();
    }
    private void saveHistoryToJson() { saveListToFile(historyList, FILE_HISTORY); }
    private void loadHistoryFromJson() {
        historyList = loadListFromFile(FILE_HISTORY, new TypeToken<ArrayList<ChallengeHistory>>(){}.getType());
        if(historyList == null) historyList = new ArrayList<>();
    }

    private void saveConfigToJson() {
        try {
            ConfigData config = new ConfigData(challengeTarget, startDate, endDate);
            String jsonString = gson.toJson(config);
            FileOutputStream fos = context.openFileOutput(FILE_CONFIG, Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes());
            fos.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadConfigFromJson() {
        try {
            File file = new File(context.getFilesDir(), FILE_CONFIG);
            if (!file.exists()) return;
            FileInputStream fis = context.openFileInput(FILE_CONFIG);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) sb.append(line);
            ConfigData config = gson.fromJson(sb.toString(), ConfigData.class);
            if (config != null) { this.challengeTarget = config.target; this.startDate = config.startDate; this.endDate = config.endDate; }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private static class ConfigData {
        int target; String startDate; String endDate;
        ConfigData(int target, String startDate, String endDate) { this.target = target; this.startDate = startDate; this.endDate = endDate; }
    }

    //리스트 내용을 json으로 바꿔서 파일에 씀
    private <T> void saveListToFile(List<T> list, String fileName) {
        try {
            String jsonString = gson.toJson(list);  //list -> json으로 변환
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes());
            fos.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    //파일 내용을 읽어서 자바 리스트로 바꿈
    private <T> List<T> loadListFromFile(String fileName, Type typeToken) {
        try {
            File file = new File(context.getFilesDir(), fileName);
            if (!file.exists()) return new ArrayList<>();
            FileInputStream fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) sb.append(line);
            return gson.fromJson(sb.toString(), typeToken);
        } catch (Exception e) { return new ArrayList<>(); }
    }

    // --- 기능 ---
    public List<Book> getBookList() { return bookList; }
    public void addBook(Book book) {
        bookList.add(0, book);
        saveBooksToJson();
        if (isWished(book)) toggleWish(book);   //위시리스트에서 뺌
    }
    public void deleteBook(int position) {
        if (position >= 0 && position < bookList.size()) {
            bookList.remove(position);
            saveBooksToJson();
        }
    }
    public boolean isRead(Book book) {
        /*List<Book> allBooks = getAllReadBooks();
        for (Book item : allBooks) {
            if (item.getTitle().equals(book.getTitle()) && item.getAuthor().equals(book.getAuthor())) return true;
        }
        return false;*/
        for (Book item : bookList) {
            //저자, 제목 비교
            if (item.getTitle().equals(book.getTitle()) && item.getAuthor().equals(book.getAuthor())) return true;
        }
        return false;
    }
    public List<Book> getWishList() { return wishList; }
    public boolean isWished(Book book) {
        for (Book item : wishList) {
            if (item.getTitle().equals(book.getTitle()) && item.getAuthor().equals(book.getAuthor())) return true;
        }
        return false;
    }
    public boolean toggleWish(Book book) {
        if (isWished(book)) {
            for (int i = 0; i < wishList.size(); i++) {
                if (wishList.get(i).getTitle().equals(book.getTitle()) && wishList.get(i).getAuthor().equals(book.getAuthor())) {
                    wishList.remove(i);
                    break;
                }
            }
            saveWishListToJson();
            return false;
        } else {
            wishList.add(0, book);
            saveWishListToJson();
            return true;
        }
    }
    public void addWish(Book book) { if(!isWished(book)) { wishList.add(0, book); saveWishListToJson(); } }

    //현재 챌린지 끝내고 기록 저장 후 새로 시작
    public void archiveCurrentChallenge() {
        List<Book> pastBooks = new ArrayList<>(bookList);
        ChallengeHistory history = new ChallengeHistory(startDate, endDate, challengeTarget, pastBooks);
        historyList.add(0, history);
        saveHistoryToJson();
        bookList.clear();
        saveBooksToJson();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        String newStart = sdf.format(cal.getTime());
        cal.add(Calendar.YEAR, 1);
        String newEnd = sdf.format(cal.getTime());
        setChallenge(challengeTarget, newStart, newEnd);
    }

    public List<ChallengeHistory> getHistoryList() { return historyList; }
    public void clearBooks() { bookList.clear(); wishList.clear(); saveBooksToJson(); saveWishListToJson(); }
    public int getTarget() { return challengeTarget; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public void setChallenge(int target, String start, String end) { this.challengeTarget = target; this.startDate = start; this.endDate = end; saveConfigToJson(); }
    public void setTarget(int target) { this.challengeTarget = target; saveConfigToJson(); }
    public int getCurrentCount() { return bookList.size(); }
}