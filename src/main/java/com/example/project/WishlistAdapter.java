package com.example.project;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private List<Book> wishList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onRemoveClick(Book book, int position); // 찜 취소
        void onReviewClick(Book book, int position); // 리뷰 작성
    }

    public WishlistAdapter(List<Book> wishList, OnItemClickListener listener) {
        this.wishList = wishList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // [중요] 찜 목록용 아이템 레이아웃 연결
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wishlist_book, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = wishList.get(position);

        holder.tvTitle.setText(book.getTitle());

        Glide.with(holder.itemView.getContext())
                .load(book.getImage())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(holder.ivCover);

        // 찜 취소 버튼 클릭
        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemoveClick(book, position);
        });

        // 리뷰 작성 버튼 클릭
        holder.btnReview.setOnClickListener(v -> {
            if (listener != null) listener.onReviewClick(book, position);
        });
    }

    @Override
    public int getItemCount() {
        return wishList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle;
        ImageButton btnRemove, btnReview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 아이템 XML 파일(item_wishlist_book.xml)의 ID와 일치해야 함
            ivCover = itemView.findViewById(R.id.iv_book_cover);
            tvTitle = itemView.findViewById(R.id.tv_book_title);
            btnRemove = itemView.findViewById(R.id.btn_remove_wish);
            btnReview = itemView.findViewById(R.id.btn_write_review);
        }
    }
}