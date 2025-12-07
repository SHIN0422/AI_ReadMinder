package com.example.project;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatsFragment extends Fragment {

    private RadarChartView radarChart;
    private TextView tvTotalCount, tvMonthlyStats;
    private TextView[] tvCats = new TextView[6];
    private Button btnSelectYear;

    private int selectedYear;

    private final String[] CATEGORIES = {
            "문학 / 소설",
            "인문 / 사회 / 철학",
            "경제 / 경영",
            "과학 / 기술",
            "실용 / 라이프스타일",
            "기타"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        radarChart = view.findViewById(R.id.chart_radar);
        tvTotalCount = view.findViewById(R.id.tv_total_count);
        tvMonthlyStats = view.findViewById(R.id.tv_monthly_stats);
        btnSelectYear = view.findViewById(R.id.btn_select_year);

        tvCats[0] = view.findViewById(R.id.tv_stat_cat1);
        tvCats[1] = view.findViewById(R.id.tv_stat_cat2);
        tvCats[2] = view.findViewById(R.id.tv_stat_cat3);
        tvCats[3] = view.findViewById(R.id.tv_stat_cat4);
        tvCats[4] = view.findViewById(R.id.tv_stat_cat5);
        tvCats[5] = view.findViewById(R.id.tv_stat_cat6);

        selectedYear = Calendar.getInstance().get(Calendar.YEAR);
        btnSelectYear.setText(selectedYear + "년 통계 보기");

        btnSelectYear.setOnClickListener(v -> showYearSelectDialog());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStats();
    }

    // [수정] 데이터가 있는 년도만 보여주는 다이얼로그
    private void showYearSelectDialog() {
        if (getContext() == null) return;

        // BookManager에서 실제 데이터가 있는 년도 목록 가져오기
        List<Integer> availableYears = BookManager.getInstance(getContext()).getAvailableYears();

        // 목록이 비어있으면 현재 년도만 보여줌 (방어 코드)
        if (availableYears.isEmpty()) {
            availableYears.add(Calendar.getInstance().get(Calendar.YEAR));
        }

        // 다이얼로그용 문자열 배열로 변환
        String[] years = new String[availableYears.size()];
        for (int i = 0; i < availableYears.size(); i++) {
            years[i] = String.valueOf(availableYears.get(i));
        }

        new AlertDialog.Builder(getContext())
                .setTitle("년도 선택")
                .setItems(years, (dialog, which) -> {
                    selectedYear = Integer.parseInt(years[which]);
                    btnSelectYear.setText(selectedYear + "년 통계 보기");
                    updateStats();
                })
                .show();
    }

    private void updateStats() {
        if (getContext() == null) return;

        BookManager manager = BookManager.getInstance(getContext());

        // [A] 전체 기록 반영: 총 권수 & 월별 통계
        List<Book> allHistoryBooks = manager.getAllReadBooks();
        List<Book> yearFilteredHistory = new ArrayList<>();

        for (Book book : allHistoryBooks) {
            if (isBookInYear(book, selectedYear)) {
                yearFilteredHistory.add(book);
            }
        }

        int totalHistoryCount = yearFilteredHistory.size();
        tvTotalCount.setText(selectedYear + "년 총 읽은 책: " + totalHistoryCount + "권");
        calculateMonthlyStats(yearFilteredHistory);


        // [B] 현재 기록 반영: 카테고리 차트 & 텍스트
        List<Book> currentBooks = manager.getBookList();
        int currentTotal = currentBooks.size();
        int[] counts = new int[6];

        if (currentTotal == 0) {
            if (radarChart != null) radarChart.setDataPoints(new float[]{0f, 0f, 0f, 0f, 0f, 0f});
            resetTexts();
            return;
        }

        for (Book book : currentBooks) {
            String category = book.getCategory();
            if (category == null) { counts[5]++; continue; }

            if (category.equals(CATEGORIES[0])) counts[0]++;
            else if (category.equals(CATEGORIES[1])) counts[1]++;
            else if (category.equals(CATEGORIES[2])) counts[2]++;
            else if (category.equals(CATEGORIES[3])) counts[3]++;
            else if (category.equals(CATEGORIES[4])) counts[4]++;
            else counts[5]++;
        }

        for (int i = 0; i < 6; i++) {
            if (tvCats[i] != null) {
                tvCats[i].setText("• " + CATEGORIES[i] + ": " + counts[i] + "권 (" + getPercent(counts[i], currentTotal) + "%)");
            }
        }

        float[] ratios = new float[6];
        float maxCount = 0;
        for (int c : counts) if (c > maxCount) maxCount = c;
        if (maxCount == 0) maxCount = 1;

        for (int i = 0; i < 6; i++) {
            ratios[i] = (float) counts[i] / maxCount;
            if (counts[i] > 0 && ratios[i] < 0.2f) ratios[i] = 0.2f;
        }

        if (radarChart != null) {
            radarChart.setDataPoints(ratios);
        }
    }

    private boolean isBookInYear(Book book, int year) {
        String date = book.getReadDate();
        if (date != null && date.length() >= 4) {
            try {
                int bookYear = Integer.parseInt(date.substring(0, 4));
                return bookYear == year;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private void calculateMonthlyStats(List<Book> books) {
        int[] monthlyCounts = new int[12];

        for (Book book : books) {
            String date = book.getReadDate();
            if (date != null && date.length() >= 7) {
                try {
                    int month = Integer.parseInt(date.substring(5, 7));
                    if (month >= 1 && month <= 12) {
                        monthlyCounts[month - 1]++;
                    }
                } catch (NumberFormatException e) {}
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            int count = monthlyCounts[i];
            sb.append(String.format(Locale.getDefault(), "%d월: ", i + 1));

            if (count > 0) {
                sb.append(count).append("권");
            } else {
                sb.append("0권");
            }
            sb.append("\n");
        }

        if (books.isEmpty()) {
            tvMonthlyStats.setText(selectedYear + "년 기록이 없습니다.");
        } else {
            tvMonthlyStats.setText(sb.toString());
        }
    }

    private void resetTexts() {
        for (int i = 0; i < 6; i++) {
            if (tvCats[i] != null) {
                tvCats[i].setText("• " + CATEGORIES[i] + ": 0권");
            }
        }
    }

    private int getPercent(int count, int total) {
        return (int) ((double) count / total * 100);
    }
}