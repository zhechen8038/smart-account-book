package com.example.project1.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DashboardViewModel extends ViewModel {

    private final MutableLiveData<String> text =
            new MutableLiveData<>("This is dashboard Fragment");

    public LiveData<String> getText() {
        return text;
    }
}
