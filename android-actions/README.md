# Welcome to the Jarek & Shawn Music sample Android app
This folder contains the source code to accompany the Google I/O 2014 codelab
[Android: Add Actions to a Music App](http://io2014codelabs.appspot.com/static/codelabs/android-actions/).

## begin
The **begin** folder is the starting point of the codelab.
It contains an Android Studio project for a simple Android music app.

This simple music app consists of two activities or screens.
A main screen showing the list of artists available in the app
and an artist detail screen. Once the media player is started,
a playlist fragment shows the list of currently playing songs.

These screens correspond to the following code files.

- `MainActivity.java`: The main entry point for the app.
It shows the available artists
(currently just one entry, the band "Dual Core").
This activity launches the `ArtistActivity` or `PlaylistFragment`
depending on the user's choice.
- `ArtistActivity.java`: This activity displays information
about a particular artist, including artist metadata and song list.
Users can launch the `PlaylistFragment` to play the artist's music.

The `AndroidManifest.xml` file configures the app components.

 `PlaylistFragment.java`: This fragment launches a service
to play audio resources defined in the app.
-  `activity_main.xml`,`activity_artist.xml`, `fragment_playlist.xml`:
Set up the UI of the visible components.
- The class `MyMusicStore` mocks a music repository.
It hardcodes one artist with two songs
along with the associated metadata and images.
- Various other files define image, music resources, and string constants.

## end
The **end** folder contains the resulting source code
after following the codelab steps. Step 4 is an optional.
If it is completed, your `ArtistActivity.java` file should look like
the one in the *step4* folder.