package com.example.mediaplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.AudioAdapter.AudioItem
import java.util.*

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
class MyAdapter     //    public MyAdapter() {}
constructor(private var mListener: onItemClickListener) : RecyclerView.Adapter<MyAdapter.MyAudioViewHolder>() {
    private var mItems: List<AudioItem> = ArrayList()

    // 아이템 클릭시 실행함수 인터페이스 정의
    interface onItemClickListener {
        fun onItemClicked(Item: AudioItem?)
    }

    // 리스너 객체 전달함수
    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    fun setItems(items: List<AudioItem>) {
        mItems = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAudioViewHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.listitem_audio, parent, false)
        val viewHolder = MyAudioViewHolder(view)

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
        return viewHolder
    }

    override fun onBindViewHolder(holder: MyAudioViewHolder, position: Int) {
        val item: AudioItem = mItems.get(position)
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

    override fun getItemCount(): Int {
        return mItems.size
    }

    inner class MyAudioViewHolder// TODO : 뷰홀더 완성하시오

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
    // TODO : 뷰홀더 완성하시오
    constructor(mView: View) : RecyclerView.ViewHolder(mView)
}