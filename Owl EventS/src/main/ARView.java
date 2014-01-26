/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package main;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.*;
import android.location.Location;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import lib.EventFetcher;
import lib.Utils;
import location.OrientationManager;
import location.Event;


public class ARView extends View {

    /** Various dimensions and other drawing-related constants. */
    private static final float DIRECTION_TEXT_HEIGHT = 84.0f;
    private static final float PLACE_TEXT_HEIGHT = 22.0f;
    private static final float PLACE_PIN_WIDTH = 14.0f;

    /**
     * If the difference between two consecutive headings is less than this value, the canvas will
     * be redrawn immediately rather than animated.
     */
    private static final float MIN_DISTANCE_TO_ANIMATE = 15.0f;

    /** The actual heading that represents the direction that the user is facing. */
    private float mHeading;

    /**
     * Represents the heading that is currently being displayed when the view is drawn. This is
     * used during animations, to keep track of the heading that should be drawn on the current
     * frame, which may be different than the desired end point.
     */
    private float mAnimatedHeading;

    private OrientationManager mOrientation;
    private List<Event> mNearbyPlaces;

    private final Paint mPaint;
    private final TextPaint mPlacePaint;
    private final List<Rect> mAllBounds;
    private final NumberFormat mDistanceFormat;
    private final ValueAnimator mAnimator;
    private final Context mcontext;
    
    private EventFetcher eventFetcher;

    private Timer timer = new Timer();
    
    public ARView(Context context) {
        this(context, null, 0);
    }

    public ARView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ARView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        this.mNearbyPlaces = new ArrayList<Event>();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(DIRECTION_TEXT_HEIGHT);
        mPaint.setTypeface(Typeface.createFromFile(new File("/system/glass_fonts",
                "Roboto-Thin.ttf")));

        mPlacePaint = new TextPaint();
        mPlacePaint.setStyle(Paint.Style.FILL);
        mPlacePaint.setAntiAlias(true);
        mPlacePaint.setColor(Color.WHITE);
        mPlacePaint.setTextSize(PLACE_TEXT_HEIGHT);
        mPlacePaint.setTypeface(Typeface.createFromFile(new File("/system/glass_fonts",
                "Roboto-Light.ttf")));

        mAllBounds = new ArrayList<Rect>();

        mDistanceFormat = NumberFormat.getNumberInstance();
        mDistanceFormat.setMinimumFractionDigits(0);
        mDistanceFormat.setMaximumFractionDigits(1);

        mAnimatedHeading = Float.NaN;

        mAnimator = new ValueAnimator();
        eventFetcher = new EventFetcher();
        eventFetcher.getAllEvents(mNearbyPlaces);
        timer.schedule(new TimerTask() {
			
 			@Override
 			public void run() {
 				eventFetcher.getAllEvents(mNearbyPlaces);
 			}
 		}, 1000, 25000);
        this.mcontext = context;
        setupAnimator();
    }

  
    public void setOrientationManager(OrientationManager orientationManager) {
        mOrientation = orientationManager;
    }

    /**
     * Gets the current heading in degrees.
     *
     * @return the current heading.
     */
    public float getHeading() {
        return mHeading;
    }

 
    public void setHeading(float degrees) {
        mHeading = Utils.mod(degrees, 360.0f);
        animateTo(mHeading);
    }

 
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // The view displays 90 degrees across its width so that one 90 degree head rotation is
        // equal to one full view cycle.
        float pixelsPerDegree = getWidth() / 90.0f;
        float centerX = getWidth() / 2.0f;
        float centerY = getHeight() / 2.0f;

        canvas.save();
        canvas.translate(-mAnimatedHeading * pixelsPerDegree + centerX, centerY);

        // In order to ensure that places on a boundary close to 0 or 360 get drawn correctly, we
        // draw them three times; once to the left, once at the "true" bearing, and once to the
        // right.
        for (int i = -1; i <= 1; i++) {
            drawEvents(canvas, pixelsPerDegree, i * pixelsPerDegree * 360);
        }

        canvas.restore();
    }
    
  
    private void drawEvents(Canvas canvas, float pixelsPerDegree, float offset) {
        if (mOrientation.hasLocation() && mNearbyPlaces != null) {
            synchronized (mNearbyPlaces) {
                Location userLocation = mOrientation.getLocation();
                double latitude1 = userLocation.getLatitude();
                double longitude1 = userLocation.getLongitude();

                mAllBounds.clear();

                int place_index = 0;
                
                System.out.println("Size of Nearby Places : "+mNearbyPlaces.size());
                ArrayList<Event> eventsCopy = new ArrayList<Event>(mNearbyPlaces);
                for (Event place : eventsCopy) {
                    double latitude2 = place.getLatitude();
                    double longitude2 = place.getLongitude();
                    float bearing = Utils.getBearing(latitude1, longitude1, latitude2,
                            longitude2);

                    int fx = (int) (offset + bearing * pixelsPerDegree
                            - PLACE_PIN_WIDTH / 2);
                    int fy = -160;
                    
                    //System.out.println("Place "+place_index++ + " fx: "+fx+" fy: "+ fy);
                    //Rect rect = new Rect(fx,fy,fx+560,fy+320);

                    //RectF rectF = new RectF(rect);
                    
                    //canvas.drawRoundRect(rectF, 40, 40, mPlacePaint);
                    
                    drawEvent(canvas, fx, "helloWorld", "helloWorld", "helloWorld", place.getCreatorPicture(), mPaint);
                }
            }
        }
    }

    
    private static final double vertical_ratio = 0.5;
    private static final double horizontal_ratio = 0.8;
    
    private void drawEvent(Canvas canvas, int offset, String textUpperRight, String textLowerLeft,
    					   String textLowerRight, Bitmap profile_picture, Paint paint) {
         //canvas.drawPaint(paint);
    	  paint = new TextPaint();
    	  paint.setStyle(Paint.Style.FILL);
    	  paint.setAntiAlias(true);
    	  paint.setColor(Color.WHITE);
    	  paint.setTextSize(PLACE_TEXT_HEIGHT);
    	  paint.setTypeface(Typeface.createFromFile(new File("/system/glass_fonts",
                  "Roboto-Light.ttf")));
          
//        TextView upperRightView = new TextView(mcontext);
//        TextView lowerRightView = new TextView(mcontext);
//        TextView lowerLeftView = new TextView(mcontext);
//        ImageView imageView = new ImageView(mcontext);
//        
//
//        imageView.setImageBitmap(profile_picture);
//        upperRightView.setText(textUpperRight);
//        lowerRightView.setText(textLowerRight);
//        lowerLeftView.setText(textLowerLeft);
//        
//        upperRightView.setDrawingCacheEnabled(true);
//        lowerRightView.setDrawingCacheEnabled(true);
//        lowerLeftView.setDrawingCacheEnabled(true);
//        imageView.setDrawingCacheEnabled(true);
//        
//        upperRightView.setTextColor(Color.BLACK);
//        upperRightView.setBackgroundColor(Color.WHITE);
//        lowerRightView.setTextColor(Color.BLACK);
//        lowerRightView.setBackgroundColor(Color.WHITE);
//        lowerLeftView.setTextColor(Color.BLACK);
//        lowerLeftView.setBackgroundColor(Color.WHITE);
        
        canvas.drawText(textUpperRight,(float) (offset+560 *(1-vertical_ratio)), 0,paint);
        canvas.drawText(textLowerRight,(float) (offset+560 *(1-vertical_ratio)), (float) (320 * (1-horizontal_ratio)),paint);
        canvas.drawText(textLowerLeft,offset,(float) (320 * (1-horizontal_ratio)), paint);
        
        		
//        upperRightView.measure(MeasureSpec.makeMeasureSpec((int) (560*(1-vertical_ratio)), MeasureSpec.EXACTLY),
//       		 MeasureSpec.makeMeasureSpec((int) (320*(horizontal_ratio)), MeasureSpec.EXACTLY));
//        lowerRightView.measure(MeasureSpec.makeMeasureSpec((int) (560*(1-vertical_ratio)), MeasureSpec.EXACTLY),
//       		 MeasureSpec.makeMeasureSpec((int) (320*(horizontal_ratio)), MeasureSpec.EXACTLY));
//        lowerLeftView.measure(MeasureSpec.makeMeasureSpec((int) (560*(vertical_ratio)), MeasureSpec.EXACTLY),
//       		 MeasureSpec.makeMeasureSpec((int) (320*(1-horizontal_ratio)), MeasureSpec.EXACTLY));
//        imageView.measure(MeasureSpec.makeMeasureSpec((int) (560*(vertical_ratio)), MeasureSpec.EXACTLY),
//       		 MeasureSpec.makeMeasureSpec((int) (320*(horizontal_ratio)), MeasureSpec.EXACTLY));
//        upperRightView.setGravity(0x10);
//
//        
//        upperRightView.layout((int)(rightOffset+560*vertical_ratio),0,560+rightOffset,(int)(320*horizontal_ratio));
//        lowerRightView.layout((int)(rightOffset+560*vertical_ratio),(int)(320*horizontal_ratio),560+rightOffset,320);
//        lowerLeftView.layout((int)rightOffset,(int)(320*horizontal_ratio),(int)(rightOffset+560*vertical_ratio),320);             
//        imageView.layout(rightOffset,0,(int)(rightOffset+560*vertical_ratio),(int)(320*horizontal_ratio));

        // draw the bitmap from the drawingcache to the canvas
//        canvas.drawBitmap(upperRightView.getDrawingCache(), (float) (rightOffset+560*vertical_ratio), 0, paint);
//        canvas.drawBitmap(lowerRightView.getDrawingCache(), (float) (rightOffset+560*vertical_ratio),
//       		 (float) (320*horizontal_ratio), paint);
//        canvas.drawBitmap(lowerLeftView.getDrawingCache(), rightOffset,  (float) (320*horizontal_ratio), paint);
        //canvas.drawBitmap(imageView.getDrawingCache(), rightOffset, 0, paint);


        // disable drawing cache
//        upperRightView.setDrawingCacheEnabled(false);
//        lowerRightView.setDrawingCacheEnabled(false);
//        lowerLeftView.setDrawingCacheEnabled(false);
//        imageView.setDrawingCacheEnabled(false);
    }

   
    private void setupAnimator() {
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setDuration(250);

        // Notifies us at each frame of the animation so we can redraw the view.
        mAnimator.addUpdateListener(new AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mAnimatedHeading = Utils.mod((Float) mAnimator.getAnimatedValue(), 360.0f);
                invalidate();
            }
        });

        mAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animator) {
                animateTo(mHeading);
            }
        });
    }

    /**
     * Animates the view to the specified heading, or simply redraws it immediately if the
     * difference between the current heading and new heading are small enough that it wouldn't be
     * noticeable.
     *
     * @param end the desired heading
     */
    private void animateTo(float end) {
        // Only act if the animator is not currently running. If the user's orientation changes
        // while the animator is running, we wait until the end of the animation to update the
        // display again, to prevent jerkiness.
        if (!mAnimator.isRunning()) {
            float start = mAnimatedHeading;
            float distance = Math.abs(end - start);
            float reverseDistance = 360.0f - distance;
            float shortest = Math.min(distance, reverseDistance);

            if (Float.isNaN(mAnimatedHeading) || shortest < MIN_DISTANCE_TO_ANIMATE) {
                // If the distance to the destination angle is small enough (or if this is the
                // first time the compass is being displayed), it will be more fluid to just redraw
                // immediately instead of doing an animation.
            	System.out.println("LOG heading data: "+end);
                mAnimatedHeading = end;
                invalidate();
            } else {
                // For larger distances (i.e., if the compass "jumps" because of sensor calibration
                // issues), we animate the effect to provide a more fluid user experience. The
                // calculation below finds the shortest distance between the two angles, which may
                // involve crossing 0/360 degrees.
                float goal;

                if (distance < reverseDistance) {
                    goal = end;
                } else if (end < start) {
                    goal = end + 360.0f;
                } else {
                    goal = end - 360.0f;
                }

                mAnimator.setFloatValues(start, goal);
                mAnimator.start();
            }
        }
    }
}
