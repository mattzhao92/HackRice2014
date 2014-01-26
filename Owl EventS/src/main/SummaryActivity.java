package main;

import java.util.ArrayList;
import java.util.List;


import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public class SummaryActivity extends Activity {
	
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
//        File file;
        String summary = ARView.eventInSight.getStory();
        String name = ARView.eventInSight.getName();

        
        card = new Card(this);
        card.setText(summary);
        card.setFootnote(name);
        mCards.add(card);
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
