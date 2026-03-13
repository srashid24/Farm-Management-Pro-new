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
import com.silageproerp.database.entities.Buyer;
import com.silageproerp.database.entities.Farmer;
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

        // Farmer include/exclude filter
        Spinner spFarmer = view.findViewById(R.id.sp_farmer_filter);
        List<com.silageproerp.database.entities.Farmer> farmers = db.farmerDao().getAll();
        List<String> farmerLabels = new ArrayList<>();
        farmerLabels.add("All Farmers (Include All)");
        for (com.silageproerp.database.entities.Farmer f : farmers) farmerLabels.add("Include: " + f.name);
        farmerLabels.add("--- EXCLUDE ---");
        for (com.silageproerp.database.entities.Farmer f : farmers) farmerLabels.add("Exclude: " + f.name);
        spFarmer.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, farmerLabels));

        // Field-wise report spinner
        Spinner spField = view.findViewById(R.id.sp_field_selector);
        List<Land> lands = db.landDao().getAll();
        List<String> fieldNames = new ArrayList<>();
        fieldNames.add("All Fields (Summary)");
        for (Land l : lands) fieldNames.add(l.fieldName + " [" + l.blockId + "] - " + l.sizeAcres + " acres");
        spField.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, fieldNames));

        view.findViewById(R.id.btn_generate_report).setOnClickListener(v -> {
            int idx = spField.getSelectedItemPosition();
            int farmerIdx = spFarmer.getSelectedItemPosition();
            if (idx == 0) generateSummaryReport(view, farmers, farmerIdx);
            else generateFieldReport(view, lands.get(idx - 1));
        });

        view.findViewById(R.id.btn_buyer_report).setOnClickListener(v -> generateBuyerLedger(view));

        generateSummaryReport(view, farmers, 0);
    }

    private void generateSummaryReport(View view, List<Farmer> farmers, int farmerFilterIdx) {
        TextView tvReport = view.findViewById(R.id.tv_report_output);

        // Determine filtered farmer ID (-1 = all, positive = include only, negative*-1 = exclude)
        int filterFarmerId = 0;
        boolean isExclude = false;
        int halfSize = farmers.size();
        if (farmerFilterIdx > 0 && farmerFilterIdx <= halfSize) {
            filterFarmerId = farmers.get(farmerFilterIdx - 1).id;
            isExclude = false;
        } else if (farmerFilterIdx > halfSize + 1) {
            filterFarmerId = farmers.get(farmerFilterIdx - halfSize - 2).id;
            isExclude = true;
        }

        // Get lands, filter if needed
        List<Land> allLands = db.landDao().getAll();
        List<Land> filteredLands = new ArrayList<>();
        for (Land l : allLands) {
            if (filterFarmerId == 0) { filteredLands.add(l); }
            else if (!isExclude && l.farmerId == filterFarmerId) { filteredLands.add(l); }
            else if (isExclude && l.farmerId != filterFarmerId) { filteredLands.add(l); }
        }

        // Overall stats
        int farmerCount = db.farmerDao().getCount();
        int landCount = filteredLands.size();
        double totalAcres = 0;
        for (Land l : filteredLands) totalAcres += l.sizeAcres;
        double totalHa = totalAcres / 2.47105;
        double totalYield = 0; int totalBales = 0;
        for (Land fl : filteredLands) {
            for (Harvest fh : db.harvestDao().getByLand(fl.id)) { totalYield += fh.yieldTons; totalBales += fh.baleCount; }
        }
        double totalRevenue = db.saleDao().getTotalRevenue();
        double totalCost = db.costEntryDao().getTotalCost();
        double totalTransport = db.saleDao().getTotalTransportCost();
        double profit = totalRevenue - totalCost;
        double yieldPerAcre = totalAcres > 0 ? totalYield / totalAcres : 0;
        double yieldPerHa = totalHa > 0 ? totalYield / totalHa : 0;
        double costPerAcre = totalAcres > 0 ? totalCost / totalAcres : 0;
        double costPerHa = totalHa > 0 ? totalCost / totalHa : 0;
        double revenuePerTon = totalYield > 0 ? totalRevenue / totalYield : 0;
        double outstanding = db.saleDao().getTotalOutstanding();

        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════\n");
        sb.append("     SILAGE BUSINESS SUMMARY  \n");
        sb.append("══════════════════════════════\n\n");

        sb.append("FARM OVERVIEW\n");
        sb.append("─────────────────────────────\n");
        if (filterFarmerId > 0) {
            String fname = !isExclude ? farmers.get(0).name : "excluding";
            sb.append(String.format("Filter: %s farmer\n\n", isExclude ? "Excluding" : "Including only"));
        }
        sb.append(String.format("Farmers Registered: %d\n", farmerCount));
        sb.append(String.format("Fields / Blocks:    %d\n", landCount));
        sb.append(String.format("Total Area:         %.2f acres  (%.2f ha)\n\n", totalAcres, totalHa));

        sb.append("PRODUCTION\n");
        sb.append("─────────────────────────────\n");
        sb.append(String.format("Total Yield:        %.2f tons\n", totalYield));
        sb.append(String.format("Total Bales:        %d\n", totalBales));
        sb.append(String.format("Yield / Acre:       %.2f t/acre\n", yieldPerAcre));
        sb.append(String.format("Yield / Hectare:    %.2f t/ha\n\n", yieldPerHa));

        sb.append("FINANCIALS\n");
        sb.append("─────────────────────────────\n");
        sb.append(String.format("Total Revenue:      $%.2f\n", totalRevenue));
        sb.append(String.format("Total Costs:        $%.2f\n", totalCost));
        sb.append(String.format("Transport Costs:    $%.2f\n", totalTransport));
        sb.append(String.format("Gross Profit:       $%.2f\n", profit));
        sb.append(String.format("Outstanding Debtors:$%.2f\n", outstanding));
        sb.append(String.format("Revenue / Ton:      $%.2f\n", revenuePerTon));
        sb.append(String.format("Cost / Acre:        $%.2f\n", costPerAcre));
        sb.append(String.format("Cost / Hectare:     $%.2f\n\n", costPerHa));

        // Per-field breakdown
        if (!filteredLands.isEmpty()) {
            sb.append("PER FIELD BREAKDOWN\n");
            sb.append("─────────────────────────────\n");
            for (Land land : filteredLands) {
                List<Harvest> harvests = db.harvestDao().getByLand(land.id);
                double fieldYield = 0;
                int fieldBales = 0;
                for (Harvest h : harvests) { fieldYield += h.yieldTons; fieldBales += h.baleCount; }
                double fieldAcres = land.sizeAcres;
                double fieldHa = land.sizeHectares;
                double ypa = fieldAcres > 0 ? fieldYield / fieldAcres : 0;
                double yph = fieldHa > 0 ? fieldYield / fieldHa : 0;
                sb.append(String.format("Field: %s [%s]\n", land.fieldName, land.blockId));
                sb.append(String.format("  Area:        %.2f acres / %.2f ha\n", fieldAcres, fieldHa));
                sb.append(String.format("  Yield:       %.2f tons (%d bales)\n", fieldYield, fieldBales));
                sb.append(String.format("  Yield/acre:  %.2f  Yield/ha: %.2f\n", ypa, yph));
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

        double areaAcres = land.sizeAcres;
        double areaHa = land.sizeHectares;
        double yieldPerAcre = areaAcres > 0 ? totalYield / areaAcres : 0;
        double yieldPerHa = areaHa > 0 ? totalYield / areaHa : 0;
        double costPerAcre = areaAcres > 0 ? totalInputCost / areaAcres : 0;
        double costPerHa = areaHa > 0 ? totalInputCost / areaHa : 0;

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
        sb.append(String.format("Size (Acres):    %.4f acres\n", areaAcres));
        sb.append(String.format("Size (Hectares): %.4f ha\n", areaHa));
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
        sb.append(String.format("Yield / Acre:    %.2f t/acre\n", yieldPerAcre));
        sb.append(String.format("Yield / Ha:      %.2f t/ha\n\n", yieldPerHa));

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

    private void generateBuyerLedger(View view) {
        TextView tvReport = view.findViewById(R.id.tv_report_output);
        StringBuilder sb = new StringBuilder();

        sb.append("══════════════════════════════\n");
        sb.append("     BUYER LEDGER REPORT      \n");
        sb.append("══════════════════════════════\n\n");

        List<Buyer> buyers = db.buyerDao().getAll();
        if (buyers.isEmpty()) {
            sb.append("No buyers registered yet.\n");
            tvReport.setText(sb.toString());
            return;
        }

        double grandTotalSales = 0, grandTotalPaid = 0, grandTotalDue = 0;

        for (Buyer buyer : buyers) {
            List<Sale> sales = db.saleDao().getByBuyer(buyer.id);
            double buyerTotal = 0, buyerPaid = 0, buyerDue = 0, buyerTransport = 0;
            double totalTons = 0;
            int totalBales = 0;

            for (Sale s : sales) {
                buyerTotal += s.totalAmount;
                buyerPaid += s.amountPaid;
                buyerDue += s.balance;
                buyerTransport += s.transportCost;
                totalTons += s.quantityTons;
                totalBales += s.balesCount;
            }

            grandTotalSales += buyerTotal;
            grandTotalPaid += buyerPaid;
            grandTotalDue += buyerDue;

            sb.append("┌────────────────────────────\n");
            sb.append(String.format("│ BUYER: %s\n", buyer.name));
            if (buyer.company != null && !buyer.company.isEmpty())
                sb.append(String.format("│ Company: %s\n", buyer.company));
            sb.append(String.format("│ Phone: %s  Type: %s\n", buyer.phone, buyer.buyerType));
            sb.append("├────────────────────────────\n");
            sb.append(String.format("│ Total Purchased: %.2f tons / %d bales\n", totalTons, totalBales));
            sb.append(String.format("│ Total Sales:     $%.2f\n", buyerTotal));
            sb.append(String.format("│ Transport Costs: $%.2f\n", buyerTransport));
            sb.append(String.format("│ Total Paid:      $%.2f\n", buyerPaid));
            sb.append(String.format("│ PAYMENT DUE:     $%.2f", buyerDue));
            if (buyerDue > 0) sb.append("  ⚠ OUTSTANDING");
            sb.append("\n");

            // Date-wise purchase history
            if (!sales.isEmpty()) {
                sb.append("│\n│ DATE-WISE PURCHASES:\n");
                for (Sale s : sales) {
                    sb.append(String.format("│  %s  %s\n", s.saleDate, s.invoiceNumber));
                    sb.append(String.format("│    %s: %.1ft / %d bales\n", s.silagType, s.quantityTons, s.balesCount));
                    sb.append(String.format("│    Silage: $%.2f  Transport: $%.2f\n", s.silageCost, s.transportCost));
                    sb.append(String.format("│    Total: $%.2f  Paid: $%.2f  Due: $%.2f [%s]\n",
                            s.totalAmount, s.amountPaid, s.balance, s.paymentStatus));
                    if (s.departureLocation != null && !s.departureLocation.isEmpty()) {
                        sb.append(String.format("│    Route: %s → %s (%.0f km)\n",
                                s.departureLocation, s.deliveryLocation, s.transportDistanceKm));
                    }
                    sb.append(String.format("│    Delivery: %s [%s]\n", s.deliveryDate, s.deliveryStatus));
                }
            }
            sb.append("└────────────────────────────\n\n");
        }

        sb.append("══════════════════════════════\n");
        sb.append("     GRAND TOTALS             \n");
        sb.append("══════════════════════════════\n");
        sb.append(String.format("Total Buyers:      %d\n", buyers.size()));
        sb.append(String.format("Total Sales:       $%.2f\n", grandTotalSales));
        sb.append(String.format("Total Received:    $%.2f\n", grandTotalPaid));
        sb.append(String.format("Total Outstanding: $%.2f\n", grandTotalDue));
        if (grandTotalDue > 0) {
            sb.append("\n⚠ OVERDUE BUYERS:\n");
            for (Buyer b : buyers) {
                double due = 0;
                for (Sale s : db.saleDao().getByBuyer(b.id)) due += s.balance;
                if (due > 0) sb.append(String.format("  • %s: $%.2f outstanding\n", b.name, due));
            }
        }

        tvReport.setText(sb.toString());
    }
}
