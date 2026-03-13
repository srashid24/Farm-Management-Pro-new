package com.silageproerp.ui.costing;

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
import com.silageproerp.adapters.CostAdapter;
import com.silageproerp.database.AppDatabase;
import com.silageproerp.database.entities.CostEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CostingFragment extends Fragment implements CostAdapter.CostListener {

    private AppDatabase db;
    private CostAdapter adapter;
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
        List<CostEntry> entries = db.costEntryDao().getAll();
        if (adapter == null) { adapter = new CostAdapter(entries, this); recyclerView.setAdapter(adapter); }
        else adapter.updateData(entries);
    }

    @Override public void onEdit(CostEntry e) { showAddDialog(e); }

    @Override
    public void onDelete(CostEntry e) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Cost Entry")
                .setMessage("Delete \"" + e.description + "\"?")
                .setPositiveButton("Delete", (d, w) -> { db.costEntryDao().delete(e); loadData(); })
                .setNegativeButton("Cancel", null).show();
    }

    private void showAddDialog(CostEntry existing) {
        View dv = LayoutInflater.from(getContext()).inflate(R.layout.dialog_cost, null);
        Spinner  spCategory  = dv.findViewById(R.id.sp_category);
        EditText etDesc      = dv.findViewById(R.id.et_description);
        EditText etQty       = dv.findViewById(R.id.et_quantity);
        Spinner  spUnit      = dv.findViewById(R.id.sp_unit);
        EditText etUnitCost  = dv.findViewById(R.id.et_unit_cost);
        EditText etSeason    = dv.findViewById(R.id.et_season);
        EditText etDate      = dv.findViewById(R.id.et_date);
        EditText etSupplier  = dv.findViewById(R.id.et_supplier);
        EditText etNotes     = dv.findViewById(R.id.et_notes);

        String[] cats = {"Seeds", "Fertilizer", "Labour", "Machinery", "Fuel", "Wrap", "Transport", "Land Rent", "Chemicals", "Other"};
        String[] units = {"Kg", "Bags", "Tons", "Liters", "Hours", "Days", "Rolls", "Pieces", "Ha", "Other"};
        spCategory.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, cats));
        spUnit.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, units));
        etDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        if (existing != null) {
            etDesc.setText(existing.description);
            etQty.setText(String.valueOf(existing.quantity));
            etUnitCost.setText(String.valueOf(existing.unitCost));
            etSeason.setText(existing.season);
            etDate.setText(existing.date);
            etSupplier.setText(existing.supplierName);
            etNotes.setText(existing.notes);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Add Cost / Expense" : "Edit Cost Entry")
                .setView(dv)
                .setPositiveButton("Save", (d, w) -> {
                    double qty = 0, unitCost = 0;
                    try { qty = Double.parseDouble(etQty.getText().toString()); } catch (NumberFormatException ignored) {}
                    try { unitCost = Double.parseDouble(etUnitCost.getText().toString()); } catch (NumberFormatException ignored) {}
                    CostEntry entry = new CostEntry(
                            spCategory.getSelectedItem().toString(),
                            etDesc.getText().toString().trim(),
                            qty, spUnit.getSelectedItem().toString(), unitCost,
                            etSeason.getText().toString().trim(),
                            etDate.getText().toString().trim(),
                            "", "general",
                            etSupplier.getText().toString().trim(),
                            etNotes.getText().toString().trim());
                    if (existing == null) { db.costEntryDao().insert(entry); }
                    else { entry.id = existing.id; db.costEntryDao().update(entry); }
                    loadData();
                })
                .setNegativeButton("Cancel", null).show();
    }
}
