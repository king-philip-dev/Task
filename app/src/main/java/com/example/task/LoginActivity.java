package com.example.task;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

/**
 * This class is will serve as the host activity for the
 * navigation component of this app, in this case
 * the nav host fragment.
 */
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
}