package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class RecentBookAdapter extends RecyclerView.Adapter<RecentBookAdapter.ViewHolder> {

    private List<Book> bookList;
    private OnItemClickListener listener;
    private int limit; // [추가] 표시할 최대 개수 (0이면 제한 없음)

    public interface OnItemClickListener {
        void onItemClick(Book book, int position);
    }

    // [수정] limit을 받는 생성자 추가
    public RecentBookAdapter(List<Book> bookList, int limit, OnItemClickListener listener) {
        this.bookList = bookList;
        this.limit = limit;
        this.listener = listener;
    }

    // (기존 생성자 호환용 - 제한 없음)
    public RecentBookAdapter(List<Book> bookList) {
        this.bookList = bookList;
        this.limit = 0; // 0은 전체 다 보여줌
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = bookList.get(position);

        holder.tvTitle.setText(book.getTitle());
        holder.ratingBar.setRating(book.getRating());

        Glide.with(holder.itemView.getContext())
                .load(book.getImage())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_delete)
                .into(holder.ivCover);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(book, position);
            }
        });
    }

    // [핵심 수정] 갯수 제한 로직
    @Override
    public int getItemCount() {
        if (limit > 0) {
            return Math.min(bookList.size(), limit);
        }
        return bookList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.book_cover);
            tvTitle = itemView.findViewById(R.id.book_title);
            ratingBar = itemView.findViewById(R.id.rating_book);
        }
    }
}