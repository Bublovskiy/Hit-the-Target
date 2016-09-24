package com.example.bublovskiy.moveaview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.ImageView;
import android.widget.RelativeLayout;


public class MainActivity extends AppCompatActivity {

    static float mainWindowMaxX, mainWindowMaxY, currentX, currentY,touchedX,touchedY;
    static Canvas canvas;
    static Paint paint;
    static RelativeLayout mainLayout;
    static Drawable background;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //fetch main layout
        mainLayout = (RelativeLayout) findViewById(R.id.mainLayout);

    }//end onCreate


    //get dimensions of the main layout after the window has been drawn
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        mainWindowMaxX = mainLayout.getWidth();
        mainWindowMaxY = mainLayout.getHeight();

        //Log.d("!!!***", mainWindowMaxX + "  " + mainWindowMaxY);

        //create bitmap for the canvas
        Bitmap bg = Bitmap.createBitmap((int)mainWindowMaxX, (int)mainWindowMaxY, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bg);

        //attach canvas to the main layout
        background = new BitmapDrawable(this.getResources(),bg);
        mainLayout.setBackground(background);
        mainLayout.setOnTouchListener(new LayoutOnClickListener());

        //center the start point
        currentX = mainWindowMaxX/2;
        currentY = mainWindowMaxY/2;

        //create color settings
        paint = new Paint();
        paint.setColor(Color.parseColor("#CD5C5C"));
        paint.setStyle(Paint.Style.STROKE);

        //draw a starting position
        canvas.drawCircle(currentX , currentY, (int)(mainWindowMaxX*0.02), paint);

        Log.d("!!!", "Success!");
        super.onWindowFocusChanged(hasFocus);
    }//end onWindowFocusChanged


}//and MainActivity
