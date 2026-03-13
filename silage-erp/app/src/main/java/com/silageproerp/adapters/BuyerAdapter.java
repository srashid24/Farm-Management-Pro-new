package com.silageproerp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.silageproerp.R;
import com.silageproerp.database.entities.Buyer;
import java.util.List;

public class BuyerAdapter extends RecyclerView.Adapter<BuyerAdapter.ViewHolder> {
    public interface BuyerListener { void onEdit(Buyer b); void onDelete(Buyer b); }
    private List<Buyer> data; private final BuyerListener listener;
    public BuyerAdapter(List<Buyer> data, BuyerListener l) { this.data = data; this.listener = l; }
    public void updateData(List<Buyer> d) { this.data = d; notifyDataSetChanged(); }
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_contact, p, false));
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Buyer b = data.get(pos);
        h.tvName.setText(b.name); h.tvPhone.setText(b.phone != null ? b.phone : "");
        h.tvSub.setText((b.company != null && !b.company.isEmpty() ? b.company + " | " : "") + b.buyerType);
        h.tvExtra.setText(b.totalOwed > 0 ? String.format("Owes: $%.2f", b.totalOwed) : "");
        h.btnEdit.setOnClickListener(v -> listener.onEdit(b));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(b));
    }
    @Override public int getItemCount() { return data.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvSub, tvExtra; ImageButton btnEdit, btnDelete;
        ViewHolder(View v) { super(v); tvName=v.findViewById(R.id.tv_name); tvPhone=v.findViewById(R.id.tv_phone);
            tvSub=v.findViewById(R.id.tv_sub); tvExtra=v.findViewById(R.id.tv_extra);
            btnEdit=v.findViewById(R.id.btn_edit); btnDelete=v.findViewById(R.id.btn_delete); }
    }
}
