/*
 * Copyright 2014 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jarekandshawnmusic.m;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * This activity is the main entry point of the app.
 *
 * It displays a grid of supported artists, and launches an intent for ArtistActivity
 * to display the artist detail when selected.
 */
public class MainActivity extends ActionBarActivity {
    private static final String TAG = MainActivity.class.getName();
    final MyMusicStore mMyMusicStore = MyMusicStore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create a grid of supported artists to choose from the main screen of the app.
        // This creates the main UI when the app is launched manually from the app launcher.
        GridView artistGrid = (GridView) findViewById(R.id.artist_grid);
        artistGrid.setAdapter(new ImageAdapter(this));

        // When selected, launch the ArtistActivity with the deep link to the selected artist.
        artistGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent artistIntent = new Intent(MainActivity.this, ArtistActivity.class);
                String artistUrl = mMyMusicStore.getArtists().get(position).getUrl();
                artistIntent.setData(Uri.parse(artistUrl));
                startActivity(artistIntent);
            }
        });
    }

    // Create a tile for each supported artist with the artist name and a graphic.
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private List<MyMusicStore.Artist> mArtists;

        public ImageAdapter(Context c) {
            mContext = c;
            mArtists = mMyMusicStore.getArtists();
        }

        public int getCount() {
            return mArtists.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                view = getLayoutInflater().inflate(R.layout.artist_tile, null);
            } else {
                view = convertView;
            }
            TextView textView = (TextView) view.findViewById(R.id.artist_tile_text);
            ImageView imageView = (ImageView) view.findViewById(R.id.artist_tile_image);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            MyMusicStore.Artist artist = mArtists.get(position);
            imageView.setImageResource(artist.getImageResource());
            textView.setText(artist.getName());
            return view;
        }
    }
}
