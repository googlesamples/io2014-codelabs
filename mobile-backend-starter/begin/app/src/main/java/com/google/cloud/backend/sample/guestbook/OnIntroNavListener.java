package com.google.cloud.backend.sample.guestbook;

/**
 * Listener used by the introductory Fragments to interact with each other and
 * with the Guestbook Activity. The hosting Guestbook Activity should implement
 * this listener.
 */
public interface OnIntroNavListener {
    public void toFirst(String fromTag);

    public void toSecond(String fromTag);

    public void toThird(String fromTag);

    public void done(boolean skipFutureIntro);
}
