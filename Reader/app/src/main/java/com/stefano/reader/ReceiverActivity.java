package com.stefano.reader;


import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;

import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class ReceiverActivity extends AppCompatActivity {

    public static final String MIME_TEXT_PLAIN = "text/plain";
    private TextView tvIncomingMessage;
    private NfcAdapter nfcAdapter;
    Map<String, String> map = new HashMap<String, String>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!isNfcSupported()) {
            Toast.makeText(this, "Nfc is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC disabled on this device. Turn on to proceed", Toast.LENGTH_SHORT).show();
        }

        initViews();
    }

    private boolean isNfcSupported() {
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        return this.nfcAdapter != null;
    }

    private void initViews() {
        this.tvIncomingMessage = findViewById(R.id.tv_in_message);
    }

    @Override
    protected void onNewIntent(Intent intent) {

        receiveMessageFromDevice(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        enableForegroundDispatch(this, this.nfcAdapter);
        receiveMessageFromDevice(getIntent());
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatch(this, this.nfcAdapter);
    }

    private void receiveMessageFromDevice(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage inNdefMessage = (NdefMessage) parcelables[0];
            NdefRecord[] inNdefRecords = inNdefMessage.getRecords();
            NdefRecord ndefRecord_0 = inNdefRecords[0];

            String inMessage = new String(ndefRecord_0.getPayload());
            this.tvIncomingMessage.setText(inMessage);

            if(map.get(inMessage.substring(0,16))!=null) {
                Date d = new Date();
                String inTime = decryptTimestamp(map.get(inMessage.substring(0,16)), inMessage.substring(17));

                if(inTime.equals(DateFormat.format("yyyy-MM-dd hh:mm", d.getTime()).toString())){
                    openTheDoor();
                }
                notopenTheDoor();
            }

        }
    }

    public String decryptTimestamp(String key, String encrypted){
        String decrypted="";
        try
        {
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            decrypted = new String(cipher.doFinal(encrypted.getBytes()));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return decrypted;
    }

    private void openTheDoor(){
        Toast.makeText(ReceiverActivity.this, "Ok!", Toast.LENGTH_LONG).show();
    }
    private void notopenTheDoor(){
        Toast.makeText(ReceiverActivity.this, "Intruder!!", Toast.LENGTH_LONG).show();
    }

    public void enableForegroundDispatch(AppCompatActivity activity, NfcAdapter adapter) {

        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);


        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException ex) {
            throw new RuntimeException("Check your MIME type");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public void disableForegroundDispatch(final AppCompatActivity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }
}