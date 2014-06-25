package com.google.cloud.backend.sample.guestbook;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.google.cloud.backend.R;

/**
 * This Fragment shows the third screen of the Guestbook introduction.
 */
public class IntroThirdFragment extends Fragment implements OnClickListener {

    public static final String TAG = "THIRD";

    private OnIntroNavListener mCallback;
    private ImageView mPrevBtn;
    private ImageView mDoneBtn;
    private CheckBox mCheckbox;

    private boolean mSkipIntro = false;

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
        View v = inflater.inflate(R.layout.fragment_intro_third, container, false);

        mPrevBtn = (ImageView) v.findViewById(R.id.intro_3_prev_btn);
        mPrevBtn.setOnClickListener(this);
        mDoneBtn = (ImageView) v.findViewById(R.id.intro_done);
        mDoneBtn.setOnClickListener(this);
        mCheckbox = (CheckBox) v.findViewById(R.id.checkbox);
        mCheckbox.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.intro_3_prev_btn:
                mCallback.toSecond(TAG);
                break;
            case R.id.intro_done:
                mCallback.done(mSkipIntro);
                break;
            case R.id.checkbox:
                mSkipIntro = mCheckbox.isChecked();
                break;
        }
    }

}
