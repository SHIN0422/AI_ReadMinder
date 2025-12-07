package com.example.project;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 매니저 초기화 (앱 켜질 때 한 번)
        BookManager.getInstance(this);

        // [이동됨] 여기서 체크하던 코드를 onResume으로 옮겼습니다.
        // checkChallengeStatus(manager);

        // 2. UI 설정
        LinearLayout btnHome = findViewById(R.id.btn_nav_home);
        LinearLayout btnSearch = findViewById(R.id.btn_nav_search);
        LinearLayout btnStats = findViewById(R.id.btn_nav_stats);
        LinearLayout btnSettings = findViewById(R.id.btn_nav_settings);
        LinearLayout btnMyPage = findViewById(R.id.btn_nav_mypage);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        btnHome.setOnClickListener(v -> replaceFragment(new HomeFragment()));
        btnSearch.setOnClickListener(v -> replaceFragment(new SearchFragment()));
        btnStats.setOnClickListener(v -> replaceFragment(new StatsFragment()));
        btnSettings.setOnClickListener(v -> replaceFragment(new SettingsFragment()));
        btnMyPage.setOnClickListener(v -> replaceFragment(new MyPageFragment()));
    }

    // [추가] 화면이 다시 보일 때마다 실행되는 메소드
    @Override
    protected void onResume() {
        super.onResume();
        // 설정 앱 갔다가 돌아왔을 때 여기서 체크합니다.
        checkChallengeStatus(BookManager.getInstance(this));
    }

    // 기간 만료 시 자동 보관 팝업
    private void checkChallengeStatus(BookManager manager) {
        if (manager.isChallengeExpired()) {
            // 이미 팝업이 떠있는지 확인하는 로직이 없으므로,
            // "나중에"를 누르기 전까지는 화면을 켤 때마다 뜰 수 있습니다. (의도된 동작)

            new AlertDialog.Builder(this)
                    .setTitle("챌린지 기간 종료")
                    .setMessage("설정하신 챌린지 기간이 끝났습니다!\n지난 기록을 보관하고 새로운 챌린지를 시작하시겠습니까?")
                    .setCancelable(false)
                    .setPositiveButton("기록 보관 및 새 시작", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            manager.archiveCurrentChallenge();
                            Toast.makeText(MainActivity.this, "지난 기록이 보관되었습니다. 새로운 1년이 시작됩니다!", Toast.LENGTH_LONG).show();
                            replaceFragment(new HomeFragment());
                        }
                    })
                    .setNegativeButton("나중에", null)
                    .show();
        }
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}