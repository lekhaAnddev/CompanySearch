package com.company.search;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class Views extends AppCompatActivity {

    Context ctx;
    TextView tv1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        setContentView(R.layout.activity_two);
        tv1 = (TextView) findViewById(R.id.tv1);
        tv1.setText("Company Name: " + getIntent().getStringExtra("companyName")
                + "\nName: " + getIntent().getStringExtra("name")
                + "\nParent: " + getIntent().getStringExtra("parent")
                + "\nManagers: " + getIntent().getStringExtra("managers")
                + "\nPhones: " + getIntent().getStringExtra("phones")
                + "\nAddresses: " + getIntent().getStringExtra("addresses"));
    }
}
