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

package com.google.cloud.backend;

import android.app.Application;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.cloud.backend.core.Consts;

import java.io.IOException;

/**
 * This class manages Google Cloud Messaging push notifications and CloudQuery
 * subscriptions.
 */
public class GCMIntentService extends IntentService {

    private static final String GCM_KEY_SUBID = "subId";

    private static final String GCM_TYPEID_QUERY = "query";

    private static final String PROPERTY_REG_ID = "registration_id";

    private static final String PROPERTY_APP_VERSION = "app_version";

    public static final String BROADCAST_ON_MESSAGE = "on-message-event";
    
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.i(Consts.TAG, "onHandleIntent: message error");
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.i(Consts.TAG, "onHandleIntent: message deleted");
            // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                String subId = intent.getStringExtra(GCM_KEY_SUBID);
                Log.i(Consts.TAG, "onHandleIntent: subId: " + subId);
                String[] tokens = subId.split(":");
                String typeId = tokens[1];

                // dispatch message
                if (GCM_TYPEID_QUERY.equals(typeId)) {
                    Intent messageIntent = new Intent(BROADCAST_ON_MESSAGE);
                    messageIntent.putExtras(intent);
                    messageIntent.putExtra("token", tokens[2]);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
                }
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Returns registration id associated with the specified {@link android.app.Application}
     * . This method will block the thread until regId will be available.
     *
     * @param app {@link android.app.Application}
     * @return registration id
     */
    public static String getRegistrationId(Application app) {
        SharedPreferences prefs = getGcmPreferences(app);
        String regId = prefs.getString(PROPERTY_REG_ID, "");
        
        if (regId.isEmpty()) {
            Log.i(Consts.TAG, "Registration not found.");
            return doRegister(app);
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(app);
        if (registeredVersion != currentVersion) {
            Log.i(Consts.TAG, "App version changed.");
            return doRegister(app);
        }
        return regId;
    }
    
    private static String doRegister(Context context) {
        String msg = "";
        try {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
            String regId = gcm.register(Consts.PROJECT_NUMBER);
            msg = "Device registered, registration ID=" + regId;

            // For this demo: we don't need to send it because the device will send
            // upstream messages to a server that echo back the message using the
            // 'from' address in the message.

            SharedPreferences prefs = getGcmPreferences(context);
            int appVersion = getAppVersion(context);
            Log.i(Consts.TAG, "Saving regId on app version " + appVersion);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PROPERTY_REG_ID, regId);
            editor.putInt(PROPERTY_APP_VERSION, appVersion);
            editor.commit();
        } catch (IOException ex) {
            msg = "Error :" + ex.getMessage();
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
        }
        return msg;
    }

    public GCMIntentService() {
        super(Consts.PROJECT_NUMBER);
    }
    
    /**
     * @return the stored SharedPreferences for GCM
     */
    private static SharedPreferences getGcmPreferences(Context context) {
        return context.getSharedPreferences(GCMIntentService.class.getSimpleName(), MODE_PRIVATE);
    }
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
