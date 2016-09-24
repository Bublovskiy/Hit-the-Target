package com.example.bublovskiy.moveaview;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class LayoutOnClickListener implements  View.OnTouchListener {


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        //execute only one as onTouch called four times at once
        if (motionEvent.getAction() == motionEvent.ACTION_DOWN) {
            float touchedX = (int) motionEvent.getX();
            float touchedY = (int) motionEvent.getY();

            Log.d("!!!","Start X: "+MainActivity.currentX + " Start Y: "+MainActivity.currentY);
            Log.d("!!!","Touched X: "+touchedX + " Touched Y: "+touchedY);

            //start drawing the path
            drawPath(touchedX, touchedY);
        }

        return false;
    }//end onTouch


    //create function with a runnable class inside
    public void drawPath(float endX, float endY) {

                //get the slope of a new line
                float slope;
                boolean isXnotChanging = false;

                if (endX - MainActivity.currentX != 0)

                {
                    slope = (endY - MainActivity.currentY) / (endX - MainActivity.currentX);

                } else

                {
                    slope = 0;
                    isXnotChanging = true;
                }

                //determine direction of drawing : to the left or to the right
                //if currentX - endX = 0 then the line is strictly vertical
                //if the line is not vertical - calculate X
                if (!isXnotChanging)

                {

                    //TO THE LEFT
                    if (MainActivity.currentX < endX) {

                        //start drawing the thread dynamically
                        for (float dynamicX = MainActivity.currentX + 1; dynamicX <= endX; dynamicX += 1) {

                            //calculate new dynamic Y coordinate
                            float dynamicY = slope * (dynamicX - MainActivity.currentX) + MainActivity.currentY;
                            //draw new dot of the line
                            MainActivity.canvas.drawCircle(dynamicX, dynamicY, 20, MainActivity.paint);

                            Log.d("!!!", "New X: " + dynamicX + " New Y: " + dynamicY);

                        }
                    }

                    //TO THE RIGHT
                    else {

                        //start drawing the thread dynamically
                        for (float dynamicX = MainActivity.currentX - 1; dynamicX >= endX; dynamicX -= 1) {

                            //calculate new dynamic Y coordinate
                            float dynamicY = slope * (dynamicX - MainActivity.currentX) + MainActivity.currentY;
                            //draw new dot of the line
                            MainActivity.canvas.drawCircle(dynamicX, dynamicY, 20, MainActivity.paint);
                            //refresh layout

                            Log.d("!!!", "New X: " + dynamicX + " New Y: " + dynamicY);

                        }
                    }
                }
                //if current X and end X are equal - the new line is strictly vertical
                else

                {
                    //X won't be changing
                    float dynamicX = endX;
                    //DOWN
                    if (MainActivity.currentY < endY) {

                        //start drawing the thread dynamically
                        for (float dynamicY = MainActivity.currentY + 1; dynamicY <= endY; dynamicY += 1) {
                            //draw new dot of the line
                            MainActivity.canvas.drawCircle(dynamicX, dynamicY, 20, MainActivity.paint);
                            //refresh layout
                            Log.d("!!!", "New X: " + dynamicX + " New Y: " + dynamicY);
                        }

                    }
                    //UP
                    else {

                        //start drawing the thread dynamically
                        for (float dynamicY = MainActivity.currentY - 1; dynamicY >= endY; dynamicY -= 1) {
                            //draw new dot of the line
                            MainActivity.canvas.drawCircle(dynamicX, dynamicY, 20, MainActivity.paint);
                            //refresh layout
                            Log.d("!!!", "New X: " + dynamicX + " New Y: " + dynamicY);
                        }
                    }
                }

                //refresh main layout
                MainActivity.mainLayout.invalidate();


                //update current coordinates
                MainActivity.currentX = endX;
                MainActivity.currentY = endY;

    }//end drawPath;


}//end LayoutOnClickListener
