package com.example.capture;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyAudioViewHolder> {
    interface onItemClickListener {
        void onItemClicked(AudioAdapter.AudioItem model);
    }
    
    private onItemClickListener mListener;
    private List<AudioAdapter.AudioItem> mItems = new ArrayList<>();

    public MyAdapter() {}

    public MyAdapter(onItemClickListener listener) {
        mListener = listener;
    }

    public void setItems(List<AudioAdapter.AudioItem> items) {
        this.mItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyAudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_audio, parent, false);
        final MyAudioViewHolder viewHolder = new MyAudioViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    final AudioAdapter.AudioItem item = mItems.get(viewHolder.getAdapterPosition());
                    mListener.onItemClicked(item);
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyAudioViewHolder holder, int position) {
        AudioAdapter.AudioItem item = mItems.get(position);
        // TODO : 데이터를 뷰홀더에 표시하시오
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class MyAudioViewHolder extends RecyclerView.ViewHolder {
        // TODO : 뷰홀더 완성하시오
        
        public MyAudioViewHolder(@NonNull View itemView) {
            super(itemView);
            // TODO : 뷰홀더 완성하시오
        }
    }
}