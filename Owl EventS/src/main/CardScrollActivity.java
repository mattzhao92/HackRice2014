package main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import location.Event;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

public class CardScrollActivity extends Activity {

    private List<Card> mCards;
    private CardScrollView mCardScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createCards();

        mCardScrollView = new CardScrollView(this);
        GalleryCardScrollAdapter adapter = new GalleryCardScrollAdapter();
        mCardScrollView.setAdapter(adapter);
        mCardScrollView.activate();
        setContentView(mCardScrollView);
    }

    private void createCards() {
        mCards = new ArrayList<Card>();
        
        List<Event> events = ARView.owlevents;
        Card card;
        File file = null;
        for (Event event : events) {
        	List<String> photos = event.getGallery();
        	if (!photos.isEmpty()) {
        		for (String photo : photos) {
        			card = new Card(this);
        			card.setImageLayout(Card.ImageLayout.FULL);
        			file = new File(getApplicationContext().getFilesDir(), photo);
        			card.addImage(Uri.parse(file.toURI().toString()));
        			mCards.add(card);
        		}
        		break;
        	} 
        }
        
//        final File fil = file;
//        String eventId = "omFao4nmfv";
//        String eventName = "CS";
//    	EventFetcher fetcher = new EventFetcher(getApplicationContext());
//    	FileInputStream fis;
//		try {
//			fis = new FileInputStream(fil);
//			byte[] bytes = new byte[(int) fil.length()];
//        	fis.read(bytes);
//        	System.out.println("Save event for id " + eventId + " name " + eventName);
//        	fetcher.uploadGalleryPicture(eventId, bytes, eventName);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//        
//        card = new Card(this);
//        card.setText("This card has a footer.");
//        card.setFootnote("I'm the footer!");
//        mCards.add(card);
//
//        card = new Card(this);
//        card.setText("This card has a puppy background image.");
//        card.setFootnote("How can you resist?");
//        card.setImageLayout(Card.ImageLayout.FULL);
//        card.addImage(R.drawable.ic_stop);
//        mCards.add(card);
//
//        card = new Card(this);
//        card.setText("This card has a mosaic of puppies.");
//        card.setFootnote("Aren't they precious?");
//        card.setImageLayout(Card.ImageLayout.LEFT);
//        card.addImage(R.drawable.ic_stop);
//        card.addImage(R.drawable.ic_stop);
//        card.addImage(R.drawable.ic_stop);
//        mCards.add(card);
    }

    private class GalleryCardScrollAdapter extends CardScrollAdapter {

        @Override
        public int findIdPosition(Object id) {
            return -1;
        }

        @Override
        public int findItemPosition(Object item) {
            return mCards.indexOf(item);
        }

        @Override
        public int getCount() {
            return mCards.size();
        }

        @Override
        public Object getItem(int position) {
            return mCards.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mCards.get(position).toView();
        }
    }
}