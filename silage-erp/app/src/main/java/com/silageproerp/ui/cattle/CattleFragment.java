package com.silageproerp.ui.cattle;

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
import com.silageproerp.adapters.CattleAdapter;
import com.silageproerp.database.AppDatabase;
import com.silageproerp.database.entities.Cattle;
import com.silageproerp.database.entities.Farmer;

import java.util.ArrayList;
import java.util.List;

public class CattleFragment extends Fragment implements CattleAdapter.OnCattleActionListener {

    private AppDatabase db;
    private CattleAdapter adapter;
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

        view.findViewById(R.id.fab_add).setOnClickListener(v -> showDialog(null));
        loadData("");
    }

    private void loadData(String query) {
        List<Cattle> list = query.isEmpty() ? db.cattleDao().getAll() : db.cattleDao().search(query);
        if (adapter == null) { adapter = new CattleAdapter(list, this); recyclerView.setAdapter(adapter); }
        else adapter.updateList(list);
    }

    private void showDialog(Cattle existing) {
        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_cattle, null);
        Spinner spOwner = dv.findViewById(R.id.sp_owner);
        EditText etTag = dv.findViewById(R.id.et_tag);
        EditText etName = dv.findViewById(R.id.et_name);
        EditText etBreed = dv.findViewById(R.id.et_breed);
        Spinner spGender = dv.findViewById(R.id.sp_gender);
        EditText etDob = dv.findViewById(R.id.et_dob);
        EditText etWeight = dv.findViewById(R.id.et_weight);
        Spinner spStatus = dv.findViewById(R.id.sp_status);
        EditText etColor = dv.findViewById(R.id.et_color);
        Spinner spSource = dv.findViewById(R.id.sp_source);
        EditText etPurchasePrice = dv.findViewById(R.id.et_purchase_price);
        EditText etPurchaseDate = dv.findViewById(R.id.et_purchase_date);
        EditText etNotes = dv.findViewById(R.id.et_notes);

        List<Farmer> farmers = db.farmerDao().getAll();
        List<String> names = new ArrayList<>();
        names.add("-- Select Owner --");
        for (Farmer f : farmers) names.add(f.name);
        spOwner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, names));

        String[] genders = {"Bull", "Cow", "Heifer", "Steer", "Calf"};
        spGender.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, genders));
        String[] statuses = {"Active", "Fattening", "Sold", "Deceased"};
        spStatus.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, statuses));
        String[] sources = {"Born on Farm", "Purchased"};
        spSource.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, sources));

        if (existing != null) {
            etTag.setText(existing.tagNumber); etName.setText(existing.name);
            etBreed.setText(existing.breed); etDob.setText(existing.dateOfBirth);
            etWeight.setText(String.valueOf(existing.currentWeightKg));
            etColor.setText(existing.color);
            etPurchasePrice.setText(String.valueOf(existing.purchasePrice));
            etPurchaseDate.setText(existing.purchaseDate); etNotes.setText(existing.notes);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Add Cattle" : "Edit Cattle")
                .setView(dv)
                .setPositiveButton("Save", (d, w) -> {
                    String tag = etTag.getText().toString().trim();
                    if (tag.isEmpty()) { Toast.makeText(requireContext(), "Tag number required", Toast.LENGTH_SHORT).show(); return; }
                    int ownerIdx = spOwner.getSelectedItemPosition();
                    int ownerId = ownerIdx > 0 ? farmers.get(ownerIdx - 1).id : 0;
                    String ownerName = ownerIdx > 0 ? farmers.get(ownerIdx - 1).name : "";
                    double weight = 0, price = 0;
                    try { weight = Double.parseDouble(etWeight.getText().toString()); } catch (Exception ignored) {}
                    try { price = Double.parseDouble(etPurchasePrice.getText().toString()); } catch (Exception ignored) {}
                    if (existing == null) {
                        db.cattleDao().insert(new Cattle(ownerId, ownerName, tag,
                                etName.getText().toString().trim(), etBreed.getText().toString().trim(),
                                spGender.getSelectedItem().toString(), etDob.getText().toString().trim(),
                                weight, spStatus.getSelectedItem().toString(),
                                etColor.getText().toString().trim(), spSource.getSelectedItem().toString(),
                                price, etPurchaseDate.getText().toString().trim(),
                                etNotes.getText().toString().trim()));
                        Toast.makeText(requireContext(), "Cattle added", Toast.LENGTH_SHORT).show();
                    } else {
                        existing.ownerId = ownerId; existing.ownerName = ownerName;
                        existing.tagNumber = tag; existing.name = etName.getText().toString().trim();
                        existing.breed = etBreed.getText().toString().trim();
                        existing.gender = spGender.getSelectedItem().toString();
                        existing.dateOfBirth = etDob.getText().toString().trim();
                        existing.currentWeightKg = weight;
                        existing.status = spStatus.getSelectedItem().toString();
                        existing.color = etColor.getText().toString().trim();
                        existing.source = spSource.getSelectedItem().toString();
                        existing.purchasePrice = price;
                        existing.purchaseDate = etPurchaseDate.getText().toString().trim();
                        existing.notes = etNotes.getText().toString().trim();
                        db.cattleDao().update(existing);
                        Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show();
                    }
                    loadData("");
                })
                .setNegativeButton("Cancel", null).show();
    }

    @Override public void onEdit(Cattle cattle) { showDialog(cattle); }
    @Override
    public void onDelete(Cattle cattle) {
        new AlertDialog.Builder(requireContext()).setTitle("Delete Cattle")
                .setMessage("Delete tag " + cattle.tagNumber + "?")
                .setPositiveButton("Delete", (d, w) -> { db.cattleDao().delete(cattle); loadData(""); })
                .setNegativeButton("Cancel", null).show();
    }
}
