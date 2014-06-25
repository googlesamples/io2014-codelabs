package com.google.cloud.backend.sample.guestbook;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.cloud.backend.R;
import com.google.cloud.backend.core.CloudBackendFragment;
import com.google.cloud.backend.core.CloudEntity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * This ArrayAdapter uses CloudEntities as items and displays them as a post in
 * the guestbook. Layout uses row.xml.
 *
 */
public class PostAdapter extends ArrayAdapter<CloudEntity> {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss ", Locale.US);

    private LayoutInflater mInflater;

    private ImageLoader mImageLoader = getFragmentReference().getImageLoader();

    private CloudBackendFragment getFragmentReference() {
        Fragment fragment = ((Activity) getContext()).getFragmentManager().findFragmentByTag(GuestbookActivity.PROCESSING_FRAGMENT_TAG);
        return (CloudBackendFragment) fragment;
    }

    /**
     * Creates a new instance of this adapter.
     *
     * @param context
     * @param textViewResourceId
     * @param objects
     */
    public PostAdapter(Context context, int textViewResourceId, List<CloudEntity> objects) {
        super(context, textViewResourceId, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView != null ?
                convertView : mInflater.inflate(R.layout.row_post, parent, false);

        CloudEntity ce = getItem(position);
        if (ce != null) {
            String entityMessage = ce.get("message").toString();

            TextView message = (TextView) view.findViewById(R.id.messageContent);
            TextView signature = (TextView) view.findViewById(R.id.signature);
            if (message != null) {
                if (entityMessage.startsWith(GuestbookActivity.BLOB_PICTURE_MESSAGE_PREFIX +
                        GuestbookActivity.BLOB_PICTURE_DELIMITER)) {
                    String imageUrl = entityMessage.split(GuestbookActivity.BLOB_PICTURE_DELIMITER)[1];
                    NetworkImageView imageView = (NetworkImageView) view.findViewById(R.id.messagePicture);
                    imageView.setImageUrl(imageUrl, mImageLoader);
                    imageView.setDefaultImageResId(R.drawable.abc_spinner_ab_default_holo_light);
                    imageView.setVisibility(View.VISIBLE);
                    message.setVisibility(View.GONE);
                } else {
                    message.setText(ce.get("message").toString());
                    message.setVisibility(View.VISIBLE);
                    NetworkImageView imageView = (NetworkImageView) view.findViewById(R.id.messagePicture);
                    imageView.setVisibility(View.GONE);
                }
            }
            if (signature != null) {
                signature.setText(getAuthor(ce) + " " + SDF.format(ce.getCreatedAt()));
            }
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return this.getView(position, convertView, parent);
    }

    /**
     * Gets the author field of the CloudEntity.
     *
     * @param post the CloudEntity
     * @return author string
     */
    private String getAuthor(CloudEntity post) {
        if (post.getCreatedBy() != null) {
            return " " + post.getCreatedBy().replaceFirst("@.*", "");
        } else {
            return "<anonymous>";
        }
    }

}
