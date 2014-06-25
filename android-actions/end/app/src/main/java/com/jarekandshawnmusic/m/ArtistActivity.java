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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.Map;

/**
 * The ArtistActivity displays detailed info about an artist and allows playback of
 * the artist's music.
 *
 * In addition, this activity records view and viewEnd actions using the
 * App Indexing API through the GoogleApiClient. Since the App Indexing API is
 * asynchronous, we don't need to implement all of the GoogleApiClient life-cycle methods.
 * This activity should be launched with an intent that has the artist's deep-link
 * from the music store set with setData(Uri).
 */
public class ArtistActivity extends ActionBarActivity {

    private static final String TAG = ArtistActivity.class.getName();
    private Boolean mArtistInfoExpanded = false;
    private MyMusicStore.Artist mArtist = null;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        ImageView artistImageView = (ImageView) findViewById(R.id.artist_image);
        TextView artistInfoView = (TextView) findViewById(R.id.artist_info);
        TextView songListHeaderView = (TextView) findViewById(R.id.song_list_header);
        ListView songListView = (ListView) findViewById(R.id.song_list);
        Button playButton = (Button) findViewById(R.id.play_button);

        // Initialize the Google API client for the App Indexing API.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(AppIndex.APP_INDEX_API)
                .build();

        // Set up toggle expansion of the artist info block.
        artistInfoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView textView = (TextView) view;
                if (!mArtistInfoExpanded) {
                    textView.setMaxLines(R.integer.artist_info_max_lines);
                    mArtistInfoExpanded = true;
                } else {
                    textView.setMaxLines(R.integer.artist_info_min_lines);
                    mArtistInfoExpanded = false;
                }
            }
        });

        // Get the intent used to launch this activity.
        Intent intent = getIntent();
        MyMusicStore musicStore = MyMusicStore.getInstance();

        if (intent.getData() != null) {
            mArtist = musicStore.getArtistFromUrl(intent.getData().getPath());
        }

        if (mArtist == null) {
            Log.e(TAG, "Can't find artist");
            artistInfoView.setText("Can't find that artist.");
        } else {
            // Artist has been found, so show the rest of the UI.
            Map<String, Integer> songList = mArtist.getSongList();
            final ArrayList<Integer> playlist = new ArrayList<Integer>();
            playlist.addAll(songList.values());

            songListHeaderView.setVisibility(View.VISIBLE);
            songListView.setVisibility(View.VISIBLE);
            playButton.setVisibility(View.VISIBLE);

            setTitle(mArtist.getName());
            artistInfoView.setText(mArtist.getInfo());
            artistImageView.setImageResource(mArtist.getImageResource());

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, songList.keySet().toArray(new String[0]));
            songListView.setAdapter(adapter);

            // If an existing playlist is not already playing, start one.
            if (!MediaPlayerService.mHasLaunched) {
                startPlaylist(playlist);
            }

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startPlaylist(playlist);
                }
            });
        }
    }

    /**
     * Shows the playlist fragment and starts playing the requested playlist.
     *
     * @param playlist the playlist of songs to play
     */
    private void startPlaylist(ArrayList<Integer> playlist) {
        findViewById(R.id.playlist_fragment_container).setVisibility(View.VISIBLE);
        PlaylistFragment playlistFragment = (PlaylistFragment) getSupportFragmentManager()
                .findFragmentById(R.id.playlist_fragment);
        playlistFragment.setPlaylist(playlist);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MediaPlayerService.mHasLaunched) {
            findViewById(R.id.playlist_fragment_container).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Initiate a connection to the Google Api client.
        mGoogleApiClient.connect();
        // Since the App Indexing API is asynchronous, we don't need to worry about tracking
        // the Google Api client's connection status, and this is also why we don't implement
        // any of the connection callback methods.
        recordAppIndexingView();
    }

    @Override
    protected void onStop() {
        recordAppIndexingViewEnd();
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    // Use the App Indexing Api to record that this activity started to show this artist
    // to the user. In this sample app, we just keep track of the result and post it to the console.
    private void recordAppIndexingView() {
        PendingResult<Status> result = AppIndex.AppIndexApi.view(
                mGoogleApiClient,
                ArtistActivity.this,
                getIntent(),
                mArtist.getName(),
                Uri.parse(mArtist.getUrl()),
                null);

        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d(TAG, "App Indexing API: Recorded artist "
                            + mArtist.getName() + " view successfully.");
                } else {
                    Log.e(TAG, "App Indexing API: There was an error recording the artist view."
                            + status.toString());
                }
            }
        });

    }

    // Use the App Indexing Api to record that this activity stopped showing this artist
    // to the user. In this sample app, we just keep track of the result and post it to the console.
    private void recordAppIndexingViewEnd() {
        PendingResult<Status> result = AppIndex.AppIndexApi.viewEnd(
                mGoogleApiClient,
                ArtistActivity.this,
                getIntent());

        result.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Log.d(TAG, "App Indexing API: Recorded artist "
                            + mArtist.getName() + " view end successfully.");
                } else {
                    Log.e(TAG, "App Indexing API: There was an error recording the artist view."
                            + status.toString());
                }
            }
        });
    }
}
