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
//사용 x
public class search_adapter extends RecyclerView.Adapter<search_adapter.ViewHolder> {

    private List<Book> searchList;
    private OnItemClickListener listener;

    // 클릭 이벤트 처리를 위한 인터페이스
    public interface OnItemClickListener {
        void onAddClick(Book book);
    }

    public search_adapter(List<Book> searchList, OnItemClickListener listener) {
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
        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());
        // 표지 이미지는 임시
        //holder.ivCover.setImageResource(book.getCoverResId());
        Glide.with(holder.itemView.getContext())
                .load(book.getImage()) // 이제 URL(String)을 불러옵니다
                .placeholder(android.R.drawable.ic_menu_gallery) // 로딩 중 이미지
                .error(android.R.drawable.ic_menu_gallery) // 에러/빈값 일 때 이미지
                .into(holder.ivCover);

        // + 버튼 클릭 시 이벤트 발생
        holder.btnAdd.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddClick(book);
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvAuthor;
        ImageButton btnAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.search_cover);
            tvTitle = itemView.findViewById(R.id.search_title);
            tvAuthor = itemView.findViewById(R.id.search_author);
            btnAdd = itemView.findViewById(R.id.btn_add_book);
        }
    }
}