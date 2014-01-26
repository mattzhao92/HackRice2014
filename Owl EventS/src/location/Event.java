/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package location;

import java.util.List;

import android.graphics.Bitmap;

/**
 * This class represents a point of interest that has geographical coordinates (latitude and
 * longitude) and a name that is displayed to the user.
 */
public class Event {

    private final String id;
    private final String name;
    private final String detail;
    private final String creatorName;
    private final String story;
    private final Bitmap creatorPicture;
    private final double latitude;
    private final double longitude;
    private final List<String> gallery;
    private List<String> reviews;


	public Event(String id, String name, String detail, String creatorName,
			String story, Bitmap creatorPicture, double latitude,
			double longitude, List<String> gallery) {
		super();
		this.id = id;
		this.name = name;
		this.detail = detail;
		this.creatorName = creatorName;
		this.story = story;
		this.creatorPicture = creatorPicture;
		this.latitude = latitude;
		this.longitude = longitude;
		this.gallery = gallery;
	}

	public Bitmap getCreatorPicture() {
    	return creatorPicture;
    }
    
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

	public String getId() {
		return id;
	}

	public String getDetail() {
		return detail;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public List<String> getGallery() {
		return gallery;
	}

	public List<String> getReviews() {
		return reviews;
	}

	public void setReviews(List<String> reviews) {
		this.reviews = reviews;
	}

	public String getStory() {
		return story;
	}

    
    
}
