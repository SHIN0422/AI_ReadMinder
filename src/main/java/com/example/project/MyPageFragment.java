package com.example.project;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyPageFragment extends Fragment {

    // ★★★ 두 API 키 모두 입력 필수! ★★★
    private static final String OPENAI_API_KEY = "";
    private static final String NAVER_CLIENT_ID = "";
    private static final String NAVER_CLIENT_SECRET = "";

    private NaverBookApi naverApi;
    private OpenAiApi openAiApi;

    private Button btnAiHistory, btnAiKeyword;
    private boolean isProcessing = false;
    private int retryCount = 0;
    private static final int MAX_RETRY = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mypage, container, false);

        Button btnWishlist = view.findViewById(R.id.btn_wishlist);
        btnAiHistory = view.findViewById(R.id.btn_ai_history);
        btnAiKeyword = view.findViewById(R.id.btn_ai_keyword);

        initApis();

        btnWishlist.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), WishlistActivity.class);
            startActivity(intent);
        });

        btnAiHistory.setOnClickListener(v -> {
            if (isProcessing) return;
            setButtonsEnabled(false);
            retryCount = 0;
            requestHistoryRecommendation();
        });

        btnAiKeyword.setOnClickListener(v -> {
            if (isProcessing) return;
            setButtonsEnabled(false);
            retryCount = 0;
            showKeywordDialog();
        });

        return view;
    }

    // [핵심] 화면이 보일 때마다 버튼 활성/비활성 상태 체크
    @Override
    public void onResume() {
        super.onResume();
        updateAiHistoryButtonState();
    }

    // 데이터 유무에 따라 '취향 분석' 버튼 활성/비활성 처리
    private void updateAiHistoryButtonState() {
        if (getContext() == null) return;

        BookManager manager = BookManager.getInstance(getContext());
        boolean hasData = !manager.getBookList().isEmpty() || !manager.getWishList().isEmpty();

        if (hasData) {
            btnAiHistory.setEnabled(true);
            btnAiHistory.setAlpha(1.0f); // 투명도 원복
            btnAiHistory.setText("내 취향 분석 추천 (읽은 책 기반)");
        } else {
            btnAiHistory.setEnabled(false); // 클릭 불가
            btnAiHistory.setAlpha(0.5f);    // 흐리게
            btnAiHistory.setText("내 취향 분석 추천 (데이터 없음)");
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        isProcessing = !enabled;
        // 키워드 추천은 언제나 가능하므로 enabled 값 따라감
        btnAiKeyword.setEnabled(enabled);
        btnAiKeyword.setAlpha(enabled ? 1.0f : 0.5f);

        // 취향 분석 버튼은 데이터가 있어야만 다시 활성화됨
        if (enabled) {
            updateAiHistoryButtonState();
        } else {
            btnAiHistory.setEnabled(false);
            btnAiHistory.setAlpha(0.5f);
        }
    }

    private void initApis() {
        Retrofit naverRetrofit = new Retrofit.Builder()
                .baseUrl("https://openapi.naver.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        naverApi = naverRetrofit.create(NaverBookApi.class);

        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                        .build();
                return chain.proceed(newRequest);
            }
        }).build();

        Retrofit openaiRetrofit = new Retrofit.Builder()
                .baseUrl("https://api.openai.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        openAiApi = openaiRetrofit.create(OpenAiApi.class);
    }

    private void showKeywordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_ai_keyword, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(d -> setButtonsEnabled(true));

        EditText etKeyword = dialogView.findViewById(R.id.et_keyword);
        Button btnRecommend = dialogView.findViewById(R.id.btn_recommend);

        btnRecommend.setOnClickListener(v -> {
            String keyword = etKeyword.getText().toString();
            if (keyword.isEmpty()) {
                Toast.makeText(getContext(), "키워드를 입력해주세요!", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            requestKeywordRecommendation(keyword);
        });

        dialog.show();
    }

    private void requestKeywordRecommendation(String keyword) {
        if (retryCount == 0) {
            Toast.makeText(getContext(), "'" + keyword + "' 관련 책을 찾는 중...", Toast.LENGTH_SHORT).show();
        }

        List<OpenAiUtils.Message> messages = new ArrayList<>();
        messages.add(new OpenAiUtils.Message("system",
                "당신은 도움이 되는 도서 추천가입니다. 사용자의 키워드에 딱 맞는 최고의 도서 1권을 추천해주세요. 오직 책 제목만 답변하세요."));
        messages.add(new OpenAiUtils.Message("user",
                "키워드: " + keyword));

        Runnable onRetry = () -> requestKeywordRecommendation(keyword);
        callOpenAi(messages, "키워드 '" + keyword + "' 추천 도서", onRetry);
    }

    private void requestHistoryRecommendation() {
        BookManager manager = BookManager.getInstance(getContext());
        List<Book> readBooks = manager.getBookList();
        List<Book> wishBooks = manager.getWishList();

        // (버튼 비활성화로 막았지만 한 번 더 체크)
        if (readBooks.isEmpty() && wishBooks.isEmpty()) {
            setButtonsEnabled(true);
            Toast.makeText(getContext(), "분석할 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (retryCount == 0) {
            Toast.makeText(getContext(), "독서 기록 분석 중...", Toast.LENGTH_SHORT).show();
        }

        StringBuilder bookContext = new StringBuilder();
        if (!readBooks.isEmpty()) {
            bookContext.append("[읽은 책 목록]\n");
            for (Book book : readBooks) {
                bookContext.append(String.format("- 제목: %s, 저자: %s, 카테고리: %s, 평점: %.1f\n",
                        book.getTitle(), book.getAuthor(), book.getCategory(), book.getRating()));
            }
        }
        if (!wishBooks.isEmpty()) {
            bookContext.append("\n[읽고 싶은 책(찜) 목록]\n");
            for (Book book : wishBooks) {
                bookContext.append(String.format("- 제목: %s, 저자: %s\n",
                        book.getTitle(), book.getAuthor()));
            }
        }

        List<OpenAiUtils.Message> messages = new ArrayList<>();
        messages.add(new OpenAiUtils.Message("system",
                "당신은 전문 AI 북 큐레이터입니다. " +
                        "제공된 사용자의 '읽은 책'과 '찜한 책' 기록을 분석하여 취향을 파악하세요. " +
                        "찜한 책과 비슷한 분위기거나, 읽은 책 중 평점이 높은 책과 유사한 " +
                        "최고의 도서 1권을 추천해주세요. (사용자가 아직 안 읽은 책이어야 합니다.) " +
                        "오직 책 제목만 답변하세요."));
        messages.add(new OpenAiUtils.Message("user",
                "다음은 내 독서 데이터야:\n" + bookContext.toString() +
                        "\n\n이걸 바탕으로 책 한 권만 추천해줘."));

        Runnable onRetry = this::requestHistoryRecommendation;
        callOpenAi(messages, "당신의 취향 저격 도서", onRetry);
    }

    private void callOpenAi(List<OpenAiUtils.Message> messages, String titlePrefix, Runnable onRetry) {
        OpenAiUtils.ChatRequest request = new OpenAiUtils.ChatRequest("gpt-4o-mini", messages);
        openAiApi.getRecommendation(request).enqueue(new Callback<OpenAiUtils.ChatResponse>() {
            @Override
            public void onResponse(Call<OpenAiUtils.ChatResponse> call, Response<OpenAiUtils.ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String recommendedTitle = response.body().choices.get(0).message.content.trim();
                    searchNaverBook(recommendedTitle, titlePrefix, onRetry);
                } else {
                    setButtonsEnabled(true);
                    Toast.makeText(getContext(), "AI 오류: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<OpenAiUtils.ChatResponse> call, Throwable t) {
                setButtonsEnabled(true);
                Toast.makeText(getContext(), "통신 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchNaverBook(String title, String titlePrefix, Runnable onRetry) {
        String cleanTitle = title.replace("\"", "").replace("'", "").replace(".", "");

        naverApi.searchBooks(NAVER_CLIENT_ID, NAVER_CLIENT_SECRET, cleanTitle, 1).enqueue(new Callback<BookSearchResponse>() {
            @Override
            public void onResponse(Call<BookSearchResponse> call, Response<BookSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BookSearchResponse.Item> items = response.body().items;
                    if (items != null && !items.isEmpty()) {
                        setButtonsEnabled(true);
                        showAiResultDialog(titlePrefix, items.get(0));
                    } else {
                        handleRetry(onRetry);
                    }
                } else {
                    setButtonsEnabled(true);
                    Toast.makeText(getContext(), "네이버 검색 실패", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<BookSearchResponse> call, Throwable t) {
                setButtonsEnabled(true);
                Toast.makeText(getContext(), "네이버 검색 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleRetry(Runnable onRetry) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            Toast.makeText(getContext(), "다른 추천 도서를 찾는 중... (" + retryCount + "/" + MAX_RETRY + ")", Toast.LENGTH_SHORT).show();
            onRetry.run();
        } else {
            setButtonsEnabled(true);
            Toast.makeText(getContext(), "죄송합니다. 적절한 추천 도서를 찾지 못했습니다.", Toast.LENGTH_LONG).show();
        }
    }

    private void showAiResultDialog(String prefix, BookSearchResponse.Item item) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_ai_result, null);
        builder.setView(dialogView);

        ImageView ivCover = dialogView.findViewById(R.id.iv_ai_cover);
        TextView tvTitle = dialogView.findViewById(R.id.tv_ai_title);
        TextView tvAuthor = dialogView.findViewById(R.id.tv_ai_author);
        final TextView tvDesc = dialogView.findViewById(R.id.tv_ai_description);
        final TextView tvMore = dialogView.findViewById(R.id.tv_ai_more);

        String cleanTitle = item.title.replace("<b>", "").replace("</b>", "");
        String cleanAuthor = item.author.replace("<b>", "").replace("</b>", "");
        String cleanDesc = item.description.replace("<b>", "").replace("</b>", "");

        tvTitle.setText(cleanTitle);
        tvAuthor.setText(cleanAuthor);
        tvDesc.setText("🤖 " + prefix + "\n\n" + cleanDesc);

        Glide.with(getContext()).load(item.image).into(ivCover);

        tvDesc.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                tvDesc.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (tvDesc.getLineCount() > 0) {
                    int lastLineIndex = tvDesc.getLineCount() - 1;
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

        builder.setPositiveButton("찜하기", (dialog, which) -> {
            Book newBook = new Book(cleanTitle, cleanAuthor, "", 0, item.image, cleanDesc, "");
            BookManager.getInstance(getContext()).addWish(newBook);
            Toast.makeText(getContext(), "찜 목록에 담았습니다!", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("닫기", null);
        builder.create().show();
    }
}