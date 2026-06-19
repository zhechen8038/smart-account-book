package com.example.project1.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project1.R;
import com.example.project1.databinding.ItemAccountRecordBinding;
import com.example.project1.model.AccountRecord;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecordAdapter
        extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {

    public interface OnRecordActionListener {
        void onEdit(AccountRecord record);

        void onDelete(AccountRecord record);
    }

    private final List<AccountRecord> records = new ArrayList<>();
    private final OnRecordActionListener listener;

    public RecordAdapter(OnRecordActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<AccountRecord> newRecords) {
        records.clear();

        if (newRecords != null) {
            records.addAll(newRecords);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemAccountRecordBinding binding =
                ItemAccountRecordBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );

        return new RecordViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull RecordViewHolder holder,
            int position
    ) {
        AccountRecord record = records.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    class RecordViewHolder extends RecyclerView.ViewHolder {

        private final ItemAccountRecordBinding binding;

        public RecordViewHolder(ItemAccountRecordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AccountRecord record) {
            binding.tvCategory.setText(record.category);
            binding.tvDate.setText(record.recordDate);

            if (record.remark == null || record.remark.trim().isEmpty()) {
                binding.tvRemark.setVisibility(View.GONE);
            } else {
                binding.tvRemark.setVisibility(View.VISIBLE);
                binding.tvRemark.setText(record.remark);
            }

            boolean isIncome = "INCOME".equals(record.type);
            BigDecimal amount = record.amount == null
                    ? BigDecimal.ZERO
                    : record.amount;

            String amountText = String.format(
                    Locale.getDefault(),
                    "%s ¥%.2f",
                    isIncome ? "+" : "-",
                    amount
            );

            binding.tvAmount.setText(amountText);

            int color = isIncome
                    ? R.color.income_green
                    : R.color.expense_red;

            binding.tvAmount.setTextColor(
                    ContextCompat.getColor(binding.getRoot().getContext(), color)
            );

            binding.btnEdit.setOnClickListener(v -> listener.onEdit(record));
            binding.btnDelete.setOnClickListener(v -> listener.onDelete(record));
        }
    }
}