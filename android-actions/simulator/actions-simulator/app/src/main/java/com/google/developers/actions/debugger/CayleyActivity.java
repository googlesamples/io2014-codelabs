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
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.developers.actions.debugger.util.CayleyResult;
import com.google.developers.actions.debugger.util.Utils;
import com.google.gson.Gson;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Activity class which gets deeplink from Cayley and fires intent..
 */
public class CayleyActivity extends Activity {
    private static final int REQUEST_RECEIVE = 1;
    private Intent mIntent;
    private EditText mEditText;
    private String mActionType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_cayley);
        Intent intent = getIntent();
        intent.setAction(intent.getStringExtra(MainActivity.ACTION));
        intent.setComponent(null);
        mIntent = intent;
        mActionType = intent.getStringExtra(MainActivity.ACTION_TYPE);
        if (mActionType.equals("PLAY_ARTIST")) {
            mEditText = (EditText) findViewById(R.id.artist);
            mEditText.setVisibility(View.VISIBLE);
            mEditText.setText(getString(R.string.dual_core));
        } else {
            mEditText = (EditText) findViewById(R.id.genre);
            mEditText.setVisibility(View.VISIBLE);
            mEditText.setText(getString(R.string.nerdcore));
        }
    }

    public void sendIntent(View view) {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    // Create a new HttpClient and Post Header
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(
                            Utils.CAYLEY_URL);
                    String queryString = mEditText.getText().toString();
                    queryString = WordUtils.capitalize(queryString) + "\")";
                    if (mActionType.equals("PLAY_GENRE")) {
                        queryString += ".In(\"http://rdf.freebase.com/ns/music.artist.genre\")";
                    }
                    String cayleyReq = "function getActionForArtists(artist) {" + "\n" +
                            "return artist.In(\"http://schema.org/sameAs\").Out(\"http://" +
                            "schema.org/potentialAction\").Has(\"a\", \"http://schema.org/" +
                            "ListenAction\").Out(\"http://schema.org/target\")" + "\n" + "}" + "\n"
                            + "function getNameFor(thing) {" + "\n" + "return g.V(thing)." +
                            "In([\"http://rdf.freebase.com/ns/type.object.name\", \"http://" +
                            "schema.org/name\"])" + "\n" + "}" + "\n" + "artists = getNameFor(\"" +
                            queryString + "\n" + "getActionForArtists(artists).All()";
                    httppost.setEntity(new StringEntity(cayleyReq));
                    // Execute HTTP Post Request
                    HttpResponse httpResponse = httpclient.execute(httppost);
                    String response = EntityUtils.toString(httpResponse.getEntity());

                    Gson gson = new Gson();
                    CayleyResult cayleyResult = gson.fromJson(response, CayleyResult.class);
                    if (cayleyResult.isEmpty()) {
                        Utils.showError(CayleyActivity.this, "No nodes found for the query in Cayley.");
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

                mIntent.setData(Uri.parse(Utils.convertGSAtoUri(appId)));

                try {
                    startActivity(mIntent);
                } catch (ActivityNotFoundException e) {
                    Utils.showError(CayleyActivity.this, appId + "Activity not found");
                }
            }

        }.execute((Void) null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.review, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
