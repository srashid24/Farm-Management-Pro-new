package com.silageproerp.ui.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.silageproerp.R;
import com.silageproerp.database.AppDatabase;
import com.silageproerp.database.entities.Harvest;
import com.silageproerp.database.entities.Land;
import com.silageproerp.database.entities.Sale;

import java.util.ArrayList;
import java.util.List;

public class ReportsFragment extends Fragment {

    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());

        // Field-wise report spinner
        Spinner spField = view.findViewById(R.id.sp_field_selector);
        List<Land> lands = db.landDao().getAll();
        List<String> fieldNames = new ArrayList<>();
        fieldNames.add("All Fields (Summary)");
        for (Land l : lands) fieldNames.add(l.fieldName + " [" + l.blockId + "] - " + l.sizeHectares + " ha");
        spField.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, fieldNames));

        view.findViewById(R.id.btn_generate_report).setOnClickListener(v -> {
            int idx = spField.getSelectedItemPosition();
            if (idx == 0) generateSummaryReport(view);
            else generateFieldReport(view, lands.get(idx - 1));
        });

        generateSummaryReport(view);
    }

    private void generateSummaryReport(View view) {
        TextView tvReport = view.findViewById(R.id.tv_report_output);

        // Overall stats
        int farmerCount = db.farmerDao().getCount();
        int landCount = db.landDao().getCount();
        double totalHa = db.landDao().getTotalHectares();
        double totalHaStr = totalHa;
        double totalAcres = totalHa * 2.471;
        double totalYield = db.harvestDao().getTotalYield();
        int totalBales = db.harvestDao().getTotalBales();
        double totalRevenue = db.saleDao().getTotalRevenue();
        double totalCost = db.costEntryDao().getTotalCost();
        double totalTransport = db.saleDao().getTotalTransportCost();
        double profit = totalRevenue - totalCost;
        double yieldPerHa = totalHa > 0 ? totalYield / totalHa : 0;
        double yieldPerAcre = totalAcres > 0 ? totalYield / totalAcres : 0;
        double costPerHa = totalHa > 0 ? totalCost / totalHa : 0;
        double costPerAcre = totalAcres > 0 ? totalCost / totalAcres : 0;
        double revenuePerTon = totalYield > 0 ? totalRevenue / totalYield : 0;
        double outstanding = db.saleDao().getTotalOutstanding();

        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════\n");
        sb.append("     SILAGE BUSINESS SUMMARY  \n");
        sb.append("══════════════════════════════\n\n");

        sb.append("FARM OVERVIEW\n");
        sb.append("─────────────────────────────\n");
        sb.append(String.format("Farmers Registered: %d\n", farmerCount));
        sb.append(String.format("Fields / Blocks:    %d\n", landCount));
        sb.append(String.format("Total Area:         %.2f ha  (%.2f acres)\n\n", totalHaStr, totalAcres));

        sb.append("PRODUCTION\n");
        sb.append("─────────────────────────────\n");
        sb.append(String.format("Total Yield:        %.2f tons\n", totalYield));
        sb.append(String.format("Total Bales:        %d\n", totalBales));
        sb.append(String.format("Yield / Hectare:    %.2f t/ha\n", yieldPerHa));
        sb.append(String.format("Yield / Acre:       %.2f t/acre\n\n", yieldPerAcre));

        sb.append("FINANCIALS\n");
        sb.append("─────────────────────────────\n");
        sb.append(String.format("Total Revenue:      $%.2f\n", totalRevenue));
        sb.append(String.format("Total Costs:        $%.2f\n", totalCost));
        sb.append(String.format("Transport Costs:    $%.2f\n", totalTransport));
        sb.append(String.format("Gross Profit:       $%.2f\n", profit));
        sb.append(String.format("Outstanding Debtors:$%.2f\n", outstanding));
        sb.append(String.format("Revenue / Ton:      $%.2f\n", revenuePerTon));
        sb.append(String.format("Cost / Hectare:     $%.2f\n", costPerHa));
        sb.append(String.format("Cost / Acre:        $%.2f\n\n", costPerAcre));

        // Per-field breakdown
        List<Land> lands = db.landDao().getAll();
        if (!lands.isEmpty()) {
            sb.append("PER FIELD BREAKDOWN\n");
            sb.append("─────────────────────────────\n");
            for (Land land : lands) {
                List<Harvest> harvests = db.harvestDao().getByLand(land.id);
                double fieldYield = 0;
                int fieldBales = 0;
                for (Harvest h : harvests) { fieldYield += h.yieldTons; fieldBales += h.baleCount; }
                double fieldAcres = land.sizeHectares * 2.471;
                double yph = land.sizeHectares > 0 ? fieldYield / land.sizeHectares : 0;
                double ypa = fieldAcres > 0 ? fieldYield / fieldAcres : 0;
                sb.append(String.format("Field: %s [%s]\n", land.fieldName, land.blockId));
                sb.append(String.format("  Area:      %.2f ha / %.2f acres\n", land.sizeHectares, fieldAcres));
                sb.append(String.format("  Yield:     %.2f tons (%d bales)\n", fieldYield, fieldBales));
                sb.append(String.format("  Yield/ha:  %.2f  Yield/acre: %.2f\n", yph, ypa));
                if (land.contractType != null && !land.contractType.isEmpty()) {
                    sb.append(String.format("  Contract:  %s - $%.2f\n", land.contractType, land.contractCost));
                }
                sb.append("\n");
            }
        }

        // Sales breakdown
        List<Sale> sales = db.saleDao().getAll();
        if (!sales.isEmpty()) {
            sb.append("SALES HISTORY\n");
            sb.append("─────────────────────────────\n");
            for (Sale s : sales) {
                sb.append(String.format("INV: %s | %s\n", s.invoiceNumber, s.buyerName));
                sb.append(String.format("  %s: %.1ft / %d bales\n", s.silagType, s.quantityTons, s.balesCount));
                sb.append(String.format("  Silage: $%.2f  Transport: $%.2f  Total: $%.2f\n",
                        s.silageCost, s.transportCost, s.totalAmount));
                sb.append(String.format("  Paid: $%.2f  Balance: $%.2f  [%s]\n",
                        s.amountPaid, s.balance, s.paymentStatus));
                if (s.departureLocation != null && !s.departureLocation.isEmpty()) {
                    sb.append(String.format("  Route: %s → %s (%.0f km)\n",
                            s.departureLocation, s.deliveryLocation, s.transportDistanceKm));
                }
                sb.append("\n");
            }
        }

        tvReport.setText(sb.toString());
    }

    private void generateFieldReport(View view, Land land) {
        TextView tvReport = view.findViewById(R.id.tv_report_output);

        List<Harvest> harvests = db.harvestDao().getByLand(land.id);
        double totalYield = 0;
        int totalBales = 0;
        double totalInputCost = 0;
        for (Harvest h : harvests) {
            totalYield += h.yieldTons;
            totalBales += h.baleCount;
            totalInputCost += h.inputCostTotal;
        }

        double areaHa = land.sizeHectares;
        double areaAcres = areaHa * 2.471;
        double yieldPerHa = areaHa > 0 ? totalYield / areaHa : 0;
        double yieldPerAcre = areaAcres > 0 ? totalYield / areaAcres : 0;
        double costPerHa = areaHa > 0 ? totalInputCost / areaHa : 0;
        double costPerAcre = areaAcres > 0 ? totalInputCost / areaAcres : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════\n");
        sb.append(String.format("  FIELD REPORT: %s\n", land.fieldName));
        sb.append("══════════════════════════════\n\n");
        sb.append(String.format("Block ID:       %s\n", land.blockId));
        sb.append(String.format("Owner Farmer:   ID #%d\n", land.farmerId));
        sb.append(String.format("Location:       %s\n", land.location));
        sb.append(String.format("GPS:            %s\n", land.gpsCoordinates));
        sb.append(String.format("Soil Type:      %s\n", land.soilType));
        sb.append(String.format("Crop Type:      %s\n\n", land.cropType));

        sb.append("LAND MEASUREMENTS\n");
        sb.append("─────────────────────────────\n");
        sb.append(String.format("Size (Hectares): %.4f ha\n", areaHa));
        sb.append(String.format("Size (Acres):    %.4f acres\n", areaAcres));
        sb.append(String.format("Size (m²):       %.0f m²\n\n", areaHa * 10000));

        if (land.contractType != null && !land.contractType.isEmpty()) {
            sb.append("LAND CONTRACT\n");
            sb.append("─────────────────────────────\n");
            sb.append(String.format("Contract Type:  %s\n", land.contractType));
            sb.append(String.format("Contract Cost:  $%.2f\n", land.contractCost));
            sb.append(String.format("Start Date:     %s\n", land.contractStartDate));
            sb.append(String.format("End Date:       %s\n", land.contractEndDate));
            sb.append(String.format("Landowner:      %s\n\n", land.landownerName));
        }

        sb.append("PRODUCTION HISTORY\n");
        sb.append("─────────────────────────────\n");
        sb.append(String.format("Total Yield:     %.2f tons\n", totalYield));
        sb.append(String.format("Total Bales:     %d\n", totalBales));
        sb.append(String.format("Yield / Ha:      %.2f t/ha\n", yieldPerHa));
        sb.append(String.format("Yield / Acre:    %.2f t/acre\n\n", yieldPerAcre));

        sb.append("COST ANALYSIS\n");
        sb.append("─────────────────────────────\n");
        sb.append(String.format("Total Input Cost: $%.2f\n", totalInputCost));
        sb.append(String.format("Cost / Hectare:   $%.2f\n", costPerHa));
        sb.append(String.format("Cost / Acre:      $%.2f\n\n", costPerAcre));

        sb.append("SEASON BREAKDOWN\n");
        sb.append("─────────────────────────────\n");
        for (Harvest h : harvests) {
            sb.append(String.format("Season: %s | Date: %s\n", h.season, h.harvestDate));
            sb.append(String.format("  Type: %s  Quality: %s\n", h.silagType, h.quality));
            sb.append(String.format("  Yield: %.2f t  Moisture: %.1f%%\n", h.yieldTons, h.moisturePercent));
            sb.append(String.format("  Bales: %d (%.1f kg each)\n", h.baleCount, h.baleWeightKg));
            sb.append(String.format("  Storage: %s\n", h.storageLocation));
            sb.append(String.format("  Input Cost: $%.2f\n\n", h.inputCostTotal));
        }

        tvReport.setText(sb.toString());
    }
}
