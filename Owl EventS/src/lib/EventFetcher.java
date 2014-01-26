package lib;


import java.util.ArrayList;
import java.util.List;

import location.Event;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.widget.ImageView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
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

	
	public void getAllEvents(final List<Event> collection) {
		ParseQuery<ParseObject> eventQuery = ParseQuery.getQuery("Event");
		eventQuery.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> events, ParseException e) {
				collection.clear();
				for (final ParseObject event : events) {
					final ParseObject creator = event.getParseObject(CREATOR_PROFILE);
					// Fetch the related user profiles.
					creator.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
						@Override
						public void done(ParseObject object, ParseException e) {
							System.out.println(creator.getString(CREATOR_NAME));
							// Download the profile picture.
							ParseFile pic = creator.getParseFile(CREATOR_PICTURE);
							Bitmap picture = parseFileToBitmap(pic);
							ParseGeoPoint point = (ParseGeoPoint)event.get(EVENT_LOCATION);
							List<Bitmap> gallery = getEventPhotoGallery(event.getObjectId());
							picture = Bitmap.createScaledBitmap(picture, (int) (560 * 0.4), (int) (320 * 0.6), false);
							Event newEvent = new Event(event.getObjectId(),
									event.getString(EVENT_NAME),
									event.getString(EVENT_DETAIL),
									event.getString(creator.getString(CREATOR_NAME)),
									picture,
									point.getLatitude(),
									point.getLongitude(),
									gallery);
							
							collection.add(newEvent);
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
	
	
	public void uploadGalleryPicture(String eventId, byte[] photoByte, String photoName) {
		ParseObject photo = new ParseObject("Gallery");
		ParseFile file = new ParseFile(photoName, photoByte);
		file.saveInBackground();
		// Associate the photo with the data.
		photo.put(GALLERY_PHOTO, photo);
		photo.put(GALLERY_EVENT_ID, eventId);
		photo.saveInBackground();
	}
}
