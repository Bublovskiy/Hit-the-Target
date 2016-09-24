package com.example.bublovskiy.moveaview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.TreeMap;

public class MainActivity2 extends AppCompatActivity implements View.OnTouchListener {

    float currentX, currentY,touchedX,touchedY;
    Canvas canvas;
    Paint paint;

    SurfaceHolder surfaceHolder;
    MySurfaceView mySurfaceView;
    boolean isRunning = false;
    int movingSpeed = 5;
    int radiusOfCircle = 10;

    //to count amount of lines in the collection
    int lineNumber = 0;
    //to store lines
    HashMap linesCollection = new HashMap<Integer,Line>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //create our surface view
        mySurfaceView = new MySurfaceView(this);
        //set background for the service view
        mySurfaceView.setBackgroundColor(Color.WHITE);
        //set pixels of surface holder to transparent to see the color of the surface view
        mySurfaceView.setZOrderOnTop(true);

        //set up on Touch listener
        mySurfaceView.setOnTouchListener(this);
        //link our custom Surface view to the Main Activity 2
        //mainLayout.addView(mySurfaceView);
        setContentView(mySurfaceView);

    }//end onCreate


    @Override
    protected void onResume() {
        super.onResume();
        //call mySurfaceView onResume
        mySurfaceView.onResume();
    }

    //get dimensions of the main window after the window has been drawn
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        //center the start point
        currentX = mySurfaceView.getWidth()/2;
        currentY = mySurfaceView.getHeight()/2;

        //create color settings
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);

        //get and lock our view canvas
        canvas = surfaceHolder.lockCanvas();
        //draw a starting position
        canvas.drawCircle(currentX , currentY, radiusOfCircle, paint);

        //unlock canvas to show the changes we have made
        surfaceHolder.unlockCanvasAndPost(canvas);

        super.onWindowFocusChanged(hasFocus);
    }//end onWindowFocusChanged


    //create Surface View class
    public class MySurfaceView extends SurfaceView implements Runnable {

        //second thread to draw trail
        Thread drawingThread;

        //constructor
        public MySurfaceView (Context context) {
               super(context);
               //get view holder to lock and unlock the view
               surfaceHolder = getHolder();
               //set pixels of surface holder to transparent to see the color of the surface view
               surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        }//end MySurfaceView


        //implement the main logic of drawing
        @Override
        public void run() {

            //keep running the method RUN constantly
            while (true) {
                //while the isRunning = true - keeps running the thread
                while (isRunning) {

                    //get the slope of a new line
                    float slope;
                    boolean isXNotChanging = false;
                    boolean didDrawingOccurred = false;
                    boolean isXandYAlreadyAssigned = false;
                    float lineStartX =0,lineStartY=0;

                    if (touchedX - currentX != 0) {
                        slope = (touchedY - currentY) / (touchedX - currentX);
                    } else {
                        slope = 0;
                        isXNotChanging = true;
                    }

                    //determine direction of drawing : to the left or to the right
                    //if currentX - touchedX = 0 then the line is strictly vertical
                    //if the line is not vertical - calculate X
                    if (!isXNotChanging)
                    {
                        //TO THE LEFT
                        if (currentX < touchedX) {

                            //start drawing the thread dynamically
                            for (float dynamicX = currentX + movingSpeed; dynamicX <= touchedX; dynamicX += movingSpeed) {

                                //calculate new dynamic Y coordinate
                                float dynamicY = slope * (dynamicX - currentX) + currentY;
                                //lock the canvas
                                surfaceHolder.lockCanvas();
                                //draw new dot of the line
                                canvas.drawCircle(dynamicX, dynamicY, 5, paint);
                                //unlock the canvas
                                surfaceHolder.unlockCanvasAndPost(canvas);

                                //safe the start point of the new line which is not on the previous line
                                if (!isXandYAlreadyAssigned) {
                                    lineStartX = dynamicX;
                                    lineStartY = dynamicY;
                                    isXandYAlreadyAssigned = true;
                                }

                                //set the flag didDrawingOccurred to true saying the drawing has been successful
                                //it is just a sign that we have entered this loop into TO THE LEFT direction
                                if (!didDrawingOccurred) didDrawingOccurred = true;
                            }

                            //if drawing had occurred then draw a circle and a trail
                            drawCircleAndTrail(didDrawingOccurred,lineStartX,lineStartY,slope);

                        }

                        //TO THE RIGHT
                        else {
                            //start drawing the thread dynamically
                            for (float dynamicX = currentX - movingSpeed; dynamicX >= touchedX; dynamicX -= movingSpeed) {

                                //calculate new dynamic Y coordinate
                                float dynamicY = slope * (dynamicX - currentX) + currentY;
                                surfaceHolder.lockCanvas();
                                //draw new dot of the line
                                canvas.drawCircle(dynamicX, dynamicY, 5, paint);
                                //unlock the canvas
                                surfaceHolder.unlockCanvasAndPost(canvas);

                                //safe the start point of the new line which is not on the previous line
                                if (!isXandYAlreadyAssigned) {
                                    lineStartX = dynamicX;
                                    lineStartY = dynamicY;
                                    isXandYAlreadyAssigned = true;
                                }

                                //set the flag didDrawingOccurred to true saying the drawing has been successful
                                if (!didDrawingOccurred) didDrawingOccurred = true;
                            }

                            //if drawing had occurred then draw a circle and a trail
                            drawCircleAndTrail(didDrawingOccurred,lineStartX,lineStartY,slope);                    }
                    }
                    //if current X and touched X are equal - the new line is strictly vertical
                    else

                    {
                        //X won't be changing
                        float dynamicX = touchedX;
                        //DOWN
                        if (currentY < touchedY) {

                            //start drawing the thread dynamically
                            for (float dynamicY = currentY + movingSpeed; dynamicY <= touchedY; dynamicY += movingSpeed) {
                                surfaceHolder.lockCanvas();
                                //draw new dot of the line
                                canvas.drawCircle(dynamicX, dynamicY, 5, paint);
                                //unlock the canvas
                                surfaceHolder.unlockCanvasAndPost(canvas);

                                //safe the start point of the new line which is not on the previous line
                                if (!isXandYAlreadyAssigned) {
                                    lineStartX = dynamicX;
                                    lineStartY = dynamicY;
                                    isXandYAlreadyAssigned = true;
                                }

                                //set the flag didDrawingOccurred to true saying the drawing has been successful
                                if (!didDrawingOccurred) didDrawingOccurred = true;

                        }

                        //if drawing had occurred then draw a circle and a trail
                            drawCircleAndTrail(didDrawingOccurred,lineStartX,lineStartY,slope);

                        }
                        //UP
                        else {
                            //start drawing the thread dynamically
                            for (float dynamicY = currentY - movingSpeed; dynamicY >= touchedY; dynamicY -= movingSpeed) {
                                surfaceHolder.lockCanvas();
                                //draw new dot of the line
                                canvas.drawCircle(dynamicX, dynamicY, 5, paint);
                                //unlock the canvas
                                surfaceHolder.unlockCanvasAndPost(canvas);

                                //safe the start point of the new line which is not on the previous line
                                if (!isXandYAlreadyAssigned) {
                                    lineStartX = dynamicX;
                                    lineStartY = dynamicY;
                                    isXandYAlreadyAssigned = true;
                                }

                                //set the flag didDrawingOccurred to true saying the drawing has been successful
                                if (!didDrawingOccurred) didDrawingOccurred = true;
                            }

                            //if drawing had occurred then draw a circle and a trail
                            drawCircleAndTrail(didDrawingOccurred,lineStartX,lineStartY,slope);
                        }
                    }

                    //update current coordinates if drawing has occurred
                    if (didDrawingOccurred) {
                        currentX = touchedX;
                        currentY = touchedY;
                    }

                    //stop drawing until the next touch occurs
                    isRunning = false;

                }//end While
            }//end outer While (true)
        }//end run method

        //draw final circle and a trail
        private boolean drawCircleAndTrail(boolean keyOfSuccess, float lineStartX, float lineStartY, double lineSlope) {
            //if drawing had occurred then draw a circle
            if (keyOfSuccess) {

                //lock the canvas
                surfaceHolder.lockCanvas();
                //draw a trail from start to finish
                canvas.drawLine(currentX,currentY,touchedX,touchedY, paint);
                //draw new circle around the end point of the new line
                canvas.drawCircle(touchedX, touchedY, 10, paint);
                //unlock the canvas
                surfaceHolder.unlockCanvasAndPost(canvas);

                //check if the new line intersects any other lines
                //prepare data to get lines equations
                //for stored line
                Line tempLine;
                float tempX1,tempY1,tempX2,tempY2;
                double A1,B1,C1;
                //for new line
                double A2,B2,C2;
                //get the new line equation
                A2 = touchedY-lineStartY;
                B2 = lineStartX-touchedX;
                C2=A2*lineStartX+B2*lineStartY;
                //get length of the new line
                double newLineFullLength = Math.sqrt( Math.pow((touchedX-lineStartX),2) + Math.pow((touchedY-lineStartY),2));

                //start looping through stored lines collections
                for (Object line: linesCollection.values()) {
                    tempLine = (Line)line;

                    //get stored line coordinates
                    tempX1 = tempLine.x1;
                    tempY1 = tempLine.y1;
                    tempX2 = tempLine.x2;
                    tempY2 = tempLine.y2;

                    //get a stored line equation
                    A1 = tempY2-tempY1;
                    B1 = tempX1-tempX2;
                    C1=A1*tempX1+B1*tempY1;
                    //get a stored line full length
                    double comparedLineFullLength = Math.sqrt( Math.pow((tempX2-tempX1),2) + Math.pow((tempY2-tempY1),2));

                    //check if two lines intersect
                    double isIntersect = A1*B2-A2*B1;
                    //lines intersect
                    if (isIntersect != 0) {

                    //calculate the point of intersection
                        double intersectX = (B2*C1-B1*C2)/(B2*A1-B1*A2);
                        double intersectY = (A1*C2-A2*C1)/(A1*B2-A2*B1);

                        //Log.d("!!!", "Line #"+tempLine.number+ " X1 "+tempX1+" Y1 "+tempY1+" X2 "+tempX2+" Y2 "+tempY2 );
                        //Log.d("!!!", "CROSS POINT WITH NEW LINE X: "+intersectX + " Y: "+intersectY);

                        //get length of segments of the line
                        double newLineLengthFromCrossToStart = Math.sqrt( Math.pow((intersectX-lineStartX),2) + Math.pow((intersectY-lineStartY),2));
                        double newLineLengthFromCrossToEnd = Math.sqrt( Math.pow((touchedX-intersectX),2) + Math.pow((touchedY-intersectY),2));
                        //get length of segments of the compared line
                        double comparedLineLengthFromCrossToStart = Math.sqrt( Math.pow((intersectX-tempX1),2) + Math.pow((intersectY-tempY1),2));
                        double comparedLineLengthFromCrossToEnd = Math.sqrt( Math.pow((tempX2-intersectX),2) + Math.pow((tempY2-intersectY),2));

                        //Log.d("!!!", "New line full length: "+ newLineFullLength);
                        //Log.d("!!!", "newLineLengthFromCrossToStart: "+ newLineLengthFromCrossToStart);
                        //Log.d("!!!", "newLineLengthFromCrossToEnd: "+ newLineLengthFromCrossToEnd);
                        //Log.d("!!!", "New Length Start + Length End: "+ (newLineLengthFromCrossToStart + newLineLengthFromCrossToEnd));
                        //Log.d("!!!", "     ");

                        //Log.d("!!!", "Compared line full length: "+ comparedLineFullLength);
                        //Log.d("!!!", "comparedLineLengthFromCrossToStart: "+ comparedLineLengthFromCrossToStart);
                        //Log.d("!!!", "comparedLineLengthFromCrossToEnd: "+ comparedLineLengthFromCrossToEnd);
                        //Log.d("!!!", "Compared Length Start + Length End: "+ (comparedLineLengthFromCrossToStart + comparedLineLengthFromCrossToEnd));
                        //Log.d("!!!", "     ");

                        //set conditions
                        //A:true - crossing point is on the new line
                        //B:true - crossing line is on a stored line
                        boolean A = (int)(newLineLengthFromCrossToStart+newLineLengthFromCrossToEnd)<=(int)newLineFullLength;
                        boolean B = (int)(comparedLineLengthFromCrossToStart+comparedLineLengthFromCrossToEnd)<=(int)comparedLineFullLength;

                        //Log.d("!!!", "A: "+A + " B: "+B);
                        //Log.d("!!!", "*****************************************************");

                        //check to see if intersection point belong to both lines
                        //if yes - teo lines intersect
                        if (A&&B) {

                            //create color settings for two lines
                            Paint tempPaint = new Paint();
                            tempPaint.setColor(Color.BLUE);
                            tempPaint.setStrokeWidth(10);

                            //draw two thick line to show crossing
                            //lock the canvas
                            surfaceHolder.lockCanvas();
                            //draw lines which intersected

                            canvas.drawLine(tempX1,tempY1,tempX2,tempY2, tempPaint);
                            canvas.drawLine(currentX,currentY,touchedX,touchedY, tempPaint);

                            //unlock the canvas
                            surfaceHolder.unlockCanvasAndPost(canvas);

                            //show a message "line crossed"
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "YOU CROSSED YOU TRAIL!!! GAMES OVER!!! ", Toast.LENGTH_SHORT).show();
                                }
                            });

                            //return true;
                        }

                    }
                }//end looping through the line collections

                //store the line in the collection
                lineNumber +=1;
                linesCollection.put(lineNumber, new Line(lineStartX,lineStartY,touchedX,touchedY, lineNumber));

            }//end if (keyOfSuccess)
            return  false;
        }//end drawCircleAndTrail


        //create method onResume to start the thread with drawing
        public void onResume() {
            drawingThread = new Thread(this);
            drawingThread.start();
        }//end onResume

    }//end class MySurfaceView

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:

                //if drawing in the process - don't react
                if (!isRunning) {
                //get touch coordinates
                touchedX = motionEvent.getX();
                touchedY = motionEvent.getY();

                //start drawing process
                isRunning = true;
                }
        }//end switch

        //listen only for one event
        return false;
    }//end onTouch

}//and MainActivity