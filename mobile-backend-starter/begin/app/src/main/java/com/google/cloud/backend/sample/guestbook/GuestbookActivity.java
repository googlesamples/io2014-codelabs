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

package com.google.cloud.backend.sample.guestbook;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.cloud.backend.R;
import com.google.cloud.backend.core.CloudBackend;
import com.google.cloud.backend.core.CloudBackendFragment;
import com.google.cloud.backend.core.CloudBackendFragment.OnListener;
import com.google.cloud.backend.core.CloudCallbackHandler;
import com.google.cloud.backend.core.CloudEntity;
import com.google.cloud.backend.core.CloudQuery.Order;
import com.google.cloud.backend.core.CloudQuery.Scope;
import com.google.cloud.backend.core.Consts;
import com.google.cloud.backend.mobilebackend.model.BlobAccess;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Sample Guestbook app with Mobile Backend Starter.
 */
public class GuestbookActivity extends Activity implements OnListener {

    private static final String BROADCAST_PROP_DURATION = "duration";
    private static final String BROADCAST_PROP_MESSAGE = "message";

    private static final int INTRO_ACTIVITY_REQUEST_CODE = 1;
    private static final int SELECT_PICTURE = 2;

    public static final String PROCESSING_FRAGMENT_TAG = "BACKEND_FRAGMENT";
    private static final String SPLASH_FRAGMENT_TAG = "SPLASH_FRAGMENT";

    public static final String GUESTBOOK_SHARED_PREFS = "GUESTBOOK_SHARED_PREFS";
    public static final String SHOW_INTRO_PREFS_KEY = "SHOW_INTRO_PREFS_KEY";
    public static final String SCOPE_PREFS_KEY = "SCOPE_PREFS_KEY";

    public static final String BLOB_PICTURE_MESSAGE_PREFIX = "_PICTURE_";
    public static final String BLOB_PICTURE_DELIMITER = "::";

    private boolean showIntro = true;

    /*
     * UI components
     */
    private ListView mPostsView;
    private TextView mEmptyView;
    private EditText mMessageTxt;
    private ImageView mSendBtn;
    private ImageView mPictureBtn;
    private TextView mAnnounceTxt;

    private FragmentManager mFragmentManager;
    private CloudBackendFragment mProcessingFragment;
    private SplashFragment mSplashFragment;

    /**
     * A list of posts to be displayed
     */
    private List<CloudEntity> mPosts = new LinkedList<CloudEntity>();

    /**
     * Override Activity lifecycle method.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the view
        mPostsView = (ListView) findViewById(R.id.posts_list);
        registerForContextMenu(mPostsView);
        mEmptyView = (TextView) findViewById(R.id.no_messages);
        mMessageTxt = (EditText) findViewById(R.id.message);
        mMessageTxt.setHint("Type message");
        mMessageTxt.setEnabled(false);
        mSendBtn = (ImageView) findViewById(R.id.send_btn);
        mPictureBtn = (ImageView) findViewById(R.id.picture_btn);
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendButtonPressed(v);
            }
        });
        mPictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPictureButtonPressed(v);
            }
        });
        mSendBtn.setEnabled(false);
        mPictureBtn.setEnabled(true);
        mAnnounceTxt = (TextView) findViewById(R.id.announce_text);

        mFragmentManager = getFragmentManager();

        checkForPreferences();

    }

    /**
     * Override Activity lifecycle method.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Override Activity lifecycle method.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem loginItem = menu.findItem(R.id.switch_account);
        loginItem.setVisible(Consts.IS_AUTH_ENABLED);
        return true;
    }

    /**
     * Override Activity lifecycle method.
     * <p/>
     * To add more option menu items in your client, add the item to menu/activity_main.xml,
     * and provide additional case statements in this method.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_account:
                mProcessingFragment.signInAndSubscribe(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.showIntro = false;
    }

    /**
     * Override Activity lifecycle method.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result codes
        if (requestCode == INTRO_ACTIVITY_REQUEST_CODE) {
            initiateFragments();
        }

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                uploadSelectedPicture(data);
            }
        }
        // call super method to ensure unhandled result codes are handled
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        Log.i(Consts.TAG, "View context menu" + v.toString());
        menu.setHeaderTitle("Image Processing");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.image_processing, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        CloudEntity selectedEntity = mPosts.get(info.position);
        if (!isCloudEntityPicture(selectedEntity)) {
            Toast.makeText(getBaseContext(), "This menu is not available with the item selected",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        switch (item.getItemId()) {
            case R.id.transform_image:
                transformImage(selectedEntity);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void transformImage(CloudEntity selectedEntity) {
        String entityMessage = selectedEntity.get("message").toString();
        CloudCallbackHandler<BlobAccess> handlerForProcessImage = new CloudCallbackHandler<BlobAccess>() {
            @Override
            public void onComplete(final BlobAccess blobAccessResult) {
                Toast.makeText(getBaseContext(), "Uploading the transformed image", Toast.LENGTH_SHORT).show();
                String pictureMesage = BLOB_PICTURE_MESSAGE_PREFIX + BLOB_PICTURE_DELIMITER
                        + blobAccessResult.getAccessUrl();
                insertNewMessage(pictureMesage);
            }

            @Override
            public void onError(final IOException exception) {
                handleEndpointException(exception);
            }
        };

        BucketAndObjectName bucketAndObjectName = parsePictureMessageToBucketAndObject(entityMessage);
        CloudBackend.ImageTransformationParam param = new CloudBackend.ImageTransformationParam();
        param.bucketName = bucketAndObjectName.bucketName;
        param.objectName = bucketAndObjectName.objectName;
        param.accessModeForTransformedImage = "PUBLIC_READ";

        Toast.makeText(getBaseContext(), "Transforming the image", Toast.LENGTH_SHORT).show();
        findViewById(R.id.progress_horizontal).setVisibility(View.VISIBLE);
        mProcessingFragment.getCloudBackend().transformImage(param, handlerForProcessImage);
    }

    /**
     * Uploads the selected image to Google Cloud Storage.
     *
     * @param data The intent with which the image was selected.
     */
    private void uploadSelectedPicture(Intent data) {
        Uri selectedImageUri = data.getData();
        final InputStream uploadInputStream = getInputStreamFromUri(selectedImageUri);
        if (uploadInputStream == null) {
            Toast.makeText(getApplicationContext(), "Failed to load the image", Toast.LENGTH_LONG).show();
            return;
        }

        final CloudBackend.BlobAccessParam blobAccessParam = new CloudBackend.BlobAccessParam();
        blobAccessParam.bucketName = Consts.PROJECT_ID;
        blobAccessParam.objectName = String.valueOf("cloudguestbook-picture-" + System.currentTimeMillis());
        blobAccessParam.accessMode = "PUBLIC_READ";
        blobAccessParam.contentType = "";

        // create a response handler that will receive the result or an error
        CloudCallbackHandler<BlobAccess> handlerForBlobUpload = new CloudCallbackHandler<BlobAccess>() {
            @Override
            public void onComplete(final BlobAccess blobAccessResult) {
                CloudBackend.BlobUploadParam uploadParam = new CloudBackend.BlobUploadParam();
                uploadParam.blobAccess = blobAccessResult;
                uploadParam.inputStream = uploadInputStream;
                uploadParam.blobAccessParam = blobAccessParam;
                mProcessingFragment.getCloudBackend().uploadBlob(uploadParam, new CloudCallbackHandler<Boolean>() {
                    @Override
                    public void onComplete(Boolean booleanResult) {
                        String message = booleanResult ? "Upload completed " : "Failed to upload";
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                        if (booleanResult) {
                            String pictureMesage = BLOB_PICTURE_MESSAGE_PREFIX + BLOB_PICTURE_DELIMITER
                                    + blobAccessResult.getAccessUrl();
                            insertNewMessage(pictureMesage);
                        }
                    }
                });
            }

            @Override
            public void onError(final IOException exception) {
                handleEndpointException(exception);
            }
        };
        findViewById(R.id.progress_horizontal).setVisibility(View.VISIBLE);
        Toast.makeText(getApplicationContext(), "Uploading the image", Toast.LENGTH_LONG).show();
        mProcessingFragment.getCloudBackend().getBlobUploadUrl(blobAccessParam, handlerForBlobUpload);
    }

    /**
     * Returns inputStream from the selectedImageUri.
     *
     * @param selectedImageUri
     */
    private InputStream getInputStreamFromUri(Uri selectedImageUri) {
        InputStream inputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(selectedImageUri);
        } catch (IOException e) {
            Log.w(Consts.TAG, String.format("Failed to load input stream from the Uri [%s] ",
                    selectedImageUri.toString()));
        }
        return inputStream;
    }

    /**
     * Method called via OnListener in {@link com.google.cloud.backend.core.CloudBackendFragment}.
     */
    @Override
    public void onCreateFinished() {
        listPosts();
    }

    /**
     * Method called via OnListener in {@link com.google.cloud.backend.core.CloudBackendFragment}.
     */
    @Override
    public void onBroadcastMessageReceived(List<CloudEntity> l) {
        for (CloudEntity e : l) {
            String message = (String) e.get(BROADCAST_PROP_MESSAGE);
            int duration = Integer.parseInt((String) e.get(BROADCAST_PROP_DURATION));
            Toast.makeText(this, message, duration).show();
            Log.i(Consts.TAG, "A message was recieved with content: " + message);
        }
    }

    private void initiateFragments() {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        // Check to see if we have retained the fragment which handles
        // asynchronous backend calls
        mProcessingFragment = (CloudBackendFragment) mFragmentManager.
                findFragmentByTag(PROCESSING_FRAGMENT_TAG);
        // If not retained (or first time running), create a new one
        if (mProcessingFragment == null) {
            mProcessingFragment = new CloudBackendFragment();
            mProcessingFragment.setRetainInstance(true);
            fragmentTransaction.add(mProcessingFragment, PROCESSING_FRAGMENT_TAG);
        }

        // Add the splash screen fragment
        mSplashFragment = new SplashFragment();
        fragmentTransaction.add(R.id.activity_main, mSplashFragment, SPLASH_FRAGMENT_TAG);
        fragmentTransaction.commit();
    }

    private void checkForPreferences() {
        SharedPreferences settings =
                getSharedPreferences(GUESTBOOK_SHARED_PREFS, Context.MODE_PRIVATE);
        boolean showIntro = true;
        if (settings != null) {
            showIntro = settings.getBoolean(SHOW_INTRO_PREFS_KEY, true) && this.showIntro;
        }
        if (showIntro) {
            Intent intent = new Intent(this, IntroductionActivity.class);
            startActivityForResult(intent, INTRO_ACTIVITY_REQUEST_CODE);
        } else {
            initiateFragments();
        }
    }

    /**
     * onClick method.
     */
    public void onSendButtonPressed(View view) {
        insertNewMessage(mMessageTxt.getText().toString());
    }

    private void insertNewMessage(String message) {
        // create a CloudEntity with the new post
        CloudEntity newPost = new CloudEntity("Guestbook");
        newPost.put("message", message);

        // create a response handler that will receive the result or an error
        CloudCallbackHandler<CloudEntity> handler = new CloudCallbackHandler<CloudEntity>() {
            @Override
            public void onComplete(final CloudEntity result) {
                mPosts.add(0, result);
                updateGuestbookView();
                mMessageTxt.setText("");
                mMessageTxt.setEnabled(true);
                mSendBtn.setEnabled(true);
                findViewById(R.id.progress_horizontal).setVisibility(View.GONE);
            }

            @Override
            public void onError(final IOException exception) {
                handleEndpointException(exception);
            }
        };

        findViewById(R.id.progress_horizontal).setVisibility(View.VISIBLE);
        // execute the insertion with the handler
        mProcessingFragment.getCloudBackend().insert(newPost, handler);
        mMessageTxt.setEnabled(false);
        mSendBtn.setEnabled(false);
    }

    /**
     * The handler when picture_btn is clicked.
     */
    public void onPictureButtonPressed(View view) {
        // in onCreate or any event where your want the user to
        // select a file
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
    }

    /**
     * Retrieves the list of all posts from the backend and updates the UI. For
     * demonstration in this sample, the query that is executed is:
     * "SELECT * FROM Guestbook ORDER BY _createdAt DESC LIMIT 50" This query
     * will be re-executed when matching entity is updated.
     */
    private void listPosts() {
        // create a response handler that will receive the result or an error
        CloudCallbackHandler<List<CloudEntity>> handler =
                new CloudCallbackHandler<List<CloudEntity>>() {
                    @Override
                    public void onComplete(List<CloudEntity> results) {
                        mAnnounceTxt.setText(R.string.announce_success);
                        mPosts = results;
                        animateArrival();
                        updateGuestbookView();
                    }

                    @Override
                    public void onError(IOException exception) {
                        mAnnounceTxt.setText(R.string.announce_fail);
                        animateArrival();
                        handleEndpointException(exception);
                    }
                };

        // execute the query with the handler
        mProcessingFragment.getCloudBackend().listByKind(
                "Guestbook", CloudEntity.PROP_CREATED_AT, Order.DESC, 50,
                Scope.FUTURE_AND_PAST, handler);
    }

    private boolean firstArrival = true;

    private void animateArrival() {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        mSplashFragment = (SplashFragment) mFragmentManager.findFragmentByTag(
                SPLASH_FRAGMENT_TAG);
        if (mSplashFragment != null) {
            fragmentTransaction.remove(mSplashFragment);
            fragmentTransaction.commitAllowingStateLoss();
        }

        if (firstArrival) {
            mAnnounceTxt.setVisibility(View.VISIBLE);
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.translate_progress);
            anim.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mAnnounceTxt.setVisibility(View.GONE);
                }
            });
            mAnnounceTxt.startAnimation(anim);
            firstArrival = false;
        }
    }

    private void updateGuestbookView() {
        mMessageTxt.setEnabled(true);
        mSendBtn.setEnabled(true);
        mPictureBtn.setEnabled(true);
        if (!mPosts.isEmpty()) {
            mEmptyView.setVisibility(View.GONE);
            mPostsView.setVisibility(View.VISIBLE);
            mPostsView.setAdapter(new PostAdapter(
                    this, android.R.layout.simple_list_item_1, mPosts));
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
            mPostsView.setVisibility(View.GONE);
        }
    }

    private void handleEndpointException(IOException e) {
        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        mSendBtn.setEnabled(true);
        findViewById(R.id.progress_horizontal).setVisibility(View.GONE);
    }

    private boolean isCloudEntityPicture(CloudEntity e) {
        String entityMessage = e.get("message").toString();
        return entityMessage.startsWith(GuestbookActivity.BLOB_PICTURE_MESSAGE_PREFIX +
                GuestbookActivity.BLOB_PICTURE_DELIMITER);
    }

    private BucketAndObjectName parsePictureMessageToBucketAndObject(String pictureMessage) {
        String imageUrl = pictureMessage.split(GuestbookActivity.BLOB_PICTURE_DELIMITER)[1];
        String urlWithoutProtocol = imageUrl.split("://")[1];

        String[] bucketAndObjectName = urlWithoutProtocol.split("/");
        BucketAndObjectName result = new BucketAndObjectName();
        result.bucketName = bucketAndObjectName[1];
        result.objectName = bucketAndObjectName[2];
        return result;
    }

    private static class BucketAndObjectName {
        public String bucketName;
        public String objectName;
    }
}
