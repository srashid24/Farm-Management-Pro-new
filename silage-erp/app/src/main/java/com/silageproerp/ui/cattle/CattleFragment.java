package com.silageproerp.ui.cattle;

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
import com.silageproerp.adapters.CattleAdapter;
import com.silageproerp.database.AppDatabase;
import com.silageproerp.database.entities.Cattle;
import com.silageproerp.database.entities.Farmer;

import java.util.List;

public class CattleFragment extends Fragment implements CattleAdapter.CattleListener {

    private AppDatabase db;
    private CattleAdapter adapter;
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
        List<Cattle> cattle = db.cattleDao().getAll();
        if (adapter == null) { adapter = new CattleAdapter(cattle, this); recyclerView.setAdapter(adapter); }
        else adapter.updateData(cattle);
    }

    @Override public void onEdit(Cattle c) { showAddDialog(c); }

    @Override
    public void onDelete(Cattle c) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Cattle Record")
                .setMessage("Delete cattle tag " + c.tagNumber + "?")
                .setPositiveButton("Delete", (d, w) -> { db.cattleDao().delete(c); loadData(); })
                .setNegativeButton("Cancel", null).show();
    }

    private void showAddDialog(Cattle existing) {
        View dv = LayoutInflater.from(getContext()).inflate(R.layout.dialog_cattle, null);
        Spinner  spOwner    = dv.findViewById(R.id.sp_owner);
        EditText etTag      = dv.findViewById(R.id.et_tag);
        EditText etName     = dv.findViewById(R.id.et_name);
        Spinner  spBreed    = dv.findViewById(R.id.sp_breed);
        Spinner  spGender   = dv.findViewById(R.id.sp_gender);
        EditText etDob      = dv.findViewById(R.id.et_dob);
        EditText etWeight   = dv.findViewById(R.id.et_weight);
        EditText etColor    = dv.findViewById(R.id.et_color);
        Spinner  spStatus   = dv.findViewById(R.id.sp_status);
        Spinner  spSource   = dv.findViewById(R.id.sp_source);
        EditText etPurchasePrice = dv.findViewById(R.id.et_purchase_price);
        EditText etPurchaseDate  = dv.findViewById(R.id.et_purchase_date);
        EditText etNotes    = dv.findViewById(R.id.et_notes);

        List<Farmer> farmers = db.farmerDao().getAll();
        String[] fNames = new String[farmers.size() + 1];
        fNames[0] = "— Select Owner —";
        for (int i = 0; i < farmers.size(); i++) fNames[i+1] = farmers.get(i).name;
        spOwner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, fNames));

        String[] breeds = {"Brahman", "Nguni", "Bonsmara", "Simmentaler", "Angus", "Holstein", "Hereford", "Afrikaner", "Mixed", "Other"};
        String[] genders = {"Bull", "Cow", "Heifer", "Steer", "Calf (M)", "Calf (F)"};
        String[] statuses = {"Active", "Fattening", "Sold", "Deceased"};
        String[] sources = {"Born on Farm", "Purchased", "Auction", "Other"};

        spBreed.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, breeds));
        spGender.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, genders));
        spStatus.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, statuses));
        spSource.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, sources));

        if (existing != null) {
            etTag.setText(existing.tagNumber); etName.setText(existing.name);
            etDob.setText(existing.dateOfBirth); etWeight.setText(String.valueOf(existing.currentWeightKg));
            etColor.setText(existing.color); etPurchasePrice.setText(String.valueOf(existing.purchasePrice));
            etPurchaseDate.setText(existing.purchaseDate); etNotes.setText(existing.notes);
            for (int i = 0; i < farmers.size(); i++) {
                if (farmers.get(i).id == existing.ownerId) { spOwner.setSelection(i+1); break; }
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Add Cattle" : "Edit Cattle")
                .setView(dv)
                .setPositiveButton("Save", (d, w) -> {
                    String tag = etTag.getText().toString().trim();
                    if (tag.isEmpty()) { etTag.setError("Required"); return; }
                    int fi = spOwner.getSelectedItemPosition();
                    int ownerId = fi > 0 ? farmers.get(fi-1).id : 0;
                    String ownerName = fi > 0 ? farmers.get(fi-1).name : "";
                    double weight = 0, price = 0;
                    try { weight = Double.parseDouble(etWeight.getText().toString()); } catch (NumberFormatException ignored) {}
                    try { price = Double.parseDouble(etPurchasePrice.getText().toString()); } catch (NumberFormatException ignored) {}

                    Cattle c = new Cattle(ownerId, ownerName, tag,
                            etName.getText().toString().trim(),
                            spBreed.getSelectedItem().toString(),
                            spGender.getSelectedItem().toString(),
                            etDob.getText().toString().trim(), weight,
                            spStatus.getSelectedItem().toString(),
                            etColor.getText().toString().trim(),
                            spSource.getSelectedItem().toString(),
                            price, etPurchaseDate.getText().toString().trim(),
                            etNotes.getText().toString().trim());
                    if (existing == null) { db.cattleDao().insert(c); }
                    else { c.id = existing.id; db.cattleDao().update(c); }
                    loadData();
                })
                .setNegativeButton("Cancel", null).show();
    }
}
