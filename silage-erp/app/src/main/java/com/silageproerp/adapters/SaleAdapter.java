package com.silageproerp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.silageproerp.R;
import com.silageproerp.database.entities.Sale;
import java.util.List;

public class SaleAdapter extends RecyclerView.Adapter<SaleAdapter.ViewHolder> {
    public interface SaleListener {
        void onEdit(Sale s); void onDelete(Sale s);
        void onMarkDelivered(Sale s); void onMarkPaid(Sale s);
    }
    private List<Sale> data; private final SaleListener listener;
    public SaleAdapter(List<Sale> data, SaleListener l) { this.data = data; this.listener = l; }
    public void updateData(List<Sale> d) { this.data = d; notifyDataSetChanged(); }
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_sale, p, false));
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Sale s = data.get(pos);
        h.tvInvoice.setText(s.invoiceNumber);
        h.tvBuyer.setText(s.buyerName);
        h.tvDetails.setText(String.format("%.1f tons | %d bales | %s", s.quantityTons, s.balesCount, s.silagType));
        h.tvTotal.setText(String.format("Total: $%.2f | Paid: $%.2f | Bal: $%.2f", s.totalAmount, s.amountPaid, s.balance));
        h.tvTransport.setText(String.format("Transport: $%.2f | %.0f km | %s → %s",
                s.transportCost, s.transportDistanceKm,
                s.departureLocation != null ? s.departureLocation : "",
                s.deliveryLocation != null ? s.deliveryLocation : ""));
        h.tvDeliveryStatus.setText("Delivery: " + s.deliveryStatus);
        h.tvPaymentStatus.setText("Payment: " + s.paymentStatus);
        int payColor = "Paid".equals(s.paymentStatus) ? Color.parseColor("#2E7D32") :
                       "Partial".equals(s.paymentStatus) ? Color.parseColor("#E65100") : Color.RED;
        h.tvPaymentStatus.setTextColor(payColor);
        h.btnEdit.setOnClickListener(v -> listener.onEdit(s));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(s));
        h.btnDeliver.setOnClickListener(v -> listener.onMarkDelivered(s));
        h.btnPaid.setOnClickListener(v -> listener.onMarkPaid(s));
        h.btnDeliver.setVisibility(!"Delivered".equals(s.deliveryStatus) ? View.VISIBLE : View.GONE);
        h.btnPaid.setVisibility(!"Paid".equals(s.paymentStatus) ? View.VISIBLE : View.GONE);
    }
    @Override public int getItemCount() { return data.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInvoice, tvBuyer, tvDetails, tvTotal, tvTransport, tvDeliveryStatus, tvPaymentStatus;
        ImageButton btnEdit, btnDelete, btnDeliver, btnPaid;
        ViewHolder(View v) {
            super(v);
            tvInvoice=v.findViewById(R.id.tv_invoice); tvBuyer=v.findViewById(R.id.tv_buyer);
            tvDetails=v.findViewById(R.id.tv_details); tvTotal=v.findViewById(R.id.tv_total);
            tvTransport=v.findViewById(R.id.tv_transport);
            tvDeliveryStatus=v.findViewById(R.id.tv_delivery_status);
            tvPaymentStatus=v.findViewById(R.id.tv_payment_status);
            btnEdit=v.findViewById(R.id.btn_edit); btnDelete=v.findViewById(R.id.btn_delete);
            btnDeliver=v.findViewById(R.id.btn_mark_delivered); btnPaid=v.findViewById(R.id.btn_mark_paid);
        }
    }
}
