package com.example.project;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

//책 상세화면
public class BookDetailActivity extends AppCompatActivity {

    private int position; // 리스트에서 몇 번째 책인지 저장

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        // 1. 인텐트로 넘어온 데이터 받기 (HomeFragment에서 보낸 것)
        Book book = (Book) getIntent().getSerializableExtra("book_data");
        position = getIntent().getIntExtra("book_position", -1);

        if (book == null) {
            finish(); // 데이터 없으면 종료
            return;
        }

        // 2. 화면 요소 연결
        ImageView ivCover = findViewById(R.id.detail_cover);
        TextView tvTitle = findViewById(R.id.detail_title);
        TextView tvAuthor = findViewById(R.id.detail_author);
        TextView tvCategory = findViewById(R.id.detail_category);
        TextView tvReview = findViewById(R.id.detail_review);
        RatingBar ratingBar = findViewById(R.id.rating_detail);
        Button btnDelete = findViewById(R.id.btn_delete_book);

        // 3. 데이터 표시
        tvTitle.setText(book.getTitle());
        tvAuthor.setText(book.getAuthor());
        tvCategory.setText("카테고리: " + book.getCategory());
        // 리뷰 필드가 없으면 임시 텍스트 표시
        if (book.getReview() != null && !book.getReview().isEmpty()) {
            tvReview.setText(book.getReview());
        } else {
            tvReview.setText("작성된 메모가 없습니다.");
        }
        ratingBar.setRating(book.getRating());

        // 이미지 로딩 (Glide 사용)
        Glide.with(this)
                .load(book.getImage())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(ivCover);

        // 4. 삭제 버튼 클릭 이벤트
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
            }
        });
    }

    // 삭제 확인 팝업창
    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("책 삭제")
                .setMessage("정말 이 책을 삭제하시겠습니까?")
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 매니저를 통해 실제 데이터 삭제
                        BookManager.getInstance(BookDetailActivity.this).deleteBook(position);

                        Toast.makeText(getApplicationContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        finish(); // 화면 닫고 목록으로 돌아가기
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }
}