package com.example.pleasehelp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatViewInflater;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LayoutInflater inflater = this.getLayoutInflater();

        //Button but = new Button(getApplicationContext());
        ConstraintLayout lay = findViewById(R.id.insert);
        View but = inflater.inflate(R.layout.yeet, lay, false);
        lay.addView(but);
        View but2 = inflater.inflate(R.layout.yeet2, lay, false);
        lay.addView(but2);

    }
}
