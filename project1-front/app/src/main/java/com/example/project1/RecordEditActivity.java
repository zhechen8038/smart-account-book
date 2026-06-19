package com.example.project1;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project1.databinding.ActivityRecordEditBinding;
import com.example.project1.model.AccountRecord;
import com.example.project1.model.BillRecognitionResponse;
import com.example.project1.model.SaveRecordRequest;
import com.example.project1.network.ApiClient;
import com.example.project1.network.ApiService;
import com.example.project1.network.ContentUriRequestBody;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordEditActivity extends AppCompatActivity {

    private ActivityRecordEditBinding binding;
    private ApiService apiService;

    private Long recordId;
    private Uri selectedImageUri;

    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRecordEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.create(this);

        registerImagePicker();
        readIntentData();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void registerImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null || !validateSelectedImage(uri)) {
                        return;
                    }

                    selectedImageUri = uri;
                    binding.ivBillImage.setImageURI(uri);
                    binding.btnRecognize.setEnabled(true);
                    binding.tvRecognitionResult.setVisibility(View.GONE);
                }
        );
    }

    private void readIntentData() {
        if (getIntent().hasExtra("recordId")) {
            recordId = getIntent().getLongExtra("recordId", -1L);

            binding.tvPageTitle.setText("编辑记账");

            String type = getIntent().getStringExtra("type");
            String category = getIntent().getStringExtra("category");
            String amount = getIntent().getStringExtra("amount");
            String recordDate = getIntent().getStringExtra("recordDate");
            String remark = getIntent().getStringExtra("remark");

            if ("INCOME".equals(type)) {
                binding.rbIncome.setChecked(true);
            } else {
                binding.rbExpense.setChecked(true);
            }

            binding.etCategory.setText(category);
            binding.etAmount.setText(amount);
            binding.etRecordDate.setText(recordDate);
            binding.etRemark.setText(remark);
        } else {
            binding.tvPageTitle.setText("添加记账");

            String initialDate = getIntent().getStringExtra("recordDate");

            if (TextUtils.isEmpty(initialDate)) {
                initialDate = new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                ).format(new Date());
            }

            binding.etRecordDate.setText(initialDate);
        }
    }

    private void setupClickListeners() {
        binding.etRecordDate.setOnClickListener(v -> showDatePicker());

        binding.btnSelectImage.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );

        binding.btnRecognize.setOnClickListener(v -> recognizeBillImage());

        binding.btnSave.setOnClickListener(v -> saveRecord());
    }

    private void setupBottomNavigation() {
        binding.recordNavView.getMenu()
                .findItem(R.id.navigation_dashboard)
                .setChecked(true);

        binding.recordNavView.setOnItemSelectedListener(item -> {
            int destinationId = item.getItemId();

            if (destinationId == R.id.navigation_dashboard) {
                return true;
            }

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_DESTINATION, destinationId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            return true;
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        String currentDate = binding.etRecordDate
                .getText()
                .toString()
                .trim();

        String[] dateParts = currentDate.split("-");

        try {
            if (dateParts.length == 3) {
                calendar.set(
                        Integer.parseInt(dateParts[0]),
                        Integer.parseInt(dateParts[1]) - 1,
                        Integer.parseInt(dateParts[2])
                );
            }
        } catch (NumberFormatException ignored) {
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String selectedDate = String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth
                    );

                    binding.etRecordDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void recognizeBillImage() {
        if (selectedImageUri == null) {
            showToast("请先选择支付宝账单图片");
            return;
        }

        String mimeType = getContentResolver().getType(selectedImageUri);

        ContentUriRequestBody imageBody = new ContentUriRequestBody(
                getContentResolver(),
                selectedImageUri,
                mimeType
        );

        MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                "image",
                "alipay_bill.jpg",
                imageBody
        );

        setRecognizing(true);

        apiService.recognizeBill(imagePart)
                .enqueue(new Callback<BillRecognitionResponse>() {
                    @Override
                    public void onResponse(
                            Call<BillRecognitionResponse> call,
                            Response<BillRecognitionResponse> response
                    ) {
                        setRecognizing(false);

                        if (!response.isSuccessful() || response.body() == null) {
                            showToast("AI识别失败，请重新选择清晰图片");
                            return;
                        }

                        fillRecognitionResult(response.body());
                    }

                    @Override
                    public void onFailure(
                            Call<BillRecognitionResponse> call,
                            Throwable throwable
                    ) {
                        setRecognizing(false);
                        showToast("无法连接AI识别服务");
                    }
                });
    }

    private void fillRecognitionResult(BillRecognitionResponse result) {
        if ("INCOME".equals(result.type)) {
            binding.rbIncome.setChecked(true);
        } else {
            binding.rbExpense.setChecked(true);
        }

        if (!TextUtils.isEmpty(result.category)) {
            binding.etCategory.setText(result.category);
        }

        if (result.amount != null) {
            binding.etAmount.setText(result.amount.toPlainString());
        }

        if (!TextUtils.isEmpty(result.recordDate)) {
            binding.etRecordDate.setText(result.recordDate);
        }

        if (!TextUtils.isEmpty(result.remark)) {
            binding.etRemark.setText(result.remark);
        }

        double confidence = result.confidence == null
                ? 0
                : result.confidence * 100;

        binding.tvRecognitionResult.setText(
                String.format(
                        Locale.getDefault(),
                        "AI识别完成，可信度：%.0f%%。请检查内容后保存。",
                        confidence
                )
        );

        binding.tvRecognitionResult.setVisibility(View.VISIBLE);
    }

    private void setRecognizing(boolean recognizing) {
        binding.progressRecognize.setVisibility(
                recognizing ? View.VISIBLE : View.GONE
        );

        binding.btnRecognize.setEnabled(
                !recognizing && selectedImageUri != null
        );

        binding.btnSelectImage.setEnabled(!recognizing);
        binding.btnSave.setEnabled(!recognizing);
    }

    private void saveRecord() {
        String category = binding.etCategory.getText().toString().trim();
        String amountText = binding.etAmount.getText().toString().trim();
        String recordDate = binding.etRecordDate.getText().toString().trim();
        String remark = binding.etRemark.getText().toString().trim();

        binding.etCategory.setError(null);
        binding.etAmount.setError(null);
        binding.etRecordDate.setError(null);

        if (TextUtils.isEmpty(category)) {
            binding.etCategory.setError("请输入记账类目");
            binding.etCategory.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(amountText)) {
            binding.etAmount.setError("请输入金额");
            binding.etAmount.requestFocus();
            return;
        }

        BigDecimal amount;

        try {
            amount = new BigDecimal(amountText);
        } catch (NumberFormatException exception) {
            binding.etAmount.setError("金额格式不正确");
            binding.etAmount.requestFocus();
            return;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            binding.etAmount.setError("金额必须大于0");
            binding.etAmount.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(recordDate)) {
            binding.etRecordDate.setError("请选择日期");
            return;
        }

        String type = binding.rbIncome.isChecked()
                ? "INCOME"
                : "EXPENSE";

        SaveRecordRequest request = new SaveRecordRequest(
                type,
                category,
                amount,
                recordDate,
                remark
        );

        binding.btnSave.setEnabled(false);

        if (recordId == null) {
            createRecord(request);
        } else {
            updateRecord(request);
        }
    }

    private void createRecord(SaveRecordRequest request) {
        apiService.createRecord(request)
                .enqueue(new Callback<AccountRecord>() {
                    @Override
                    public void onResponse(
                            Call<AccountRecord> call,
                            Response<AccountRecord> response
                    ) {
                        binding.btnSave.setEnabled(true);

                        if (response.isSuccessful()) {
                            showToast("添加成功");
                            finish();
                        } else {
                            showToast("添加失败，请检查填写内容");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AccountRecord> call,
                            Throwable throwable
                    ) {
                        binding.btnSave.setEnabled(true);
                        showToast("无法连接服务器");
                    }
                });
    }

    private void updateRecord(SaveRecordRequest request) {
        apiService.updateRecord(recordId, request)
                .enqueue(new Callback<AccountRecord>() {
                    @Override
                    public void onResponse(
                            Call<AccountRecord> call,
                            Response<AccountRecord> response
                    ) {
                        binding.btnSave.setEnabled(true);

                        if (response.isSuccessful()) {
                            showToast("修改成功");
                            finish();
                        } else {
                            showToast("修改失败，请检查填写内容");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<AccountRecord> call,
                            Throwable throwable
                    ) {
                        binding.btnSave.setEnabled(true);
                        showToast("无法连接服务器");
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean validateSelectedImage(Uri uri) {
        String mimeType = getContentResolver().getType(uri);

        if (mimeType == null ||
                !(mimeType.equals("image/jpeg") ||
                        mimeType.equals("image/png") ||
                        mimeType.equals("image/webp"))) {

            showToast("只支持 JPG、PNG 或 WebP 图片");
            return false;
        }

        try {
            long size = getContentResolver()
                    .openAssetFileDescriptor(uri, "r")
                    .getLength();

            if (size > 10 * 1024 * 1024) {
                showToast("图片大小不能超过10MB");
                return false;
            }
        } catch (Exception exception) {
            showToast("无法读取图片");
            return false;
        }

        return true;
    }
}
