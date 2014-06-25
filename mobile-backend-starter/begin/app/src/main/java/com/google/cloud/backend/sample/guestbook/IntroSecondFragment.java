package com.google.cloud.backend.sample.guestbook;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.cloud.backend.R;

/**
 * This Fragment shows the second screen of the Guestbook introduction.
 */
public class IntroSecondFragment extends Fragment implements OnClickListener {

    public static final String TAG = "SECOND";

    private OnIntroNavListener mCallback;
    private ImageView mPrevBtn;
    private ImageView mNextBtn;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Set the callback activity to use
        try {
            mCallback = (OnIntroNavListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    activity.toString() + " must implement OnIntroNavListener");
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_intro_second, container, false);

        mPrevBtn = (ImageView) v.findViewById(R.id.intro_2_prev_btn);
        mPrevBtn.setOnClickListener(this);
        mNextBtn = (ImageView) v.findViewById(R.id.intro_2_next_btn);
        mNextBtn.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.intro_2_prev_btn:
                mCallback.toFirst(TAG);
                break;
            case R.id.intro_2_next_btn:
                mCallback.toThird(TAG);
                break;
        }
    }
}
