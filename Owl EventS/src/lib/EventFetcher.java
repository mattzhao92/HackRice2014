package lib;


import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class EventFetcher {

	/**
	 * All keys to get info from dict.
	 */
	public static final String EVENT_NAME = "name";
	public static final String EVENT_LOCATION = "location";
	public static final String EVENT_DETAIL = "detail";
	public static final String CREATOR_PROFILE = "creator";
	public static final String CREATOR_PICTURE = "picture";
	public static final String CREATOR_NAME = "name";
	public static final String GALLERY_EVENT_ID = "eventId";
	public static final String GALLERY_PHOTO = "photo";

	
	public void getAllEvents(final ImageView iv) {
		ParseQuery<ParseObject> eventQuery = ParseQuery.getQuery("Event");
		eventQuery.findInBackground(new FindCallback<ParseObject>() {
			
			@Override
			public void done(List<ParseObject> events, ParseException e) {
				for (ParseObject event : events) {
					final ParseObject creator = event.getParseObject(CREATOR_PROFILE);
					// Fetch the related user profiles.
					creator.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
						@Override
						public void done(ParseObject object, ParseException e) {
//							System.out.println(creator);
//							System.out.println(creator.getString(CREATOR_NAME));
							// Download the profile picture.
//							ParseFile pic = creator.getParseFile(CREATOR_PICTURE);
//							Bitmap bitmap = parseFileToBitmap(pic);
//							iv.setImageBitmap(bitmap);
//							getEventPhotoGallery(event.ge)
						}
					});
				}
			}
		});
	}
	
	public ArrayList<Bitmap> getEventPhotoGallery(String eventId) {
		ParseQuery<ParseObject> galleryQuery = ParseQuery.getQuery("Gallery");
		galleryQuery.whereEqualTo(GALLERY_EVENT_ID, eventId);
		ArrayList<Bitmap> photos = new ArrayList<Bitmap>();
		try {
			List<ParseObject> gallery = galleryQuery.find();
			for (ParseObject photo : gallery) {
				ParseFile photoFile = photo.getParseFile(GALLERY_PHOTO);
				Bitmap bitmap = parseFileToBitmap(photoFile);
				if (bitmap != null) {
					photos.add(bitmap);
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return photos;
	}
		
	public Bitmap parseFileToBitmap(ParseFile file) {
		Bitmap bitmap = null;
		try {
			byte[] picData = file.getData();
			if (picData != null) {
				bitmap = BitmapFactory.decodeByteArray(picData, 0, picData.length);
			}
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		return bitmap;
	}
	
	public void uploadPic(String event) {
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
		query.getInBackground("omFao4nmfv", new GetCallback<ParseObject>() {
			@Override
			public void done(final ParseObject eve, ParseException e) {
				
				ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Profile");
				query2.getInBackground("kzFEajftY1", new GetCallback<ParseObject>() {
					@Override
					public void done(ParseObject object, ParseException e) {
						eve.put(CREATOR_PROFILE, object);
						eve.saveInBackground();
					}
				});
			}
		});
	}
	
	
}
