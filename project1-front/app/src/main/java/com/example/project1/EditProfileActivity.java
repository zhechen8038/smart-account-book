package com.example.project1;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project1.databinding.ActivityEditProfileBinding;
import com.example.project1.model.UpdateUserRequest;
import com.example.project1.model.UserResponse;
import com.example.project1.network.ApiClient;
import com.example.project1.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.create(this);
        binding.btnSaveProfile.setOnClickListener(view -> saveProfile());
        loadProfile();
    }

    private void loadProfile() {
        apiService.getCurrentUser().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    binding.etName.setText(user.name);
                    binding.etAge.setText(user.age == null ? "" : String.valueOf(user.age));
                    binding.etOccupation.setText(user.occupation);
                    if ("男".equals(user.gender)) {
                        binding.rbMale.setChecked(true);
                    } else if ("女".equals(user.gender)) {
                        binding.rbFemale.setChecked(true);
                    }
                } else {
                    showToast("个人信息加载失败");
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable throwable) {
                showToast("无法连接服务器");
            }
        });
    }

    private void saveProfile() {
        String name = binding.etName.getText().toString().trim();
        String ageText = binding.etAge.getText().toString().trim();
        String occupation = binding.etOccupation.getText().toString().trim();

        binding.etName.setError(null);
        binding.etAge.setError(null);
        binding.etOccupation.setError(null);

        if (TextUtils.isEmpty(name)) {
            binding.etName.setError("请输入姓名");
            binding.etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(ageText)) {
            binding.etAge.setError("请输入年龄");
            binding.etAge.requestFocus();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageText);
        } catch (NumberFormatException exception) {
            binding.etAge.setError("年龄必须为数字");
            binding.etAge.requestFocus();
            return;
        }
        if (age < 1 || age > 120) {
            binding.etAge.setError("请输入1到120之间的年龄");
            binding.etAge.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(occupation)) {
            binding.etOccupation.setError("请输入职业");
            binding.etOccupation.requestFocus();
            return;
        }

        int genderId = binding.rgGender.getCheckedRadioButtonId();
        if (genderId == -1) {
            showToast("请选择性别");
            return;
        }
        String gender = genderId == R.id.rb_male ? "男" : "女";

        binding.btnSaveProfile.setEnabled(false);
        apiService.updateCurrentUser(new UpdateUserRequest(name, age, occupation, gender))
                .enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Call<UserResponse> call,
                                           Response<UserResponse> response) {
                        binding.btnSaveProfile.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            showToast("个人信息修改成功");
                            finish();
                        } else {
                            showToast("个人信息修改失败");
                        }
                    }

                    @Override
                    public void onFailure(Call<UserResponse> call, Throwable throwable) {
                        binding.btnSaveProfile.setEnabled(true);
                        showToast("无法连接服务器");
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
