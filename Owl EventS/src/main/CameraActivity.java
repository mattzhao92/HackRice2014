package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import lib.EventFetcher;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
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
        eventId = savedInstanceState.getString("eventId");
        eventName = savedInstanceState.getString("eventName");
        takePicture();
    }
    
    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, TAKE_PICTURE_REQUEST);
    }
    
    
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
        System.out.println("File is ready???");
        if (pictureFile.exists()) {
            // The picture is ready; process it.
        	System.out.println("File is ready");
        	EventFetcher fetcher = new EventFetcher(getApplicationContext());
        	FileInputStream fis;
			try {
				fis = new FileInputStream(pictureFile);
				byte[] bytes = new byte[(int) pictureFile.length()];
	        	fis.read(bytes);
	        	fetcher.uploadGalleryPicture(eventId, bytes, eventName);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        
        } else {
            // The file does not exist yet. Before starting the file observer, you
            // can update your UI to let the user know that the application is
            // waiting for the picture (for example, by displaying the thumbnail
            // image and a progress indicator).

            final File parentDirectory = pictureFile.getParentFile();
            FileObserver observer = new FileObserver(parentDirectory.getPath()) {
                // Protect against additional pending events after CLOSE_WRITE is
                // handled.
                private boolean isFileWritten;

                @Override
                public void onEvent(int event, String path) {
                    if (!isFileWritten) {
                        // For safety, make sure that the file that was created in
                        // the directory is actually the one that we're expecting.
                        File affectedFile = new File(parentDirectory, path);
                        isFileWritten = (event == FileObserver.CLOSE_WRITE
                                && affectedFile.equals(pictureFile));

                        if (isFileWritten) {
                            stopWatching();

                            // Now that the file is ready, recursively call
                            // processPictureWhenReady again (on the UI thread).
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    processPictureWhenReady(picturePath);
                                }
                            });
                        }
                    }
                }
            };
            observer.startWatching();
        }
    }
}
