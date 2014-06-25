package com.google.cloud.backend.sample.guestbook;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;

import com.google.cloud.backend.R;

/**
 * This activity holds the Fragments of the introduction and coordinates
 * communication.
 */
public class IntroductionActivity extends Activity implements OnIntroNavListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_introduction);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(
                R.id.intro_frame, new IntroFirstFragment(), IntroFirstFragment.TAG);
        fragmentTransaction.commit();
    }

    @Override
    public void toFirst(String fromTag) {
        goToFragment(fromTag, IntroFirstFragment.TAG, new IntroFirstFragment());
    }

    @Override
    public void toSecond(String fromTag) {
        goToFragment(fromTag, IntroSecondFragment.TAG, new IntroSecondFragment());
    }

    @Override
    public void toThird(String fromTag) {
        goToFragment(fromTag, IntroThirdFragment.TAG, new IntroThirdFragment());
    }

    @Override
    public void done(boolean skipFutureIntro) {
        if (skipFutureIntro) {
            updateIntroPrefs();
        }
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Fragment fragment = getFragmentManager().findFragmentByTag(IntroThirdFragment.TAG);
        if (fragment != null) {
            fragmentTransaction.remove(
                    getFragmentManager().findFragmentByTag(IntroThirdFragment.TAG));
        }
        fragmentTransaction.commit();
        finish();

    }

    private void updateIntroPrefs() {
        SharedPreferences settings = getSharedPreferences(
                GuestbookActivity.GUESTBOOK_SHARED_PREFS, Context.MODE_PRIVATE);
        if (settings != null) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(GuestbookActivity.SHOW_INTRO_PREFS_KEY, false);
            editor.commit();
        }
    }

    private void goToFragment(String oldFragmentTag, String newFragmentTag, Fragment newFragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.intro_frame, newFragment, newFragmentTag);
        Fragment fragment = getFragmentManager().findFragmentByTag(oldFragmentTag);
        if (fragment != null) {
            fragmentTransaction.remove(getFragmentManager().findFragmentByTag(oldFragmentTag));
        }
        fragmentTransaction.commit();
    }
}
