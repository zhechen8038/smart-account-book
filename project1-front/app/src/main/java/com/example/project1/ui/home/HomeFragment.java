package com.example.project1.ui.home;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.project1.RecordEditActivity;
import com.example.project1.databinding.FragmentHomeBinding;
import com.example.project1.model.AccountRecord;
import com.example.project1.model.RecordSummary;
import com.example.project1.network.ApiClient;
import com.example.project1.network.ApiService;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecordAdapter adapter;
    private ApiService apiService;
    private String selectedMonth;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        apiService = ApiClient.create(requireContext());

        selectedMonth = new SimpleDateFormat(
                "yyyy-MM",
                Locale.getDefault()
        ).format(new Date());

        setupRecordList();
        setupClickListeners();

        return binding.getRoot();
    }

    private void setupRecordList() {
        adapter = new RecordAdapter(new RecordAdapter.OnRecordActionListener() {
            @Override
            public void onEdit(AccountRecord record) {
                openEditPage(record);
            }

            @Override
            public void onDelete(AccountRecord record) {
                showDeleteDialog(record);
            }
        });

        binding.rvRecords.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        binding.rvRecords.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnSelectMonth.setOnClickListener(v -> showMonthPicker());

        binding.fabAddRecord.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), RecordEditActivity.class);
            intent.putExtra("recordDate", selectedMonth + "-01");
            startActivity(intent);
        });
    }

    private void loadMonthData() {
        if (binding == null) {
            return;
        }

        binding.btnSelectMonth.setText(selectedMonth);
        loadRecords();
        loadSummary();
    }

    private void loadRecords() {
        apiService.getRecords(selectedMonth)
                .enqueue(new Callback<List<AccountRecord>>() {
                    @Override
                    public void onResponse(
                            Call<List<AccountRecord>> call,
                            Response<List<AccountRecord>> response
                    ) {
                        if (binding == null) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            List<AccountRecord> records = response.body();

                            adapter.submitList(records);

                            binding.tvEmpty.setVisibility(
                                    records.isEmpty() ? View.VISIBLE : View.GONE
                            );
                        } else {
                            showToast("账单加载失败");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<AccountRecord>> call,
                            Throwable throwable
                    ) {
                        showToast("无法连接服务器");
                    }
                });
    }

    private void loadSummary() {
        apiService.getRecordSummary(selectedMonth)
                .enqueue(new Callback<RecordSummary>() {
                    @Override
                    public void onResponse(
                            Call<RecordSummary> call,
                            Response<RecordSummary> response
                    ) {
                        if (binding == null) {
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            RecordSummary summary = response.body();

                            BigDecimal income = summary.totalIncome == null
                                    ? BigDecimal.ZERO
                                    : summary.totalIncome;

                            BigDecimal expense = summary.totalExpense == null
                                    ? BigDecimal.ZERO
                                    : summary.totalExpense;

                            binding.tvTotalIncome.setText(
                                    String.format(
                                            Locale.getDefault(),
                                            "¥%.2f",
                                            income
                                    )
                            );

                            binding.tvTotalExpense.setText(
                                    String.format(
                                            Locale.getDefault(),
                                            "¥%.2f",
                                            expense
                                    )
                            );
                        } else {
                            showToast("汇总加载失败");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<RecordSummary> call,
                            Throwable throwable
                    ) {
                        showToast("无法加载汇总数据");
                    }
                });
    }

    private void showMonthPicker() {
        Calendar calendar = Calendar.getInstance();

        String[] monthParts = selectedMonth.split("-");

        if (monthParts.length == 2) {
            calendar.set(
                    Integer.parseInt(monthParts[0]),
                    Integer.parseInt(monthParts[1]) - 1,
                    1
            );
        }

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedMonth = String.format(
                            Locale.getDefault(),
                            "%04d-%02d",
                            year,
                            month + 1
                    );

                    loadMonthData();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.setTitle("选择月份");
        dialog.show();
    }

    private void openEditPage(AccountRecord record) {
        Intent intent = new Intent(requireContext(), RecordEditActivity.class);

        intent.putExtra("recordId", record.id);
        intent.putExtra("type", record.type);
        intent.putExtra("category", record.category);
        intent.putExtra("amount", record.amount.toPlainString());
        intent.putExtra("recordDate", record.recordDate);
        intent.putExtra("remark", record.remark);

        startActivity(intent);
    }

    private void showDeleteDialog(AccountRecord record) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除账单")
                .setMessage("确定删除“" + record.category + "”这条账单吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("删除", (dialog, which) -> deleteRecord(record.id))
                .show();
    }

    private void deleteRecord(Long recordId) {
        apiService.deleteRecord(recordId)
                .enqueue(new Callback<Map<String, String>>() {
                    @Override
                    public void onResponse(
                            Call<Map<String, String>> call,
                            Response<Map<String, String>> response
                    ) {
                        if (response.isSuccessful()) {
                            showToast("删除成功");
                            loadMonthData();
                        } else {
                            showToast("删除失败");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Map<String, String>> call,
                            Throwable throwable
                    ) {
                        showToast("无法连接服务器");
                    }
                });
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (binding != null) {
            loadMonthData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}