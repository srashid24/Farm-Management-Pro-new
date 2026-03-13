package com.silageproerp.ui.harvest;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.silageproerp.R;
import com.silageproerp.adapters.HarvestAdapter;
import com.silageproerp.database.AppDatabase;
import com.silageproerp.database.entities.Harvest;
import com.silageproerp.database.entities.Land;

import java.util.ArrayList;
import java.util.List;

public class HarvestFragment extends Fragment implements HarvestAdapter.OnHarvestActionListener {

    private AppDatabase db;
    private HarvestAdapter adapter;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_with_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> showAddDialog(null));

        loadData();
    }

    private void loadData() {
        List<Harvest> list = db.harvestDao().getAll();
        if (adapter == null) {
            adapter = new HarvestAdapter(list, this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(list);
        }
    }

    private void showAddDialog(Harvest existing) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_harvest, null);
        Spinner spLand = dialogView.findViewById(R.id.sp_land);
        EditText etSeason = dialogView.findViewById(R.id.et_season);
        EditText etDate = dialogView.findViewById(R.id.et_date);
        EditText etYield = dialogView.findViewById(R.id.et_yield);
        EditText etMoisture = dialogView.findViewById(R.id.et_moisture);
        Spinner spQuality = dialogView.findViewById(R.id.sp_quality);
        Spinner spSilageType = dialogView.findViewById(R.id.sp_silage_type);
        EditText etBaleCount = dialogView.findViewById(R.id.et_bale_count);
        EditText etBaleWeight = dialogView.findViewById(R.id.et_bale_weight);
        EditText etStorage = dialogView.findViewById(R.id.et_storage);
        EditText etInputCost = dialogView.findViewById(R.id.et_input_cost);
        EditText etNotes = dialogView.findViewById(R.id.et_notes);

        List<Land> lands = db.landDao().getAll();
        List<String> landNames = new ArrayList<>();
        landNames.add("-- Select Field --");
        for (Land l : lands) landNames.add(l.fieldName + " (" + l.sizeHectares + " ha)");
        ArrayAdapter<String> la = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, landNames);
        la.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLand.setAdapter(la);

        String[] qualities = {"Excellent", "Good", "Fair", "Poor"};
        spQuality.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, qualities));

        String[] types = {"Maize", "Grass", "Sorghum", "Wheat", "Oats", "Mixed"};
        spSilageType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, types));

        if (existing != null) {
            etSeason.setText(existing.season);
            etDate.setText(existing.harvestDate);
            etYield.setText(String.valueOf(existing.yieldTons));
            etMoisture.setText(String.valueOf(existing.moisturePercent));
            etBaleCount.setText(String.valueOf(existing.baleCount));
            etBaleWeight.setText(String.valueOf(existing.baleWeightKg));
            etStorage.setText(existing.storageLocation);
            etInputCost.setText(String.valueOf(existing.inputCostTotal));
            etNotes.setText(existing.notes);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Record Harvest" : "Edit Harvest")
                .setView(dialogView)
                .setPositiveButton("Save", (d, w) -> {
                    int landIdx = spLand.getSelectedItemPosition();
                    if (landIdx == 0 || lands.isEmpty()) {
                        Toast.makeText(requireContext(), "Please select a field", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int landId = lands.get(landIdx - 1).id;
                    double yieldTons = 0, moisture = 0, baleWeight = 0, inputCost = 0;
                    int baleCount = 0;
                    try { yieldTons = Double.parseDouble(etYield.getText().toString()); } catch (Exception ignored) {}
                    try { moisture = Double.parseDouble(etMoisture.getText().toString()); } catch (Exception ignored) {}
                    try { baleCount = Integer.parseInt(etBaleCount.getText().toString()); } catch (Exception ignored) {}
                    try { baleWeight = Double.parseDouble(etBaleWeight.getText().toString()); } catch (Exception ignored) {}
                    try { inputCost = Double.parseDouble(etInputCost.getText().toString()); } catch (Exception ignored) {}

                    if (existing == null) {
                        db.harvestDao().insert(new Harvest(landId,
                                etSeason.getText().toString().trim(),
                                etDate.getText().toString().trim(),
                                yieldTons, moisture,
                                spQuality.getSelectedItem().toString(),
                                spSilageType.getSelectedItem().toString(),
                                baleCount, baleWeight,
                                etStorage.getText().toString().trim(),
                                inputCost,
                                etNotes.getText().toString().trim()));
                        Toast.makeText(requireContext(), "Harvest recorded", Toast.LENGTH_SHORT).show();
                    } else {
                        existing.landId = landId;
                        existing.season = etSeason.getText().toString().trim();
                        existing.harvestDate = etDate.getText().toString().trim();
                        existing.yieldTons = yieldTons;
                        existing.moisturePercent = moisture;
                        existing.quality = spQuality.getSelectedItem().toString();
                        existing.silagType = spSilageType.getSelectedItem().toString();
                        existing.baleCount = baleCount;
                        existing.baleWeightKg = baleWeight;
                        existing.storageLocation = etStorage.getText().toString().trim();
                        existing.inputCostTotal = inputCost;
                        existing.notes = etNotes.getText().toString().trim();
                        db.harvestDao().update(existing);
                        Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show();
                    }
                    loadData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEdit(Harvest harvest) { showAddDialog(harvest); }

    @Override
    public void onDelete(Harvest harvest) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Harvest Record")
                .setMessage("Delete this harvest record?")
                .setPositiveButton("Delete", (d, w) -> {
                    db.harvestDao().delete(harvest);
                    loadData();
                    Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
