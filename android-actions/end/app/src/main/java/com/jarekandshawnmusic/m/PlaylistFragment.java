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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * This fragment provides common playlist functionality.
 *
 * In this sample, it is used only within one parent activity, but it can be extended to be reused
 * between several container activities. It supports playing a list of raw resources contained in
 * the app by using a background media player service.
 */
public class PlaylistFragment extends Fragment implements MediaPlayerService.ProgressListener {

    private static final String TAG = PlaylistFragment.class.getName();
    private MediaPlayerService mMediaPlayerService;
    private boolean mBound = false;
    private SeekBar mSeekBar;
    private ImageButton mPlayPauseButton;
    private ImageButton mPreviousButton;
    private ImageButton mNextButton;
    private ImageButton mFullscreenButton;
    private TextView mArtistNameView;
    private TextView mSongNameView;
    private boolean mPlaylistExpanded;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mMediaPlayerService = binder.getService();
            mMediaPlayerService.setProgressListener(PlaylistFragment.this);
            onSongStart();
            mBound = true;
            play();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mPlaylistExpanded = false;
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);

        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
        mPlayPauseButton = (ImageButton) view.findViewById(R.id.button_play);
        mPreviousButton = (ImageButton) view.findViewById(R.id.button_previous);
        mNextButton = (ImageButton) view.findViewById(R.id.button_next);
        mFullscreenButton = (ImageButton) view.findViewById(R.id.button_fullscreen);
        mArtistNameView = (TextView) view.findViewById(R.id.artist_name);
        mSongNameView = (TextView) view.findViewById(R.id.song_name);

        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayerService.isPlaying()) {
                    pause();
                    mPlayPauseButton.setImageResource(R.drawable.ic_action_play);
                } else {
                    play();
                    mPlayPauseButton.setImageResource(R.drawable.ic_action_pause);
                }
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayerService.isPlaying()) {
                    mMediaPlayerService.next();
                }
            }
        });

        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayerService.isPlaying()) {
                    mMediaPlayerService.previous();
                }
            }
        });

        // Create a callback to toggle maximizing the playlist fragment in the parent container.
        // In this sample, we assume the fragment exists in a parent activity that has its
        // views contained in a scroll view.
        mFullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageButton button = (ImageButton) view;
                View scrollView = getActivity().findViewById(R.id.artist_scroll_view);
                View playlistDetailView = getActivity().findViewById(R.id.playlist_detail);
                ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
                if (!mPlaylistExpanded) {
                    scrollView.setVisibility(View.GONE);
                    playlistDetailView.setVisibility(View.VISIBLE);
                    button.setImageResource(R.drawable.ic_action_return_from_full_screen);
                    actionBar.hide();
                    ListView playlistView = (ListView) getActivity().findViewById(R.id.playlist);
                    ArrayAdapter adapter = new ArrayAdapter<MediaPlayerService.PlaylistEntry>(
                            getActivity(),
                            android.R.layout.simple_list_item_1,
                            mMediaPlayerService.getSongs());
                    playlistView.setAdapter(adapter);
                    mPlaylistExpanded = true;
                } else {
                    scrollView.setVisibility(View.VISIBLE);
                    playlistDetailView.setVisibility(View.GONE);
                    button.setImageResource(R.drawable.ic_action_full_screen);
                    mPlaylistExpanded = false;
                    actionBar.show();
                }
            }
        });
        return view;
    }

    /**
     * Launches the {@link com.jarekandshawnmusic.m.MediaPlayerService}
     * to play the requested playlist, or re-initializes an existing
     * {@link com.jarekandshawnmusic.m.MediaPlayerService} with a new playlist.
     * @param playlist the list of songs to play
     */
    public void setPlaylist(ArrayList<Integer> playlist) {
        if (mBound) {
            mMediaPlayerService.setPlaylist(playlist);
            mMediaPlayerService.play();
        } else {
            Activity containerActivity = getActivity();
            Intent playlistIntent = new Intent(containerActivity, MediaPlayerService.class);
            playlistIntent.putIntegerArrayListExtra("playlist", playlist);
            containerActivity.startService(playlistIntent);
            // We start this service and then bind to it, so we can control the playback
            // and get progress updates.
            containerActivity.bindService(playlistIntent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void play() {
        if (mBound) {
            mMediaPlayerService.play();
        }
    }

    public void pause() {
        if (mBound) {
            mMediaPlayerService.pause();
        }
    }

    @Override
    public void onStop() {
        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        // We only stop the media player service if we know that this was an intentional
        // app exit. (So, for example, we can keep playing through orientation changes)
        if(getActivity().isFinishing()) {
            mMediaPlayerService.stopSelf();
        }
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        Activity containerActivity = getActivity();
        Intent playlistIntent = new Intent(containerActivity, MediaPlayerService.class);
        if (MediaPlayerService.mHasLaunched) {
            if (!mBound) {
                containerActivity.bindService(playlistIntent, mConnection, Context.BIND_AUTO_CREATE);
            }
        } else {
            containerActivity.startService(playlistIntent);
            containerActivity.bindService(playlistIntent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onSongStart() {
        // Initialize the UI components to show info and progress for the currently playing song.
        mSeekBar.setMax(mMediaPlayerService.getDuration());
        mArtistNameView.setText(mMediaPlayerService.getArtist());
        mSongNameView.setText(mMediaPlayerService.getSong());
        mPlayPauseButton.setImageResource(R.drawable.ic_action_pause);
    }

    @Override
    public void onSongComplete() {
        mSeekBar.setProgress(0);
        mPlayPauseButton.setImageResource(R.drawable.ic_action_play);
    }

    @Override
    public void onProgress(int progress) {
      // Callback function that updates the progress bar.
      mSeekBar.setProgress(progress);
    }
}