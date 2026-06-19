package com.example.project1.ui.notifications;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NotificationsViewModel extends ViewModel {

    private final MutableLiveData<String> text =
            new MutableLiveData<>("This is notifications Fragment");

    public LiveData<String> getText() {
        return text;
    }
}
