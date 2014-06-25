package com.google.example.squash;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.request.GameRequest;
import com.google.android.gms.games.request.Requests;
import com.google.example.games.basegameutils.BaseGameActivity;

public class SquashActivity extends Activity {

    public SquashActivity() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reset:
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_squash);

        // This sets up the click listener.
        findViewById(R.id.sign_in_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // start the asynchronous sign in flow
                        // ...
                    }
                });

        findViewById(R.id.sign_out_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // User initiated sign out...
                        // ..
                    }
                });

        findViewById(R.id.quick_match_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startQuickGame();
                    }

                });

    }

    private void startQuickGame() {
    	((SquashView) findViewById(R.id.squashView)).start();
    	findViewById(R.id.sign_out_button).setVisibility(View.GONE);
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.quick_match_button).setVisibility(View.GONE);
        findViewById(R.id.squashView).setVisibility(View.VISIBLE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.squash, menu);

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((SquashView) findViewById(R.id.squashView)).setAnimating(true);
    }

    @Override
    public void onStop() {
        super.onStop();
        ((SquashView) findViewById(R.id.squashView)).setAnimating(false);
    }

    // Called whenever the Squash game starts.
    public void onGameStart(SquashView v) {

    }

    // Called whenever the Squash game stops.
    public void onGameStop(SquashView v) {
        findViewById(R.id.squashView).setVisibility(View.GONE);
        findViewById(R.id.quick_match_button).setVisibility(View.VISIBLE);

    }

    @Override
    public void onActivityResult(int request, int response, Intent intent) {
        super.onActivityResult(request, response, intent);

    }
}
