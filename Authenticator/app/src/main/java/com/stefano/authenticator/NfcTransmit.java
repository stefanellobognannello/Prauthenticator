package com.stefano.authenticator;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.security.Key;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class NfcTransmit extends AppCompatActivity implements OutcomingNfcManager.NfcActivity {

    private NfcAdapter nfcAdapter;
    private OutcomingNfcManager outcomingNfccallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_transmitter);

        if (!isNfcSupported()) {
            Toast.makeText(this, "Nfc is not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "NFC disabled on this device. Turn on to proceed", Toast.LENGTH_SHORT).show();
        }

        this.outcomingNfccallback = new OutcomingNfcManager(this);
        this.nfcAdapter.setOnNdefPushCompleteCallback(outcomingNfccallback, this);
        this.nfcAdapter.setNdefPushMessageCallback(outcomingNfccallback, this);
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private boolean isNfcSupported() {
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        return this.nfcAdapter != null;
    }


    @Override
    public String getOutcomingMessage() {

        String deviceId = Settings.System.getString(getContentResolver(),
                Settings.System.ANDROID_ID);

        return deviceId+encryptedTimestamp();
    }

    public String encryptedTimestamp(){
        String encrypted="";
        try
        {
            String key = "ProGettoDiIngegn";
            Date d = new Date();
            String date = DateFormat.format("yyyy-MM-dd hh:mm", d.getTime()).toString();
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            // encrypt the text
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            encrypted = cipher.doFinal(date.getBytes()).toString();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return encrypted;

    }

    @Override
    public void signalResult() {
        runOnUiThread(() -> Toast.makeText(NfcTransmit.this, R.string.message_beaming_complete, Toast.LENGTH_SHORT).show());
        System.exit(0);
    }


}
