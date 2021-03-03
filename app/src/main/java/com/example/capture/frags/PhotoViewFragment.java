package com.example.capture.frags;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.capture.PhotoAdapter;
import com.example.capture.R;
import com.example.capture.R.drawable;
import com.github.chrisbanes.photoview.PhotoView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class PhotoViewFragment extends Fragment {
    private static final String TAG = "포토 플레이어";
    private PhotoView photoView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // EventBus에 구독자로 현재 액티비티 추가
        EventBus.getDefault().register(this);
        return inflater.inflate(R.layout.fragment_photoplayer, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // EventBus에 구독자에서 제거
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        photoView = view.findViewById(R.id.photoview);
    }

    @Subscribe
    public void playPhoto(PhotoAdapter.PhotoItem item) {

        photoView.setImageURI(item.mUri);

    }
}
