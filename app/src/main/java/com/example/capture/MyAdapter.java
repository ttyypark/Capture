package com.example.capture;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


//  Adapter 에서 ClickListener 를 활용하는 3가지 방법
//  1. onCreateViewHolder 에서
//  2. onBindViewHolder 에서
//  3. ViewHolder Constructor 에서
//
//  사용할 Activity, Fragment 에서는
//   mAdapter.setOnItemClickListener(new MyAdapter.onItemClickListener(){
//        @Override
//        public void onItemClicked(AudioAdapter.AudioItem Item) {
//            // ...
//        }
//   }
//
// Item class 에 대한 정의를 Adapter 내부(혹은 외부)에 정의 함
// AudioAdapter.AudioItem Item
//
//  RecyclerView의 Item layout 정의 필요 R.layout.listitem_audio
//

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyAudioViewHolder> {
    private List<AudioAdapter.AudioItem> mItems = new ArrayList<>();

    // 아이템 클릭시 실행함수 인터페이스 정의
    public interface onItemClickListener {
        void onItemClicked(AudioAdapter.AudioItem Item);
    }
    private onItemClickListener mListener;

    // 리스너 객체 전달함수
    public void setOnItemClickListener(onItemClickListener listener){
        this.mListener = listener;
    }


//    public MyAdapter() {}

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
        MyAudioViewHolder viewHolder = new MyAudioViewHolder(view);

// Item Click
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mListener != null) {
//                    final AudioAdapter.AudioItem item = mItems.get(viewHolder.getAdapterPosition());
//                    mListener.onItemClicked(item);
//                }
//            }
//        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyAudioViewHolder holder, int position) {
        final AudioAdapter.AudioItem item = mItems.get(position);
        // TODO : 데이터를 뷰홀더에 표시하시오

//        //  Item Click
//        holder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mListener != null) {
//                    mListener.onItemClicked(item);
//                }
//            }
//        });

    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


    public class MyAudioViewHolder extends RecyclerView.ViewHolder {
        View mView;
        // TODO : 뷰홀더 완성하시오

        public MyAudioViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mView = itemView;
            // TODO : 뷰홀더 완성하시오

//  Item Click
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int pos = getAdapterPosition();  // 위치 찾기
//                    if (pos != RecyclerView.NO_POSITION) {
//                        if (mListener != null) {
//                            mListener.onItemClicked(mItems.get(pos));
//                        }
//                    }
//                }
//            });

        }
    }
}