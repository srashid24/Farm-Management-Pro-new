package com.silageproerp.ui.farmers;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.silageproerp.R;
import com.silageproerp.adapters.FarmerAdapter;
import com.silageproerp.database.AppDatabase;
import com.silageproerp.database.entities.Farmer;

import java.util.List;

public class FarmersFragment extends Fragment implements FarmerAdapter.OnFarmerActionListener {

    private AppDatabase db;
    private FarmerAdapter adapter;
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

        FloatingActionButton fab = view.findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> showAddDialog(null));

        loadData("");
    }

    private void loadData(String query) {
        List<Farmer> list = query.isEmpty()
                ? db.farmerDao().getAll()
                : db.farmerDao().search(query);
        if (adapter == null) {
            adapter = new FarmerAdapter(list, this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(list);
        }
    }

    private void showAddDialog(Farmer existing) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_farmer, null);
        EditText etName = dialogView.findViewById(R.id.et_name);
        EditText etPhone = dialogView.findViewById(R.id.et_phone);
        EditText etAddress = dialogView.findViewById(R.id.et_address);
        EditText etNationalId = dialogView.findViewById(R.id.et_national_id);
        EditText etEmail = dialogView.findViewById(R.id.et_email);
        EditText etNotes = dialogView.findViewById(R.id.et_notes);

        if (existing != null) {
            etName.setText(existing.name);
            etPhone.setText(existing.phone);
            etAddress.setText(existing.address);
            etNationalId.setText(existing.nationalId);
            etEmail.setText(existing.email);
            etNotes.setText(existing.notes);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Add Farmer" : "Edit Farmer")
                .setView(dialogView)
                .setPositiveButton("Save", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(requireContext(), "Name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (existing == null) {
                        Farmer f = new Farmer(name,
                                etPhone.getText().toString().trim(),
                                etAddress.getText().toString().trim(),
                                etNationalId.getText().toString().trim(),
                                etEmail.getText().toString().trim(),
                                etNotes.getText().toString().trim());
                        db.farmerDao().insert(f);
                        Toast.makeText(requireContext(), "Farmer added", Toast.LENGTH_SHORT).show();
                    } else {
                        existing.name = name;
                        existing.phone = etPhone.getText().toString().trim();
                        existing.address = etAddress.getText().toString().trim();
                        existing.nationalId = etNationalId.getText().toString().trim();
                        existing.email = etEmail.getText().toString().trim();
                        existing.notes = etNotes.getText().toString().trim();
                        db.farmerDao().update(existing);
                        Toast.makeText(requireContext(), "Farmer updated", Toast.LENGTH_SHORT).show();
                    }
                    loadData("");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEdit(Farmer farmer) {
        showAddDialog(farmer);
    }

    @Override
    public void onDelete(Farmer farmer) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Farmer")
                .setMessage("Delete " + farmer.name + "? This will also remove their lands.")
                .setPositiveButton("Delete", (d, w) -> {
                    db.farmerDao().delete(farmer);
                    loadData("");
                    Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
