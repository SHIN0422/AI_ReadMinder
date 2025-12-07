package com.example.project;


import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

//전체 보기에서 책 나열
public class TotalBooksActivity extends AppCompatActivity {

    private RecyclerView rvTotalBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_total_books);

        rvTotalBooks = findViewById(R.id.rv_total_books);
        ImageButton btnBack = findViewById(R.id.btn_back);

        // 뒤로가기 버튼
        btnBack.setOnClickListener(v -> finish());

        rvTotalBooks.setLayoutManager(new GridLayoutManager(this, 3));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    private void updateList() {
        List<Book> books = BookManager.getInstance(this).getBookList();

        // limit = 0 (전체 보기)
        RecentBookAdapter adapter = new RecentBookAdapter(books, 0, new RecentBookAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Book book, int position) {
                Intent intent = new Intent(TotalBooksActivity.this, BookDetailActivity.class);
                intent.putExtra("book_data", book);
                intent.putExtra("book_position", position);
                startActivity(intent);
            }
        });
        rvTotalBooks.setAdapter(adapter);
    }
}