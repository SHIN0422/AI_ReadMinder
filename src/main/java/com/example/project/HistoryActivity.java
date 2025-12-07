package com.example.project;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ListView listView = findViewById(R.id.list_history);
        TextView tvNoHistory = findViewById(R.id.tv_no_history);

        // BookManager에서 히스토리 목록 가져오기
        List<ChallengeHistory> historyList = BookManager.getInstance(this).getHistoryList();

        if (historyList == null || historyList.isEmpty()) {
            listView.setVisibility(View.GONE);
            tvNoHistory.setVisibility(View.VISIBLE);
            return;
        }

        // 화면에 보여줄 문자열 리스트 만들기
        List<String> displayList = new ArrayList<>();
        for (int i = 0; i < historyList.size(); i++) {
            ChallengeHistory h = historyList.get(i);
            // 예: "2025-01-01 ~ 2025-12-31 \n 목표 50권 중 12권 달성 (24%)"
            String title = String.format("[%d회차] %s\n%s",
                    historyList.size() - i, // 최신순 번호
                    h.getPeriod(),
                    h.getResultSummary());
            displayList.add(title);
        }

        // 리스트뷰 어댑터 연결
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listView.setAdapter(adapter);

        // 클릭 시 상세 목록(그때 읽은 책들) 보여주기
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ChallengeHistory history = historyList.get(position);
            showHistoryDetail(history);
        });
    }

    // 상세 정보 팝업
    private void showHistoryDetail(ChallengeHistory history) {
        StringBuilder sb = new StringBuilder();
        List<Book> books = history.getBooks();

        if (books.isEmpty()) {
            sb.append("읽은 책이 없습니다.");
        } else {
            for (Book book : books) {
                sb.append("• ").append(book.getTitle())
                        .append(" (").append(book.getAuthor()).append(")\n");
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("기록 상세")
                .setMessage(sb.toString())
                .setPositiveButton("닫기", null)
                .show();
    }
}