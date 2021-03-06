package main;


import android.animation.*;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.*;
import android.location.Location;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import java.util.*;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

import lib.EventFetcher;
import lib.Utils;
import location.OrientationManager;
import location.Event;


public class ARView extends View {

	private static final int CARD_FRAME_OFFSET = 10;
	private static final int NUMBER_EMPTY_CARDS = 12;
	private static final float EVENT_PICTURE_OFFSETX = 560/20;
	private static final float EVENT_PICTURE_OFFSETY = 320/20;
    private static final float EVENT_TEXT_SIZE = 22.0f;
    private static final float LOADING_TEXT_SIZE = 50.0f;
    private static final double vertical_ratio = 0.5;
    private static final double horizontal_ratio = 0.8;

    /**
     * If the difference between two consecutive headings is less than this value, the canvas will
     * be redrawn immediately rather than animated.
     */
    private static final float MIN_DISTANCE_TO_ANIMATE = 15.0f;

    private float mHeading;
    private float mAnimatedHeading;
    private final TextPaint loadingTextPaint;
    private final TextPaint eventTextPaint;
    private final TextPaint eventBackgroundPaint;
    private final ValueAnimator mAnimator;
    private EventFetcher eventFetcher;
    private OrientationManager mOrientation;
    private boolean eventsLoaded = false;
    public static List<Event> owlevents;
    public static Event eventInSight = null;


    private Timer timer = new Timer();
    
    public ARView(Context context) {
        this(context, null, 0);
    }

    public ARView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ARView(final Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setBackgroundColor(Color.DKGRAY);
        this.invalidate();
        
        owlevents = new ArrayList<Event>();

        loadingTextPaint = new TextPaint();
        loadingTextPaint.setStyle(Paint.Style.FILL);
        loadingTextPaint.setAntiAlias(true);
        loadingTextPaint.setColor(Color.WHITE);
        loadingTextPaint.setTextSize(LOADING_TEXT_SIZE);
        
        Typeface tf = Typeface.create("Helvetica",Typeface.BOLD);
        eventTextPaint = new TextPaint();
        eventTextPaint.setTypeface(tf);
        eventTextPaint.setStyle(Paint.Style.FILL);
        eventTextPaint.setAntiAlias(true);
        eventTextPaint.setColor(Color.BLACK);
        eventTextPaint.setTextSize(EVENT_TEXT_SIZE);
        
        eventBackgroundPaint  = new TextPaint();
        eventBackgroundPaint.setStyle(Paint.Style.FILL);
        eventBackgroundPaint.setAntiAlias(true);
        eventBackgroundPaint.setColor(Color.WHITE);
        eventsLoaded = false;
        mAnimatedHeading = Float.NaN;

        mAnimator = new ValueAnimator();
        eventFetcher = new EventFetcher(context);
        
        GetCallback<ParseObject> onEventDataLoadedCallback = new GetCallback<ParseObject>() {
			@Override
			public void done(ParseObject object, ParseException e) {
				eventsLoaded = true;
			}
		};
        eventFetcher.getAllEvents(owlevents, onEventDataLoadedCallback);
        timer.schedule(new TimerTask() {
			
 			@Override
 			public void run() {
 				eventFetcher.getAllEvents(owlevents, null);
 			}
 		}, 1000, 25000);
        setupAnimator();
    }

    @Override
	protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    	canvas.save();
        if (eventsLoaded) {
        	float pixelsPerCard = getWidth() / 30.0f;
        	float centerX = getWidth() / 2.0f;
        	float centerY = getHeight() / 2.0f;

        	canvas.translate(-mAnimatedHeading * pixelsPerCard + centerX, centerY);
        	drawEmptyCards(canvas, pixelsPerCard);
        	for (int i = -1; i <= 1; i++) {
        		drawEvents(canvas, pixelsPerCard, i * pixelsPerCard * 360);
        	}
        } else {
        	drawLoading(canvas);
        }
    	canvas.restore();       
    }
   
    
    
    private void drawLoading(Canvas canvas) {
    	canvas.drawText("Loading ...", 100, 200, loadingTextPaint);
    }
    
    private void drawEmptyCards(Canvas canvas, float pixelsPerCard) {
    	
    	float degreesPerCard = 360 / NUMBER_EMPTY_CARDS;
    	
    	Boolean [] non_alpha = new Boolean [NUMBER_EMPTY_CARDS];
    	Event   [] event_map = new Event [NUMBER_EMPTY_CARDS];
    	Arrays.fill(non_alpha, true);
    	Arrays.fill(event_map, null);
    	
    	Location userLocation = mOrientation.getLocation();
        double latitude1 = userLocation.getLatitude();
        double longitude1 = userLocation.getLongitude();
        
        ArrayList<Event> eventsCopy = new ArrayList<Event>(owlevents);
        for (Event event : eventsCopy) {
            double latitude2 = event.getLatitude();
            double longitude2 = event.getLongitude();
            float angle = Utils.getBearing(latitude1, longitude1, latitude2,
                    longitude2);
            non_alpha[(int)Math.floor(angle/30)] = false;
            event_map[(int)Math.floor(angle/30)] = event;
        }
        
        int card_index = (int)Math.floor(mAnimatedHeading/30);
        if (non_alpha[card_index]) {
        	ARView.eventInSight = null;
        } else {
        	ARView.eventInSight = event_map[card_index];
        }
    	
    	for (int i = -2; i < NUMBER_EMPTY_CARDS + 3; i++) {
    		int fx = (int) (i * degreesPerCard * pixelsPerCard);
    		int fy = -320/2;
            Rect rect = new Rect(fx + CARD_FRAME_OFFSET/2,fy, (int) (fx+ degreesPerCard * pixelsPerCard) - CARD_FRAME_OFFSET/2, fy+320);
            RectF rectF = new RectF(rect);

            if (non_alpha[(12+i) % 12]) {
                eventBackgroundPaint.setAlpha(30);	
            } else {
                eventBackgroundPaint.setAlpha(90);
            }
            
            canvas.drawRoundRect(rectF, 40, 40, eventBackgroundPaint); 
    	}
    }
    
    private Rect mBounds = new Rect();
    void drawMultilineText(Canvas canvas, String str, int x, int y, Paint paint) {
        int      lineHeight = 0;
        int      yoffset    = 0;
        System.out.println("INPUT_STRING "+str+"\n");
        String[] lines      = str.split("\\$");

        paint.getTextBounds("Ig", 0, 2, mBounds);
        lineHeight = (int) ((float) mBounds.height() * 1.2);
        
        for (int i = 0; i < Math.min(3,lines.length); ++i) {
        	if (i == 0)
        		paint.setTextSize(40);
            canvas.drawText(lines[i], x, y + yoffset, paint);
            yoffset = yoffset + lineHeight;
            if (i == 0) 
            	paint.setTextSize(EVENT_TEXT_SIZE);
        }
    }
    
    private void drawEvents(Canvas canvas, float pixelsPerCard, float offset) {
        if (mOrientation.hasLocation() && owlevents != null) {
                Location userLocation = mOrientation.getLocation();
                double latitude1 = userLocation.getLatitude();
                double longitude1 = userLocation.getLongitude();

                ArrayList<Event> eventsCopy = new ArrayList<Event>(owlevents);
                for (Event place : eventsCopy) {
                    double latitude2 = place.getLatitude();
                    double longitude2 = place.getLongitude();
                    float angle = Utils.getBearing(latitude1, longitude1, latitude2,
                            longitude2);

                    int fx = (int) (offset + Math.floor(angle/30) * 30 * pixelsPerCard
                            - 14 / 2);
                    int fy = -320/2;
                    
                    //System.out.println("Place "+place_index++ + " fx: "+fx+" fy: "+ fy);
                    drawEvent(canvas, fx, fy, place.getName(), place.getCreatorName(), place.getDetail(), place.getCreatorPicture(), eventTextPaint);
                }
        }
    }
    
    private void drawEvent(Canvas canvas, int offsetX, int offsetY, 
    					   String textUpperRight, String textLowerLeft,
    					   String textMiddleRight, Bitmap profile_picture, Paint eventTextPaint) {
    	
    	drawMultilineText(canvas, textUpperRight, (int) (offsetX+560 *(1-vertical_ratio)), (int)(320 * 0.2) + offsetY,eventTextPaint);
    	drawMultilineText(canvas, textMiddleRight, (int) (offsetX+560 *(1-vertical_ratio)), (int)(320 * 0.55) + offsetY,eventTextPaint);
    	
        // canvas.drawText(textMiddleRight,(int) (offsetX+560 *(1-vertical_ratio)), (int)(320 * (horizontal_ratio)) + offsetY,eventTextPaint);
        canvas.drawText(textLowerLeft, (int) (offsetX + 560 * 0.12),(int) (320 * (horizontal_ratio) + offsetY), eventTextPaint);
        // draw the bitmap from the drawingcache to the canvas
        canvas.drawBitmap(profile_picture, offsetX+EVENT_PICTURE_OFFSETX, offsetY+EVENT_PICTURE_OFFSETY, eventTextPaint);
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


    public void setOrientationManager(OrientationManager orientationManager) {
        mOrientation = orientationManager;
    }

    public float getHeading() {
        return mHeading;
    }

 
    public void setHeading(float degrees) {
        mHeading = Utils.mod(degrees, 360.0f);
        animateTo(mHeading);
    }
    
    
    
    private void animateTo(float end) {
        if (!mAnimator.isRunning()) {
            float start = mAnimatedHeading;
            float distance = Math.abs(end - start);
            float reverseDistance = 360.0f - distance;
            float shortest = Math.min(distance, reverseDistance);

            if (Float.isNaN(mAnimatedHeading) || shortest < MIN_DISTANCE_TO_ANIMATE) {
                // If the distance to the destination angle is small enough (or if this is the
                // first time the compass is being displayed), it will be more fluid to just redraw
                // immediately instead of doing an animation.
//            	System.out.println("LOG heading data: "+end);
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
    
    public List<Event> getEvents() {
    	return owlevents;
    }
}
