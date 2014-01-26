package main;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

public class ReviewDisplayActivity extends Activity {

	
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
	        Card card;
	        List<String> reviews = ARView.eventInSight.getReviews();
	        
	        for (String review : reviews) {
	        	card = new Card(this);
		        card.setText(review);
		        mCards.add(card);
	        }
	        
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
