package com.af.camerap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.ProcessLifecycleOwner;

/**
 * Project    CameraP
 * Path       com.af.camerap
 * Date       2021/09/06 - 15:57
 * Author     Payne.
 * About      类描述：
 */
public class TTtt implements LifecycleOwner {

    Lifecycle lifecycle = new LifecycleRegistry(this);

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new MyLifecycleEventObserver());
        return lifecycle;
    }

    static class MyFragment extends Fragment {

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
    }
}