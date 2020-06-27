package com.example.capture.frags;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.capture.R;

import java.io.Serializable;
import java.util.List;


public class ListViewFragment extends Fragment {

    public ListViewFragment() {
    }

    // TODO: Customize parameter initialization
    public static ListViewFragment newInstance(List<String> data) {
        ListViewFragment fragment = new ListViewFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable("data", (Serializable) data);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listView = (ListView) view.findViewById(R.id.list_view);
        Bundle bundle = getArguments();
        assert bundle != null;
        List<String> mData = (List<String>) bundle.getSerializable("data");
        MyAdapter adapter = new MyAdapter(mData);
        listView.setAdapter(adapter);
    }


    private static class MyAdapter extends BaseAdapter {

        private final List<String> mmData;

        private MyAdapter(List<String> mmData) {
            this.mmData = mmData;
        }

        @Override
        public int getCount() {
            return mmData.size();
        }

        @Override
        public Object getItem(int position) {
            return mmData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();

                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(android.R.layout.simple_list_item_1, parent, false);

                viewHolder.textView = (TextView) convertView.findViewById(android.R.id.text1);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            String data = mmData.get(position);

            viewHolder.textView.setText(data);

            return convertView;        }
    }

    private static class ViewHolder {
        TextView textView;
    }

}
