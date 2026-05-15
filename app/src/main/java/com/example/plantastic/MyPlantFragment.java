package com.example.plantastic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.plantastic.data.PlantasticDatabase;
import com.example.plantastic.data.entities.HooldusAjalugu;
import com.example.plantastic.data.entities.HooldusTüüp;
import com.example.plantastic.data.entities.TaimWithDetails;
import com.example.plantastic.data.entities.Teade;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyPlantFragment extends Fragment {
    private EditText editNickname;
    private EditText editSpecies;
    private EditText editDescription;
    private Button btnWater;
    private Button btnEdit;
    private ImageView selectedPlantImage;
    private android.widget.ImageButton btnBack;
    private android.widget.TableLayout tableCareHistory;

    private TaimWithDetails currentPlant;
    private boolean isEditMode = false;
    private PlantasticDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private int plantId;

    // Store original values for canceling edits
    private String originalNickname;
    private String originalDescription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.my_plant, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        editNickname = view.findViewById(R.id.editNickname);
        editSpecies = view.findViewById(R.id.editSpecies);
        editDescription = view.findViewById(R.id.editDescription);
        btnWater = view.findViewById(R.id.btnWater);
        btnEdit = view.findViewById(R.id.btnEdit);
        selectedPlantImage = view.findViewById(R.id.selectedPlantImage);
        btnBack = view.findViewById(R.id.btnBack);
        tableCareHistory = view.findViewById(R.id.tableCareHistory);
        db = PlantasticDatabase.getInstance(requireContext());

        // Get plant ID from arguments
        if (getArguments() != null) {
            plantId = getArguments().getInt("plantId", -1);
            if (plantId != -1) {
                loadPlantDetails();
            }
        }

        // Set up button listeners
        btnWater.setOnClickListener(v -> onWaterOrCancelClick());
        btnEdit.setOnClickListener(v -> onEditOrSaveClick());
        btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            } else if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void loadPlantDetails() {
        executorService.execute(() -> {
            currentPlant = db.taimDao().getWithDetailsById(plantId);

            if (getActivity() != null && currentPlant != null) {
                getActivity().runOnUiThread(this::displayPlantDetails);
            }
        });
    }

    private void displayPlantDetails() {
        if (currentPlant != null) {
            originalNickname = currentPlant.taim.nimi;
            originalDescription = currentPlant.taim.kirjeldus != null ? currentPlant.taim.kirjeldus : "";

            editNickname.setText(originalNickname);

            if (currentPlant.sort != null) {
                editSpecies.setText(currentPlant.sort.nimetus);
            }

            editDescription.setText(originalDescription);

            // Load plant image from database
            loadPlantImage();

            // Load care history table
            loadCareHistory();
        }
    }

    private void loadCareHistory() {
        if (currentPlant == null || currentPlant.taim == null) return;
        executorService.execute(() -> {
            try {
                final java.util.List<com.example.plantastic.data.entities.HooldusAjalugu> history = db.hooldusAjaluguDao().getByTaimId(currentPlant.taim.id);
                
                // Prepare data on background thread before switching to UI thread
                java.util.List<CareHistoryItem> items = new java.util.ArrayList<>();
                if (history != null) {
                    for (com.example.plantastic.data.entities.HooldusAjalugu item : history) {
                        String typeName = "-";
                        try {
                            if (item.hooldusTüüp_id != null) {
                                com.example.plantastic.data.entities.HooldusTüüp t = db.hooldusTüüpDao().getById(item.hooldusTüüp_id);
                                if (t != null) typeName = t.nimetus;
                            }
                        } catch (Exception ex) {
                            android.util.Log.e("CareHistory", "Error fetching care type", ex);
                        }
                        items.add(new CareHistoryItem(typeName, item.aeg));
                    }
                }
                
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    // keep header (index 0), remove others
                    int count = tableCareHistory.getChildCount();
                    for (int i = count - 1; i >= 1; i--) {
                        tableCareHistory.removeViewAt(i);
                    }

                    // Display prepared items on UI thread
                    for (CareHistoryItem item : items) {
                        android.widget.TableRow row = new android.widget.TableRow(requireContext());
                        android.widget.TextView typeTv = new android.widget.TextView(requireContext());
                        android.widget.TextView dateTv = new android.widget.TextView(requireContext());
                        android.widget.TextView timeTv = new android.widget.TextView(requireContext());

                        typeTv.setText(item.typeName);
                        
                        java.util.Date date = new java.util.Date(item.aeg);
                        java.text.SimpleDateFormat dateFmt = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                        java.text.SimpleDateFormat timeFmt = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                        dateTv.setText(dateFmt.format(date));
                        timeTv.setText(timeFmt.format(date));

                        // Ensure columns match header weights
                        android.widget.TableRow.LayoutParams lp = new android.widget.TableRow.LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                        typeTv.setLayoutParams(lp);
                        dateTv.setLayoutParams(lp);
                        timeTv.setLayoutParams(lp);

                        int padding = (int) (8 * requireContext().getResources().getDisplayMetrics().density);
                        typeTv.setPadding(padding, padding, padding, padding);
                        dateTv.setPadding(padding, padding, padding, padding);
                        timeTv.setPadding(padding, padding, padding, padding);

                        // Set text appearance
                        typeTv.setTextSize(14);
                        dateTv.setTextSize(14);
                        timeTv.setTextSize(14);
                        typeTv.setTextColor(requireContext().getColor(android.R.color.black));
                        dateTv.setTextColor(requireContext().getColor(android.R.color.black));
                        timeTv.setTextColor(requireContext().getColor(android.R.color.black));

                        row.addView(typeTv, lp);
                        row.addView(dateTv, lp);
                        row.addView(timeTv, lp);
                        tableCareHistory.addView(row);
                    }
                });
            } catch (Exception ex) {
                android.util.Log.e("CareHistory", "Error loading care history", ex);
            }
        });
    }

    // Helper class to hold care history data
    private static class CareHistoryItem {
        String typeName;
        long aeg;
        
        CareHistoryItem(String typeName, long aeg) {
            this.typeName = typeName;
            this.aeg = aeg;
        }
    }

    private void loadPlantImage() {
        if (currentPlant.fotos != null && !currentPlant.fotos.isEmpty()) {
            Glide.with(requireContext())
                    .load(currentPlant.fotos.get(0).foto)
                    .placeholder(R.drawable.ic_flower)
                    .error(R.drawable.ic_flower)
                    .into(selectedPlantImage);
        } else {
            selectedPlantImage.setImageResource(R.drawable.ic_flower);
        }
    }

    private void onWaterOrCancelClick() {
        if (isEditMode) {
            // Cancel editing - discard changes
            cancelEditMode();
        } else {
            // Water the plant
            waterPlant();
        }
    }

    private void onEditOrSaveClick() {
        if (isEditMode) {
            // Save changes
            saveEditMode();
        } else {
            // Enter edit mode
            enterEditMode();
        }
    }

    private void enterEditMode() {
        isEditMode = true;

        // Enable editing
        editNickname.setEnabled(true);
        editNickname.setFocusable(true);
        editNickname.setFocusableInTouchMode(true);
        editNickname.setCursorVisible(true);

        editSpecies.setEnabled(true);
        editSpecies.setFocusable(true);
        editSpecies.setFocusableInTouchMode(true);
        editSpecies.setCursorVisible(true);

        editDescription.setEnabled(true);
        editDescription.setFocusable(true);
        editDescription.setFocusableInTouchMode(true);
        editDescription.setCursorVisible(true);

        // Update button texts for edit mode
        btnWater.setText("Katkesta");
        btnEdit.setText("Salvesta");
    }

    private void cancelEditMode() {
        isEditMode = false;

        // Revert changes
        editNickname.setText(originalNickname);
        editDescription.setText(originalDescription);

        // Disable editing
        editNickname.setEnabled(false);
        editNickname.setFocusable(false);
        editNickname.setCursorVisible(false);

        editSpecies.setEnabled(false);
        editSpecies.setFocusable(false);
        editSpecies.setCursorVisible(false);

        editDescription.setEnabled(false);
        editDescription.setFocusable(false);
        editDescription.setCursorVisible(false);

        // Revert button texts to view mode
        btnWater.setText("Kasta");
        btnEdit.setText("Muuta");
    }

    private void saveEditMode() {
        isEditMode = false;

        // Save to database
        if (currentPlant != null) {
            currentPlant.taim.nimi = editNickname.getText().toString();
            originalNickname = currentPlant.taim.nimi;

            String newDescription = editDescription.getText().toString();
            currentPlant.taim.kirjeldus = newDescription;
            originalDescription = newDescription;

            savePlantChanges();
        }

        // Disable editing
        editNickname.setEnabled(false);
        editNickname.setFocusable(false);
        editNickname.setCursorVisible(false);

        editSpecies.setEnabled(false);
        editSpecies.setFocusable(false);
        editSpecies.setCursorVisible(false);

        editDescription.setEnabled(false);
        editDescription.setFocusable(false);
        editDescription.setCursorVisible(false);

        // Revert button texts to view mode
        btnWater.setText("Kasta");
        btnEdit.setText("Muuta");
    }

    private void waterPlant() {
        if (currentPlant != null && currentPlant.taim != null) {
            executorService.execute(() -> {
                try {
                    // Get or create "Kastmine" care type
                    HooldusTüüp kastmineType = db.hooldusTüüpDao().getByName("Kastmine");
                    if (kastmineType == null) {
                        HooldusTüüp createdType = new HooldusTüüp();
                        createdType.nimetus = "Kastmine";
                        long typeId = db.hooldusTüüpDao().insert(createdType);
                        createdType.id = (int) typeId;
                        kastmineType = createdType;
                    }

                    // Record care history
                    HooldusAjalugu history = new HooldusAjalugu();
                    history.taim_id = currentPlant.taim.id;
                    history.hooldusTüüp_id = kastmineType.id;
                    history.aeg = System.currentTimeMillis();
                    history.kommentaar = "Kastetud";
                    db.hooldusAjaluguDao().insert(history);

                    // Schedule next notification based on plant's watering needs
                    scheduleNextNotification(kastmineType.id);

                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Kastetud", Toast.LENGTH_SHORT).show();
                                loadCareHistory();
                        });
                    }
                } catch (Exception ex) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Kastmise salvestamine ebaõnnestus", Toast.LENGTH_SHORT).show()
                        );
                    }
                }
            });
        }
    }

    private void scheduleNextNotification(int careTypeId) {
        if (currentPlant == null || currentPlant.taim == null || currentPlant.sort == null) return;

        long now = System.currentTimeMillis();
        long intervalMillis = getIntervalMillisFromWateringIntensity(currentPlant.sort.kastmisvajadus);
        long nextNotificationTime = now + intervalMillis;

        // Get or create notification record
        Teade notification = db.teadeDao().getByTaimAndType(currentPlant.taim.id, careTypeId);
        if (notification == null) {
            notification = new Teade();
            notification.taim_id = currentPlant.taim.id;
            notification.hooldusTüüp_id = careTypeId;
            notification.aeg = nextNotificationTime;
            notification.kommentaar = "Watering reminder";
            db.teadeDao().insert(notification);
        } else {
            notification.aeg = nextNotificationTime;
            db.teadeDao().update(notification);
        }
    }

    private long getIntervalMillisFromWateringIntensity(int intensity) {
        // Map watering intensity to milliseconds between waterings
        switch (intensity) {
            case 0: return 365L * 24 * 60 * 60 * 1000; // Never/rarely - annual
            case 1: return 30L * 24 * 60 * 60 * 1000;  // Minimum - monthly
            case 2: return 14L * 24 * 60 * 60 * 1000;  // Average - bi-weekly
            case 3: return 7L * 24 * 60 * 60 * 1000;   // Frequent - weekly
            case 4: return 5L * 60 * 1000;             // Testing - 5 minutes
            default: return 14L * 24 * 60 * 60 * 1000; // fallback
        }
    }

    private void savePlantChanges() {
        executorService.execute(() -> {
            if (currentPlant != null && currentPlant.taim != null) {
                db.taimDao().update(currentPlant.taim);
            }
        });
    }
}



