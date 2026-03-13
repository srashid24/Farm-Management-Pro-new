package com.silageproerp.ui.dashboard;

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

public class DashboardFragment extends Fragment {

    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());
        loadStats(view);
    }

    private void loadStats(View view) {
        // Farmers
        setText(view, R.id.tv_farmer_count, String.valueOf(db.farmerDao().getCount()));
        // Lands
        setText(view, R.id.tv_land_count, String.valueOf(db.landDao().getCount()));
        String totalHa = String.format("%.1f ha", db.landDao().getTotalHectares());
        setText(view, R.id.tv_total_hectares, totalHa);
        // Harvest
        String totalYield = String.format("%.1f tons", db.harvestDao().getTotalYield());
        setText(view, R.id.tv_total_yield, totalYield);
        setText(view, R.id.tv_total_bales, String.valueOf(db.harvestDao().getTotalBales()));
        // Sales
        String totalRevenue = String.format("$%.2f", db.saleDao().getTotalRevenue());
        setText(view, R.id.tv_total_revenue, totalRevenue);
        String outstanding = String.format("$%.2f", db.saleDao().getTotalOutstanding());
        setText(view, R.id.tv_outstanding, outstanding);
        // Costs
        String totalCost = String.format("$%.2f", db.costEntryDao().getTotalCost());
        setText(view, R.id.tv_total_cost, totalCost);
        // Cattle
        setText(view, R.id.tv_cattle_count, String.valueOf(db.cattleDao().getActiveCount()));
        setText(view, R.id.tv_fattening_count, String.valueOf(db.fatteningDao().getActiveCount()));
        // Inventory
        String inventoryValue = String.format("$%.2f", db.inventoryDao().getTotalValue());
        setText(view, R.id.tv_inventory_value, inventoryValue);
        // Low stock warning
        int lowStock = db.inventoryDao().getLowStock().size();
        TextView tvLowStock = view.findViewById(R.id.tv_low_stock_alert);
        if (lowStock > 0) {
            tvLowStock.setVisibility(View.VISIBLE);
            tvLowStock.setText(lowStock + " item(s) low on stock!");
        } else {
            tvLowStock.setVisibility(View.GONE);
        }
    }

    private void setText(View root, int id, String text) {
        TextView tv = root.findViewById(id);
        if (tv != null) tv.setText(text);
    }
}
