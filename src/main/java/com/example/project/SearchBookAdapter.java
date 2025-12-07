package com.example.project;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // 토스트 메시지용
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class SearchBookAdapter extends RecyclerView.Adapter<SearchBookAdapter.ViewHolder> {

    private List<Book> searchList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onAddClick(Book book);
        void onWishClick(Book book, int position);
    }

    public SearchBookAdapter(List<Book> searchList, OnItemClickListener listener) {
        this.searchList = searchList;
        this.listener = listener;
    }

    public void setList(List<Book> newList) {
        this.searchList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Book book = searchList.get(position);

        String cleanTitle = book.getTitle().replace("<b>", "").replace("</b>", "");
        String cleanAuthor = book.getAuthor().replace("<b>", "").replace("</b>", "");

        holder.tvTitle.setText(cleanTitle);
        holder.tvAuthor.setText(cleanAuthor);

        Glide.with(holder.itemView.getContext())
                .load(book.getImage())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_delete)
                .into(holder.ivCover);

        // [핵심 수정] 이미 읽은 책인지 확인
        boolean isRead = BookManager.getInstance(holder.itemView.getContext()).isRead(book);

        if (isRead) {
            // 1. 이미 읽은 경우
            holder.btnAdd.setImageResource(android.R.drawable.checkbox_on_background); // 체크 표시
            holder.btnAdd.setAlpha(0.5f); // 흐리게

            holder.btnWish.setVisibility(View.INVISIBLE); // 찜 버튼 숨김 (이미 읽었으니 찜 불필요)

            // 클릭 방지 및 안내 메시지
            holder.btnAdd.setOnClickListener(v ->
                    Toast.makeText(holder.itemView.getContext(), "이미 서재에 있는 책입니다.", Toast.LENGTH_SHORT).show()
            );
            holder.btnWish.setOnClickListener(null);

        } else {
            // 2. 아직 안 읽은 경우 (정상 상태)
            holder.btnAdd.setImageResource(android.R.drawable.ic_input_add); // + 아이콘
            holder.btnAdd.setAlpha(1.0f);

            holder.btnWish.setVisibility(View.VISIBLE); // 찜 버튼 보임

            // 찜 아이콘 상태 (채워진 별 / 빈 별)
            boolean isWished = BookManager.getInstance(holder.itemView.getContext()).isWished(book);
            if (isWished) {
                holder.btnWish.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                holder.btnWish.setImageResource(android.R.drawable.btn_star_big_off);
            }

            // 정상 클릭 이벤트 연결
            holder.btnAdd.setOnClickListener(v -> {
                if (listener != null) listener.onAddClick(book);
            });

            holder.btnWish.setOnClickListener(v -> {
                if (listener != null) listener.onWishClick(book, position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvAuthor;
        ImageButton btnAdd, btnWish;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.search_cover);
            tvTitle = itemView.findViewById(R.id.search_title);
            tvAuthor = itemView.findViewById(R.id.search_author);
            btnAdd = itemView.findViewById(R.id.btn_add_book);
            btnWish = itemView.findViewById(R.id.btn_wish_book);
        }
    }
}