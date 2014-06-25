/*
 * Copyright (c) 2013 Google Inc.
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

package com.google.cloud.backend.core;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.services.AbstractGoogleClient;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;

/**
 * Common utilities.
 */
public class CloudEndpointUtils {

    public static final boolean LOCAL_ANDROID_RUN = false;
    private static final String LOCAL_APP_ENGINE_SERVER_URL = "http://10.0.2.2:8888";

    /**
     * Updates the Google client builder to connect the appropriate server based
     * on whether LOCAL_ANDROID_RUN is true or false.
     * 
     * @param builder Google client builder
     * @return same Google client builder
     */
    public static <B extends AbstractGoogleClient.Builder> B updateBuilder(B builder) {
        if (LOCAL_ANDROID_RUN) {
            builder.setRootUrl(LOCAL_APP_ENGINE_SERVER_URL + "/_ah/api/");
        }

        // only enable GZip when connecting to remote server
        final boolean enableGZip = builder.getRootUrl().startsWith("https:");

        builder.setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
            @Override
            public void initialize(AbstractGoogleClientRequest<?> request) throws IOException {
                if (!enableGZip) {
                    request.setDisableGZipContent(true);
                }
            }
        });

        return builder;
    }

    /**
     * Logs the given message and shows an error alert dialog with it.
     * 
     * @param activity activity
     * @param tag log tag to use
     * @param message message to log and show or {@code null} for none
     */
    public static void logAndShow(Activity activity, String tag, String message) {
        Log.e(tag, message);
        showError(activity, message);
    }

    /**
     * Logs the given throwable and shows an error alert dialog with its
     * message.
     * 
     * @param activity activity
     * @param tag log tag to use
     * @param t throwable to log and show
     */
    public static void logAndShow(Activity activity, String tag, Throwable t) {
        Log.e(tag, "Error", t);
        String message = t.getMessage();
        if (t instanceof GoogleJsonResponseException) {
            GoogleJsonError details = ((GoogleJsonResponseException) t).getDetails();
            if (details != null) {
                message = details.getMessage();
            }
        }
        showError(activity, message);
    }

    /**
     * Shows an error alert dialog with the given message.
     * 
     * @param activity activity
     * @param message message to show or {@code null} for none
     */
    public static void showError(final Activity activity, String message) {
        final String errorMessage = message == null ? "Error" : "[Error ] " + message;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}
