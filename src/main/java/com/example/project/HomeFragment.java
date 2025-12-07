package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {

    private TextView tvBookCount, tvPercentage, tvDDay;
    private ProgressBar progressBar;
    private RecyclerView rvRecentBooks;
    private TextView tvViewAll; // [추가] 전체보기 버튼

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvBookCount = view.findViewById(R.id.book_count);
        tvPercentage = view.findViewById(R.id.percentage);
        tvDDay = view.findViewById(R.id.tv_d_day);
        progressBar = view.findViewById(R.id.progress_challenge);
        rvRecentBooks = view.findViewById(R.id.rv_recent_books);
        tvViewAll = view.findViewById(R.id.tv_view_all);

        //미리 보기 3개만
        rvRecentBooks.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // 전체 보기
        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), TotalBooksActivity.class);
                startActivity(intent);
            });
        }

        return view;
    }

    //상세화면에서 삭제하면 갱신하기 위해
    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        if (getContext() == null) return;

        BookManager manager = BookManager.getInstance(getContext());
        List<Book> books = manager.getBookList();
        int currentBooks = manager.getCurrentCount();
        int targetBooks = manager.getTarget();

        tvBookCount.setText(currentBooks + " / " + targetBooks);
        int percent = (targetBooks == 0) ? 0 : (int) ((double) currentBooks / targetBooks * 100);
        tvPercentage.setText(percent + "% 달성");
        progressBar.setProgress(percent);

        calculateAndSetDDay(manager.getEndDate());

        // [수정] limit = 3 으로 설정하여 어댑터 생성
        RecentBookAdapter adapter = new RecentBookAdapter(books, 3, new RecentBookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Book book, int position) {
                Intent intent = new Intent(getContext(), BookDetailActivity.class);
                intent.putExtra("book_data", book);
                intent.putExtra("book_position", position);
                startActivity(intent);
            }
        });
        rvRecentBooks.setAdapter(adapter);
    }

    private void calculateAndSetDDay(String endDateStr) {
        if (endDateStr == null || endDateStr.isEmpty()) {
            tvDDay.setText("목표 날짜 없음");
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date endDate = sdf.parse(endDateStr);
            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);
            Date today = todayCal.getTime();

            long diffInMillis = endDate.getTime() - today.getTime();
            long days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

            if (days > 0) tvDDay.setText("D-" + days);
            else if (days == 0) tvDDay.setText("D-Day");
            else tvDDay.setText("D+" + Math.abs(days));
        } catch (Exception e) {
            e.printStackTrace();
            tvDDay.setText("날짜 오류");
        }
    }
}