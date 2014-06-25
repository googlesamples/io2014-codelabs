package com.google.cloud.backend.sample.guestbook;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.cloud.backend.R;

/**
 * This Fragment provides the splash screen that is shown when the guestbook app
 * is retrieving entries from the server.
 */
public class SplashFragment extends Fragment {
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

}
