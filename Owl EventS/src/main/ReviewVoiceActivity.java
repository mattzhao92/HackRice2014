package main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.MotionEvent;
import android.widget.TextView;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import java.util.List;

import lib.EventFetcher;

import com.google.android.glass.sample.compass.R;

public class ReviewVoiceActivity extends Activity
{
    private GestureDetector gestureHandler;
    private TextView contentView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review);
        contentView = (TextView) findViewById(R.id.voicedictation_main_content);
        gestureHandler = createGestureDetector(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            if(spokenText == null) {
                spokenText = "";
            }
            contentView.setText(spokenText);
            EventFetcher ef = new EventFetcher(getApplicationContext());
            ef.saveReivew(spokenText, ARView.eventInSight.getId());
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    @Override
    public boolean onGenericMotionEvent(MotionEvent event)
    {
        if (gestureHandler != null) {
            return gestureHandler.onMotionEvent(event);
        }
        return super.onGenericMotionEvent(event);
    }

    private GestureDetector createGestureDetector(Context context)
    {
        GestureDetector gestureDetector = new GestureDetector(context);
        gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                if (gesture == Gesture.TAP) {
                	Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    startActivityForResult(intent, 0);
                    return true;
                }
                return false;
            }
        });
        return gestureDetector;
    }
}