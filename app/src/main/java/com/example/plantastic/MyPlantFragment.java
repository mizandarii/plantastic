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
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    // keep header (index 0), remove others
                    int count = tableCareHistory.getChildCount();
                    for (int i = count - 1; i >= 1; i--) {
                        tableCareHistory.removeViewAt(i);
                    }

                    java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());

                    if (history != null) {
                        for (com.example.plantastic.data.entities.HooldusAjalugu item : history) {
                            android.widget.TableRow row = new android.widget.TableRow(requireContext());
                            android.widget.TextView typeTv = new android.widget.TextView(requireContext());
                            android.widget.TextView dateTv = new android.widget.TextView(requireContext());

                            String typeName = "-";
                            try {
                                if (item.hooldusTüüp_id != null) {
                                    com.example.plantastic.data.entities.HooldusTüüp t = db.hooldusTüüpDao().getById(item.hooldusTüüp_id);
                                    if (t != null) typeName = t.nimetus;
                                }
                            } catch (Exception ex) { /* ignore */ }

                            typeTv.setText(typeName);
                            dateTv.setText(fmt.format(new java.util.Date(item.aeg)));

                            // Ensure columns match header weights
                            android.widget.TableRow.LayoutParams lp = new android.widget.TableRow.LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                            typeTv.setLayoutParams(lp);
                            dateTv.setLayoutParams(lp);

                            int padding = (int) (8 * requireContext().getResources().getDisplayMetrics().density);
                            typeTv.setPadding(padding,padding,padding,padding);
                            dateTv.setPadding(padding,padding,padding,padding);

                            row.addView(typeTv, lp);
                            row.addView(dateTv, lp);
                            tableCareHistory.addView(row);
                        }
                    }
                });
            } catch (Exception ex) {
                // ignore
            }
        });
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
                    HooldusTüüp kastmineType = db.hooldusTüüpDao().getByName("Kastmine");
                    if (kastmineType == null) {
                        HooldusTüüp createdType = new HooldusTüüp();
                        createdType.nimetus = "Kastmine";
                        long typeId = db.hooldusTüüpDao().insert(createdType);
                        createdType.id = (int) typeId;
                        kastmineType = createdType;
                    }

                    HooldusAjalugu history = new HooldusAjalugu();
                    history.taim_id = currentPlant.taim.id;
                    history.hooldusTüüp_id = kastmineType.id;
                    history.aeg = System.currentTimeMillis();
                    history.kommentaar = "Kastetud";
                    db.hooldusAjaluguDao().insert(history);

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

    private void savePlantChanges() {
        executorService.execute(() -> {
            if (currentPlant != null && currentPlant.taim != null) {
                db.taimDao().update(currentPlant.taim);
            }
        });
    }
}



