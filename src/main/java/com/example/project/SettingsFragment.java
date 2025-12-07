package com.example.project;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private TextView tvCurrentGoal;

    // 날짜 포맷 및 캘린더
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Calendar startCal = Calendar.getInstance();
    private Calendar endCal = Calendar.getInstance();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        LinearLayout btnEditGoal = view.findViewById(R.id.btn_edit_goal);
        LinearLayout btnArchive = view.findViewById(R.id.btn_archive_challenge);
        LinearLayout btnViewHistory = view.findViewById(R.id.btn_view_history);
        LinearLayout btnResetData = view.findViewById(R.id.btn_reset_data);
        tvCurrentGoal = view.findViewById(R.id.tv_current_goal);

        updateGoalText();

        btnEditGoal.setOnClickListener(v -> showEditGoalDialog());
        btnArchive.setOnClickListener(v -> showArchiveDialog());
        btnViewHistory.setOnClickListener(v -> {
            if (getContext() != null) {
                // HistoryActivity 이동
                startActivity(new android.content.Intent(getContext(), HistoryActivity.class));
            }
        });
        btnResetData.setOnClickListener(v -> showResetDialog());

        return view;
    }

    private void updateGoalText() {
        if (getContext() == null) return;
        BookManager manager = BookManager.getInstance(getContext());
        tvCurrentGoal.setText(String.format("목표: %d권 (%s ~ %s)",
                manager.getTarget(), manager.getStartDate(), manager.getEndDate()));
    }

    // --- [기능 1] 챌린지 수정 다이얼로그 (권수 + 기간 선택) ---
    private void showEditGoalDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("챌린지 설정");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        // 1. 목표 권수 입력
        final EditText inputTarget = new EditText(getContext());
        inputTarget.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputTarget.setHint("목표 권수 (예: 50)");
        inputTarget.setText(String.valueOf(BookManager.getInstance(getContext()).getTarget()));
        layout.addView(inputTarget);

        // 2. 기간 선택 (Spinner)
        final Spinner spinnerPeriod = new Spinner(getContext());
        String[] periods = {"1개월", "3개월", "6개월", "1년", "직접 입력"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, periods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(adapter);
        layout.addView(spinnerPeriod);

        // 3. 날짜 표시 텍스트 (직접 입력 시 사용)
        final TextView tvDateRange = new TextView(getContext());
        tvDateRange.setText("날짜를 선택해주세요");
        tvDateRange.setPadding(0, 20, 0, 0);
        tvDateRange.setVisibility(View.GONE);
        layout.addView(tvDateRange);

        builder.setView(layout);

        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 4) { // "직접 입력" 선택 시
                    tvDateRange.setVisibility(View.VISIBLE);
                    // 시작일은 오늘로 초기화
                    startCal = Calendar.getInstance();
                    showCustomDatePicker(tvDateRange); // 종료일 선택 팝업 띄우기
                } else {
                    tvDateRange.setVisibility(View.GONE);
                    calculateDatePeriod(position);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        builder.setPositiveButton("저장", (dialog, which) -> {
            String targetStr = inputTarget.getText().toString();
            if (!targetStr.isEmpty()) {
                int newTarget = Integer.parseInt(targetStr);
                String start = sdf.format(startCal.getTime());
                String end = sdf.format(endCal.getTime());

                BookManager.getInstance(getContext()).setChallenge(newTarget, start, end);
                updateGoalText();
                Toast.makeText(getContext(), "챌린지가 수정되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("취소", null);
        builder.show();
    }

    private void calculateDatePeriod(int index) {
        startCal = Calendar.getInstance(); // 오늘
        endCal = Calendar.getInstance(); // 오늘

        switch (index) {
            case 0: endCal.add(Calendar.MONTH, 1); break; // 1개월
            case 1: endCal.add(Calendar.MONTH, 3); break; // 3개월
            case 2: endCal.add(Calendar.MONTH, 6); break; // 6개월
            case 3: endCal.add(Calendar.YEAR, 1); break;  // 1년
        }
    }

    // [수정됨] 날짜 직접 입력 (시작일=오늘 고정, 종료일만 선택)
    private void showCustomDatePicker(TextView displayView) {
        // 시작일은 무조건 오늘
        startCal = Calendar.getInstance();

        // 종료일 선택 다이얼로그 (기본값: 오늘)
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            endCal.set(year, month, dayOfMonth);

            // 화면 표시 (시작일 ~ 종료일)
            String start = sdf.format(startCal.getTime());
            String end = sdf.format(endCal.getTime());
            displayView.setText(start + " ~ " + end);

        }, startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DAY_OF_MONTH));

        // 오늘 이전 날짜는 선택 못하게 막기 (시작일 < 종료일 보장)
        datePickerDialog.getDatePicker().setMinDate(startCal.getTimeInMillis());

        datePickerDialog.show();
    }

    // --- [기능 2] 챌린지 종료 및 보관 ---
    private void showArchiveDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("챌린지 종료")
                .setMessage("현재 챌린지를 종료하고 '지난 기록'으로 이동하시겠습니까?\n\n현재 읽은 책 목록은 초기화되고 히스토리에 저장됩니다.")
                .setPositiveButton("종료 및 보관", (dialog, which) -> {
                    BookManager.getInstance(getContext()).archiveCurrentChallenge();
                    Toast.makeText(getContext(), "챌린지가 보관되었습니다. 새 챌린지를 설정해주세요!", Toast.LENGTH_LONG).show();
                    showEditGoalDialog();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    // --- [기능 3] 데이터 초기화 ---
    private void showResetDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("데이터 초기화")
                .setMessage("정말 모든 독서 기록을 삭제하시겠습니까? 복구할 수 없습니다.")
                .setPositiveButton("삭제", (dialog, which) -> {
                    BookManager.getInstance(getContext()).clearBooks();
                    updateGoalText();
                    Toast.makeText(getContext(), "모든 데이터가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
    }
}