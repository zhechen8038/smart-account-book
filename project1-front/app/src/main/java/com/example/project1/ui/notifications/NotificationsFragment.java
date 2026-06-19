package com.example.project1.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.project1.EditProfileActivity;
import com.example.project1.LoginActivity;
import com.example.project1.SessionManager;
import com.example.project1.databinding.FragmentNotificationsBinding;
import com.example.project1.model.UserResponse;
import com.example.project1.network.ApiClient;
import com.example.project1.network.ApiService;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private ApiService apiService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        apiService = ApiClient.create(requireContext());

        binding.btnEditProfile.setOnClickListener(view ->
                startActivity(new Intent(requireContext(), EditProfileActivity.class)));
        binding.btnLogout.setOnClickListener(view -> logout());

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUser();
        loadRecordCount();
    }

    private void loadUser() {
        apiService.getCurrentUser().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (binding == null) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse user = response.body();
                    String name = user.name == null || user.name.trim().isEmpty()
                            ? "未设置姓名"
                            : user.name.trim();
                    binding.tvAvatar.setText(name.substring(0, 1));
                    binding.tvName.setText(name);
                    binding.tvPhone.setText("手机号：" + user.phone);
                    binding.tvProfileDetail.setText(
                            user.age + "岁 · " + user.occupation + " · " + user.gender
                    );
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

    private void loadRecordCount() {
        apiService.getRecordCount().enqueue(new Callback<Map<String, Long>>() {
            @Override
            public void onResponse(Call<Map<String, Long>> call,
                                   Response<Map<String, Long>> response) {
                if (binding == null) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    Long count = response.body().get("count");
                    binding.tvRecordCount.setText((count == null ? 0 : count) + " 次");
                } else {
                    showToast("记账次数加载失败");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Long>> call, Throwable throwable) {
                showToast("无法加载记账次数");
            }
        });
    }

    private void logout() {
        new SessionManager(requireContext()).clearToken();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
