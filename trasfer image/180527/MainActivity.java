package com.example.hayeon.myapplication;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    ImageView detect_btn;
    ImageView explain_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        detect_btn  = (ImageView) findViewById(R.id.btn_detect);
        explain_btn  = (ImageView) findViewById(R.id.btn_explain);
    }

    public void detectClicked(View v) {
        Intent intent = new Intent(getBaseContext(), DetectActivity.class);
        startActivity(intent);
    }
    public void expainClicked(View v) {
        //해야함
        Intent intent = new Intent(getBaseContext(), DetectActivity.class);
        startActivity(intent);
    }


}