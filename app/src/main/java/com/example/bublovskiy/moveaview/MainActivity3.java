package com.example.bublovskiy.moveaview;


import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.HashMap;
import java.util.Random;


public class MainActivity3 extends AppCompatActivity implements View.OnTouchListener {

    Resources res;

    SurfaceView mySurfaceView;
    SurfaceHolder surfaceHolder;
    ImageView target;

    //Set up ad view
    AdView adBanner;

    TextView gameOverText, exitTextView, newTextView, scoreTextView;

    boolean shallStopTheGame = false;
    boolean isFirstStart = true;
    boolean needToRedrawLines = false;

    //second thread to draw trail
    Thread drawingThread;

    float currentX, currentY,touchedX,touchedY,swiftChangeX,swiftChangeY;
    //max dimensions of the screen;
    int maxX, maxY;
    //dimensions of the target
    int targetWidth,targetHeight;
    //current target position
    int targetCurrentX,targetCurrentY;
    //game score
    int gameScore = 0;
    //random to move target
    Random random = new Random();
    Canvas canvas;
    Paint paint;

    boolean isRunning = false;
    boolean hasAnotherTouchOccurred = false;
    int mainSpeed;
    //second speed is to be used if the incline of the line is too steep
    //Example: current x = 100, touched x = 105, delta = 105-100 = 5
    //if speed is 10 -> we don't get a single cycle -> we want to decrease speed to something like 0.5
    float secondSpeed;
    int radiusOfCircle;

    //to count amount of lines in the collection
    int lineNumber = 0;
    //to store lines
    HashMap linesCollection = new HashMap<Integer,Line>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //prevent landscape orientation
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //ger R file
        res = getResources();

        //get value from R file
        mainSpeed = res.getInteger(R.integer.mainSpeedOfMovingDots);
        secondSpeed = mainSpeed/res.getInteger(R.integer.secondSpeedLessThanMainIn);
        radiusOfCircle = res.getInteger(R.integer.radiusOfBigCircles);

        //get the text views
        gameOverText = (TextView) findViewById(R.id.textViewGameOver);
        exitTextView = (TextView) findViewById(R.id.exitTextView);
        newTextView = (TextView) findViewById(R.id.newTextView);
        scoreTextView = (TextView) findViewById(R.id.scoreTextView);

        //get target view
        target = (ImageView) findViewById(R.id.targetImageView);

        //set old game font to all text views
        Typeface oldGameFont = Typeface.createFromAsset(getAssets(),"fonts/ARCADECLASSIC.TTF");
        gameOverText.setTypeface(oldGameFont);
        exitTextView.setTypeface(oldGameFont);
        scoreTextView.setTypeface(oldGameFont);
        newTextView.setTypeface(oldGameFont);

        //get surface view and surface holder
        mySurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        //place surface holder on top of the surface view
        mySurfaceView.setZOrderOnTop(true);



        //surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        //set up on Touch listener
        mySurfaceView.setOnTouchListener(this);

        //hide top action bar
        if (getSupportActionBar() !=null) getSupportActionBar().hide();

        //create color settings
        paint = new Paint();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            paint.setColor(res.getColor(R.color.colorOfDots, getTheme()));
        }else{
            paint.setColor(res.getColor(R.color.colorOfDots));
        }

        paint.setStrokeWidth(res.getInteger(R.integer.mainStrokeWidth));

        //get the surface holder of the surface view
        surfaceHolder = mySurfaceView.getHolder();

        //override call back for the surface holder to provide smooth return after on pause not
        //losing previously drown lines
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {

                                      @Override
                                      public void surfaceCreated(final SurfaceHolder surfaceHolder) {

                                          //set pixels of surface holder to transparent to see the color of the surface view
                                          //this method will be implemented each time we put the app on pause
                                          surfaceHolder.setFormat(PixelFormat.TRANSPARENT);

                                          //redraw all lines only if the app resumes after on pause
                                          //and the game was not finished due to the lines crossing
                                          if (needToRedrawLines&&!shallStopTheGame) {
                                              Thread t = new Thread(redrawCanvasAfterPause);
                                              t.start();
                                              needToRedrawLines = false;
                                          }

                                      }//end surfaceCreated

                                      @Override
                                      public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                                      }

                                      @Override
                                      public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                                      }
                                  });

        //Create ad banner
        adBanner = (AdView) findViewById(R.id.adBanner);
        //!!!!! IMPORTANT !!!!! Delete addTestDevice methods when publish the App
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice(getString(R.string.testDeviceID))
                .build();
        adBanner.loadAd(adRequest);

        //create and star thread with the logic
        drawingThread = new Thread(new MainLogic());
        drawingThread.start();


    }//end onCreate

    //redraw the canvas after the user put app on pause
    Runnable redrawCanvasAfterPause = new Runnable() {
        @Override
        public void run() {

            //lock the canvas
            Canvas c = surfaceHolder.lockCanvas();

            //if we have line in the collections - redraw them again;
            if (linesCollection.size()>0) {
                Line line;



                //complete all drawings at once
                for (Object o : linesCollection.values()) {
                    line = (Line) o;
                    c.drawLine(line.x1,line.y1,line.x2,line.y2, paint);
                    c.drawCircle(line.x1,line.y1,radiusOfCircle, paint);
                }

                //draw the circle around the last point
                line = (Line)linesCollection.get(lineNumber);
                c.drawCircle(line.x2,line.y2,radiusOfCircle,paint);


            }
            //if the collection is still empty - then the user has not moved yet the cursor
            //so draw a center circle
            else {
                c.drawCircle(currentX,currentY,radiusOfCircle,paint);
            }

            //unlock and post new canvas
            surfaceHolder.unlockCanvasAndPost(c);

        }// end run
    }; //end redrawCanvasAfterPause

    @Override
    protected void onPause() {
        super.onPause();

        //tell the app to redraw all line in the collection
        needToRedrawLines = true;

    }//end onPause

    //get dimensions of the main window after the window has been drawn
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        //do that only ar the first run
        if (isFirstStart) {
            //get max dimensions of the surface view
            maxX = mySurfaceView.getWidth();
            maxY = mySurfaceView.getHeight();

            //center the start point
            currentX = maxX / 2;
            currentY = maxY / 2;

            //get and lock our view canvas
            canvas = surfaceHolder.lockCanvas();
            //draw a starting position
            canvas.drawCircle(currentX, currentY, radiusOfCircle, paint);
            //unlock canvas to show the changes we have made
            surfaceHolder.unlockCanvasAndPost(canvas);

            //get target width and height
            targetHeight = target.getHeight();
            targetWidth = target.getWidth();

            //set current random target position
            targetCurrentX = random.nextInt((maxX - targetWidth * 2) + targetWidth);
            targetCurrentY = random.nextInt((maxY - targetHeight * 2) + targetHeight);
            target.setX(targetCurrentX);
            target.setY(targetCurrentY);
            //show target at random location
            target.setVisibility(View.VISIBLE);
            //indicate that all is completed for the first app run
            isFirstStart = false;
        }

        super.onWindowFocusChanged(hasFocus);

    }//end onWindowFocusChanged


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if (isRunning) {
                    //read coordinates of the touch in different variables
                    swiftChangeX = motionEvent.getX();
                    swiftChangeY = motionEvent.getY();
                    //signal to the main logic that another touch has just occurred
                    hasAnotherTouchOccurred = true;

                } else {
                    //read coordinates of the touch
                    touchedX = motionEvent.getX();
                    touchedY = motionEvent.getY();
                    //make sure we do not perceive "fresh touch" as "another touch"
                    //another touch is meant to change the direction rapidly
                    hasAnotherTouchOccurred = false;
                    //start drawing process with a "fresh touch"
                    isRunning = true;
                }
        }//end switch (motionEvent.getAction())

        //listen only for one event
        return false;
    }//end onTouch


    //start new game
    public void startNewGame(View view) {

        //if the game was stop due to line conversion
        if (shallStopTheGame) {
            //set surface view background to White
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mySurfaceView.setBackgroundColor(res.getColor(R.color.mainViewBackgroundColor,getTheme()));
            }
            else {
                mySurfaceView.setBackgroundColor(res.getColor(R.color.mainViewBackgroundColor));
            }

            //clear the canvas by making surface holder 100% TRANSPARENT
            surfaceHolder.setFormat(PixelFormat.TRANSPARENT);

            //put away signs the texts
            gameOverText.setVisibility(View.GONE);

            //show the target
            target.setVisibility(View.VISIBLE);

            //begin new game
            shallStopTheGame = false;
        }

        //hide Google Ad
        adBanner.setVisibility(View.GONE);

        //set main score counter to 0
        gameScore = 0;
        //set main score screen to 00000
        scoreTextView.setText(res.getString(R.string.scoreText));

        //clear up the line collection;
        linesCollection = new HashMap<Integer,Line>();

        //center the new starting point
        currentX = mySurfaceView.getWidth()/2;
        currentY = mySurfaceView.getHeight()/2;

        //draw new starting point
        //get and lock our view canvas
        canvas = surfaceHolder.lockCanvas();
        //clear the canvas from any previous drawings
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        //draw a starting position
        canvas.drawCircle(currentX , currentY, radiusOfCircle, paint);
        //unlock canvas to show the changes we have made
        surfaceHolder.unlockCanvasAndPost(canvas);

        //set new random target position
        targetCurrentX = random.nextInt((maxX-targetWidth*2)+targetWidth);
        targetCurrentY = random.nextInt((maxY-targetHeight*2)+targetHeight);
        target.setX(targetCurrentX);
        target.setY(targetCurrentY);

    }//end startNewGame

    //finish the game
    public void endTheGame(View view) {
        //this.finish();
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }//endTheGame

    //main logic of the code
    private class MainLogic implements Runnable {

        //implement the main logic of drawing
        @Override
        public void run() {

            while (true) {
                //keep running the method RUN constantly unless the game is over
                while (!shallStopTheGame) {

                    //while the isRunning = true - keeps running the thread
                    while (isRunning) {

                        float slope;
                        boolean isXNotChanging = false;
                        boolean didDrawingOccurred = false;
                        boolean isXandYAlreadyAssigned = false;
                        float lineStartX = 0, lineStartY = 0;
                        float tempMovingSpeed;
                        int changeToSecondSpeedIfLessThan = res.getInteger(R.integer.changeToSecondSpeedIfLessThan);
                        int radiusOfSmallCircles = res.getInteger(R.integer.radiusOfSmallCircles);
                        Rect tempRect;

                        //calculate the slope
                        if (touchedX - currentX != 0) {
                            slope = (touchedY - currentY) / (touchedX - currentX);
                        } else {
                            slope = 0;
                            isXNotChanging = true;
                        }

                        //determine direction of drawing : to the left or to the right
                        //if currentX - touchedX = 0 then the line is strictly vertical
                        //if the line is not vertical - calculate X
                        if (!isXNotChanging) {
                            //TO THE RIGHT
                            if (currentX < touchedX) {

                                //if we get less than N(2) Y coordinates of the new line -> decrease moving speed
                                tempMovingSpeed = ((touchedX-currentX)/ mainSpeed)<=changeToSecondSpeedIfLessThan ? secondSpeed : mainSpeed;

                                // /start drawing the thread dynamically
                                for (float dynamicX = currentX; dynamicX <= touchedX; dynamicX += tempMovingSpeed) {

                                    //calculate new dynamic Y coordinate
                                    float dynamicY = slope * (dynamicX - currentX) + currentY;

                                    //if during drawing the user touched another spot
                                    if (hasAnotherTouchOccurred) {
                                        //draw trail and analise for intersection
                                        //true - means that some drawing has already happened so we need:
                                        //                      draw trail
                                        //                      check for intersections with other lines in the collection
                                        //                      put new line in the collection
                                        drawCircleAndTrail(true, currentX, currentY, dynamicX, dynamicY, slope);
                                        //refresh/catch the current position's coordinates at which interruption has happened
                                        //to use them as a starting point for new drawing
                                        currentX = dynamicX;
                                        currentY = dynamicY;
                                        //refresh coordinates of the "another touch"
                                        touchedX = swiftChangeX;
                                        touchedY = swiftChangeY;
                                        //exit the main IF to start the process over again
                                        break;
                                    }

                                    //define rectangle area to be locked on the canvas
                                    //if we look the entire canvas and post it again the all screen will be flickering
                                    tempRect = new Rect((int)dynamicX-radiusOfSmallCircles,(int)dynamicY-radiusOfSmallCircles,(int)dynamicX+radiusOfSmallCircles,(int)dynamicY+radiusOfSmallCircles);
                                    //lock the canvas
                                    surfaceHolder.lockCanvas(tempRect);
                                    //draw new dot of the line
                                    canvas.drawCircle(dynamicX, dynamicY, radiusOfSmallCircles, paint);
                                    //unlock the canvas
                                    surfaceHolder.unlockCanvasAndPost(canvas);

                                    //after drawing each dot of the new line check if we hit the target
                                    isTargetHit(dynamicX,dynamicY);

                                    //safe the start point of the new line which is not on the previous line
                                    //we do that only once per a cycle
                                    if (!isXandYAlreadyAssigned) {
                                        lineStartX = dynamicX;
                                        lineStartY = dynamicY;
                                        isXandYAlreadyAssigned = true;
                                    }

                                    //set the flag didDrawingOccurred to true saying the drawing has been successful
                                    //it is just a sign that we have entered this loop into TO THE RIGHT direction
                                    if (!didDrawingOccurred) didDrawingOccurred = true;

                                }

                            }

                            //TO THE LEFT
                            else {

                                //if we get less than three Y coordinates of the new line -> decrease moving speed
                                tempMovingSpeed = (currentX-touchedX)/ mainSpeed <=changeToSecondSpeedIfLessThan ? secondSpeed : mainSpeed;

                                //start drawing the thread dynamically
                                for (float dynamicX = currentX; dynamicX >= touchedX; dynamicX -= tempMovingSpeed) {

                                    //calculate new dynamic Y coordinate
                                    float dynamicY = slope * (dynamicX - currentX) + currentY;

                                    //if during drawing the user touched another spot
                                    if (hasAnotherTouchOccurred) {
                                        //draw trail and analise for intersection
                                        //true - means that some drawing has already happaned so we need:
                                        //                      draw trail
                                        //                      check for intersections with other lines in the collection
                                        //                      put new line in the collection
                                        drawCircleAndTrail(true, currentX, currentY, dynamicX, dynamicY, slope);
                                        //refresh/catch the current position's coordinates at which interruption has happened
                                        //to use them as a starting point for new drawing
                                        currentX = dynamicX;
                                        currentY = dynamicY;
                                        //refresh coordinates of the "another touch"
                                        touchedX = swiftChangeX;
                                        touchedY = swiftChangeY;
                                        //exit the main IF to start the process over again
                                        break;
                                    }

                                    //define rectangle area to be locked on the canvas
                                    //if we look the entire canvas and post it again the all screen will be flickering
                                    tempRect = new Rect((int)dynamicX-radiusOfSmallCircles,(int)dynamicY-radiusOfSmallCircles,(int)dynamicX+radiusOfSmallCircles,(int)dynamicY+radiusOfSmallCircles);
                                    surfaceHolder.lockCanvas(tempRect);
                                    //draw new dot of the line
                                    canvas.drawCircle(dynamicX, dynamicY, radiusOfSmallCircles, paint);
                                    //unlock the canvas
                                    surfaceHolder.unlockCanvasAndPost(canvas);

                                    //after drawing each dot of the new line check if we hit the target
                                    isTargetHit(dynamicX,dynamicY);

                                    //safe the start point of the new line which is not on the previous line
                                    if (!isXandYAlreadyAssigned) {
                                        lineStartX = dynamicX;
                                        lineStartY = dynamicY;
                                        isXandYAlreadyAssigned = true;
                                    }

                                    //set the flag didDrawingOccurred to true saying the drawing has been successful
                                    if (!didDrawingOccurred) didDrawingOccurred = true;
                                }
                            }
                        }
                        //if current X and touched X are equal - the new line is strictly vertical
                        else

                        {
                            //X won't be changing
                            float dynamicX = touchedX;

                            //DOWN
                            if (currentY < touchedY) {
                                //start drawing the thread dynamically
                                for (float dynamicY = currentY; dynamicY <= touchedY; dynamicY += mainSpeed) {

                                    //if during drawing the user touched another spot
                                    if (hasAnotherTouchOccurred) {
                                        //draw trail and analise for intersection
                                        //true - means that some drawing has already happaned so we need:
                                        //                      draw trail
                                        //                      check for intersections with other lines in the collection
                                        //                      put new line in the collection
                                        drawCircleAndTrail(true, currentX, currentY, dynamicX, dynamicY, slope);
                                        //refresh/catch the current position's coordinates at which interruption has happened
                                        //to use them as a starting point for new drawing
                                        currentX = dynamicX;
                                        currentY = dynamicY;
                                        //refresh coordinates of the "another touch"
                                        touchedX = swiftChangeX;
                                        touchedY = swiftChangeY;
                                        //exit the main IF to start the process over again
                                        break;
                                    }

                                    //define rectangle area to be locked on the canvas
                                    //if we look the entire canvas and post it again the all screen will be flickering
                                    tempRect = new Rect((int)dynamicX-radiusOfSmallCircles,(int)dynamicY-radiusOfSmallCircles,(int)dynamicX+radiusOfSmallCircles,(int)dynamicY+radiusOfSmallCircles);
                                    surfaceHolder.lockCanvas(tempRect);
                                    //draw new dot of the line
                                    canvas.drawCircle(dynamicX, dynamicY, radiusOfSmallCircles, paint);
                                    //unlock the canvas
                                    surfaceHolder.unlockCanvasAndPost(canvas);

                                    //after drawing each dot of the new line check if we hit the target
                                    isTargetHit(dynamicX,dynamicY);

                                    //safe the start point of the new line which is not on the previous line
                                    if (!isXandYAlreadyAssigned) {
                                        lineStartX = dynamicX;
                                        lineStartY = dynamicY;
                                        isXandYAlreadyAssigned = true;
                                    }

                                    //set the flag didDrawingOccurred to true saying the drawing has been successful
                                    if (!didDrawingOccurred) didDrawingOccurred = true;

                                }
                            }
                            //UP
                            else {
                                //start drawing the thread dynamically
                                for (float dynamicY = currentY; dynamicY >= touchedY; dynamicY -= mainSpeed) {

                                    //if during drawing the user touched another spot
                                    if (hasAnotherTouchOccurred) {
                                        //draw trail and analise for intersection
                                        //true - means that some drawing has already happaned so we need:
                                        //                      draw trail
                                        //                      check for intersections with other lines in the collection
                                        //                      put new line in the collection
                                        drawCircleAndTrail(true, currentX, currentY, dynamicX, dynamicY, slope);
                                        //refresh/catch the current position's coordinates at which interruption has happened
                                        //to use them as a starting point for new drawing
                                        currentX = dynamicX;
                                        currentY = dynamicY;
                                        //refresh coordinates of the "another touch"
                                        touchedX = swiftChangeX;
                                        touchedY = swiftChangeY;
                                        //exit the main IF to start the process over again
                                        break;
                                    }


                                    //define rectangle area to be locked on the canvas
                                    //if we look the entire canvas and post it again the all screen will be flickering
                                    tempRect = new Rect((int)dynamicX-radiusOfSmallCircles,(int)dynamicY-radiusOfSmallCircles,(int)dynamicX+radiusOfSmallCircles,(int)dynamicY+radiusOfSmallCircles);
                                    surfaceHolder.lockCanvas(tempRect);
                                    //draw new dot of the line
                                    canvas.drawCircle(dynamicX, dynamicY, radiusOfSmallCircles, paint);
                                    //unlock the canvas
                                    surfaceHolder.unlockCanvasAndPost(canvas);

                                    //after drawing each dot of the new line check if we hit the target
                                    isTargetHit(dynamicX,dynamicY);

                                    //safe the start point of the new line which is not on the previous line
                                    if (!isXandYAlreadyAssigned) {
                                        lineStartX = dynamicX;
                                        lineStartY = dynamicY;
                                        isXandYAlreadyAssigned = true;
                                    }

                                    //set the flag didDrawingOccurred to true saying the drawing has been successful
                                    if (!didDrawingOccurred) didDrawingOccurred = true;
                                }
                            }
                        }//edn global IF


                        //set to false to indicate that we got that touch
                        if (hasAnotherTouchOccurred) {
                            hasAnotherTouchOccurred = false;
                            //go to the very start of the loop to commence the process over again
                            continue;
                        }

                        //double check if we hit the target
                        isTargetHit(touchedX,touchedY);

                        //if drawing had occurred then draw a circle and a trail
                        drawCircleAndTrail(didDrawingOccurred, lineStartX, lineStartY, touchedX, touchedY, slope);

                        //if we are at this point it means the drawing has stop because the gamer did not
                        //react on time to provide a new direction
                        //uncomment the next line to mekr the game more difficult
                        //runOnUiThread(stopTheGame);

                        //update current coordinates if drawing has occurred
                        if (didDrawingOccurred) {
                            currentX = touchedX;
                            currentY = touchedY;
                        }

                        //stop drawing until the next touch occurs
                        isRunning = false;

                    }//end While
                }//end outer While (shallStopTheGame)
            }//end global While (true)
        }//end run method

        //draw final circle and a trail
        private void drawCircleAndTrail(boolean keyOfSuccess, float lineStartX, float lineStartY,float lineEndsX, float lineEndsY, double lineSlope) {

            //if drawing had occurred then draw a circle
            if (keyOfSuccess) {

                //lock the canvas
                surfaceHolder.lockCanvas();
                //draw a trail from start to finish
                canvas.drawLine(lineStartX,lineStartY,lineEndsX,lineEndsY, paint);
                //draw new circle around the end point of the new line
                canvas.drawCircle(lineEndsX, lineEndsY, res.getInteger(R.integer.radiusOfBigCircles), paint);
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
                A2 = lineEndsY-lineStartY;
                B2 = lineStartX-lineEndsX;
                C2=A2*lineStartX+B2*lineStartY;
                //get length of the new line
                double newLineFullLength = Math.sqrt( Math.pow((lineEndsX-lineStartX),2) + Math.pow((lineEndsY-lineStartY),2));

                //start looping through stored lines collections
                //to see if we crossed any of them
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

                        //get length of segments of the new line
                        double newLineLengthFromCrossToStart = Math.sqrt( Math.pow((intersectX-lineStartX),2) + Math.pow((intersectY-lineStartY),2));
                        double newLineLengthFromCrossToEnd = Math.sqrt( Math.pow((lineEndsX-intersectX),2) + Math.pow((lineEndsY-intersectY),2));
                        //get length of segments of the compared line
                        double comparedLineLengthFromCrossToStart = Math.sqrt( Math.pow((intersectX-tempX1),2) + Math.pow((intersectY-tempY1),2));
                        double comparedLineLengthFromCrossToEnd = Math.sqrt( Math.pow((tempX2-intersectX),2) + Math.pow((tempY2-intersectY),2));

                        //set conditions
                        //A:true - crossing point is on the new line
                        //B:true - crossing line is on a stored line
                        boolean A = (int)(newLineLengthFromCrossToStart+newLineLengthFromCrossToEnd)<=(int)newLineFullLength;
                        boolean B = (int)(comparedLineLengthFromCrossToStart+comparedLineLengthFromCrossToEnd)<=(int)comparedLineFullLength;

                        //check to see if intersection point belong to both lines
                        //if yes - teo lines intersect
                        //tempLine.number != lineNumber means that the new line is not the next line running right from a checked line
                        if (A&&B&&(tempLine.number != lineNumber)) {

                            //create color settings for two lines
                            Paint tempPaint = new Paint();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                tempPaint.setColor(res.getColor(R.color.colorOfCrossingLines,getTheme()));
                            }
                            else {
                                tempPaint.setColor(res.getColor(R.color.colorOfCrossingLines));
                            }
                            tempPaint.setStrokeWidth(res.getInteger(R.integer.mainStrokeWidth));

                            //draw two thick line to show crossing
                            //lock the canvas
                            surfaceHolder.lockCanvas();

                            //draw lines which intersected
                            canvas.drawLine(tempX1,tempY1,tempX2,tempY2, tempPaint);
                            canvas.drawLine(currentX,currentY,lineEndsX,lineEndsY, tempPaint);

                            //unlock the canvas
                            surfaceHolder.unlockCanvasAndPost(canvas);

                            //show a message "line crossed"
                             runOnUiThread(stopTheGame);
                        }//end if ((A&&B)&&(tempLine.number != lineNumber))
                    }
                }//end looping through the line collections

                //increase game score
                gameScore +=res.getInteger(R.integer.regularScore);
                //show score on the screen
                runOnUiThread(showScore);
                //store the line in the collection
                lineNumber +=1;
                linesCollection.put(lineNumber, new Line(lineStartX, lineStartY, lineEndsX, lineEndsY, lineNumber));


            }//end if (keyOfSuccess)
        }//end drawCircleAndTrail

        //show score on the screen
        Runnable showScore = new Runnable() {
            @Override
            public void run() {
                //show new game score on the screen
                scoreTextView.setText(gameScore+"");
            }
        };//end showScore


        //stop the game procedure
        Runnable stopTheGame = new Runnable() {

        @Override
        public void run() {

            //tell the main thread to stop the games
            shallStopTheGame = true;

            //stop thread to let the game see where the lines crossed
            try {
                Thread.sleep(res.getInteger(R.integer.sleepAfterLinesCrossed));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //hide the target
            target.setVisibility(View.GONE);
            //clear the canvas by making surface holder 100% opaque
            surfaceHolder.setFormat(PixelFormat.OPAQUE);
            //set the texts
            gameOverText.setVisibility(View.VISIBLE);
            //set red background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mySurfaceView.setBackgroundColor(res.getColor(R.color.colorOfGameOverScreen,getTheme()));
            }
            else {
                mySurfaceView.setBackgroundColor(res.getColor(R.color.colorOfGameOverScreen));
            }
            //show the Google Ad
            adBanner.setVisibility(View.VISIBLE);

        }// end run method
    };//end stopTheGame Runnable;


        //check if the target got hit
        private void isTargetHit (float cursorX, float cursorY) {
        //determine if the current dot in the area of the target coordinates
        //TO REMEMBER: the target's view setX and setY set the top left corner to display the view
        //we need consider this when calculating the coordinates of the target's centre
        //Target's center x,y: X = targetCurrentX+targetWidth/2
        //                     Y = targetCurrentY+targetHeight/2
        boolean A = (cursorX >= targetCurrentX)&&(cursorX<=targetCurrentX + targetWidth);
        boolean B = (cursorY >= targetCurrentY)&&(cursorY<=targetCurrentY + targetHeight);

        //check if target got hit
        if (A&&B) {
            //set current random target position
            targetCurrentX = random.nextInt((maxX-targetWidth*2)+targetWidth);
            targetCurrentY = random.nextInt((maxY-targetHeight*2)+targetHeight);
            target.setX(targetCurrentX);
            target.setY(targetCurrentY);
            //give extra score for target hit
            gameScore +=res.getInteger(R.integer.targetHitScore);
            runOnUiThread(showScore);
        }
    }//end isTargetHit

    }//end MainLogic class

}//end MainActivity3
