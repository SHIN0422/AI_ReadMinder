package com.example.project;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private RecyclerView rvWishlist;
    private WishlistAdapter adapter;
    private List<Book> wishList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        rvWishlist = findViewById(R.id.wishlist); // XML ID와 일치
        rvWishlist.setLayoutManager(new GridLayoutManager(this, 3));

        loadWishList();
    }

    private void loadWishList() {
        // 매니저에서 찜 목록 가져오기
        wishList = BookManager.getInstance(this).getWishList();

        // 업로드해주신 WishlistAdapter 사용
        adapter = new WishlistAdapter(wishList, new WishlistAdapter.OnItemClickListener() {
            @Override
            public void onRemoveClick(Book book, int position) {
                // [수정] deleteWish 대신 기존의 toggleWish 사용!
                // toggleWish는 이미 있으면 삭제하므로, 삭제 기능으로 동작함
                BookManager.getInstance(WishlistActivity.this).toggleWish(book);

                // 리스트뷰 갱신
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, wishList.size());

                Toast.makeText(getApplicationContext(), "삭제했습니다.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReviewClick(Book book, int position) {
                showReviewDialog(book, position);
            }
        });
        rvWishlist.setAdapter(adapter);
    }

    private void showReviewDialog(Book book, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.search_result_popup, null);
        builder.setView(dialogView);

        ImageView ivCover = dialogView.findViewById(R.id.dialog_cover);
        TextView tvTitle = dialogView.findViewById(R.id.dialog_book_title);
        TextView tvAuthor = dialogView.findViewById(R.id.dialog_author);
        TextView tvDesc = dialogView.findViewById(R.id.dialog_description);

        Spinner spinner = dialogView.findViewById(R.id.spinner_category);
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_dialog);
        EditText etReview = dialogView.findViewById(R.id.et_review);

        tvTitle.setText(book.getTitle());
        tvAuthor.setText(book.getAuthor());
        if (tvDesc != null) tvDesc.setVisibility(View.GONE);
        if (dialogView.findViewById(R.id.dialog_more) != null) {
            dialogView.findViewById(R.id.dialog_more).setVisibility(View.GONE);
        }

        Glide.with(this).load(book.getImage()).into(ivCover);

        String[] categories = {
                "문학 / 소설",
                "인문 / 사회 / 철학",
                "경제 / 경영",
                "과학 / 기술",
                "실용 / 라이프스타일",
                "기타"
        };
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(catAdapter);

        builder.setPositiveButton("저장 (독서 완료)", (dialog, which) -> {
            String category = spinner.getSelectedItem().toString();
            float rating = ratingBar.getRating();
            String review = etReview.getText().toString();

            Book readBook = new Book(
                    book.getTitle(),
                    book.getAuthor(),
                    category,
                    rating,
                    book.getImage(),
                    book.getDescription(),
                    review
            );
            BookManager.getInstance(this).toggleWish(book); // 삭제
            // 서재 추가
            BookManager.getInstance(this).addBook(readBook);

            // [수정] 여기서도 찜 삭제 시 toggleWish 사용

            adapter.notifyItemRemoved(position);
            if (position < wishList.size()) {
                adapter.notifyItemRangeChanged(position, wishList.size() - position);
            }
            Toast.makeText(this, "서재로 이동되었습니다.", Toast.LENGTH_LONG).show();
        });

        builder.setNegativeButton("취소", null);
        builder.create().show();
    }
}