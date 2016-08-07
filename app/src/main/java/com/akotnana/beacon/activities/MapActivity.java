package com.akotnana.beacon.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akotnana.beacon.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.SphericalUtil;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final int MESSAGE_ID_SAVE_CAMERA_POSITION = 1;
    private static final int MESSAGE_ID_READ_CAMERA_POSITION = 2;
    private static final int PERMISSIONS_MAP = 1337;
    private static int reloadedTimes = 0;
    private static CameraPosition lastCameraPosition;
    private static Location userLoc;
    private Button current;
    private Handler handler;
    private TextInputEditText et;
    private MapView mMapView;
    private static GoogleMap mMap;
    private Bundle mBundle;
    private Toast mToast;

    private HashMap<String, String> ids = new HashMap<String, String>();

    public static String dataString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setTitle("Map");

        if (!selfPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) || !selfPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_MAP);
        }

        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            Log.e("mapview", "", e);
        }

        current = (Button) findViewById(R.id.currentLoc);
        current.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToCurrent();
            }
        });

        Button toggle = (Button) findViewById(R.id.toggle);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMode(v);
            }
        });

        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(mBundle);
        mMapView.getMapAsync(this);
        String intermediate = (getData("callsMade"));
        if (intermediate.equals("")) {
            reloadedTimes = 0;
            storeData("callsMade", Integer.toString(reloadedTimes));
            storeData("lastRetrieved", Long.toString(System.currentTimeMillis()));
            Log.d("DashboardFragment", "create callsMade");
            Log.d("DashboardFragment", getData("lastRetrieved"));
        } else {
            reloadedTimes = Integer.parseInt(intermediate);
            Log.d("DashboardFragment", "stored reloaded times");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // this takes the user 'back', as if they pressed the left-facing triangle icon on the main android toolbar.
                // if this doesn't work as desired, another possibility is to call `finish()` here.
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleMode(View v) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (mMap.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else {
        }
    }

    private void returnToCurrent() {
        SmartLocation.with(this).location()
                .oneFix()
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        userLoc = location;
                        Log.d("Got location", location.toString());
                    }
                });

        new Thread(new Runnable() {
            public void run() {
                while (userLoc == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("Dash", "got location");
                LatLng newLatLng = new LatLng(userLoc.getLatitude(), userLoc.getLongitude());
                LatLngBounds bounds = new LatLngBounds.Builder().
                        include(SphericalUtil.computeOffset(newLatLng, 1.5 * 1609.344d, 0)).
                        include(SphericalUtil.computeOffset(newLatLng, 1.5 * 1609.344d, 90)).
                        include(SphericalUtil.computeOffset(newLatLng, 1.5 * 1609.344d, 180)).
                        include(SphericalUtil.computeOffset(newLatLng, 1.5 * 1609.344d, 270)).build();
                final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0);
                runOnUiThread(new Runnable() {
                    public void run() {
                        mMap.animateCamera(cameraUpdate);
                    }
                });
            }
        }).start();
    }

    private void showToast(String message) {
        if (mToast != null) {
            mToast.cancel();
            mToast = null;
        }
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        } else {
            mMap.setMyLocationEnabled(true);
        }
        */

        Log.d("DashboardFragment", "map ready");

        if(selfPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) || selfPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            Log.d("Dash", "good");
            SmartLocation.with(this).location()
                    .oneFix()
                    .start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {
                            userLoc = location;
                            //Log.d("Got location", location.toString());
                        }
                    });

            new Thread(new Runnable() {
                public void run() {
                    while (userLoc == null) {
                        try {
                            Thread.sleep(100);
                            //Log.d("Got location", "searching");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    LatLng newLatLng = new LatLng(userLoc.getLatitude(), userLoc.getLongitude());
                    LatLngBounds bounds = new LatLngBounds.Builder().
                            include(SphericalUtil.computeOffset(newLatLng, 1.5 * 1609.344d, 0)).
                            include(SphericalUtil.computeOffset(newLatLng, 1.5 * 1609.344d, 90)).
                            include(SphericalUtil.computeOffset(newLatLng, 1.5 * 1609.344d, 180)).
                            include(SphericalUtil.computeOffset(newLatLng, 1.5 * 1609.344d, 270)).build();
                    final CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 0);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mMap.moveCamera(cameraUpdate);
                            Marker home = mMap.addMarker(new MarkerOptions().position(new LatLng(userLoc.getLatitude(), userLoc.getLongitude())).title("Your location!").icon(BitmapDescriptorFactory.fromBitmap(bitmapSizeByScale(BitmapFactory.decodeResource(getResources(), R.drawable.current_location), 0.4f))));
                            loadAllMarkers();
                        }
                    });
                }
            }).start();
        } else {
            Log.d("Dash", "bad");
            current.setEnabled(false);
        }

        handler = new MapStateHandler();

        GoogleMap.OnInfoWindowClickListener InfoWindowListener = new GoogleMap.OnInfoWindowClickListener(){
            @Override
            public void onInfoWindowClick(Marker marker) {
                Log.d("GU", "" + marker.getTitle().equals("Your location!"));
                if(marker.getTitle().equals("Your location!")) {
                    marker.hideInfoWindow();
                }
            }};

        mMap.setOnInfoWindowClickListener(InfoWindowListener);

        mMap.setOnMarkerClickListener(this);

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                handler.removeMessages(MESSAGE_ID_SAVE_CAMERA_POSITION);
                handler.removeMessages(MESSAGE_ID_READ_CAMERA_POSITION);
                handler.sendEmptyMessageDelayed(MESSAGE_ID_SAVE_CAMERA_POSITION, 500);
                handler.sendEmptyMessageDelayed(MESSAGE_ID_READ_CAMERA_POSITION, 1000);
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent i = null;
                if(ids.get(marker.getTitle()).contains("b")) {
                    i = new Intent(MapActivity.this, BeaconDisplaySponsorActivity.class);
                } else {
                    i = new Intent(MapActivity.this, BeaconDisplayActivity.class);
                }
                i.putExtra("key", ids.get(marker.getTitle()));
                startActivity(i);
                overridePendingTransition(R.anim.pull_in_down, R.anim.push_out_up);
                Log.d("Map", marker.getTitle());

            }
        });

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker arg0) {

                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater().inflate(R.layout.window_layout, null);

                // Getting reference to the TextView to set latitude
                TextView tvLat = (TextView) v.findViewById(R.id.beacon_title1);

                // Getting reference to the TextView to set longitude
                ImageView img = (ImageView) v.findViewById(R.id.goat);

                // Setting the latitude
                tvLat.setText(arg0.getTitle());
                // Returning the view containing InfoWindow contents
                return v;

            }
        });

        //mMap.getUiSettings().setAllGesturesEnabled(false);
    }

    private void loadAllMarkers() {
        Marker mc = mMap.addMarker(new MarkerOptions().position(new LatLng(38.863940, -77.060118)).title("McDonald's").icon(BitmapDescriptorFactory.fromBitmap(bitmapSizeByScale(BitmapFactory.decodeResource(getResources(), R.drawable.red_pin), 0.6f))));
        ids.put(mc.getTitle(), "0b");
        Marker hack = mMap.addMarker(new MarkerOptions().position(new LatLng(38.853940, -77.080118)).title("IncubateDC").icon(BitmapDescriptorFactory.fromBitmap(bitmapSizeByScale(BitmapFactory.decodeResource(getResources(), R.drawable.red_pin), 0.4f))));
        ids.put(hack.getTitle(), "1");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_up, R.anim.push_out_down);
    }

    public Bitmap bitmapSizeByScale( Bitmap bitmapIn, float scall_zero_to_one_f) {

        Bitmap bitmapOut = Bitmap.createScaledBitmap(bitmapIn,
                Math.round(bitmapIn.getWidth() * scall_zero_to_one_f),
                Math.round(bitmapIn.getHeight() * scall_zero_to_one_f), false);

        return bitmapOut;
    }

    public boolean selfPermissionGranted(String permission) {
        // For Android < Android M, self permissions are always granted.
        boolean result = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // targetSdkVersion >= Android M, we can
                // use Context#checkSelfPermission
                result = checkSelfPermission(permission)
                        == PackageManager.PERMISSION_GRANTED;
            } else {
                // targetSdkVersion < Android M, we have to use PermissionChecker
                result = PermissionChecker.checkSelfPermission(this, permission)
                        == PermissionChecker.PERMISSION_GRANTED;
            }
        }

        return result;
    }

    public void storeData(String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences("XPLORE_PREFS", Context.MODE_PRIVATE).edit();
        editor.putString(key,value);
        editor.apply();
    }

    public String getData(String key) {
        return getSharedPreferences("XPLORE_PREFS", Context.MODE_PRIVATE).getString(key, "");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
        storeData("callsMade", Integer.toString(reloadedTimes));
    }

    @Override
    public void onStop() {
        super.onStop();
        storeData("callsMade", Integer.toString(reloadedTimes));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d("GU", "" + marker.getTitle().equals("Your location!"));
        if(marker.getTitle().equals("Your location!")) {
            Log.d("GU", "" + marker.getTitle().equals("Your location!"));
            return true;
        }
        return false;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d("DashboardFragment", "Permission result");
        switch (requestCode) {

            case PERMISSIONS_MAP: {
                Log.d("DashboardFragment", "Permission result");
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    current.setEnabled(true);
                    returnToCurrent();
                } else {
                    showErrorDialog();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void showErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission denied");
        builder.setMessage("Without these permissions, the app is unable to display the map relative to your location. Are you sure you want to deny these permissions?");

        String positiveText = "I'M SURE";
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        current.setEnabled(false);
                    }
                });

        String negativeText = "RE-TRY";
        builder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                PERMISSIONS_MAP);
                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

    private double getMapRadius(GoogleMap googleMap) {
        VisibleRegion vr = mMap.getProjection().getVisibleRegion();
        double left = vr.latLngBounds.southwest.longitude;
        double top = vr.latLngBounds.northeast.latitude;
        double right = vr.latLngBounds.northeast.longitude;
        double bottom = vr.latLngBounds.southwest.latitude;

        Location center=new Location("center");
        center.setLatitude(vr.latLngBounds.getCenter().latitude);
        center.setLongitude(vr.latLngBounds.getCenter().longitude);
        Location middleLeft = new Location("middleLeftCenter");
        middleLeft.setLatitude(center.getLatitude());
        middleLeft.setLongitude(left);

        float dis = (center.distanceTo(middleLeft));//calculate distane between middleLeftcorner and center

        dis = getMiles(dis);

        return roundToHalf(dis);
    }

    public float roundToHalf(float d) {
        return Math.round(d * 2f) / 2.0f;
    }

    public float getMiles(float i) {
        return i*0.000621371192f;
    }

    public float getMeters(float i) {
        return i*1609.344f;
    }

    class MapStateHandler extends Handler {
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_ID_SAVE_CAMERA_POSITION) {
                lastCameraPosition = mMap.getCameraPosition();
            } else if (msg.what == MESSAGE_ID_READ_CAMERA_POSITION) {
                if (lastCameraPosition.equals(mMap.getCameraPosition())) {
                    if(reloadedTimes % 6 == 0 && reloadedTimes > 0) {
                        long current = System.currentTimeMillis();
                        long prev = Long.parseLong(getData("lastRetrieved"));
                        Log.d("DashboardFragment", "measured: " + Long.toString(prev));
                        Log.d("DashboardFragment", "diff: " + Long.toString(current - prev));
                        if(current - prev <= 60.0 * 1000.0) {
                            Log.d("DashboardFragment", "too many requests");
                            storeData("isPaused", "true");
                            storeData("lastRetrieved", Long.toString(current + 60 * 1000));
                            reloadedTimes += 1;
                        } else {
                            storeData("lastRetrieved", Long.toString(current));
                            Log.d("DashboardFragment", "new last retrieved: " + Long.toString(current));
                        }
                    }
                    if(getData("isPaused").equals("true")) {
                        long current = System.currentTimeMillis();
                        long prev = Long.parseLong(getData("lastRetrieved"));
                        if(current >= prev) {
                            storeData("isPaused", "false");
                        } else {
                            showToast("Slow down! Please wait " + (((int) ((prev - current) / 1000))) + " more seconds.");
                        }
                    }
                    if(!getData("isPaused").equals("true")) {
                        reloadedTimes += 1;
                        Log.d("DashboardFragment", "Camera position stable, retrieved " + Integer.toString(reloadedTimes) + ", with radius of " + Double.toString(getMapRadius(mMap)));
                        //get data here
                    }
                }
            }
        }
    }


    class MapRetriever extends AsyncTask< Void, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Void...params) {
            Socket socket = null;
            ObjectOutputStream oos = null;
            ObjectInputStream ois = null;
            String message = "";
            try {
                socket = new Socket("alaskapi4713.ddns.net", 1337);
                oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject("retrieveschedule");
                ois = new ObjectInputStream(socket.getInputStream());
                message = (String) ois.readObject();
                ois.close();
                oos.close();
                socket.close();
            } catch (ClassNotFoundException e) {
                return "errorcla";
            } catch (UnknownHostException e) {
                return "erroruhe";
            } catch (SocketTimeoutException e) {
                return "errorste";
            } catch (Exception e) {
                return "errorio";
            }
            return message;
        }

        protected void onPostExecute(Boolean result) {

        }
    }
}