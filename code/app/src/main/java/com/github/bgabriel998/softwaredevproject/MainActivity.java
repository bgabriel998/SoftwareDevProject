package com.github.bgabriel998.softwaredevproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void button1(View view) {
        Intent intent = new Intent(this, Button1Activity.class);
        startActivity(intent);
    }

    public void button2(View view) {
        Intent intent = new Intent(this, Button2Activity.class);
        startActivity(intent);
    }

    public void button3(View view) {
        Intent intent = new Intent(this, Button3Activity.class);
        startActivity(intent);
    }

    public void button4(View view) {
        Intent intent = new Intent(this, Button4Activity.class);
        startActivity(intent);
    }

    public void button5(View view) {
        Intent intent = new Intent(this, Button5Activity.class);
        startActivity(intent);
    }

    public void button6(View view) {
        Intent intent = new Intent(this, Button6Activity.class);
        startActivity(intent);
    }

    public void button7(View view) {
        Intent intent = new Intent(this, Button7Activity.class);
        startActivity(intent);
    }

    public void button8(View view) {
        Intent intent = new Intent(this, Button8Activity.class);
        startActivity(intent);
    }
}