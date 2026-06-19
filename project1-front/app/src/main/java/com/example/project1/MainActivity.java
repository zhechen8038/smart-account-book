package com.example.project1;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.project1.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_DESTINATION = "destination";

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!new SessionManager(this).isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = binding.navView;

        NavController navController = Navigation.findNavController(
                this,
                R.id.nav_host_fragment_activity_main
        );

        NavigationUI.setupWithNavController(navView, navController);

        // 监听底部导航栏点击事件
        navView.setOnItemSelectedListener(item -> {

            // 点击“记账”
            if (item.getItemId() == R.id.navigation_dashboard) {
                Intent intent = new Intent(
                        MainActivity.this,
                        RecordEditActivity.class
                );

                startActivity(intent);

                // false 表示底部导航栏继续保持在首页
                return false;
            }

            // 首页和“我的”继续使用原来的导航逻辑
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        openRequestedDestination(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        openRequestedDestination(intent);
    }

    private void openRequestedDestination(Intent intent) {
        if (binding == null || intent == null) {
            return;
        }

        int destinationId = intent.getIntExtra(EXTRA_DESTINATION, -1);
        if (destinationId == R.id.navigation_home
                || destinationId == R.id.navigation_notifications) {
            binding.navView.setSelectedItemId(destinationId);
            intent.removeExtra(EXTRA_DESTINATION);
        }
    }
}
