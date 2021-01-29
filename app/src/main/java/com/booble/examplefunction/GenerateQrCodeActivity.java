package com.booble.examplefunction;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.booble.examplefunction.databinding.ActivityGenerateQrCodeBinding;

public class GenerateQrCodeActivity extends AppCompatActivity {

    private ActivityGenerateQrCodeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_generate_qr_code);
    }
}