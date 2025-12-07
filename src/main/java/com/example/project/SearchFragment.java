package com.example.project;



import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver; // 추가됨
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView; // 추가됨
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // 추가됨
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchFragment extends Fragment {

    // ★★★ 여기에 네이버 API 키를 넣으세요 ★★★
    private static final String CLIENT_ID = "";
    private static final String CLIENT_SECRET = "";

    private EditText etSearch;
    private RecyclerView rvSearchResults;
    private SearchBookAdapter adapter;
    private NaverBookApi naverApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        etSearch = view.findViewById(R.id.et_search);
        rvSearchResults = view.findViewById(R.id.search_results);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://openapi.naver.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        naverApi = retrofit.create(NaverBookApi.class);

        adapter = new SearchBookAdapter(new ArrayList<>(), new SearchBookAdapter.OnItemClickListener() {
            @Override
            public void onAddClick(Book book) {
                showAddBookDialog(book);
            }

            @Override
            public void onWishClick(Book book, int position) {
                // 찜하기는 간략 정보만 저장 (메모/카테고리 없음)
                Book newBook = new Book(book.getTitle(), book.getAuthor(), "", 0, book.getImage(), book.getDescription(), "");

                boolean isAdded = BookManager.getInstance(getContext()).toggleWish(newBook);

                if (isAdded) {
                    Toast.makeText(getContext(), "찜 목록에 추가되었습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "찜 목록에서 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                }

                adapter.notifyItemChanged(position); // 아이콘 갱신
            }
        });

        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSearchResults.setAdapter(adapter);

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchBooks(v.getText().toString());
                return true;
            }
            return false;
        });

        return view;
    }

    private void searchBooks(String query) {
        if (query.isEmpty()) return;

        // 로딩바 없이 그냥 호출
        naverApi.searchBooks(CLIENT_ID, CLIENT_SECRET, query, 20).enqueue(new Callback<BookSearchResponse>() {
            @Override
            public void onResponse(Call<BookSearchResponse> call, Response<BookSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BookSearchResponse.Item> items = response.body().items;
                    List<Book> bookList = new ArrayList<>();
                    if (items != null) {
                        for (BookSearchResponse.Item item : items) {
                            String title = item.title.replace("<b>", "").replace("</b>", "");
                            String author = item.author.replace("<b>", "").replace("</b>", "");
                            String desc = item.description != null ? item.description.replace("<b>", "").replace("</b>", "") : "";
                            bookList.add(new Book(title, author, "", 0, item.image, desc, ""));
                        }
                    }
                    adapter.setList(bookList);
                } else {
                    Toast.makeText(getContext(), "검색 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BookSearchResponse> call, Throwable t) {
                Toast.makeText(getContext(), "통신 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddBookDialog(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.search_result_popup, null);
        builder.setView(dialogView);

        ImageView ivCover = dialogView.findViewById(R.id.dialog_cover);
        TextView tvTitle = dialogView.findViewById(R.id.dialog_book_title);
        TextView tvAuthor = dialogView.findViewById(R.id.dialog_author);
        final TextView tvDesc = dialogView.findViewById(R.id.dialog_description);
        final TextView tvMore = dialogView.findViewById(R.id.dialog_more);

        Spinner spinner = dialogView.findViewById(R.id.spinner_category);
        RatingBar ratingBar = dialogView.findViewById(R.id.rating_dialog);
        EditText etReview = dialogView.findViewById(R.id.et_review); // 메모 입력창

        tvTitle.setText(book.getTitle());
        tvAuthor.setText(book.getAuthor());

        // 줄거리 설정 및 더보기 처리
        if (book.getDescription() != null && !book.getDescription().isEmpty()) {
            tvDesc.setText(book.getDescription());

            // ViewTreeObserver로 실제 그려진 줄 수 확인
            tvDesc.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    tvDesc.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    if (tvDesc.getLineCount() > 0) {
                        int lastLineIndex = tvDesc.getLineCount() - 1;
                        // 말줄임표(...)가 생겼다면 더보기 버튼 표시
                        if (tvDesc.getLayout().getEllipsisCount(lastLineIndex) > 0) {
                            tvMore.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });

            tvMore.setOnClickListener(v -> {
                if (tvDesc.getMaxLines() == 3) {
                    tvDesc.setMaxLines(Integer.MAX_VALUE);
                    tvMore.setText("접기 ▲");
                } else {
                    tvDesc.setMaxLines(3);
                    tvMore.setText("더보기 ▼");
                }
            });
        } else {
            tvDesc.setText("줄거리 정보가 없습니다.");
            tvMore.setVisibility(View.GONE);
        }

        Glide.with(getContext()).load(book.getImage()).into(ivCover);

        String[] categories = {
                "문학 / 소설",
                "인문 / 사회 / 철학",
                "경제 / 경영",
                "과학 / 기술",
                "실용 / 라이프스타일",
                "기타"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        builder.setPositiveButton("저장", (dialog, which) -> {
            String selectedCategory = spinner.getSelectedItem().toString();
            float rating = ratingBar.getRating();
            String review = etReview.getText().toString(); // 메모 내용 가져오기

            // [완성] 모든 정보 저장
            Book newBook = new Book(book.getTitle(), book.getAuthor(), selectedCategory, rating, book.getImage(), book.getDescription(), review);

            BookManager.getInstance(getContext()).addBook(newBook);
            Toast.makeText(getContext(), "내 서재에 저장되었습니다!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("취소", null);
        builder.create().show();
    }
}