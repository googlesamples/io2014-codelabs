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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class mocks a music store.
 *
 * An instance of the music store can be retrieved using the getInstance() method.
 * Since this is a sample app, this class hard-codes one artist, "Dual Core", with two songs.
 * In a real app, this class would be expanded to retrieve all supported media.
 */
public class MyMusicStore {

    /**
     * Contains metadata for an artist, a collection of songs, and a deep link to uniquely
     * represent this artist.
     */
    public static class Artist {
        private String mName;
        private String mInfo;
        private int mImage;
        private Map<String, Integer> mSongs;
        private String mUrl;

        public Artist(String name, String info, int image, Map<String, Integer> songs, String url) {
            mName = name;
            mInfo = info;
            mImage = image;
            mSongs = songs;
            mUrl = url;
        }

        public String getName() {
            return mName;
        }
        public String getInfo() {
            return mInfo;
        }

        /**
         * Gets the {@link Artist} image resource identifier.
         * @return the image resource identifier
         */
        public int getImageResource() {
            return mImage;
        }

        /**
         * Gets a map of artist songs to their resource identifiers.
         */
        public Map<String, Integer> getSongList() {
            return mSongs;
        }
        public String getUrl() {return mUrl; }
    }

    private static final String pathPrefix = "http://jarekandshawnmusic.com";
    private static MyMusicStore instance = null;
    private List<Artist> artists;

    // We only want to create one music store accessed from various locations, so we instantiate it
    // as a singleton.
    public static MyMusicStore getInstance() {
        if (instance == null) {
            instance = new MyMusicStore();
        }
        return instance;
    }

    private MyMusicStore() {
        // Hard code only supported artist
        String name = "Dual Core";
        String info = "Brought together by the power of the internet (and perhaps a touch of musical providence), California based computer programmer / rapper int eighty and English web designer / music producer c64 have been rocking the more studious side of the hip hop underground since 2007.";
        int image = R.drawable.artist_image_dual_core;
        Map<String, Integer> songs = new HashMap<String, Integer>();
        String url = "http://jarekandshawnmusic.com/artist/DualCore";
        songs.put("All The Things", R.raw.dual_core_all_the_things);
        songs.put("Mastering Success and Failure", R.raw.dual_core_mastering_success_and_failure);
        artists = new ArrayList<Artist>();
        artists.add(new Artist(name, info, image, songs, url));
    }

    /**
     * Gets the {@link Artist} with the given name.
     */
    public Artist getArtist(String artistName) {
        if (artistName == null) return null;

        for (Artist artist : artists) {
            if (artist.getName().equals(artistName)) {
                return artist;
            }
        }
        return null;
    }

    /**
     * Gets the {@link Artist} given the artist URL sub-path.
     *
     * Input URL paths are expected to be of the form /artist/{ArtistName}, returning an
     * {@link Artist} with the link http://jarekandshawnmusic.com/artist/{ArtistName}.
     * @param url the url sub-path of the artist
     * @return the requested artist
     */
    public Artist getArtistFromUrl(String url) {
        if (url == null) return null;
        String fullPath = pathPrefix.concat(url);
        for (Artist artist : artists) {
            if (artist.getUrl().equals(fullPath)) {
                return artist;
            }
        }
        return null;
    }

    public List<Artist> getArtists() {
        return artists;
    }
}