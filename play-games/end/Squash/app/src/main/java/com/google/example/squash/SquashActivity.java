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

public class SquashActivity extends BaseGameActivity {
    public static final int REQUEST_ACHIEVEMENTS    = 1001;
    public static final int REQUEST_LEADERBOARD     = 1002;
    public static final int SEND_GIFT               = 1003;
    public static final int SHOW_INBOX              = 1004;

    public SquashActivity() {
        super(CLIENT_GAMES | CLIENT_PLUS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_leaderboard:
            startActivityForResult(
                    Games.Leaderboards.getLeaderboardIntent( getApiClient(),
                            getResources().getString(
                                    R.string.leaderboard_bounces)),
                    REQUEST_LEADERBOARD);
            return true;
        case R.id.menu_reset:
            return true;
        case R.id.menu_achievements:
            if (isSignedIn()) {
                startActivityForResult(
                        Games.Achievements.getAchievementsIntent( getApiClient() ),
                        REQUEST_ACHIEVEMENTS);
            }
            return true;

        case R.id.menu_send_gift:
            if (isSignedIn()) {
                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                Intent intent = Games.Requests.getSendIntent(getApiClient(), GameRequest.TYPE_GIFT,
                        "Gold".getBytes(), Requests.REQUEST_DEFAULT_LIFETIME_DAYS, bm, "A treasure chest!");

                startActivityForResult(intent, SEND_GIFT);


            }
            return true;

        case R.id.menu_gift_inbox:
            if (isSignedIn()) {
                startActivityForResult(Games.Requests.getInboxIntent(getApiClient()), SHOW_INBOX);
            }
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
                        beginUserInitiatedSignIn();
                    }
                });

        findViewById(R.id.sign_out_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        signOut();
                        setViewVisibility();
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
        if (isSignedIn()) {
        	Games.Achievements.unlock( getApiClient(),
                    getResources().getString(R.string.achievement_first) );
        }
    }

    // Called whenever the Squash game stops.
    public void onGameStop(SquashView v) {
        setViewVisibility();
        findViewById(R.id.squashView).setVisibility(View.GONE);
        findViewById(R.id.quick_match_button).setVisibility(View.VISIBLE);
        if (isSignedIn() && v.mScore > 0) {

        	Games.Achievements.increment( getApiClient(),
                            getResources().getString(R.string.achievement_20),
                            v.mScore);

        	Games.Leaderboards.submitScore(getApiClient(),
                    getResources().getString(R.string.leaderboard_bounces),
                    v.mScore);
        }
    }

    // Set the login button visible or not
    public void setViewVisibility() {
        if (isSignedIn()) {
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        } else {
            findViewById(R.id.sign_out_button).setVisibility(View.GONE);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSignInFailed() {
        setViewVisibility();
    }

    @Override
    public void onSignInSucceeded() {
        setViewVisibility();

        ArrayList<GameRequest> requests = getGameHelper().getRequests();
        if (requests != null) {
            handleGiftRequest(requests);
        }

    }

    @Override
    public void onActivityResult(int request, int response, Intent intent) {
        super.onActivityResult(request, response, intent);

        if (request == SEND_GIFT) {
            if (response == GamesActivityResultCodes.RESULT_SEND_REQUEST_FAILED) {
                Toast.makeText(this, "Failed to send gift!", Toast.LENGTH_LONG).show();
            }
        }

        else if (request == SHOW_INBOX) {
            if (response == Activity.RESULT_OK && intent != null) {
                handleGiftRequest(Games.Requests.getGameRequestsFromInboxResponse(intent));
            } else {
                Toast.makeText(this, "Error receiving gift!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void handleGiftRequest(ArrayList<GameRequest> requests) {
        if (requests == null) {
            return;
        }

        // Attempt to accept these requests.
        ArrayList<String> requestIds = new ArrayList<String>();

        final HashMap<String, GameRequest> gameRequestMap
                = new HashMap<String, GameRequest>();

        // Cache the requests.
        for (GameRequest request : requests) {
            String requestId = request.getRequestId();
            requestIds.add(requestId);
            gameRequestMap.put(requestId, request);
        }

        // Accept the requests.
        PendingResult<Requests.UpdateRequestsResult> pendingResults = Games.Requests.acceptRequests(getApiClient(), requestIds);

        pendingResults.setResultCallback(
                new ResultCallback<Requests.UpdateRequestsResult>() {
                    @Override
                    public void onResult(Requests.UpdateRequestsResult result) {
                        // Scan each result outcome and process accordingly.
                        for (String requestId : result.getRequestIds()) {
                            // We must have a local cached copy of the request
                            // and the request needs to be a
                            // success in order to continue.
                            if (!gameRequestMap.containsKey(requestId)
                                    || result.getRequestOutcome(requestId)
                                    != Requests.REQUEST_UPDATE_OUTCOME_SUCCESS) {
                                continue;
                            }
                            // Update succeeded here. Find the type of request
                            // and act accordingly. For wishes, a
                            // responding gift will be automatically sent.
                            switch (gameRequestMap.get(requestId).getType()) {
                                case GameRequest.TYPE_GIFT:
                                    // Reward the player here

                                    try {
                                        Toast.makeText(getApplicationContext(), "Accepted a gift! It is... " + new String(gameRequestMap.get(requestId).getData(), "UTF-8"), Toast.LENGTH_LONG).show();
                                    } catch (UnsupportedEncodingException e) {
                                        Toast.makeText(getApplicationContext(), "Accepted a gift!", Toast.LENGTH_LONG).show();
                                    }

                                    break;
                                case GameRequest.TYPE_WISH:
                                    // Process the wish request
                                    Toast.makeText(getApplicationContext(), "Accepted wish!", Toast.LENGTH_LONG).show();

                                    break;
                            }
                        }
                    }
                }
        );
    }
}
