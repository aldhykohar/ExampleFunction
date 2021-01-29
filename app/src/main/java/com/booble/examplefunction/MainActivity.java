package com.booble.examplefunction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;

import com.booble.examplefunction.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        doOnclick();
    }

    private void doOnclick() {
        binding.mbGenerateQRCode.setOnClickListener(v -> startActivity(new Intent(this, GenerateQrCodeActivity.class)));
    }
}