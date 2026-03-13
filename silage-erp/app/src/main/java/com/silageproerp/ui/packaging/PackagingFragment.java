package com.silageproerp.ui.packaging;

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
import com.silageproerp.adapters.PackagingAdapter;
import com.silageproerp.database.AppDatabase;
import com.silageproerp.database.entities.Harvest;
import com.silageproerp.database.entities.Packaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PackagingFragment extends Fragment implements PackagingAdapter.OnPackagingActionListener {

    private AppDatabase db;
    private PackagingAdapter adapter;
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
        view.findViewById(R.id.fab_add).setOnClickListener(v -> showDialog(null));
        loadData();
    }

    private void loadData() {
        List<Packaging> list = db.packagingDao().getAll();
        if (adapter == null) { adapter = new PackagingAdapter(list, this); recyclerView.setAdapter(adapter); }
        else adapter.updateList(list);
    }

    private void showDialog(Packaging existing) {
        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_packaging, null);
        Spinner spHarvest = dv.findViewById(R.id.sp_harvest);
        EditText etBaleCount = dv.findViewById(R.id.et_bale_count);
        EditText etBaleWeight = dv.findViewById(R.id.et_bale_weight);
        Spinner spPackType = dv.findViewById(R.id.sp_pack_type);
        Spinner spWrapLayers = dv.findViewById(R.id.sp_wrap_layers);
        EditText etWrapColour = dv.findViewById(R.id.et_wrap_colour);
        EditText etWrapCost = dv.findViewById(R.id.et_wrap_cost);
        EditText etDate = dv.findViewById(R.id.et_date);
        EditText etStorage = dv.findViewById(R.id.et_storage);
        EditText etContractor = dv.findViewById(R.id.et_contractor);
        EditText etContractorCost = dv.findViewById(R.id.et_contractor_cost);
        EditText etNotes = dv.findViewById(R.id.et_notes);

        List<Harvest> harvests = db.harvestDao().getAll();
        List<String> harvestLabels = new ArrayList<>();
        harvestLabels.add("-- Select Harvest --");
        for (Harvest h : harvests) harvestLabels.add(h.season + " - " + h.silagType + " (" + h.yieldTons + "t)");
        spHarvest.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, harvestLabels));

        String[] packTypes = {"Round Bale", "Square Bale", "Clamp/Pit", "Bag Silage"};
        spPackType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, packTypes));
        String[] layers = {"4 Layers", "6 Layers", "8 Layers"};
        spWrapLayers.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, layers));

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (existing == null) { etDate.setText(today); }
        else {
            etBaleCount.setText(String.valueOf(existing.baleCount));
            etBaleWeight.setText(String.valueOf(existing.baleWeightKg));
            etWrapColour.setText(existing.wrapColour);
            etWrapCost.setText(String.valueOf(existing.wrapCostPerBale));
            etDate.setText(existing.packagingDate);
            etStorage.setText(existing.storageLocation);
            etContractor.setText(existing.contractor);
            etContractorCost.setText(String.valueOf(existing.contractorCost));
            etNotes.setText(existing.notes);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Record Packaging" : "Edit Packaging")
                .setView(dv)
                .setPositiveButton("Save", (d, w) -> {
                    int hIdx = spHarvest.getSelectedItemPosition();
                    if (hIdx == 0) { Toast.makeText(requireContext(), "Select a harvest", Toast.LENGTH_SHORT).show(); return; }
                    Harvest h = harvests.get(hIdx - 1);
                    int baleCount = 0;
                    double baleWeight = 0, wrapCost = 0, contractorCost = 0;
                    try { baleCount = Integer.parseInt(etBaleCount.getText().toString()); } catch (Exception ignored) {}
                    try { baleWeight = Double.parseDouble(etBaleWeight.getText().toString()); } catch (Exception ignored) {}
                    try { wrapCost = Double.parseDouble(etWrapCost.getText().toString()); } catch (Exception ignored) {}
                    try { contractorCost = Double.parseDouble(etContractorCost.getText().toString()); } catch (Exception ignored) {}
                    if (existing == null) {
                        db.packagingDao().insert(new Packaging(h.id, h.season, h.silagType,
                                baleCount, baleWeight, spPackType.getSelectedItem().toString(),
                                spWrapLayers.getSelectedItem().toString(),
                                etWrapColour.getText().toString().trim(), wrapCost,
                                etDate.getText().toString().trim(),
                                etStorage.getText().toString().trim(),
                                etContractor.getText().toString().trim(), contractorCost,
                                etNotes.getText().toString().trim()));
                        Toast.makeText(requireContext(), "Packaging recorded", Toast.LENGTH_SHORT).show();
                    } else {
                        existing.harvestId = h.id; existing.harvestSeason = h.season;
                        existing.baleCount = baleCount; existing.baleWeightKg = baleWeight;
                        existing.totalWeightTons = (baleCount * baleWeight) / 1000.0;
                        existing.packagingType = spPackType.getSelectedItem().toString();
                        existing.wrapLayers = spWrapLayers.getSelectedItem().toString();
                        existing.wrapColour = etWrapColour.getText().toString().trim();
                        existing.wrapCostPerBale = wrapCost; existing.totalWrapCost = baleCount * wrapCost;
                        existing.packagingDate = etDate.getText().toString().trim();
                        existing.storageLocation = etStorage.getText().toString().trim();
                        existing.contractor = etContractor.getText().toString().trim();
                        existing.contractorCost = contractorCost; existing.notes = etNotes.getText().toString().trim();
                        db.packagingDao().update(existing);
                        Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show();
                    }
                    loadData();
                })
                .setNegativeButton("Cancel", null).show();
    }

    @Override public void onEdit(Packaging p) { showDialog(p); }
    @Override
    public void onDelete(Packaging p) {
        new AlertDialog.Builder(requireContext()).setTitle("Delete Packaging")
                .setMessage("Delete this packaging record?")
                .setPositiveButton("Delete", (d, w) -> { db.packagingDao().delete(p); loadData(); })
                .setNegativeButton("Cancel", null).show();
    }
}
