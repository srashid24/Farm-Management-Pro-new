package com.silageproerp.ui.fattening;

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
import com.silageproerp.adapters.FatteningAdapter;
import com.silageproerp.database.AppDatabase;
import com.silageproerp.database.entities.Cattle;
import com.silageproerp.database.entities.Fattening;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FatteningFragment extends Fragment implements FatteningAdapter.FatteningListener {

    private AppDatabase db;
    private FatteningAdapter adapter;
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
        List<Fattening> list = db.fatteningDao().getAll();
        if (adapter == null) { adapter = new FatteningAdapter(list, this); recyclerView.setAdapter(adapter); }
        else adapter.updateData(list);
    }

    @Override public void onEdit(Fattening f) { showAddDialog(f); }

    @Override
    public void onDelete(Fattening f) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Fattening Record")
                .setMessage("Delete fattening record for " + f.cattleTag + "?")
                .setPositiveButton("Delete", (d, w) -> { db.fatteningDao().delete(f); loadData(); })
                .setNegativeButton("Cancel", null).show();
    }

    @Override
    public void onUpdateWeight(Fattening f) {
        View dv = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update_weight, null);
        EditText etWeight   = dv.findViewById(R.id.et_current_weight);
        EditText etFeedCost = dv.findViewById(R.id.et_feed_cost_added);
        etWeight.setText(String.valueOf(f.currentWeightKg));

        new AlertDialog.Builder(requireContext())
                .setTitle("Update Weight & Feed Cost")
                .setView(dv)
                .setPositiveButton("Update", (d, w) -> {
                    try { f.currentWeightKg = Double.parseDouble(etWeight.getText().toString()); } catch (NumberFormatException ignored) {}
                    try { f.totalFeedCostToDate += Double.parseDouble(etFeedCost.getText().toString()); } catch (NumberFormatException ignored) {}
                    db.fatteningDao().update(f);
                    loadData();
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void showAddDialog(Fattening existing) {
        View dv = LayoutInflater.from(getContext()).inflate(R.layout.dialog_fattening, null);
        Spinner  spCattle      = dv.findViewById(R.id.sp_cattle);
        EditText etStartDate   = dv.findViewById(R.id.et_start_date);
        EditText etStartWeight = dv.findViewById(R.id.et_start_weight);
        EditText etTargetWeight= dv.findViewById(R.id.et_target_weight);
        EditText etDailyFeed   = dv.findViewById(R.id.et_daily_feed_kg);
        Spinner  spFeedType    = dv.findViewById(R.id.sp_feed_type);
        EditText etNotes       = dv.findViewById(R.id.et_notes);

        List<Cattle> cattleList = db.cattleDao().getAll();
        String[] cNames = new String[cattleList.size() + 1];
        cNames[0] = "— Select Cattle —";
        for (int i = 0; i < cattleList.size(); i++)
            cNames[i+1] = cattleList.get(i).tagNumber + " - " + cattleList.get(i).name;
        spCattle.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, cNames));

        String[] feedTypes = {"Silage Only", "Grain + Silage", "TMR", "Pasture + Silage", "Other"};
        spFeedType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, feedTypes));
        etStartDate.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

        if (existing != null) {
            etStartDate.setText(existing.startDate);
            etStartWeight.setText(String.valueOf(existing.startWeightKg));
            etTargetWeight.setText(String.valueOf(existing.targetWeightKg));
            etDailyFeed.setText(String.valueOf(existing.dailyFeedKg));
            etNotes.setText(existing.notes);
            for (int i = 0; i < cattleList.size(); i++) {
                if (cattleList.get(i).id == existing.cattleId) { spCattle.setSelection(i+1); break; }
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Start Fattening Program" : "Edit Fattening")
                .setView(dv)
                .setPositiveButton("Save", (d, w) -> {
                    int ci = spCattle.getSelectedItemPosition();
                    if (ci == 0) return;
                    Cattle c = cattleList.get(ci - 1);
                    double sw = 0, tw = 0, df = 0;
                    try { sw = Double.parseDouble(etStartWeight.getText().toString()); } catch (NumberFormatException ignored) {}
                    try { tw = Double.parseDouble(etTargetWeight.getText().toString()); } catch (NumberFormatException ignored) {}
                    try { df = Double.parseDouble(etDailyFeed.getText().toString()); } catch (NumberFormatException ignored) {}

                    Fattening fat = new Fattening(c.id, c.tagNumber, c.name,
                            etStartDate.getText().toString().trim(),
                            sw, tw, df,
                            spFeedType.getSelectedItem().toString(),
                            etNotes.getText().toString().trim());
                    if (existing == null) { db.fatteningDao().insert(fat); }
                    else { fat.id = existing.id; fat.totalFeedCostToDate = existing.totalFeedCostToDate; db.fatteningDao().update(fat); }
                    loadData();
                })
                .setNegativeButton("Cancel", null).show();
    }
}
