package com.silageproerp.ui.buyers;

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
import com.silageproerp.adapters.BuyerAdapter;
import com.silageproerp.database.AppDatabase;
import com.silageproerp.database.entities.Buyer;

import java.util.List;

public class BuyersFragment extends Fragment implements BuyerAdapter.OnBuyerActionListener {

    private AppDatabase db;
    private BuyerAdapter adapter;
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
        List<Buyer> list = query.isEmpty() ? db.buyerDao().getAll() : db.buyerDao().search(query);
        if (adapter == null) { adapter = new BuyerAdapter(list, this); recyclerView.setAdapter(adapter); }
        else adapter.updateList(list);
    }

    private void showDialog(Buyer existing) {
        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_buyer, null);
        EditText etName = dv.findViewById(R.id.et_name);
        EditText etPhone = dv.findViewById(R.id.et_phone);
        EditText etAddress = dv.findViewById(R.id.et_address);
        EditText etCompany = dv.findViewById(R.id.et_company);
        EditText etEmail = dv.findViewById(R.id.et_email);
        Spinner spType = dv.findViewById(R.id.sp_buyer_type);
        EditText etNotes = dv.findViewById(R.id.et_notes);

        String[] types = {"Individual", "Farm", "Feedlot", "Dairy", "Other"};
        spType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, types));

        if (existing != null) {
            etName.setText(existing.name);
            etPhone.setText(existing.phone);
            etAddress.setText(existing.address);
            etCompany.setText(existing.company);
            etEmail.setText(existing.email);
            etNotes.setText(existing.notes);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Add Buyer" : "Edit Buyer")
                .setView(dv)
                .setPositiveButton("Save", (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) { Toast.makeText(requireContext(), "Name required", Toast.LENGTH_SHORT).show(); return; }
                    if (existing == null) {
                        db.buyerDao().insert(new Buyer(name, etPhone.getText().toString().trim(),
                                etAddress.getText().toString().trim(), etCompany.getText().toString().trim(),
                                etEmail.getText().toString().trim(), spType.getSelectedItem().toString(),
                                etNotes.getText().toString().trim()));
                        Toast.makeText(requireContext(), "Buyer added", Toast.LENGTH_SHORT).show();
                    } else {
                        existing.name = name; existing.phone = etPhone.getText().toString().trim();
                        existing.address = etAddress.getText().toString().trim();
                        existing.company = etCompany.getText().toString().trim();
                        existing.email = etEmail.getText().toString().trim();
                        existing.buyerType = spType.getSelectedItem().toString();
                        existing.notes = etNotes.getText().toString().trim();
                        db.buyerDao().update(existing);
                        Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show();
                    }
                    loadData("");
                })
                .setNegativeButton("Cancel", null).show();
    }

    @Override public void onEdit(Buyer buyer) { showDialog(buyer); }
    @Override
    public void onDelete(Buyer buyer) {
        new AlertDialog.Builder(requireContext()).setTitle("Delete Buyer")
                .setMessage("Delete " + buyer.name + "?")
                .setPositiveButton("Delete", (d, w) -> { db.buyerDao().delete(buyer); loadData(""); })
                .setNegativeButton("Cancel", null).show();
    }
}
