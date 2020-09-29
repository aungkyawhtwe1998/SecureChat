package com.example.securechat.ActivityUi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securechat.R;

public class SplashActi extends AppCompatActivity {

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        setContentView(R.layout.);
//
//        Handler handler= new Handler();
//        handler.postDelayed(new Runnable(){
//            @Override
//            public void run(){
//                startActivity(new Intent(SplashActi.this,StartActivity.class));
//                finish();
//
//            }
//
//        },3000);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!isFinishing()) {
                startActivity(new Intent(SplashActi.this, StartActivity.class));
                finish();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 50);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
}
