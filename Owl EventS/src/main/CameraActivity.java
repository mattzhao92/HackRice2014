package main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import lib.EventFetcher;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;

import com.google.android.glass.media.CameraManager;

public class CameraActivity extends Activity {

	private String eventId;
	private String eventName;
    // Camera part.
    private static final int TAKE_PICTURE_REQUEST = 1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        eventId = bundle.getString("eventId");
        eventName = bundle.getString("eventName") + ".jpg";
//        Event event = ARView.eventInSight;
//        eventId = event.getId();
//        eventName = event.getName() + ".jpg";
        takePicture();
    }
    
    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, TAKE_PICTURE_REQUEST);
    }
    
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	System.out.println("Pic takesss");
        if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK) {
        	System.out.println("Pic take");
            String picturePath = data.getStringExtra(
                    CameraManager.EXTRA_PICTURE_FILE_PATH);
            processPictureWhenReady(picturePath);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    
    
    private void processPictureWhenReady(final String picturePath) {
        final File pictureFile = new File(picturePath);
        if (pictureFile.exists()) {
            // The picture is ready; process it.
        	System.out.println("File is ready");
        	EventFetcher fetcher = new EventFetcher(getApplicationContext());
        	FileInputStream fis;
			try {
				fis = new FileInputStream(pictureFile);
				byte[] bytes = new byte[(int) pictureFile.length()];
				fis.read(bytes);
				Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
						bytes.length);
				Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 480, 320, true);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				byte[] byteArray = stream.toByteArray();
				System.out.println("Resized pic byte " + byteArray.length);
	        	fetcher.uploadGalleryPicture(eventId, byteArray, eventName);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        
        } else {
        	System.out.println("File is ready??");
        	Timer timer = new Timer();
        	timer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					processPictureWhenReady(picturePath);
				}
			}, 1000);
        }
    }
}
