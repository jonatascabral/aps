package br.com.unip.aps;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends BaseActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private Location myLocation;
    private boolean canGetLocation = false;
    private static final int MIN_TIME_BW_UPDATES = 15000;
    private static final int MIN_DISTANCE_CHANGE_FOR_UPDATES = 15000;
    protected HashMap<Integer, JSONObject> markers = new HashMap<>();
    protected HashMap<Marker, Integer> mHashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            WebService service = new WebService(this, null);
            service.setAction(WebService.ACTION_GET_NOTICES);
            Object jsonObject = service.execute().get();
            if (jsonObject != null) {

                try {
                    JSONObject json = new JSONObject(jsonObject.toString());
                    JSONArray jsonMarkers = json.getJSONArray("markers");
                    int numMarkers = jsonMarkers.length();
                    for (int i = 0; i < numMarkers; ++i) {
                        JSONObject marker = new JSONObject(jsonMarkers.getString(i));
                        markers.put(marker.getInt("id"), marker);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, R.string.json_error, Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.i("json is ", "null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.json_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_new_notice && this.canGetLocation) {
            Intent newNoticeIntent = new Intent(getBaseContext(), FormActivity.class);
            newNoticeIntent.putExtra("lat", myLocation.getLatitude());
            newNoticeIntent.putExtra("lng", myLocation.getLongitude());
            startActivityForResult(newNoticeIntent, 100);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        myLocation = this.getLocation();
        mMap.setOnMarkerClickListener(this);
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker marker) {
                int id = mHashMap.get(marker);
                View view = getLayoutInflater().inflate(R.layout.info_window_layout, null);

                ImageView image = (ImageView) view.findViewById(R.id.notice_image);
                TextView username = (TextView) view.findViewById(R.id.username);
                TextView date = (TextView) view.findViewById(R.id.date);
                TextView description = (TextView) view.findViewById(R.id.description);
//                TextView voteYes = (TextView) view.findViewById(R.id.votes_yes);
//                TextView voteNo = (TextView) view.findViewById(R.id.votes_no);
                try {
                    Bitmap bitmap = getImageFromString(markers.get(id).getString("image"));
                    if (bitmap == null) {
                        image.setVisibility(View.GONE);
                    } else {
                        image.setImageBitmap(bitmap);
                    }
                    username.setText(markers.get(id).getString("username"));
                    date.setText(markers.get(id).getString("date"));
                    description.setText(markers.get(id).getString("description"));
//                    voteYes.setText(markers.get(id).getString("votes_yes"));
//                    voteNo.setText(markers.get(id).getString("votes_no"));
                    return view;
                } catch (JSONException e) {
                    // Toast.makeText(this, R.string.info_error, Toast.LENGTH_SHORT).show();
                }
                return null;
            }
        });

        if (!this.canGetLocation) {
            Toast.makeText(this, R.string.gps_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (myLocation != null) {
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
        if (markers.size() > 0) {
            for (int i : markers.keySet()){
                try {
                    addUserMarker(markers.get(i).getDouble("lat"), markers.get(i).getDouble("lng"), markers.get(i).getInt("id"));
                } catch (JSONException e) {
                    showError(e);
                }
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(this, R.string.gps_error, Toast.LENGTH_SHORT).show();
    }

    private void addUserMarker(double latitude, double longitude, int id) {
        LatLng position = new LatLng(latitude, longitude);
        Marker marker = mMap.addMarker(new MarkerOptions().position(position));
        mHashMap.put(marker, id);
    }

    private Location getLocation() {
        Location location = null;
        try {
            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                Toast.makeText(this, R.string.need_gps, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
//                    locationProvider = LocationManager.NETWORK_PROVIDER;
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled && location == null) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
//                    locationProvider = LocationManager.GPS_PROVIDER;
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }
            }

        } catch (SecurityException e) {
            canGetLocation = false;
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == BaseActivity.NEW_NOTICE_RESULT_CODE) {
            addUserMarker(data.getDoubleExtra("lat", 0), data.getDoubleExtra("lng", 0), data.getIntExtra("id", 1));
            try {
                markers.put(data.getIntExtra("id", 1), new JSONObject(data.getStringExtra("marker")));
            } catch (Exception e) {
                showError(e);
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        return true;
    }
}
