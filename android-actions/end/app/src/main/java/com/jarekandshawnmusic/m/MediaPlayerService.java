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

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides simple music player functionality as a background service for this sample.
 *
 * In a real app, we would probably start this as a foreground service that posts in the
 * notification panel and we would allow it to continue running independently of the activity
 * used to launch it.
 */
public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener {
    private static String TAG = MediaPlayerService.class.getName();
    public static boolean mHasLaunched = false;
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private List<PlaylistEntry> mPlaylist = new ArrayList<PlaylistEntry>();
    private int mPlaylistIndex;
    private MediaPlayer mMediaPlayer;
    private int mStartId;
    private ProgressListener mProgressListener;
    private Handler mProgressHandler = new Handler();
    private boolean mIsPaused = false;

    /**
     * Contains metadata about a media resource.
     */
    public class PlaylistEntry {
        private String title;
        private String artist;
        private int resourceId;

        public PlaylistEntry (int resourceId) {
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            AssetFileDescriptor song = getResources().openRawResourceFd(resourceId);
            metadataRetriever.setDataSource(song.getFileDescriptor(), song.getStartOffset(),
                    song.getDeclaredLength());
            this.resourceId = resourceId;
            artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (artist == null) {
                artist = "unknown artist";
            }
            if (title == null) {
                title = "unknown title";
            }
            metadataRetriever.release();
        }

        public int getResourceId() {
            return resourceId;
        }
        public String getTitle() {
            return title;
        }
        public String getArtist() {
            return artist;
        }
        @Override
        public String toString() {
            return getArtist() + " - " + getTitle();
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        MediaPlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MediaPlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        updateProgress();
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
    }

    @Override
    public void onDestroy() {
        mHasLaunched = false;
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mProgressHandler.removeCallbacks(runnable);
    }

    // Called when the service starts, this method starts the service, and optionally
    // starts playback of a playlist if passed in as an intent extra.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mStartId = startId;
        mHasLaunched = true;
        ArrayList<Integer> playlist = intent.getIntegerArrayListExtra("playlist");
        if (playlist != null && playlist.size() > 0) {
            setPlaylist(playlist);
            play();
        }
        return START_NOT_STICKY;
    }

    /**
     * Changes the current playlist.
     * @param playlist the new playlist
     */
    public void setPlaylist(ArrayList<Integer> playlist) {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mPlaylist.clear();
        for (Integer resource : playlist) {
            mPlaylist.add(new PlaylistEntry(resource));
        }
        mPlaylistIndex = 0;
    }

    /**
     * Starts asynchronous load of the next song in the playlist. The onPrepared() method
     * is called once loading is finished.
     */
    public void play() {
        if (mIsPaused) {
            mMediaPlayer.start();
            mIsPaused = false;
        } else if (!mMediaPlayer.isPlaying()) {
            try {
                mMediaPlayer.reset();
                PlaylistEntry song;
                try {
                    song = mPlaylist.get(mPlaylistIndex);
                } catch (IndexOutOfBoundsException e) {
                    Log.e(TAG, "Error: Tried to fetch non-existent song.");
                    return;
                }
                AssetFileDescriptor songFD = getResources().openRawResourceFd(song.getResourceId());
                mMediaPlayer.setDataSource(songFD.getFileDescriptor(), songFD.getStartOffset(),
                        songFD.getDeclaredLength());
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.prepareAsync();
                songFD.close();
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Unable to play audio queue do to exception: " + e.getMessage(), e);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Unable to play audio queue do to exception: " + e.getMessage(), e);
            } catch (IOException e) {
                Log.e(TAG, "Unable to play audio queue do to exception: " + e.getMessage(), e);
            }
        }
    }

    // This callback is called when a song finishes loading asynchronously, signaling
    // that we can start playback.
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        if (mProgressListener != null) {
            mProgressListener.onSongStart();
        }
    }

    // This callback is called when a song is finished playing, so we can advance to the
    // next song.
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mProgressListener != null) {
            mProgressListener.onSongComplete();
        }
        mPlaylistIndex++;
        play();
    }

    public int getDuration() { return mMediaPlayer.getDuration(); }

    public List<PlaylistEntry> getSongs() { return mPlaylist; }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mIsPaused = true;
        }
    }

    public void next() {
        mMediaPlayer.stop();
        if (mPlaylistIndex + 1 < mPlaylist.size()) {
            mPlaylistIndex++;
            play();
        } else {
            // No more songs in playlist, so stop.
            if (mProgressListener != null) {
                mProgressListener.onSongComplete();
            }
        }
    }

    public void previous() {
        mMediaPlayer.stop();
        if (mPlaylistIndex > 0) {
            mPlaylistIndex--;
        }
        play();
    }

    public String getArtist() {
        if (mPlaylist.size() < mPlaylistIndex) {
            return null;
        }
        return mPlaylist.get(mPlaylistIndex).getArtist();
    }

    public String getSong() {
        if (mPlaylist.size() < mPlaylistIndex) {
            return null;
        }
        return mPlaylist.get(mPlaylistIndex).getTitle();
    }

    /**
     * Sets a {@link com.jarekandshawnmusic.m.MediaPlayerService.ProgressListener} that clients
     * of {@link com.jarekandshawnmusic.m.MediaPlayerService} can implement to know when to update
     * their UI's as the song changes.
     */
    public void setProgressListener(ProgressListener progressListener) {
        mProgressListener = progressListener;
    }

    /**
     * A {@link com.jarekandshawnmusic.m.MediaPlayerService.ProgressListener} defines callback
     * methods that are notified when the song playback status / progress changes. It can be
     * set using the setProgressListener() method and is a means for clients to update their
     * views based on the currently playing song.
     */
    public interface ProgressListener {
        public void onSongStart();
        public void onSongComplete();
        public void onProgress(int progress);
    }

    /**
     * Updates any progress listeners on the current song progress.
     */
    private void updateProgress() {
        // Posts progress updates at 1 second intervals to a ProgressListener.
        if (mProgressListener != null && mMediaPlayer.isPlaying()) {
            mProgressListener.onProgress(mMediaPlayer.getCurrentPosition());
        }
        mProgressHandler.postDelayed(runnable, 1000);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };
}
