/*
 * Copyright (c) 2014 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.developers.actions.debugger;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.developers.actions.debugger.util.ActionData;
import com.google.developers.actions.debugger.util.CayleyResult;
import com.google.developers.actions.debugger.util.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Main activity class which handles intents.
 */
public class MainActivity extends Activity implements
        ActionsListFragment.Callbacks {
    private static final int RECOGNIZER_REQ_CODE = 1234;
    // prefix of play voice command.
    private static final String PLAY_PREFIX = "play ";
    public static final String ACTION = "action";
    public static final String ACTION_TYPE = "actionType";

    private List<ActionData> mActionList;

    private ActionsListFragment mActionsListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mActionsListFragment = (ActionsListFragment) getFragmentManager()
                .findFragmentById(R.id.list_fragment);
        loadActions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                loadActions();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadActions() {
        setProgressBarIndeterminateVisibility(true);
        new AsyncTask<Void, Void, List<ActionData>>() {
            @Override
            protected List<ActionData> doInBackground(Void... voids) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<ActionData>>() {
                }.getType();
                List<ActionData> actionList = new ArrayList<ActionData>();
                try {
                    Reader reader = new InputStreamReader(getResources()
                            .openRawResource(R.raw.intents));
                    actionList = gson.fromJson(reader, listType);
                } catch (JsonIOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (JsonSyntaxException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mActionList = actionList;
                return actionList;
            }

            @Override
            protected void onPostExecute(List<ActionData> actions) {
                setProgressBarIndeterminateVisibility(false);

                if (actions == null) {
                    return;
                }

                mActionsListFragment.setActions(actions);
            }

        }.execute((Void) null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOGNIZER_REQ_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (!matches.isEmpty()) {
                for (String match : matches) {
                    if (match.startsWith(PLAY_PREFIX)) {
                        askCayley(match.substring(PLAY_PREFIX.length()));
                        Toast.makeText(this, match, Toast.LENGTH_LONG).show();
                        break;
                    }
                }
                //Result code for various error.
            } else if (resultCode == RecognizerIntent.RESULT_AUDIO_ERROR) {
                Utils.showError(MainActivity.this, "Audio Error");
            } else if (resultCode == RecognizerIntent.RESULT_CLIENT_ERROR) {
                Utils.showError(MainActivity.this, "Client Error");
            } else if (resultCode == RecognizerIntent.RESULT_NETWORK_ERROR) {
                Utils.showError(MainActivity.this, "Network Error");
            } else if (resultCode == RecognizerIntent.RESULT_NO_MATCH) {
                Utils.showError(MainActivity.this, "No Match");
            } else if (resultCode == RecognizerIntent.RESULT_SERVER_ERROR) {
                Utils.showError(MainActivity.this, "Server Error");
            }
        }
    }

    public void voiceSearch(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        // Specify the calling package to identify your application
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());

        // Display an hint to the user about what he should say.
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.try_play_dual_core));

        // Given an hint to the recognizer about what the user is going to say
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        startActivityForResult(intent, RECOGNIZER_REQ_CODE);
    }

    public void askCayley(final String query) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    // Create a new HttpClient and Post Header
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(
                            Utils.CAYLEY_URL);
                    String queryString = WordUtils.capitalize(query);
                    String cayleyReq = "function getActionForArtists(artist) {\n" +
                            "  return artist.In(\"http://schema.org/sameAs\").Out(\"http://schema.org/potentialAction\").Has(\"a\", \"http://schema.org/ListenAction\").Out(\"http://schema.org/target\")\n" +
                            "}\n" +
                            "\n" +
                            "function getIdFor(thing) {\n" +
                            "  return g.V(thing).In([\"http://rdf.freebase.com/ns/type.object.name\", \"http://schema.org/name\"]).ToValue()\n" +
                            "}\n" +
                            "\n" +
                            "function getTypeFor(id) {\n" +
                            "  return g.V(id).Out(\"a\").ToValue()\n" +
                            "}\n" +
                            "\n" +
                            "function getActionForName(name) {\n" +
                            "  var uri = getIdFor(name)\n" +
                            "  if (uri == null) {\n" +
                            "    return\n" +
                            "  }\n" +
                            "  var type = getTypeFor(uri)\n" +
                            "  var artist_query\n" +
                            "  if (type == \"http://rdf.freebase.com/ns/music.genre\") {\n" +
                            "    artist_query = g.V(uri).In(\"http://rdf.freebase.com/ns/music.artist.genre\")\n" +
                            "  } else {\n" +
                            "    artist_query = g.V(uri)\n" +
                            "  }\n" +
                            "  return getActionForArtists(artist_query)\n" +
                            "}\n" +
                            "\n" +
                            "getActionForName(\"" + queryString + "\").All()";
                    httppost.setEntity(new StringEntity(cayleyReq));
                    // Execute HTTP Post Request
                    HttpResponse httpResponse = httpclient.execute(httppost);
                    String response = EntityUtils.toString(httpResponse.getEntity());

                    Gson gson = new Gson();
                    CayleyResult cayleyResult = gson.fromJson(response, CayleyResult.class);
                    if (cayleyResult.isEmpty()) {
                        Utils.showError(MainActivity.this, "No nodes found for " + queryString + " in Cayley.");
                        return null;
                    }
                    return cayleyResult.getResult(0).getId();
                } catch (ClientProtocolException e) {
                    // TODO Auto-generated catch block
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
                return null;
            }

            @Override
            protected void onPostExecute(String appId) {
                setProgressBarIndeterminateVisibility(false);

                if (appId == null) {
                    return;
                }

                Intent intent = new Intent();
                intent.setAction("android.media.action.MEDIA_PLAY_FROM_SEARCH");
                intent.setData(Uri.parse(Utils.convertGSAtoUri(appId)));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Utils.showError(MainActivity.this, appId + "Activity not found");
                }
            }

        }.execute((Void) null);
    }

    @Override
    public void onActionSelected(ActionData action) {
        Intent reviewIntent = new Intent(MainActivity.this, CayleyActivity.class);
        reviewIntent.putExtra(ACTION, action.getAction());
        reviewIntent.putExtra(ACTION_TYPE, action.getActionType());
        for (Pair<String, String> pair : action.getActionExtras()) {
            reviewIntent.putExtra(pair.first, pair.second);
        }

        startActivity(reviewIntent);
    }
}
