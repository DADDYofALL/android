package com.example.centralside;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText pressText;
    private EditText latText;
    private EditText lonText;
    private Button enterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //View object section
        enterBtn = (Button) findViewById(R.id.enterBtn);
        enterBtn.setOnClickListener(this);
        pressText = (EditText) findViewById(R.id.pressText);
        latText = (EditText) findViewById(R.id.latText);
        lonText = (EditText) findViewById(R.id.lonText);

    }

    @Override
    public void onClick(View view) {
        double destLat = Double.parseDouble(latText.getText().toString());
        double destLon = Double.parseDouble(lonText.getText().toString());
        double destPress = Double.parseDouble(pressText.getText().toString());
        Intent intent = new Intent(this, dashboard.class);
        intent.putExtra("destLat",destLat);
        intent.putExtra("destLon",destLon);
        intent.putExtra("destPress",destPress);
        startActivity(intent);
    }
}