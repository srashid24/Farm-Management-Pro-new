package com.silageproerp.ui.costing;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.silageproerp.R;
import com.silageproerp.adapters.CostAdapter;
import com.silageproerp.database.AppDatabase;
import com.silageproerp.database.entities.CostEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CostingFragment extends Fragment implements CostAdapter.OnCostActionListener {

    private AppDatabase db;
    private CostAdapter adapter;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_costing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        view.findViewById(R.id.fab_add).setOnClickListener(v -> showDialog(null));
        view.findViewById(R.id.btn_calculator).setOnClickListener(v -> showCostCalculator());

        loadData();
    }

    private void loadData() {
        List<CostEntry> list = db.costEntryDao().getAll();
        double totalCost = db.costEntryDao().getTotalCost();
        double totalRevenue = db.saleDao().getTotalRevenue();
        double profit = totalRevenue - totalCost;

        TextView tvSummary = requireView().findViewById(R.id.tv_cost_summary);
        tvSummary.setText(String.format(
                "Total Costs: $%.2f  |  Revenue: $%.2f  |  Profit: $%.2f",
                totalCost, totalRevenue, profit));

        if (adapter == null) { adapter = new CostAdapter(list, this); recyclerView.setAdapter(adapter); }
        else adapter.updateList(list);
    }

    private void showDialog(CostEntry existing) {
        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_cost, null);
        Spinner spCategory = dv.findViewById(R.id.sp_category);
        EditText etDesc = dv.findViewById(R.id.et_description);
        EditText etQty = dv.findViewById(R.id.et_quantity);
        Spinner spUnit = dv.findViewById(R.id.sp_unit);
        EditText etUnitCost = dv.findViewById(R.id.et_unit_cost);
        EditText etSeason = dv.findViewById(R.id.et_season);
        EditText etDate = dv.findViewById(R.id.et_date);
        EditText etSupplier = dv.findViewById(R.id.et_supplier);
        EditText etNotes = dv.findViewById(R.id.et_notes);
        TextView tvAmount = dv.findViewById(R.id.tv_calculated_amount);

        String[] cats = {"Seeds", "Fertilizer", "Labour", "Machinery", "Fuel", "Wrap", "Transport", "Irrigation", "Other"};
        spCategory.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, cats));
        String[] units = {"Ha", "Acres", "Kg", "Tons", "Liters", "Hours", "Days", "Pieces"};
        spUnit.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, units));

        // Auto-calc amount
        android.text.TextWatcher tw = new android.text.TextWatcher() {
            public void afterTextChanged(android.text.Editable s) {
                double q = 0, uc = 0;
                try { q = Double.parseDouble(etQty.getText().toString()); } catch (Exception ignored) {}
                try { uc = Double.parseDouble(etUnitCost.getText().toString()); } catch (Exception ignored) {}
                tvAmount.setText(String.format("Amount: $%.2f", q * uc));
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
        etQty.addTextChangedListener(tw);
        etUnitCost.addTextChangedListener(tw);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (existing == null) { etDate.setText(today); }
        else {
            etDesc.setText(existing.description); etQty.setText(String.valueOf(existing.quantity));
            etUnitCost.setText(String.valueOf(existing.unitCost)); etSeason.setText(existing.season);
            etDate.setText(existing.date); etSupplier.setText(existing.supplierName); etNotes.setText(existing.notes);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "Add Cost Entry" : "Edit Cost Entry")
                .setView(dv)
                .setPositiveButton("Save", (d, w) -> {
                    String desc = etDesc.getText().toString().trim();
                    if (desc.isEmpty()) { Toast.makeText(requireContext(), "Description required", Toast.LENGTH_SHORT).show(); return; }
                    double qty = 0, uc = 0;
                    try { qty = Double.parseDouble(etQty.getText().toString()); } catch (Exception ignored) {}
                    try { uc = Double.parseDouble(etUnitCost.getText().toString()); } catch (Exception ignored) {}
                    if (existing == null) {
                        db.costEntryDao().insert(new CostEntry(
                                spCategory.getSelectedItem().toString(), desc, qty,
                                spUnit.getSelectedItem().toString(), uc,
                                etSeason.getText().toString().trim(), etDate.getText().toString().trim(),
                                "", "general", etSupplier.getText().toString().trim(),
                                etNotes.getText().toString().trim()));
                        Toast.makeText(requireContext(), "Cost added", Toast.LENGTH_SHORT).show();
                    } else {
                        existing.category = spCategory.getSelectedItem().toString();
                        existing.description = desc; existing.quantity = qty;
                        existing.unit = spUnit.getSelectedItem().toString();
                        existing.unitCost = uc; existing.amount = qty * uc;
                        existing.season = etSeason.getText().toString().trim();
                        existing.date = etDate.getText().toString().trim();
                        existing.supplierName = etSupplier.getText().toString().trim();
                        existing.notes = etNotes.getText().toString().trim();
                        db.costEntryDao().update(existing);
                        Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show();
                    }
                    loadData();
                })
                .setNegativeButton("Cancel", null).show();
    }

    private void showCostCalculator() {
        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_cost_calculator, null);
        EditText etHectares = dv.findViewById(R.id.et_hectares);
        EditText etSeedCost = dv.findViewById(R.id.et_seed_cost);
        EditText etFertCost = dv.findViewById(R.id.et_fert_cost);
        EditText etLabourCost = dv.findViewById(R.id.et_labour_cost);
        EditText etMachineryCost = dv.findViewById(R.id.et_machinery_cost);
        EditText etWrapCost = dv.findViewById(R.id.et_wrap_cost);
        EditText etFuelCost = dv.findViewById(R.id.et_fuel_cost);
        EditText etTransportCost = dv.findViewById(R.id.et_transport_cost);
        EditText etDistanceKm = dv.findViewById(R.id.et_distance_km);
        EditText etTransportRate = dv.findViewById(R.id.et_transport_rate_per_km);
        EditText etExpectedYield = dv.findViewById(R.id.et_expected_yield);
        EditText etExpectedPrice = dv.findViewById(R.id.et_expected_price);
        TextView tvResult = dv.findViewById(R.id.tv_calc_result);

        android.text.TextWatcher calcWatcher = new android.text.TextWatcher() {
            public void afterTextChanged(android.text.Editable s) { calculateAndShow(); }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            void calculateAndShow() {
                double ha = 0, seed = 0, fert = 0, labour = 0, machinery = 0,
                        wrap = 0, fuel = 0, transport = 0, distKm = 0, rateKm = 0,
                        yieldTons = 0, price = 0;
                try { ha = Double.parseDouble(etHectares.getText().toString()); } catch (Exception ignored) {}
                try { seed = Double.parseDouble(etSeedCost.getText().toString()); } catch (Exception ignored) {}
                try { fert = Double.parseDouble(etFertCost.getText().toString()); } catch (Exception ignored) {}
                try { labour = Double.parseDouble(etLabourCost.getText().toString()); } catch (Exception ignored) {}
                try { machinery = Double.parseDouble(etMachineryCost.getText().toString()); } catch (Exception ignored) {}
                try { wrap = Double.parseDouble(etWrapCost.getText().toString()); } catch (Exception ignored) {}
                try { fuel = Double.parseDouble(etFuelCost.getText().toString()); } catch (Exception ignored) {}
                try { transport = Double.parseDouble(etTransportCost.getText().toString()); } catch (Exception ignored) {}
                try { distKm = Double.parseDouble(etDistanceKm.getText().toString()); } catch (Exception ignored) {}
                try { rateKm = Double.parseDouble(etTransportRate.getText().toString()); } catch (Exception ignored) {}
                try { yieldTons = Double.parseDouble(etExpectedYield.getText().toString()); } catch (Exception ignored) {}
                try { price = Double.parseDouble(etExpectedPrice.getText().toString()); } catch (Exception ignored) {}

                // Auto-calculate transport if rate & distance given
                double calcTransport = (distKm > 0 && rateKm > 0) ? distKm * rateKm : transport;
                double totalCost = seed + fert + labour + machinery + wrap + fuel + calcTransport;
                double costPerHa = ha > 0 ? totalCost / ha : 0;
                double costPerAcre = costPerHa / 2.471;
                double costPerTon = yieldTons > 0 ? totalCost / yieldTons : 0;
                double revenue = yieldTons * price;
                double profit = revenue - totalCost;
                double breakEvenPrice = yieldTons > 0 ? totalCost / yieldTons : 0;

                tvResult.setText(String.format(
                        "──── COST ANALYSIS ────\n" +
                        "Total Cost:       $%.2f\n" +
                        "Cost / Hectare:   $%.2f\n" +
                        "Cost / Acre:      $%.2f\n" +
                        "Cost / Ton:       $%.2f\n" +
                        "Transport Cost:   $%.2f\n" +
                        "──── REVENUE ────\n" +
                        "Expected Revenue: $%.2f\n" +
                        "Gross Profit:     $%.2f\n" +
                        "Break-even Price: $%.2f/ton",
                        totalCost, costPerHa, costPerAcre, costPerTon, calcTransport,
                        revenue, profit, breakEvenPrice));
            }
        };

        etHectares.addTextChangedListener(calcWatcher);
        etSeedCost.addTextChangedListener(calcWatcher);
        etFertCost.addTextChangedListener(calcWatcher);
        etLabourCost.addTextChangedListener(calcWatcher);
        etMachineryCost.addTextChangedListener(calcWatcher);
        etWrapCost.addTextChangedListener(calcWatcher);
        etFuelCost.addTextChangedListener(calcWatcher);
        etTransportCost.addTextChangedListener(calcWatcher);
        etDistanceKm.addTextChangedListener(calcWatcher);
        etTransportRate.addTextChangedListener(calcWatcher);
        etExpectedYield.addTextChangedListener(calcWatcher);
        etExpectedPrice.addTextChangedListener(calcWatcher);

        new AlertDialog.Builder(requireContext())
                .setTitle("Cost Calculator")
                .setView(dv)
                .setPositiveButton("Close", null)
                .show();
    }

    @Override public void onEdit(CostEntry entry) { showDialog(entry); }
    @Override
    public void onDelete(CostEntry entry) {
        new AlertDialog.Builder(requireContext()).setTitle("Delete Entry")
                .setMessage("Delete \"" + entry.description + "\"?")
                .setPositiveButton("Delete", (d, w) -> { db.costEntryDao().delete(entry); loadData(); })
                .setNegativeButton("Cancel", null).show();
    }
}
