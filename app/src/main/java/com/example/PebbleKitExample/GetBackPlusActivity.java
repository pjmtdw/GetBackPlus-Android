package com.example.PebbleKitExample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.UUID;

public class GetBackPlusActivity extends Activity {
    // This value corresponds to appKeys in appinfo.json
    private static final int TARGET_KEY = 5;
    // This value corresponds to uuid in appinfo.json
    private static final UUID WATCHAPP_UUID = UUID.fromString("62bbc10d-70d2-41f4-bb0a-1d3dcf702ce4");

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_layout);
    }
    @Override
    public void onResume() {
      super.onResume();
      Intent intent = getIntent();
      if(intent != null) {
        if(intent.getAction() == Intent.ACTION_SEND) {
          String txt = intent.getStringExtra(Intent.EXTRA_TEXT);
          EditText et = (EditText)findViewById(R.id.latlng);
          et.setText(txt);
        }
      }
    }

    public void sendTarget(View view) {
      EditText et = (EditText)findViewById(R.id.latlng);
      sendLatLngToWatch(et.getText().toString());
    }
    
    private void sendLatLngToWatch(String latlng) {
      PebbleDictionary data = new PebbleDictionary();
      data.addString(TARGET_KEY, latlng);
      PebbleKit.sendDataToPebble(getApplicationContext(), WATCHAPP_UUID, data);
    }
} 
