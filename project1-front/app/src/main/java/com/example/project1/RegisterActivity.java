package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.project1.databinding.ActivityRegisterBinding;
import com.example.project1.model.RegisterRequest;
import com.example.project1.network.ApiClient;

import com.example.project1.model.UserResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnRegister.setOnClickListener(view -> register());

        binding.tvGoLogin.setOnClickListener(view -> finish());
    }

    private void register() {
        String phone = binding.etRegisterPhone.getText().toString().trim();
        String password = binding.etRegisterPassword.getText().toString();
        String confirmPassword = binding.etConfirmPassword.getText().toString();
        String name = binding.etRegisterName.getText().toString().trim();
        String ageText = binding.etRegisterAge.getText().toString().trim();
        String occupation = binding.etRegisterOccupation.getText().toString().trim();

        clearErrors();

        if (TextUtils.isEmpty(phone)) {
            binding.etRegisterPhone.setError("请输入手机号");
            binding.etRegisterPhone.requestFocus();
            return;
        }

        if (!phone.matches("^1[3-9]\\d{9}$")) {
            binding.etRegisterPhone.setError("请输入正确的11位手机号");
            binding.etRegisterPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etRegisterPassword.setError("请输入密码");
            binding.etRegisterPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.etRegisterPassword.setError("密码至少需要6位");
            binding.etRegisterPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.etConfirmPassword.setError("请再次输入密码");
            binding.etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("两次输入的密码不一致");
            binding.etConfirmPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            binding.etRegisterName.setError("请输入姓名");
            binding.etRegisterName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(ageText)) {
            binding.etRegisterAge.setError("请输入年龄");
            binding.etRegisterAge.requestFocus();
            return;
        }

        int age;

        try {
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException exception) {
            binding.etRegisterAge.setError("年龄必须为数字");
            binding.etRegisterAge.requestFocus();
            return;
        }

        if (age < 1 || age > 120) {
            binding.etRegisterAge.setError("请输入1至120之间的年龄");
            binding.etRegisterAge.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(occupation)) {
            binding.etRegisterOccupation.setError("请输入职业");
            binding.etRegisterOccupation.requestFocus();
            return;
        }



        int selectedGenderId = binding.rgGender.getCheckedRadioButtonId();



        if (selectedGenderId == -1) {
            Toast.makeText(this, "请选择性别", Toast.LENGTH_SHORT).show();
            return;
        }

        String gender;

        if (selectedGenderId == R.id.rb_male) {
            gender = "男";
        } else {
            gender = "女";
        }


        binding.btnRegister.setEnabled(false);

        ApiClient.create(this)
                .register(new RegisterRequest(
                        phone,
                        password,
                        name,
                        age,
                        occupation,
                        gender
                ))
                .enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(
                            Call<UserResponse> call,
                            Response<UserResponse> response) {

                        binding.btnRegister.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(
                                    RegisterActivity.this,
                                    "注册成功，请登录",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        } else {
                            Toast.makeText(
                                    RegisterActivity.this,
                                    "注册失败，手机号可能已经注册",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<UserResponse> call,
                            Throwable throwable) {

                        binding.btnRegister.setEnabled(true);

                        Toast.makeText(
                                RegisterActivity.this,
                                "无法连接服务器：" + throwable.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void clearErrors() {
        binding.etRegisterPhone.setError(null);
        binding.etRegisterPassword.setError(null);
        binding.etConfirmPassword.setError(null);
        binding.etRegisterName.setError(null);
        binding.etRegisterAge.setError(null);
        binding.etRegisterOccupation.setError(null);
    }
}