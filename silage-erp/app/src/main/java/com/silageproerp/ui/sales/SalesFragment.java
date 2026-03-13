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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.silageproerp.R;
import com.silageproerp.adapters.SaleAdapter;
import com.silageproerp.database.AppDatabase;
import com.silageproerp.database.entities.Buyer;
import com.silageproerp.database.entities.Sale;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SalesFragment extends Fragment implements SaleAdapter.SaleListener {

    private AppDatabase db;
    private SaleAdapter adapter;
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
        List<Sale> sales = db.saleDao().getAll();
        if (adapter == null) { adapter = new SaleAdapter(sales, this); recyclerView.setAdapter(adapter); }
        else adapter.updateData(sales);
    }

    @Override public void onEdit(Sale s) { showAddDialog(s); }

    @Override
    public void onDelete(Sale s) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Sale")
                .setMessage("Delete invoice " + s.invoiceNumber + "?")
                .setPositiveButton("Delete", (d, w) -> { db.saleDao().delete(s); loadData(); })
                .setNegativeButton("Cancel", null).show();
    }

    @Override
    public void onMarkDelivered(Sale s) {
        s.deliveryStatus = "Delivered";
        db.saleDao().update(s);
        loadData();
    }

    @Override
    public void onMarkPaid(Sale s) {
        s.amountPaid = s.totalAmount;
        s.balance = 0;
        s.paymentStatus = "Paid";
        db.saleDao().update(s);
        loadData();
    }

    private void showAddDialog(Sale existing) {
        View dv = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sale, null);

        Spinner  spBuyer       = dv.findViewById(R.id.sp_buyer);
        Spinner  spSilageType  = dv.findViewById(R.id.sp_silage_type);
        EditText etQtyTons     = dv.findViewById(R.id.et_qty_tons);
        EditText etBales       = dv.findViewById(R.id.et_bales);
        EditText etPriceTon    = dv.findViewById(R.id.et_price_per_ton);
        EditText etPriceBale   = dv.findViewById(R.id.et_price_per_bale);
        EditText etTransport   = dv.findViewById(R.id.et_transport_cost);
        EditText etDistanceKm  = dv.findViewById(R.id.et_distance_km);
        EditText etDeparture   = dv.findViewById(R.id.et_departure_location);
        EditText etDelivery    = dv.findViewById(R.id.et_delivery_location);
        EditText etAmountPaid  = dv.findViewById(R.id.et_amount_paid);
        EditText etSaleDate    = dv.findViewById(R.id.et_sale_date);
        EditText etDelivDate   = dv.findViewById(R.id.et_delivery_date);
        EditText etNotes       = dv.findViewById(R.id.et_notes);
        TextView tvTotal       = dv.findViewById(R.id.tv_total_amount);
        TextView tvBalance     = dv.findViewById(R.id.tv_balance);

        // Auto-calculate total
        TextWatcher calc = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void afterTextChanged(Editable s) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                try {
                    double tons = parse(etQtyTons); int bales = (int) parse(etBales);
                    double pTon = parse(etPriceTon); double pBale = parse(etPriceBale);
                    double transport = parse(etTransport); double paid = parse(etAmountPaid);
                    double total = (tons * pTon) + (bales * pBale) + transport;
                    double bal = total - paid;
                    tvTotal.setText(String.format("Total: $%.2f", total));
                    tvBalance.setText(String.format("Balance: $%.2f", bal));
                } catch (Exception ignored) {}
            }
        };
        etQtyTons.addTextChangedListener(calc); etBales.addTextChangedListener(calc);
        etPriceTon.addTextChangedListener(calc); etPriceBale.addTextChangedListener(calc);
        etTransport.addTextChangedListener(calc); etAmountPaid.addTextChangedListener(calc);

        List<Buyer> buyers = db.buyerDao().getAll();
        String[] bNames = new String[buyers.size() + 1];
        bNames[0] = "— Select Buyer —";
        for (int i = 0; i < buyers.size(); i++) bNames[i+1] = buyers.get(i).name;
        spBuyer.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, bNames));

        String[] silageTypes = {"Maize", "Grass", "Sorghum", "Mixed", "Other"};
        spSilageType.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, silageTypes));

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        etSaleDate.setText(today);

        if (existing != null) {
            etQtyTons.setText(String.valueOf(existing.quantityTons));
            etBales.setText(String.valueOf(existing.balesCount));
            etPriceTon.setText(String.valueOf(existing.pricePerTon));
            etPriceBale.setText(String.valueOf(existing.pricePerBale));
            etTransport.setText(String.valueOf(existing.transportCost));
            etDistanceKm.setText(String.valueOf(existing.transportDistanceKm));
            etDeparture.setText(existing.departureLocation);
            etDelivery.setText(existing.deliveryLocation);
            etAmountPaid.setText(String.valueOf(existing.amountPaid));
            etSaleDate.setText(existing.saleDate);
            etDelivDate.setText(existing.deliveryDate);
            etNotes.setText(existing.notes);
            for (int i = 0; i < buyers.size(); i++) {
                if (buyers.get(i).id == existing.buyerId) { spBuyer.setSelection(i+1); break; }
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(existing == null ? "New Sale / Delivery" : "Edit Sale")
                .setView(dv)
                .setPositiveButton("Save", (d, w) -> {
                    int bi = spBuyer.getSelectedItemPosition();
                    if (bi == 0) return;
                    Buyer buyer = buyers.get(bi - 1);
                    Sale sale = new Sale(buyer.id, buyer.name,
                            spSilageType.getSelectedItem().toString(),
                            parse(etQtyTons), (int) parse(etBales),
                            parse(etPriceTon), parse(etPriceBale),
                            parse(etTransport), parse(etDistanceKm),
                            etDeparture.getText().toString().trim(),
                            etDelivery.getText().toString().trim(),
                            parse(etAmountPaid),
                            etSaleDate.getText().toString().trim(),
                            etDelivDate.getText().toString().trim(),
                            etNotes.getText().toString().trim());
                    if (existing == null) { db.saleDao().insert(sale); }
                    else { sale.id = existing.id; db.saleDao().update(sale); }
                    loadData();
                })
                .setNegativeButton("Cancel", null).show();
    }

    private double parse(EditText et) {
        try { return Double.parseDouble(et.getText().toString()); } catch (NumberFormatException e) { return 0; }
    }
}
