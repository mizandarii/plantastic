package com.example.plantastic;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.Kasutaja;

import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {

    private SwitchCompat notificationSwitch;
    private TextView startTimeText;
    private TextView endTimeText;
    private PlantasticDatabase db;
    private Kasutaja currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_page_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationSwitch = view.findViewById(R.id.notificationSwitch);
        startTimeText = view.findViewById(R.id.tvStartTime);
        endTimeText = view.findViewById(R.id.tvEndTime);

        db = PlantasticDatabase.getInstance(requireContext());

        loadSettings();

        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentUser != null) {
                currentUser.teade_on = isChecked;
                saveSettings();
            }
        });

        startTimeText.setOnClickListener(v -> showTimePicker(startTimeText, true));
        endTimeText.setOnClickListener(v -> showTimePicker(endTimeText, false));
    }

    private void loadSettings() {
        Executors.newSingleThreadExecutor().execute(() -> {
            currentUser = db.kasutajaDao().getFirstUser();
            if (currentUser != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    notificationSwitch.setChecked(currentUser.teade_on);
                    startTimeText.setText(formatTime(currentUser.teade_start));
                    endTimeText.setText(formatTime(currentUser.teade_aeg));
                });
            }
        });
    }

    private void showTimePicker(TextView targetTextView, boolean isStart) {
        if (currentUser == null) return;

        long currentTimeValue = isStart ? currentUser.teade_start : currentUser.teade_aeg;
        int hour = (int) (currentTimeValue / 60);
        int minute = (int) (currentTimeValue % 60);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, hourOfDay, selectedMinute) -> {
                    long minutesFromMidnight = hourOfDay * 60 + selectedMinute;
                    targetTextView.setText(formatTime(minutesFromMidnight));

                    if (isStart) {
                        currentUser.teade_start = minutesFromMidnight;
                    } else {
                        currentUser.teade_aeg = minutesFromMidnight;
                    }
                    saveSettings();
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void saveSettings() {
        if (currentUser == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            db.kasutajaDao().update(currentUser);
        });
    }

    private String formatTime(long minutesFromMidnight) {
        int h = (int) (minutesFromMidnight / 60);
        int m = (int) (minutesFromMidnight % 60);
        return String.format("%02d:%02d", h, m);
    }
}
