package com.example.project1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project1.databinding.ActivityLoginBinding;
import com.example.project1.model.LoginRequest;
import com.example.project1.model.LoginResponse;
import com.example.project1.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (new SessionManager(this).isLoggedIn()) {
            openMainActivity();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(view -> login());
        binding.tvGoRegister.setOnClickListener(view ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void login() {
        String phone = binding.etLoginPhone.getText().toString().trim();
        String password = binding.etLoginPassword.getText().toString();

        binding.etLoginPhone.setError(null);
        binding.etLoginPassword.setError(null);

        if (TextUtils.isEmpty(phone)) {
            binding.etLoginPhone.setError("请输入手机号");
            binding.etLoginPhone.requestFocus();
            return;
        }

        if (!phone.matches("^1[3-9]\\d{9}$")) {
            binding.etLoginPhone.setError("请输入正确的11位手机号");
            binding.etLoginPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etLoginPassword.setError("请输入密码");
            binding.etLoginPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.etLoginPassword.setError("密码至少需要6位");
            binding.etLoginPassword.requestFocus();
            return;
        }

        binding.btnLogin.setEnabled(false);
        ApiClient.create(this)
                .login(new LoginRequest(phone, password))
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(
                            Call<LoginResponse> call,
                            Response<LoginResponse> response) {
                        binding.btnLogin.setEnabled(true);

                        if (!response.isSuccessful()
                                || response.body() == null
                                || TextUtils.isEmpty(response.body().token)) {
                            Toast.makeText(
                                    LoginActivity.this,
                                    "手机号或密码错误",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        new SessionManager(LoginActivity.this)
                                .saveToken(response.body().token);
                        openMainActivity();
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                        binding.btnLogin.setEnabled(true);
                        Toast.makeText(
                                LoginActivity.this,
                                "无法连接服务器",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
