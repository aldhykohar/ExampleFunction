package com.booble.examplefunction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.booble.examplefunction.databinding.ActivityMainBinding;
import com.booble.examplefunction.databinding.DialogSelectPhoneNumberBinding;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding binding;
    List<String> listPhone = new ArrayList<>();

    private static final int PERMISSIONS_READ_CONTACT = 101;
    private static final int REQUEST_CODE_PICK_CONTACTS = 200;
    private Uri uriContact;
    private String contactID;     // contacts unique ID


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        doOnclick();
    }

    private void doOnclick() {
        binding.mbGenerateQRCode.setOnClickListener(v -> startActivity(new Intent(this, GenerateQrCodeActivity.class)));
        binding.mbScanner.setOnClickListener(v -> startActivity(new Intent(this, ScannerActivity.class)));
        binding.mbGetContact.setOnClickListener(v -> askCameraPermissions());
    }

    private void onClickSelectContact() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
    }

    private void askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_READ_CONTACT);
        } else {
            onClickSelectContact();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_READ_CONTACT) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onClickSelectContact();
            } else {
                Toast.makeText(this, "Perizinian contact dibutuhkan untuk mengakses kamera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            Log.e(TAG, "Response: " + data.toString());
            uriContact = data.getData();

            retrieveContactName();
            retrieveContactNumber();
            retrieveAllPhoneNumber();
//            retrieveContactPhoto();

        }
    }

    private void retrieveContactPhoto() {

        Bitmap photo = null;

        try {
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactID)));

            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
                /*ImageView imageView = (ImageView) findViewById(R.id.img_contact);
                imageView.setImageBitmap(photo);*/
            }

            assert inputStream != null;
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void retrieveContactNumber() {

        String contactNumber = null;

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        Log.e(TAG, "Contact ID: " + contactID);

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();

        Log.e(TAG, "Contact Phone Number: " + contactNumber);
    }

    private void retrieveAllPhoneNumber() {
        listPhone.clear();
        ContentResolver cr = getContentResolver();

        Cursor cursor = cr.query(uriContact,
                null,
                null, null, null);
        if (cursor.moveToFirst()) {
            String contactId =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            //
            //  Get all phone numbers.
            //
            Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
            while (phones.moveToNext()) {
                String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                switch (type) {
                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                        // do something with the Mobile number here...
                        // do something with the Work number here...
                        // do something with the Home number here...
                        if (number.contains("+62 ")) {
                            number = number.replace("+62 ", "0");
                        }
                        if (number.contains("+62")) {
                            number = number.replace("+62", "0");
                        }
                        number = number.replace("-", "");
                        listPhone.add(number);
                        break;
                }
            }
            phones.close();
        }
        cursor.close();

        if (listPhone.size() > 1) {
            initializationDialogPhone(listPhone);
        } else {
            binding.tvPhone.setText(listPhone.get(listPhone.size() - 1));
        }
    }

    private void retrieveContactName() {

        String contactName = null;

        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);

        if (cursor.moveToFirst()) {

            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.

            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();

        Log.e(TAG, "Contact Name: " + contactName);

    }

    private void initializationDialogPhone(List<String> listPhone) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        DialogSelectPhoneNumberBinding bindingDialog = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_select_phone_number, null, false);
//        setContentView(binding.getRoot());

        alertDialogBuilder.setView(bindingDialog.getRoot()).setCancelable(false);
        AlertDialog dialog_create = alertDialogBuilder.create();
        dialog_create.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog_create.show();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listPhone);
        bindingDialog.spinerPhone.setAdapter(adapter);

        bindingDialog.tvSelect.setOnClickListener(v -> {
            dialog_create.dismiss();
            binding.tvPhone.setText(bindingDialog.spinerPhone.getSelectedItem().toString());
        });
    }
}