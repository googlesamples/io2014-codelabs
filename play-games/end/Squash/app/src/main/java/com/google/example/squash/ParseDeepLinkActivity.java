package com.google.example.squash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.plus.PlusShare;

public class ParseDeepLinkActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String deepLinkId = PlusShare.getDeepLinkId(this.getIntent());

        int scoreToBeat = 0;

        try {
            scoreToBeat = Integer.parseInt(deepLinkId.substring(1));
        } catch (NumberFormatException e) {
            // Bad parse for some reason. Beware of using raw
            // input from the web.
            Log.e("ParseDeepLinkActivity", "Bad parse of " + deepLinkId);
        }

        // Set the challenge score
        // Now start the activity with

        Intent newIntent = new Intent(this, SquashActivity.class);

        startActivity(newIntent);

        finish();
    }
}
