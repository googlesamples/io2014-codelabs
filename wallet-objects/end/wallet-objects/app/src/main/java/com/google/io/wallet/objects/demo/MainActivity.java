package com.google.io.wallet.objects.demo;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.wallet.CreateWalletObjectsRequest;
import com.google.android.gms.wallet.OfferWalletObject;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.android.gms.wallet.wobs.UriData;
import com.google.android.gms.wallet.wobs.WalletObjectsConstants;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

  private GoogleApiClient mGoogleApiClient;

  public static final int SAVE_TO_WALLET = 888;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      mGoogleApiClient = new GoogleApiClient.Builder(this)
          .addConnectionCallbacks(this)
          .addOnConnectionFailedListener(this)
          .addApi(Wallet.API, new Wallet.WalletOptions.Builder()
              .setEnvironment(WalletConstants.ENVIRONMENT_PRODUCTION)
              .setTheme(WalletConstants.THEME_HOLO_LIGHT)
              .build())
          .build();

      setContentView(R.layout.activity_main);
  }

  protected void onStart() {
    super.onStart();
    mGoogleApiClient.connect();
  }

  protected void onStop() {
    mGoogleApiClient.disconnect();
    super.onStop();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      int id = item.getItemId();
      if (id == R.id.action_settings) {
          return true;
      }
      return super.onOptionsItemSelected(item);
  }

  public void saveToWallet(View view){
    CreateWalletObjectsRequest request = new CreateWalletObjectsRequest(generateOfferObject());
    Wallet.WalletObjects.createWalletObjects(mGoogleApiClient, request, SAVE_TO_WALLET);
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data){
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode){
      case SAVE_TO_WALLET:
        switch (resultCode) {
          case Activity.RESULT_OK:
            Toast.makeText(this, "Saved Offer", Toast.LENGTH_LONG).show();
            break;
          default:
            Toast.makeText(this, "An Error Occurred", Toast.LENGTH_LONG).show();
        }
      break;
    }
  }

  public OfferWalletObject generateOfferObject(){

    //Image Uris
    List<UriData> imageUris = new ArrayList<UriData>();
    UriData imageUri = new UriData("https://www.google.com/events/io/images/photos/LogisticsGettingAround-1600.jpg", "San Francisco");
    imageUris.add(imageUri);

    //Geolocations
    LatLng google = new LatLng(37.422601, -122.085286);
    LatLng mosconeWest = new LatLng(37.7842, -122.4016);

    List<LatLng> locations = new ArrayList<LatLng>();
    locations.add(google);
    locations.add(mosconeWest);

    //URIs
    UriData mosconeWestMaps = new UriData("http://maps.google.com/maps?q=moscone%20west","Moscone West");
    UriData googleIo = new UriData("http://google.com/io", "Google I/O");

    List<UriData> uris = new ArrayList<UriData>();

    uris.add(mosconeWestMaps);
    uris.add(googleIo);

    OfferWalletObject offerWalletObject = OfferWalletObject
        .newBuilder()
        .setClassId("2979629121142263025.OfferClassIO")
        .setId("2979629121142263025.OfferOfferObjectIO")
        .setState(WalletObjectsConstants.State.ACTIVE)
        .setIssuerName("Google I/O Demo")
        .setTitle("20% off Code Labs")
        .setBarcodeType("qrCode")
        .setBarcodeValue("28343E3")
        .setBarcodeAlternateText("12345")
        .setBarcodeLabel("Coupon Code")
        .addLinksModuleDataUris(uris)
        .addImageModuleDataMainImageUris(imageUris)
        .addLocations(locations)
        .build();

    return offerWalletObject;

  }

  public void onConnected(Bundle bundle) {
    // don't need to do anything here
    // subclasses may override if they need to do anything
  }

  public void onConnectionSuspended(int cause) {
    // don't need to do anything here
    // subclasses may override if they need to do anything
  }

  public void onConnectionFailed(ConnectionResult result) {
    //Log error and retry in the future
  }
}
