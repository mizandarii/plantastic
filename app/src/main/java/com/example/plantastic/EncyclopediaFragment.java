package com.example.plantastic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class EncyclopediaFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Просто возвращаем твой XML (в котором теперь только контент, без меню!)
        return inflater.inflate(R.layout.encyclopedia_main_fragment, container, false);
    }
}

