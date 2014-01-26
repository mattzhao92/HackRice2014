package lib;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import location.Event;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class EventFetcher {

	/**
	 * All keys to get info from dict.
	 */
	public static final String EVENT_NAME = "name";
	public static final String EVENT_LOCATION = "location";
	public static final String EVENT_DETAIL = "detail";
	public static final String EVENT_STORY = "story";
	public static final String CREATOR_PROFILE = "creator";
	public static final String CREATOR_PICTURE = "picture";
	public static final String CREATOR_NAME = "name";
	public static final String GALLERY_EVENT_ID = "eventId";
	public static final String GALLERY_PHOTO = "photo";
	public static final String REVIEW_CONTENT = "content";
	public static final String REVIEW_EVENT_ID = "eventId";
	
	private Context context;

	public EventFetcher(Context context) {
		this.context = context;
	}

	public void getAllEvents(final List<Event> collection, final GetCallback<ParseObject> callback) {
		ParseQuery<ParseObject> eventQuery = ParseQuery.getQuery("Event");
		eventQuery.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> events, ParseException e) {
				collection.clear();
				for (final ParseObject event : events) {
					final ParseObject creator = event
							.getParseObject(CREATOR_PROFILE);
					// Fetch the related user profiles.
					creator.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
						@Override
						public void done(ParseObject object, ParseException e) {
							System.out.println(creator.getString(CREATOR_NAME));
							System.out.println("==============");
							// Download the profile picture.
							ParseFile pic = creator.getParseFile(CREATOR_PICTURE);
							Bitmap picture = parseFileToBitmap(pic);
							ParseGeoPoint point = (ParseGeoPoint) event.get(EVENT_LOCATION);
							List<String> gallery = getEventPhotoGallery(event.getObjectId());
							picture = Bitmap.createScaledBitmap(picture,
											(int) (560 * 0.4),
											(int) (320 * 0.6), false);
							Event newEvent = new Event(event.getObjectId(),
									event.getString(EVENT_NAME), 
									event.getString(EVENT_DETAIL), 
									creator.getString(CREATOR_NAME),
									event.getString(EVENT_STORY),
									picture, point.getLatitude(), 
									point.getLongitude(), 
									gallery);

							collection.add(newEvent);
							if (callback != null)
								callback.done(null, new ParseException(0, "parse"));
						}
					});
				}
			}
		});
	}

	public ArrayList<String> getEventPhotoGallery(String eventId) {
		System.out.println("Get gallery for " + eventId);
		ParseQuery<ParseObject> galleryQuery = ParseQuery.getQuery("Gallery");
		galleryQuery.whereEqualTo(GALLERY_EVENT_ID, eventId);
		ArrayList<String> photos = new ArrayList<String>();
		try {
			List<ParseObject> gallery = galleryQuery.find();
			for (ParseObject photo : gallery) {
				ParseFile photoFile = photo.getParseFile(GALLERY_PHOTO);
				byte[] fileByte = photoFile.getData();
				String name = photoFile.getName();
				System.out.println("Saveeee " + name);
				if (fileByte != null) {
					saveFile(name, fileByte);
					photos.add(name);
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
				bitmap = BitmapFactory.decodeByteArray(picData, 0,
						picData.length);
			}
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		return bitmap;
	}
	
	public void saveReivew(String content, String eventId) {
		ParseObject review = new ParseObject("Review");
		review.put(REVIEW_CONTENT, content);
		review.put(REVIEW_EVENT_ID, eventId);
		review.saveInBackground();
	}
	
	public void getReviews(String eventId, final ICallBack callBack) {
		ParseQuery<ParseObject> eventQuery = ParseQuery.getQuery("Review");
		eventQuery.whereEqualTo(REVIEW_EVENT_ID, eventId);
		eventQuery.findInBackground(new FindCallback<ParseObject>() {
			@Override
			public void done(List<ParseObject> reviewObjs, ParseException e) {
				ArrayList<String> reviews = new ArrayList<String>();
				for (ParseObject review : reviewObjs) {
					reviews.add(review.getString(REVIEW_CONTENT));
				}
				callBack.call(reviews);
			}
		});
	}

	public void uploadGalleryPicture(final String eventId, byte[] photoByte,
			String photoName) {
		final ParseObject photo = new ParseObject("Gallery");
		final ParseFile file = new ParseFile(photoName, photoByte);
		file.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException e) {
				// Associate the photo with the data.
				photo.put(GALLERY_PHOTO, file);
				photo.put(GALLERY_EVENT_ID, eventId);
				System.out.println("Save file");
				photo.saveInBackground();
			}
		});
	
	}

	public void saveFile(String filename, byte[] file) {
		File f = new File(context.getFilesDir(), filename);
		if (!f.exists()) {
			FileOutputStream outputStream;
			try {
				System.out.println("Save " + filename);
				outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
				outputStream.write(file);
				outputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
