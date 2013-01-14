package com.example.silencer;

import android.location.Location;
import android.media.AudioManager;
import android.location.LocationListener;
import android.location.LocationManager;
//import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	LocationManager locationManager;
	Location currentLocation;
	Location[] hotSpots;
	int currentSpots;
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			return true;
		}
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;
		if (isSignificantlyNewer) {
			return true;
		}
		else if (isSignificantlyOlder) {
			return false;
		}
		
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;
		
		boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());
		
		if (isMoreAccurate) {
			return true;
		}
		else if (isNewer && !isLessAccurate) {
			return true;
		}
		else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}
	
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
	private final LocationListener listener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			updateData(location);
			
		}

		@Override
		public void onProviderDisabled(String provider) {
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status,
				Bundle extras) {
			// TODO Auto-generated method stub
			
		}
	};
	
	public void updateData(Location location) {
//		if (isBetterLocation(location, currentLocation)) {
			currentLocation = location;
//		}
		TextView latitude = (TextView) findViewById(R.id.latitude);
		TextView longitude = (TextView) findViewById(R.id.longitude);
		TextView status = (TextView) findViewById(R.id.status);
		latitude.setText(Double.toString(currentLocation.getLatitude()));
		longitude.setText(Double.toString(currentLocation.getLongitude()));
		for (int i = 0; i < currentSpots; i++) {
			if (hotSpots[i].distanceTo(currentLocation) < 5) {
				status.setText("Inside Hotspot");
				mutePhone();
				return;
			}
		}
		status.setText("Outside Hotspot");
		unmutePhone();
	}
	
	void mutePhone() {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
	}
	
	void unmutePhone() {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		System.out.println("Creating app");
		hotSpots = new Location[1000];
		currentSpots = 0;
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//		LocationProvider locationProvider = (LocationProvider) locationManager.getProvider(LocationManager.GPS_PROVIDER);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5, listener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 5, listener);
		/*Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_HIGH);
		criteria.setAltitudeRequired(false);
		String providerName = locationManager.getBestProvider(criteria, true);
		if (providerName != null) {
		}*/
	}

	public void addLocation(View view) {
		hotSpots[currentSpots++] = currentLocation;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		System.out.println("Starting app");
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (!gpsEnabled) {
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("GPS Disabled");
			alertDialog.setMessage("The application will try to automatically enable GPS.");
			alertDialog.show();
			
			enableLocationSettings();
		}
	}
	
	public void enableLocationSettings() {
		Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(settingsIntent);
	}
	
	public void onDestroy() {
		locationManager.removeUpdates(listener);
		hotSpots = null;
	}
	
}
