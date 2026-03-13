package com.silageproerp.ui.inventory;

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
import com.silageproerp.adapters.InventoryAdapter;
import com.silageproerp.database.AppDatabase;
import com.silageproerp.database.entities.InventoryItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InventoryFragment extends Fragment implements InventoryAdapter.OnInventoryActionListener {

    private AppDatabase db;
    private InventoryAdapter adapter;
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
        List<InventoryItem> list = query.isEmpty()
                ? db.inventoryDao().getAll()
                : db.inventoryDao().search(query);
        if (adapter == null) {
            adapter = new InventoryAdapter(list, this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateList(list);
        }
    }

    private void showAddDialog(InventoryItem existing) {
        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_inventory, null);
        EditText etName = dv.findViewById(R.id.et_item_name);
        Spinner spCategory = dv.findViewById(R.id.sp_category);
        EditText etQty = dv.findViewById(R.id.et_quantity);
        Spinner spUnit = dv.findViewById(R.id.sp_unit);
        EditText etUnitCost = dv.findViewById(R.id.et_unit_cost);
        EditText etLocation = dv.findViewById(R.id.et_location);
        EditText etSupplier = dv.findViewById(R.id.et_supplier);
        EditText etMinStock = dv.findViewById(R.id.et_min_stock);
        EditText etNotes = dv.findViewById(R.id.et_notes);

        String[] categories = {"Silage", "Wrap", "Seeds", "Fertilizer", "Fuel", "Tools", "Other"};
        spCategory.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories));
        String[] units = {"Tons", "Bales", "Liters", "Kg", "Bags", "Pieces", "Rolls"};
        spUnit.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, units));

        if (existing != null) {
            etName.setText(existing.itemName);
            etQty.setText(String.valueOf(existing.quantity));
            etUnitCost.setText(String.valueOf(existing.unitCost));
            etLocation.setText(existing.storageLocation);
            etSupplier.setText(existing.supplierName);
            etMinStock.setText(String.valueOf(existing.minimumStock));
            etNotes.setText(existing.notes);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Add Inventory Item" : "Edit Item")
                .setView(dv)
                .setPositiveButton("Save", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) { Toast.makeText(requireContext(), "Name required", Toast.LENGTH_SHORT).show(); return; }
                    double qty = 0, unitCost = 0, minStock = 0;
                    try { qty = Double.parseDouble(etQty.getText().toString()); } catch (Exception ignored) {}
                    try { unitCost = Double.parseDouble(etUnitCost.getText().toString()); } catch (Exception ignored) {}
                    try { minStock = Double.parseDouble(etMinStock.getText().toString()); } catch (Exception ignored) {}
                    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    if (existing == null) {
                        db.inventoryDao().insert(new InventoryItem(name,
                                spCategory.getSelectedItem().toString(), qty,
                                spUnit.getSelectedItem().toString(), unitCost,
                                etLocation.getText().toString().trim(),
                                etSupplier.getText().toString().trim(),
                                date, minStock, etNotes.getText().toString().trim()));
                        Toast.makeText(requireContext(), "Item added", Toast.LENGTH_SHORT).show();
                    } else {
                        existing.itemName = name;
                        existing.category = spCategory.getSelectedItem().toString();
                        existing.quantity = qty;
                        existing.unit = spUnit.getSelectedItem().toString();
                        existing.unitCost = unitCost;
                        existing.totalValue = qty * unitCost;
                        existing.storageLocation = etLocation.getText().toString().trim();
                        existing.supplierName = etSupplier.getText().toString().trim();
                        existing.minimumStock = minStock;
                        existing.notes = etNotes.getText().toString().trim();
                        db.inventoryDao().update(existing);
                        Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show();
                    }
                    loadData("");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEdit(InventoryItem item) { showAddDialog(item); }

    @Override
    public void onDelete(InventoryItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Item")
                .setMessage("Delete \"" + item.itemName + "\"?")
                .setPositiveButton("Delete", (d, w) -> { db.inventoryDao().delete(item); loadData(""); })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
