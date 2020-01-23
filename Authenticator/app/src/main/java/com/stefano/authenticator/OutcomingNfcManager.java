package com.stefano.authenticator;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;

public class OutcomingNfcManager implements NfcAdapter.CreateNdefMessageCallback,
        NfcAdapter.OnNdefPushCompleteCallback {

    public static final String MIME_TEXT_PLAIN = "text/plain";
    private NfcActivity activity;

    public OutcomingNfcManager(NfcActivity activity) {
        this.activity = activity;
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {

        String outString = activity.getOutcomingMessage();
        byte[] outBytes = outString.getBytes();
        NdefRecord outRecord = NdefRecord.createMime(MIME_TEXT_PLAIN, outBytes);

        return new NdefMessage(outRecord);
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        activity.signalResult();
    }


    public interface NfcActivity {
        String getOutcomingMessage();

        void signalResult();
    }
}
