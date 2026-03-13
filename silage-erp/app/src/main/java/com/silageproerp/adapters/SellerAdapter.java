package com.silageproerp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silageproerp.R;
import com.silageproerp.database.entities.Seller;

import java.util.List;

public class SellerAdapter extends RecyclerView.Adapter<SellerAdapter.ViewHolder> {

    public interface OnSellerActionListener {
        void onEdit(Seller seller);
        void onDelete(Seller seller);
    }

    private List<Seller> list;
    private final OnSellerActionListener listener;

    public SellerAdapter(List<Seller> list, OnSellerActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateList(List<Seller> newList) { this.list = newList; notifyDataSetChanged(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seller, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Seller s = list.get(position);
        h.tvName.setText(s.name);
        h.tvCompany.setText(s.company != null && !s.company.isEmpty() ? s.company : s.supplyType);
        h.tvPhone.setText(s.phone != null ? s.phone : "");
        h.tvSupplyType.setText(s.supplyType != null ? s.supplyType : "");
        h.btnEdit.setOnClickListener(v -> listener.onEdit(s));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(s));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCompany, tvPhone, tvSupplyType;
        View btnEdit, btnDelete;
        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tv_name);
            tvCompany = v.findViewById(R.id.tv_company);
            tvPhone = v.findViewById(R.id.tv_phone);
            tvSupplyType = v.findViewById(R.id.tv_supply_type);
            btnEdit = v.findViewById(R.id.btn_edit);
            btnDelete = v.findViewById(R.id.btn_delete);
        }
    }
}
