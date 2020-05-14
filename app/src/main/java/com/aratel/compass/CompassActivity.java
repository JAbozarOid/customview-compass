package com.aratel.compass;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.aratel.compass.view.CompassView;

public class CompassActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //use of custom view
        CompassView cv = new CompassView(this, null, R.attr.bearing);
        setContentView(cv);
        cv.setmBearing(45);

    }
}
