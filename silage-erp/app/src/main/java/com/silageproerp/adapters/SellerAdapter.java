package com.silageproerp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.silageproerp.R;
import com.silageproerp.database.entities.Seller;
import java.util.List;

public class SellerAdapter extends RecyclerView.Adapter<SellerAdapter.ViewHolder> {
    public interface SellerListener { void onEdit(Seller s); void onDelete(Seller s); }
    private List<Seller> data; private final SellerListener listener;
    public SellerAdapter(List<Seller> data, SellerListener l) { this.data = data; this.listener = l; }
    public void updateData(List<Seller> d) { this.data = d; notifyDataSetChanged(); }
    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_contact, p, false));
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Seller s = data.get(pos);
        h.tvName.setText(s.name); h.tvPhone.setText(s.phone != null ? s.phone : "");
        h.tvSub.setText((s.company != null && !s.company.isEmpty() ? s.company + " | " : "") + s.supplyType);
        h.tvExtra.setText("");
        h.btnEdit.setOnClickListener(v -> listener.onEdit(s));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(s));
    }
    @Override public int getItemCount() { return data.size(); }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvSub, tvExtra; ImageButton btnEdit, btnDelete;
        ViewHolder(View v) { super(v); tvName=v.findViewById(R.id.tv_name); tvPhone=v.findViewById(R.id.tv_phone);
            tvSub=v.findViewById(R.id.tv_sub); tvExtra=v.findViewById(R.id.tv_extra);
            btnEdit=v.findViewById(R.id.btn_edit); btnDelete=v.findViewById(R.id.btn_delete); }
    }
}
