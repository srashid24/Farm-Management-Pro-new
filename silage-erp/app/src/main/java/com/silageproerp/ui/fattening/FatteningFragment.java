package com.silageproerp.ui.fattening;

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
import com.silageproerp.adapters.FatteningAdapter;
import com.silageproerp.database.AppDatabase;
import com.silageproerp.database.entities.Cattle;
import com.silageproerp.database.entities.Fattening;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FatteningFragment extends Fragment implements FatteningAdapter.OnFatteningActionListener {

    private AppDatabase db;
    private FatteningAdapter adapter;
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
        List<Fattening> list = db.fatteningDao().getAll();
        if (adapter == null) { adapter = new FatteningAdapter(list, this); recyclerView.setAdapter(adapter); }
        else adapter.updateList(list);
    }

    private void showDialog(Fattening existing) {
        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_fattening, null);
        Spinner spCattle = dv.findViewById(R.id.sp_cattle);
        EditText etStartDate = dv.findViewById(R.id.et_start_date);
        EditText etStartWeight = dv.findViewById(R.id.et_start_weight);
        EditText etTargetWeight = dv.findViewById(R.id.et_target_weight);
        EditText etDailyFeed = dv.findViewById(R.id.et_daily_feed);
        Spinner spFeedType = dv.findViewById(R.id.sp_feed_type);
        EditText etNotes = dv.findViewById(R.id.et_notes);

        List<Cattle> cattleList = db.cattleDao().getAll();
        List<String> tags = new ArrayList<>();
        tags.add("-- Select Cattle --");
        for (Cattle c : cattleList) tags.add(c.tagNumber + (c.name.isEmpty() ? "" : " (" + c.name + ")"));
        spCattle.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tags));

        String[] feedTypes = {"Silage", "Grain", "Mixed TMR", "Hay + Silage", "Other"};
        spFeedType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, feedTypes));

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (existing == null) { etStartDate.setText(today); }
        else {
            etStartDate.setText(existing.startDate);
            etStartWeight.setText(String.valueOf(existing.startWeightKg));
            etTargetWeight.setText(String.valueOf(existing.targetWeightKg));
            etDailyFeed.setText(String.valueOf(existing.dailyFeedKg));
            etNotes.setText(existing.notes);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Start Fattening Program" : "Edit Fattening")
                .setView(dv)
                .setPositiveButton("Save", (d, w) -> {
                    int idx = spCattle.getSelectedItemPosition();
                    if (idx == 0) { Toast.makeText(requireContext(), "Select cattle", Toast.LENGTH_SHORT).show(); return; }
                    Cattle cattle = cattleList.get(idx - 1);
                    double startW = 0, targetW = 0, dailyFeed = 0;
                    try { startW = Double.parseDouble(etStartWeight.getText().toString()); } catch (Exception ignored) {}
                    try { targetW = Double.parseDouble(etTargetWeight.getText().toString()); } catch (Exception ignored) {}
                    try { dailyFeed = Double.parseDouble(etDailyFeed.getText().toString()); } catch (Exception ignored) {}
                    if (existing == null) {
                        db.fatteningDao().insert(new Fattening(cattle.id, cattle.tagNumber, cattle.name,
                                etStartDate.getText().toString().trim(), startW, targetW, dailyFeed,
                                spFeedType.getSelectedItem().toString(), etNotes.getText().toString().trim()));
                        // Update cattle status
                        cattle.status = "Fattening";
                        db.cattleDao().update(cattle);
                        Toast.makeText(requireContext(), "Fattening program started", Toast.LENGTH_SHORT).show();
                    } else {
                        existing.cattleId = cattle.id; existing.cattleTag = cattle.tagNumber;
                        existing.startDate = etStartDate.getText().toString().trim();
                        existing.startWeightKg = startW; existing.targetWeightKg = targetW;
                        existing.dailyFeedKg = dailyFeed;
                        existing.feedType = spFeedType.getSelectedItem().toString();
                        existing.notes = etNotes.getText().toString().trim();
                        db.fatteningDao().update(existing);
                        Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show();
                    }
                    loadData();
                })
                .setNegativeButton("Cancel", null).show();
    }

    @Override public void onEdit(Fattening f) { showDialog(f); }
    @Override
    public void onDelete(Fattening f) {
        new AlertDialog.Builder(requireContext()).setTitle("Delete Record")
                .setMessage("Delete fattening record for " + f.cattleTag + "?")
                .setPositiveButton("Delete", (d, w) -> { db.fatteningDao().delete(f); loadData(); })
                .setNegativeButton("Cancel", null).show();
    }
}
