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

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import lib.Utils;
import location.OrientationManager;
import location.Place;


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
    private List<Place> mNearbyPlaces;

    private final Paint mPaint;
    private final TextPaint mPlacePaint;
    private final List<Rect> mAllBounds;
    private final NumberFormat mDistanceFormat;
    private final ValueAnimator mAnimator;

    public ARView(Context context) {
        this(context, null, 0);
    }

    public ARView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ARView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

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
        setupAnimator();
    }

    /**
     * Sets the instance of {@link OrientationManager} that this view will use to get the current
     * heading and location.
     *
     * @param orientationManager the instance of {@code OrientationManager} that this view will use
     */
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

 
    public void setNearbyPlaces(List<Place> places) {
        mNearbyPlaces = places;
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
            drawPlaces(canvas, pixelsPerDegree, i * pixelsPerDegree * 360);
        }

        canvas.restore();
    }

  
    private void drawPlaces(Canvas canvas, float pixelsPerDegree, float offset) {
        if (mOrientation.hasLocation() && mNearbyPlaces != null) {
            synchronized (mNearbyPlaces) {
                Location userLocation = mOrientation.getLocation();
                double latitude1 = userLocation.getLatitude();
                double longitude1 = userLocation.getLongitude();

                mAllBounds.clear();

                // Loop over the list of nearby places (those within 10 km of the user's current
                // location), and compute the relative bearing from the user's location to the
                // place's location. This determines the position on the compass view where the
                // pin will be drawn.
                int place_index = 0;
                for (Place place : mNearbyPlaces) {
                    double latitude2 = place.getLatitude();
                    double longitude2 = place.getLongitude();
                    float bearing = Utils.getBearing(latitude1, longitude1, latitude2,
                            longitude2);

                    int fx = (int) (offset + bearing * pixelsPerDegree
                            - PLACE_PIN_WIDTH / 2);
                    int fy = -160;
                    //canvas.drawRect(fx, fy, fx+150, fy+150, mPlacePaint);
                    
                    System.out.println("Place "+place_index++ + " fx: "+fx+" fy: "+ fy);
                    Rect rect = new Rect(fx,fy,fx+560,fy+320);

                    RectF rectF = new RectF(rect);
                    
                    canvas.drawRoundRect(rectF, 40, 40, mPlacePaint);
                }
            }
        }
    }

    /**
     * Sets up a {@link ValueAnimator} that will be used to animate the compass
     * when the distance between two sensor events is large.
     */
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

        // Notifies us when the animation is over. During an animation, the user's head may have
        // continued to move to a different orientation than the original destination angle of the
        // animation. Since we can't easily change the animation goal while it is running, we call
        // animateTo() again, which will either redraw at the new orientation (if the difference is
        // small enough), or start another animation to the new heading. This seems to produce
        // fluid results.
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
