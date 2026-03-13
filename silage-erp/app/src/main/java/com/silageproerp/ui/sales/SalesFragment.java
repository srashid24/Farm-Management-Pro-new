package com.silageproerp.ui.sales;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.silageproerp.R;
import com.silageproerp.adapters.SaleAdapter;
import com.silageproerp.database.AppDatabase;
import com.silageproerp.database.entities.Buyer;
import com.silageproerp.database.entities.Sale;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SalesFragment extends Fragment implements SaleAdapter.OnSaleActionListener {

    private AppDatabase db;
    private SaleAdapter adapter;
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
        List<Sale> list = query.isEmpty() ? db.saleDao().getAll() : db.saleDao().search(query);
        if (adapter == null) { adapter = new SaleAdapter(list, this); recyclerView.setAdapter(adapter); }
        else adapter.updateList(list);
    }

    private void showDialog(Sale existing) {
        View dv = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_sale, null);
        Spinner spBuyer = dv.findViewById(R.id.sp_buyer);
        Spinner spSilageType = dv.findViewById(R.id.sp_silage_type);
        EditText etQtyTons = dv.findViewById(R.id.et_qty_tons);
        EditText etBaleCount = dv.findViewById(R.id.et_bale_count);
        EditText etPricePerTon = dv.findViewById(R.id.et_price_per_ton);
        EditText etPricePerBale = dv.findViewById(R.id.et_price_per_bale);
        EditText etTransportCost = dv.findViewById(R.id.et_transport_cost);
        EditText etDistanceKm = dv.findViewById(R.id.et_distance_km);
        EditText etDeparture = dv.findViewById(R.id.et_departure_location);
        EditText etDelivery = dv.findViewById(R.id.et_delivery_location);
        EditText etAmountPaid = dv.findViewById(R.id.et_amount_paid);
        EditText etSaleDate = dv.findViewById(R.id.et_sale_date);
        EditText etDeliveryDate = dv.findViewById(R.id.et_delivery_date);
        EditText etNotes = dv.findViewById(R.id.et_notes);
        TextView tvTotal = dv.findViewById(R.id.tv_total_amount);

        // Auto-calculate total
        TextWatcher calcWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                double tons = 0, bales = 0, ppt = 0, ppb = 0, transport = 0;
                try { tons = Double.parseDouble(etQtyTons.getText().toString()); } catch (Exception ignored) {}
                try { bales = Double.parseDouble(etBaleCount.getText().toString()); } catch (Exception ignored) {}
                try { ppt = Double.parseDouble(etPricePerTon.getText().toString()); } catch (Exception ignored) {}
                try { ppb = Double.parseDouble(etPricePerBale.getText().toString()); } catch (Exception ignored) {}
                try { transport = Double.parseDouble(etTransportCost.getText().toString()); } catch (Exception ignored) {}
                double total = (tons * ppt) + (bales * ppb) + transport;
                tvTotal.setText(String.format("Total: $%.2f", total));
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
        etQtyTons.addTextChangedListener(calcWatcher);
        etBaleCount.addTextChangedListener(calcWatcher);
        etPricePerTon.addTextChangedListener(calcWatcher);
        etPricePerBale.addTextChangedListener(calcWatcher);
        etTransportCost.addTextChangedListener(calcWatcher);

        List<Buyer> buyers = db.buyerDao().getAll();
        List<String> buyerNames = new ArrayList<>();
        buyerNames.add("-- Select Buyer --");
        for (Buyer b : buyers) buyerNames.add(b.name);
        spBuyer.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, buyerNames));

        String[] types = {"Maize Silage", "Grass Silage", "Sorghum Silage", "Wheat Silage", "Mixed"};
        spSilageType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, types));

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (existing == null) { etSaleDate.setText(today); etDeliveryDate.setText(today); }
        else {
            etQtyTons.setText(String.valueOf(existing.quantityTons));
            etBaleCount.setText(String.valueOf(existing.balesCount));
            etPricePerTon.setText(String.valueOf(existing.pricePerTon));
            etPricePerBale.setText(String.valueOf(existing.pricePerBale));
            etTransportCost.setText(String.valueOf(existing.transportCost));
            etDistanceKm.setText(String.valueOf(existing.transportDistanceKm));
            etDeparture.setText(existing.departureLocation);
            etDelivery.setText(existing.deliveryLocation);
            etAmountPaid.setText(String.valueOf(existing.amountPaid));
            etSaleDate.setText(existing.saleDate);
            etDeliveryDate.setText(existing.deliveryDate);
            etNotes.setText(existing.notes);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "New Sale / Delivery" : "Edit Sale")
                .setView(dv)
                .setPositiveButton("Save", (d, w) -> {
                    int buyerIdx = spBuyer.getSelectedItemPosition();
                    if (buyerIdx == 0) { Toast.makeText(requireContext(), "Select a buyer", Toast.LENGTH_SHORT).show(); return; }
                    Buyer buyer = buyers.get(buyerIdx - 1);
                    double tons = 0, ppt = 0, ppb = 0, transport = 0, distKm = 0, paid = 0;
                    int bales = 0;
                    try { tons = Double.parseDouble(etQtyTons.getText().toString()); } catch (Exception ignored) {}
                    try { bales = Integer.parseInt(etBaleCount.getText().toString()); } catch (Exception ignored) {}
                    try { ppt = Double.parseDouble(etPricePerTon.getText().toString()); } catch (Exception ignored) {}
                    try { ppb = Double.parseDouble(etPricePerBale.getText().toString()); } catch (Exception ignored) {}
                    try { transport = Double.parseDouble(etTransportCost.getText().toString()); } catch (Exception ignored) {}
                    try { distKm = Double.parseDouble(etDistanceKm.getText().toString()); } catch (Exception ignored) {}
                    try { paid = Double.parseDouble(etAmountPaid.getText().toString()); } catch (Exception ignored) {}

                    if (existing == null) {
                        db.saleDao().insert(new Sale(buyer.id, buyer.name,
                                spSilageType.getSelectedItem().toString(),
                                tons, bales, ppt, ppb, transport, distKm,
                                etDeparture.getText().toString().trim(),
                                etDelivery.getText().toString().trim(),
                                paid,
                                etSaleDate.getText().toString().trim(),
                                etDeliveryDate.getText().toString().trim(),
                                etNotes.getText().toString().trim()));
                        Toast.makeText(requireContext(), "Sale recorded", Toast.LENGTH_SHORT).show();
                    } else {
                        existing.buyerId = buyer.id; existing.buyerName = buyer.name;
                        existing.silagType = spSilageType.getSelectedItem().toString();
                        existing.quantityTons = tons; existing.balesCount = bales;
                        existing.pricePerTon = ppt; existing.pricePerBale = ppb;
                        existing.transportCost = transport; existing.transportDistanceKm = distKm;
                        existing.departureLocation = etDeparture.getText().toString().trim();
                        existing.deliveryLocation = etDelivery.getText().toString().trim();
                        existing.silageCost = (tons * ppt) + (bales * ppb);
                        existing.totalAmount = existing.silageCost + transport;
                        existing.amountPaid = paid;
                        existing.balance = existing.totalAmount - paid;
                        existing.paymentStatus = paid <= 0 ? "Unpaid" : (paid >= existing.totalAmount ? "Paid" : "Partial");
                        existing.saleDate = etSaleDate.getText().toString().trim();
                        existing.deliveryDate = etDeliveryDate.getText().toString().trim();
                        existing.notes = etNotes.getText().toString().trim();
                        db.saleDao().update(existing);
                        Toast.makeText(requireContext(), "Updated", Toast.LENGTH_SHORT).show();
                    }
                    loadData("");
                })
                .setNegativeButton("Cancel", null).show();
    }

    @Override public void onEdit(Sale sale) { showDialog(sale); }
    @Override
    public void onDelete(Sale sale) {
        new AlertDialog.Builder(requireContext()).setTitle("Delete Sale")
                .setMessage("Delete invoice " + sale.invoiceNumber + "?")
                .setPositiveButton("Delete", (d, w) -> { db.saleDao().delete(sale); loadData(""); })
                .setNegativeButton("Cancel", null).show();
    }
}
