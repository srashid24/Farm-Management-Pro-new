package com.silageproerp.ui.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.silageproerp.R;
import com.silageproerp.database.AppDatabase;

public class ReportsFragment extends Fragment {

    private AppDatabase db;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());
        loadReport(view);
    }

    private void loadReport(View view) {
        // --- Yield summary ---
        setText(view, R.id.tv_rpt_total_yield,      String.format("%.1f tons", db.harvestDao().getTotalYield()));
        setText(view, R.id.tv_rpt_total_bales,      String.valueOf(db.harvestDao().getTotalBales()));
        setText(view, R.id.tv_rpt_total_hectares,   String.format("%.1f ha / %.1f acres",
                db.landDao().getTotalHectares(), db.landDao().getTotalAcres()));

        // --- Financial summary ---
        double revenue  = db.saleDao().getTotalRevenue();
        double costs    = db.costEntryDao().getTotalCost();
        double profit   = revenue - costs;
        setText(view, R.id.tv_rpt_revenue,      String.format("$%.2f", revenue));
        setText(view, R.id.tv_rpt_total_cost,   String.format("$%.2f", costs));
        setText(view, R.id.tv_rpt_profit,       String.format("$%.2f", profit));
        setText(view, R.id.tv_rpt_outstanding,  String.format("$%.2f", db.saleDao().getTotalOutstanding()));
        setText(view, R.id.tv_rpt_transport,    String.format("$%.2f", db.saleDao().getTotalTransportCost()));

        // --- Counts ---
        setText(view, R.id.tv_rpt_farmers,     String.valueOf(db.farmerDao().getCount()));
        setText(view, R.id.tv_rpt_lands,       String.valueOf(db.landDao().getCount()));
        setText(view, R.id.tv_rpt_buyers,      String.valueOf(db.buyerDao().getCount()));
        setText(view, R.id.tv_rpt_sellers,     String.valueOf(db.sellerDao().getCount()));
        setText(view, R.id.tv_rpt_sales,       String.valueOf(db.saleDao().getCount()));
        setText(view, R.id.tv_rpt_cattle,      String.valueOf(db.cattleDao().getCount()));
        setText(view, R.id.tv_rpt_fattening,   String.valueOf(db.fatteningDao().getActiveCount()));
        setText(view, R.id.tv_rpt_inventory_val, String.format("$%.2f", db.inventoryDao().getTotalValue()));

        // --- Cost breakdown by category ---
        setText(view, R.id.tv_cost_seeds,      String.format("$%.2f", safeGet(() -> db.costEntryDao().getCostByCategory("Seeds"))));
        setText(view, R.id.tv_cost_fertilizer, String.format("$%.2f", safeGet(() -> db.costEntryDao().getCostByCategory("Fertilizer"))));
        setText(view, R.id.tv_cost_labour,     String.format("$%.2f", safeGet(() -> db.costEntryDao().getCostByCategory("Labour"))));
        setText(view, R.id.tv_cost_machinery,  String.format("$%.2f", safeGet(() -> db.costEntryDao().getCostByCategory("Machinery"))));
        setText(view, R.id.tv_cost_fuel,       String.format("$%.2f", safeGet(() -> db.costEntryDao().getCostByCategory("Fuel"))));
        setText(view, R.id.tv_cost_wrap,       String.format("$%.2f", safeGet(() -> db.costEntryDao().getCostByCategory("Wrap"))));
        setText(view, R.id.tv_cost_transport,  String.format("$%.2f", safeGet(() -> db.costEntryDao().getCostByCategory("Transport"))));
    }

    private void setText(View root, int id, String text) {
        TextView tv = root.findViewById(id);
        if (tv != null) tv.setText(text);
    }

    private double safeGet(java.util.concurrent.Callable<Double> fn) {
        try { Double v = fn.call(); return v != null ? v : 0; }
        catch (Exception e) { return 0; }
    }
}
