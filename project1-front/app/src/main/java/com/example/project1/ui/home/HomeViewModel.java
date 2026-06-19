package com.example.project1.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> text = new MutableLiveData<>("This is home Fragment");

    public LiveData<String> getText() {
        return text;
    }
}
