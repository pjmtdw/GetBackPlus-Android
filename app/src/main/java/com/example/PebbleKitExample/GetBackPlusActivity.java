package com.example.PebbleKitExample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.JsResult;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.lang.Runnable;
import java.lang.StringBuilder;

import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
      if(intent != null && !intent.getBooleanExtra("alreadyDone",false)) {
        if(intent.getAction() == Intent.ACTION_SEND) {
          String txt = intent.getStringExtra(Intent.EXTRA_TEXT);
          Log.d("GetBackPlus", "action_send: " + txt);
          Pattern pat = Pattern.compile("https?://goo\\.gl/maps/\\S+");
          Matcher mat = pat.matcher(txt);
          if(mat.find()) {
            startWebView(mat.group());
            Log.d("GetBackPlus", "starting webview: " + mat.group());
          }else{
            Toast.makeText(getApplicationContext(), "map url not in text", Toast.LENGTH_LONG).show();
          }
          intent.putExtra("alreadyDone",true);
        }else if(intent.getAction() == Intent.ACTION_VIEW) {
          Pattern pat = Pattern.compile("geo:([0-9.,-]+)");
          Matcher mat = pat.matcher(intent.getDataString());
          if(mat.find()) {
            String latlng = mat.group(1);
            TextView et = (TextView)findViewById(R.id.latlng);
            et.setText(latlng);
            startWebView("http://www.google.com/maps/search/" + latlng);
          }
          intent.putExtra("alreadyDone",true);
        }
      }
    }

    public void sendTarget(View view) {
      TextView et = (TextView)findViewById(R.id.latlng);
      sendLatLngToWatch(et.getText().toString());
    }
    
    private void sendLatLngToWatch(String latlng) {
      PebbleDictionary data = new PebbleDictionary();
      data.addString(TARGET_KEY, latlng);
      PebbleKit.sendDataToPebble(getApplicationContext(), WATCHAPP_UUID, data);
    }
    private void startWebView(String loadUrl) {
      Toast.makeText(getApplicationContext(), "loading map..", Toast.LENGTH_LONG).show();
      final WebView webView = (WebView)findViewById(R.id.webview);

      webView.setWebChromeClient(new WebChromeClient() {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result){
          try{
            //Log.d("GetBackPlus",message);
            Pattern pat = Pattern.compile("head:([0-9]+),tail:([0-9]+)");
            Matcher mat = pat.matcher(message);
            if(mat.find()) {
              int head = Integer.parseInt(mat.group(1));
              int tail = Integer.parseInt(mat.group(2));
              OverlayView ov = (OverlayView)findViewById(R.id.overlay);
              ov.setOffsetYAndInvalidate((head-tail)/2);
            }
            return true;
          } finally {
            // TODO: maybe this is the reason that text input inside WebView loses focus.
            //       we should consider avoid using alert() and onJsAlert() for communicating between WebView and this activity.
            result.confirm();
          }
        }
      });

      webView.setWebViewClient(new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
          Log.d("GetBackPlus", "onPageFinished: " + url);
          final Handler handler = new Handler();
          // Google Map Rewrites URL using JavaScript when map loading is done.
          // http://.../@<lat>,<lng>,<zoom>
          // These latitude and longitudes are center of the map excluding searchbox on top, and information on bottom.
          // Thus we have to calibrate the cross marker on the OverlayView in order to indicate the *real* center.
          // Only tested in portrait mode. TODO: test in landscape mode.
          handler.postDelayed(new Runnable(){
            @Override
            public void run(){
              String js = new StringBuilder()
                .append("javascript:")
                .append("head=0;tail=0;")
                .append("try{head=document.getElementById('ml-searchbox').offsetHeight}catch(e){};")
                .append("try{tail=document.getElementById('ml-cards-entity').offsetHeight}catch(e){};")
                .append("alert('head:'+head+',tail:'+tail);")
                .toString();
              // the result of JavaScript alert() is captured by WebChromeClient.onJsAlert()
              webView.loadUrl(js);
              handler.postDelayed(this,5000);
            }
          },3000);
          handler.postDelayed(new Runnable(){
            @Override
            public void run(){
              boolean found = false;
              if(webView.getUrl() != null){
                //Log.d("GetBackPlus", "current_url:" + webView.getUrl());
                Pattern pat = Pattern.compile("/@([0-9.-]+,[0-9.-]+)");
                Matcher mat = pat.matcher(webView.getUrl());
                if(mat.find()) {
                  TextView et = (TextView)findViewById(R.id.latlng);
                  et.setText(mat.group(1));
                  found = true;
                }
              }
              if(!found) {
                Toast.makeText(getApplicationContext(), "latlong not found", Toast.LENGTH_SHORT).show();
              }else{
                handler.postDelayed(this,1000);
              }
            }
          },3000);
        }
      });
      webView.getSettings().setJavaScriptEnabled(true);
      webView.loadUrl(loadUrl);
    }
} 
