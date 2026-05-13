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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {

    private SwitchCompat notificationSwitch;
    private TextView startTimeText;
    private TextView endTimeText;
    private PlantasticDatabase db;
    private Kasutaja currentUser;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private static final long DEFAULT_START_MINUTES = 8 * 60;
    private static final long DEFAULT_END_MINUTES = 22 * 60;

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
        ioExecutor.execute(() -> {
            currentUser = db.kasutajaDao().getFirstUser();
            if (currentUser == null) {
                Kasutaja user = new Kasutaja();
                user.kasutajanimi = "Primary User";
                user.teade_on = false;
                user.teade_start = DEFAULT_START_MINUTES;
                user.teade_aeg = DEFAULT_END_MINUTES;
                long id = db.kasutajaDao().insert(user);
                user.id = (int) id;
                currentUser = user;
            }

            long normalizedStart = normalizeMinutes(currentUser.teade_start, DEFAULT_START_MINUTES);
            long normalizedEnd = normalizeMinutes(currentUser.teade_aeg, DEFAULT_END_MINUTES);
            boolean needsUpdate = normalizedStart != currentUser.teade_start || normalizedEnd != currentUser.teade_aeg;
            currentUser.teade_start = normalizedStart;
            currentUser.teade_aeg = normalizedEnd;
            if (needsUpdate) {
                db.kasutajaDao().update(currentUser);
            }

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
        ioExecutor.execute(() -> {
            db.kasutajaDao().update(currentUser);
        });
    }

    private long normalizeMinutes(long rawValue, long fallback) {
        long minutes = rawValue;
        if (minutes < 0) {
            return fallback;
        }

        // Handle old values accidentally stored as epoch millis or seconds.
        if (minutes > 24 * 60 - 1) {
            if (minutes > 24L * 60L * 60L) {
                minutes = minutes / 60000L;
            } else {
                minutes = minutes / 60L;
            }
        }

        return minutes % (24 * 60);
    }

    private String formatTime(long minutesFromMidnight) {
        int h = (int) (minutesFromMidnight / 60);
        int m = (int) (minutesFromMidnight % 60);
        return String.format("%02d:%02d", h, m);
    }
}