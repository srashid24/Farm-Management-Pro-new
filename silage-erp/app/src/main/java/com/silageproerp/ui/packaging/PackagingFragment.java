package com.silageproerp.ui.packaging;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.silageproerp.R;
import com.silageproerp.adapters.PackagingAdapter;
import com.silageproerp.database.AppDatabase;
import com.silageproerp.database.entities.Harvest;
import com.silageproerp.database.entities.Packaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PackagingFragment extends Fragment implements PackagingAdapter.PackagingListener {

    private AppDatabase db;
    private PackagingAdapter adapter;
    private RecyclerView recyclerView;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        view.findViewById(R.id.fab_add).setOnClickListener(v -> showAddDialog(null));
        loadData();
    }

    private void loadData() {
        List<Packaging> list = db.packagingDao().getAll();
        if (adapter == null) { adapter = new PackagingAdapter(list, this); recyclerView.setAdapter(adapter); }
        else adapter.updateData(list);
    }

    @Override public void onEdit(Packaging p) { showAddDialog(p); }

    @Override
    public void onDelete(Packaging p) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Packaging Record")
                .setMessage("Delete this packaging record?")
                .setPositiveButton("Delete", (d, w) -> { db.packagingDao().delete(p); loadData(); })
                .setNegativeButton("Cancel", null).show();
    }

    private void showAddDialog(Packaging existing) {
        View dv = LayoutInflater.from(getContext()).inflate(R.layout.dialog_packaging, null);
        Spinner  spHarvest    = dv.findViewById(R.id.sp_harvest);
        EditText etBales      = dv.findViewById(R.id.et_bale_count);
        EditText etBaleWeight = dv.findViewById(R.id.et_bale_weight);
        Spinner  spPkgType    = dv.findViewById(R.id.sp_packaging_type);
        Spinner  spWrapLayers = dv.findViewById(R.id.sp_wrap_layers);
        EditText etWrapColour = dv.findViewById(R.id.et_wrap_colour);
        EditText etWrapCost   = dv.findViewById(R.id.et_wrap_cost_per_bale);
        EditText etDate       = dv.findViewById(R.id.et_date);
        EditText etStorage    = dv.findViewById(R.id.et_storage_location);
        EditText etContractor = dv.findViewById(R.id.et_contractor);
        EditText etContractorCost = dv.findViewById(R.id.et_contractor_cost);
        EditText etNotes      = dv.findViewById(R.id.et_notes);

        List<Harvest> harvests = db.harvestDao().getAll();
        String[] hNames = new String[harvests.size() + 1];
        hNames[0] = "— Select Harvest —";
        for (int i = 0; i < harvests.size(); i++)
            hNames[i+1] = harvests.get(i).season + " | " + harvests.get(i).silagType;
        spHarvest.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, hNames));

        String[] pkgTypes = {"Round Bale", "Square Bale", "Silage Pit", "Silage Bag", "Other"};
        String[] layers = {"4 Layers", "6 Layers", "8 Layers"};
        spPkgType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, pkgTypes));
        spWrapLayers.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, layers));
        etDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        if (existing != null) {
            etBales.setText(String.valueOf(existing.baleCount));
            etBaleWeight.setText(String.valueOf(existing.baleWeightKg));
            etWrapColour.setText(existing.wrapColour);
            etWrapCost.setText(String.valueOf(existing.wrapCostPerBale));
            etDate.setText(existing.packagingDate);
            etStorage.setText(existing.storageLocation);
            etContractor.setText(existing.contractor);
            etContractorCost.setText(String.valueOf(existing.contractorCost));
            etNotes.setText(existing.notes);
            for (int i = 0; i < harvests.size(); i++) {
                if (harvests.get(i).id == existing.harvestId) { spHarvest.setSelection(i+1); break; }
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Add Packaging Record" : "Edit Packaging")
                .setView(dv)
                .setPositiveButton("Save", (d, w) -> {
                    int hi = spHarvest.getSelectedItemPosition();
                    if (hi == 0) return;
                    Harvest h = harvests.get(hi - 1);
                    int bales = 0; double baleWt = 0, wrapCost = 0, contrCost = 0;
                    try { bales = Integer.parseInt(etBales.getText().toString()); } catch (NumberFormatException ignored) {}
                    try { baleWt = Double.parseDouble(etBaleWeight.getText().toString()); } catch (NumberFormatException ignored) {}
                    try { wrapCost = Double.parseDouble(etWrapCost.getText().toString()); } catch (NumberFormatException ignored) {}
                    try { contrCost = Double.parseDouble(etContractorCost.getText().toString()); } catch (NumberFormatException ignored) {}

                    Packaging pkg = new Packaging(h.id, h.season, h.silagType,
                            bales, baleWt, spPkgType.getSelectedItem().toString(),
                            spWrapLayers.getSelectedItem().toString(),
                            etWrapColour.getText().toString().trim(), wrapCost,
                            etDate.getText().toString().trim(),
                            etStorage.getText().toString().trim(),
                            etContractor.getText().toString().trim(), contrCost,
                            etNotes.getText().toString().trim());
                    if (existing == null) { db.packagingDao().insert(pkg); }
                    else { pkg.id = existing.id; db.packagingDao().update(pkg); }
                    loadData();
                })
                .setNegativeButton("Cancel", null).show();
    }
}
