package com.silageproerp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silageproerp.R;
import com.silageproerp.database.entities.Buyer;

import java.util.List;

public class BuyerAdapter extends RecyclerView.Adapter<BuyerAdapter.ViewHolder> {

    public interface OnBuyerActionListener {
        void onEdit(Buyer buyer);
        void onDelete(Buyer buyer);
    }

    private List<Buyer> list;
    private final OnBuyerActionListener listener;

    public BuyerAdapter(List<Buyer> list, OnBuyerActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateList(List<Buyer> newList) { this.list = newList; notifyDataSetChanged(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_buyer, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Buyer b = list.get(position);
        h.tvName.setText(b.name);
        h.tvCompany.setText(b.company != null && !b.company.isEmpty() ? b.company : b.buyerType);
        h.tvPhone.setText(b.phone != null ? b.phone : "");
        h.tvOwed.setText(String.format("Owed: $%.2f", b.totalOwed));
        h.btnEdit.setOnClickListener(v -> listener.onEdit(b));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(b));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCompany, tvPhone, tvOwed;
        View btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvCompany = v.findViewById(R.id.tv_company);
            tvPhone = v.findViewById(R.id.tv_phone);
            tvOwed = v.findViewById(R.id.tv_owed);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
