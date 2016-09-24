package com.example.bublovskiy.moveaview;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Start extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        //prevent landscape orientation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        TextView startTheGame = (TextView) findViewById(R.id.textViewStart);

        //set old game font
        Typeface oldGameFont = Typeface.createFromAsset(getAssets(),"fonts/ARCADECLASSIC.TTF");
        startTheGame.setTypeface(oldGameFont);

        //set onClick Listener to the start layout
        findViewById(R.id.startLayout).setOnClickListener(this);

    }//end onCreate

    @Override
    public void onClick(View view) {
        Intent startTheGame = new Intent(this,MainActivity3.class);
        startActivity(startTheGame);
    }//end onClick

}
