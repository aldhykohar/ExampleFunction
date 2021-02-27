package com.booble.examplefunction;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.booble.examplefunction.databinding.ActivityGenerateQrCodeBinding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.CaptureActivity;

public class GenerateQrCodeActivity extends AppCompatActivity {

    private ActivityGenerateQrCodeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_generate_qr_code);
        doInitialization();
    }

    private void doInitialization() {
        binding.mbGenerate.setOnClickListener(v -> {
            String mText = binding.etText.getText().toString().trim();

            MultiFormatWriter writer = new MultiFormatWriter();
            try {
                //Initilize bit matrix
                BitMatrix matrix = writer.encode(mText, BarcodeFormat.QR_CODE, 350, 350);
                //Initilize barcode encoder
                BarcodeEncoder encoder = new BarcodeEncoder();
                //Initialize bitmap
                Bitmap bitmap = encoder.createBitmap(matrix);
                //Set bitmap on image view
                binding.ivOutput.setImageBitmap(bitmap);
                //Initialize input manager
                InputMethodManager manager = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE
                );
                //hide softkeyboard
                manager.hideSoftInputFromWindow(binding.etText.getApplicationWindowToken(), 0);

            } catch (WriterException e) {
                e.printStackTrace();
            }
        });
        binding.mbScan.setOnClickListener(v -> scanCode());
    }

    private void scanCode() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scanning Code");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(result.getContents());
                builder.setTitle("Hasil Scan");
                builder.setPositiveButton("Scan lagi", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        scanCode();
                    }
                }).setNegativeButton("Selesai", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                Toast.makeText(this, "Tidak Ada hasil", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}