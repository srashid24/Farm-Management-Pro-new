package com.silageproerp.ui.lands;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.silageproerp.adapters.LandAdapter;
import com.silageproerp.database.AppDatabase;
import com.silageproerp.database.entities.Farmer;
import com.silageproerp.database.entities.Land;

import java.util.ArrayList;
import java.util.List;

public class LandsFragment extends Fragment implements LandAdapter.OnLandActionListener {

    private AppDatabase db;
    private LandAdapter adapter;
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

        EditText etSearch = view.findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) { loadData(s.toString()); }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        view.findViewById(R.id.fab_add).setOnClickListener(v -> showAddDialog(null));
        loadData("");
    }

    private void loadData(String query) {
        List<Land> list = query.isEmpty() ? db.landDao().getAll() : db.landDao().search(query);
        if (adapter == null) { adapter = new LandAdapter(list, this); recyclerView.setAdapter(adapter); }
        else adapter.updateList(list);
    }

    private void showAddDialog(Land existing) {
        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_land, null);
        Spinner spFarmer = dv.findViewById(R.id.sp_farmer);
        EditText etBlockId = dv.findViewById(R.id.et_block_id);
        EditText etFieldName = dv.findViewById(R.id.et_field_name);
        EditText etSizeHa = dv.findViewById(R.id.et_size);
        EditText etSizeAcres = dv.findViewById(R.id.et_size_acres);
        EditText etLocation = dv.findViewById(R.id.et_location);
        EditText etGps = dv.findViewById(R.id.et_gps);
        EditText etSoilType = dv.findViewById(R.id.et_soil_type);
        EditText etCropType = dv.findViewById(R.id.et_crop_type);
        Spinner spContractType = dv.findViewById(R.id.sp_contract_type);
        EditText etLandowner = dv.findViewById(R.id.et_landowner);
        EditText etContractCost = dv.findViewById(R.id.et_contract_cost);
        EditText etContractStart = dv.findViewById(R.id.et_contract_start);
        EditText etContractEnd = dv.findViewById(R.id.et_contract_end);
        EditText etNotes = dv.findViewById(R.id.et_notes);

        // Auto convert ha <-> acres
        etSizeHa.addTextChangedListener(new TextWatcher() {
            boolean updating = false;
            public void afterTextChanged(Editable s) {
                if (updating) return;
                updating = true;
                try { double ha = Double.parseDouble(s.toString()); etSizeAcres.setText(String.format("%.4f", ha * 2.47105)); }
                catch (Exception ignored) {}
                updating = false;
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        etSizeAcres.addTextChangedListener(new TextWatcher() {
            boolean updating = false;
            public void afterTextChanged(Editable s) {
                if (updating) return;
                updating = true;
                try { double acres = Double.parseDouble(s.toString()); etSizeHa.setText(String.format("%.4f", acres / 2.47105)); }
                catch (Exception ignored) {}
                updating = false;
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        List<Farmer> farmers = db.farmerDao().getAll();
        List<String> farmerNames = new ArrayList<>();
        farmerNames.add("-- Select Farmer --");
        for (Farmer f : farmers) farmerNames.add(f.name);
        spFarmer.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, farmerNames));

        String[] contractTypes = {"Owned", "Leased", "Rented", "Communal", "Other"};
        spContractType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, contractTypes));

        if (existing != null) {
            etBlockId.setText(existing.blockId);
            etFieldName.setText(existing.fieldName);
            etSizeHa.setText(String.valueOf(existing.sizeHectares));
            etSizeAcres.setText(String.format("%.4f", existing.sizeAcres));
            etLocation.setText(existing.location);
            etGps.setText(existing.gpsCoordinates);
            etSoilType.setText(existing.soilType);
            etCropType.setText(existing.cropType);
            etLandowner.setText(existing.landownerName);
            etContractCost.setText(String.valueOf(existing.contractCost));
            etContractStart.setText(existing.contractStartDate);
            etContractEnd.setText(existing.contractEndDate);
            etNotes.setText(existing.notes);
            for (int i = 0; i < farmers.size(); i++) {
                if (farmers.get(i).id == existing.farmerId) { spFarmer.setSelection(i + 1); break; }
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Add Field / Block" : "Edit Field / Block")
                .setView(dv)
                .setPositiveButton("Save", (d, w) -> {
                    String fieldName = etFieldName.getText().toString().trim();
                    int selIdx = spFarmer.getSelectedItemPosition();
                    if (fieldName.isEmpty() || selIdx == 0) {
                        Toast.makeText(requireContext(), "Field name and farmer required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int farmerId = farmers.get(selIdx - 1).id;
                    double ha = 0, contractCost = 0;
                    try { ha = Double.parseDouble(etSizeHa.getText().toString()); } catch (Exception ignored) {}
                    try { contractCost = Double.parseDouble(etContractCost.getText().toString()); } catch (Exception ignored) {}

                    if (existing == null) {
                        db.landDao().insert(new Land(farmerId,
                                etBlockId.getText().toString().trim(),
                                fieldName, ha,
                                etLocation.getText().toString().trim(),
                                etGps.getText().toString().trim(),
                                etSoilType.getText().toString().trim(),
                                etCropType.getText().toString().trim(),
                                spContractType.getSelectedItem().toString(),
                                etLandowner.getText().toString().trim(),
                                contractCost,
                                etContractStart.getText().toString().trim(),
                                etContractEnd.getText().toString().trim(),
                                etNotes.getText().toString().trim()));
                        Toast.makeText(requireContext(), "Field added", Toast.LENGTH_SHORT).show();
                    } else {
                        existing.farmerId = farmerId;
                        existing.blockId = etBlockId.getText().toString().trim();
                        existing.fieldName = fieldName;
                        existing.sizeHectares = ha;
                        existing.sizeAcres = ha * 2.47105;
                        existing.location = etLocation.getText().toString().trim();
                        existing.gpsCoordinates = etGps.getText().toString().trim();
                        existing.soilType = etSoilType.getText().toString().trim();
                        existing.cropType = etCropType.getText().toString().trim();
                        existing.contractType = spContractType.getSelectedItem().toString();
                        existing.landownerName = etLandowner.getText().toString().trim();
                        existing.contractCost = contractCost;
                        existing.contractStartDate = etContractStart.getText().toString().trim();
                        existing.contractEndDate = etContractEnd.getText().toString().trim();
                        existing.notes = etNotes.getText().toString().trim();
                        db.landDao().update(existing);
                        Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show();
                    }
                    loadData("");
                })
                .setNegativeButton("Cancel", null).show();
    }

    @Override public void onEdit(Land land) { showAddDialog(land); }
    @Override
    public void onDelete(Land land) {
        new AlertDialog.Builder(requireContext()).setTitle("Delete Field")
                .setMessage("Delete \"" + land.fieldName + "\"?")
                .setPositiveButton("Delete", (d, w) -> { db.landDao().delete(land); loadData(""); })
                .setNegativeButton("Cancel", null).show();
    }
}
