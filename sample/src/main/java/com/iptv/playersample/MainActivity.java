package com.iptv.playersample;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn).setOnClickListener(v -> PlayerActivity.start(this));

//        subMenuComonent.setCallcack();
//
//        Player.Builder()
//            .setMainCategory()
//            .setCategoty()
//            .setMedia()
//            .enableLoading()
//            .addComponent()
//            .addComponent()
//            .addComponent()
//            .addComponent()
//            .addComponent()
//            .start();
    }
}