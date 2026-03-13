package com.silageproerp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silageproerp.R;
import com.silageproerp.database.entities.Sale;

import java.util.List;

public class SaleAdapter extends RecyclerView.Adapter<SaleAdapter.ViewHolder> {

    public interface OnSaleActionListener {
        void onEdit(Sale sale);
        void onDelete(Sale sale);
    }

    private List<Sale> list;
    private final OnSaleActionListener listener;

    public SaleAdapter(List<Sale> list, OnSaleActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateList(List<Sale> newList) { this.list = newList; notifyDataSetChanged(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Sale s = list.get(position);
        h.tvInvoice.setText(s.invoiceNumber);
        h.tvBuyer.setText(s.buyerName);
        h.tvDetails.setText(String.format("%s: %.1ft / %d bales", s.silagType, s.quantityTons, s.balesCount));
        h.tvSilage.setText(String.format("Silage: $%.2f  Transport: $%.2f", s.silageCost, s.transportCost));
        h.tvTotal.setText(String.format("Total: $%.2f", s.totalAmount));
        h.tvBalance.setText(String.format("Balance: $%.2f [%s]", s.balance, s.paymentStatus));
        h.tvRoute.setText(s.departureLocation != null && !s.departureLocation.isEmpty()
                ? s.departureLocation + " → " + s.deliveryLocation : "");
        h.tvDeliveryStatus.setText(s.deliveryStatus);
        // Color by payment status
        int color = s.paymentStatus.equals("Paid") ? Color.parseColor("#2E7D32") :
                s.paymentStatus.equals("Partial") ? Color.parseColor("#E65100") : Color.RED;
        h.tvBalance.setTextColor(color);
        h.btnEdit.setOnClickListener(v -> listener.onEdit(s));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(s));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoice, tvBuyer, tvDetails, tvSilage, tvTotal, tvBalance, tvRoute, tvDeliveryStatus;
        View btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvInvoice = v.findViewById(R.id.tv_invoice);
            tvBuyer = v.findViewById(R.id.tv_buyer);
            tvDetails = v.findViewById(R.id.tv_details);
            tvSilage = v.findViewById(R.id.tv_silage_cost);
            tvTotal = v.findViewById(R.id.tv_total);
            tvBalance = v.findViewById(R.id.tv_balance);
            tvRoute = v.findViewById(R.id.tv_route);
            tvDeliveryStatus = v.findViewById(R.id.tv_delivery_status);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
