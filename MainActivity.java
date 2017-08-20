/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.database;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.quickstart.database.adapter.PostRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;

import android.location.LocationManager;
import android.location.Location;
import android.location.LocationListener;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.facebook.AccessToken;

import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

import com.google.firebase.quickstart.database.fragment.PostListFragment;


public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    private FragmentPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location prevLocation;

    private long MIN_DISTANCE_CHANGE_FOR_UPDATES;
    private long MIN_TIME_BW_UPDATES;
    private short PRECISION;
    private short TAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PRECISION = (short)getResources().getInteger(R.integer.location_precision);
        TAB = (short)getResources().getInteger(R.integer.tab_size);
        MIN_DISTANCE_CHANGE_FOR_UPDATES = (long) getResources().getInteger(R.integer.min_dist_updt);
        MIN_TIME_BW_UPDATES = (long) getResources().getInteger(R.integer.min_inteval_updt);

        loadGPS();

        // Button launches NewPostActivity
        findViewById(R.id.fab_new_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MainActivity.this, NewPostActivity.class);
                //intent.putExtra(NewPostActivity.mPostPath, mPostPath);
                //startActivity(intent);
                startActivity(new Intent(MainActivity.this, NewPostActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_logout) {
            //IMP: clean up RecyclerView FirebaseRecyclerAdapter of each Fragments to avoid permission err when reconnect after logout
            for (int j = 0; j < TAB; j++) {
                PostListFragment pf = (PostListFragment) mPagerAdapter.getItem(j);
                RecyclerView rv = pf.getRecyclerView();
                //MyTopPostsFragment.getRecyclerView[2] returns null if only visit tab 0
                if(rv != null) {
                    PostRecyclerAdapter fa = (PostRecyclerAdapter) rv.getAdapter();
                    fa.cleanup();
                }
            }
            FirebaseAuth.getInstance().signOut();
            if (AccessToken.getCurrentAccessToken() != null) {
                LoginManager.getInstance().logOut();//facebook: static can be refered out of signin class
                Log.d(TAG, "sign out from facebook");
            }
            TwitterSession twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
            if (twitterSession != null) {
                TwitterCore.getInstance().getSessionManager().clearActiveSession();
                Log.d(TAG, "sign out from twitter");
            }
            //clean up location listener
            locationManager.removeUpdates(locationListener);
            locationListener=null;
            //IMP: avoid leaked window android.widget.PopupWindow$PopupDecorView
            closeOptionsMenu();

            startActivity(new Intent(this, SignInActivity.class));
            //finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    //IMP: avoid race condition btw closeOptionsMenu() before calling finish():
    //https://stackoverflow.com/questions/36676412/leaked-window-when-exit-app-through-popup-menu
    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        finish();
    }

    private void loadGPS() {
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if (prevLocation == null){
                    updatePosition(location);
                    return;
                }
                if (location.distanceTo(prevLocation)>MIN_DISTANCE_CHANGE_FOR_UPDATES) {
                    updatePosition(location);
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        //android.Manifest.permission.ACCESS_FINE_LOCATION is overkill
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (prevLocation == null){
                updatePosition(lastKnownLocation);
                return;
            }
            if (lastKnownLocation.distanceTo(prevLocation)>MIN_DISTANCE_CHANGE_FOR_UPDATES) {
                updatePosition(lastKnownLocation);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission Granted
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (prevLocation == null){
                    updatePosition(lastKnownLocation);
                    return;
                }
                if (lastKnownLocation.distanceTo(prevLocation)>MIN_DISTANCE_CHANGE_FOR_UPDATES) {
                    updatePosition(lastKnownLocation);
                }
            }
        }
    }
    //40.852,-74.170 = 1300106/81/57/20/
    int[] convertPosition(double latitude, double longitude){
        int[] positions= new int[SIZE];
        if(latitude > 90 || latitude < -90 || longitude > 180 || longitude < -180){
            return positions; //0s
        }
        double d_lat = latitude*PRECISION;
        double d_lgt = longitude*PRECISION;
        int lat = (int)d_lat;
        int lgt = (int)d_lgt;
        int i = SIZE-1;
        while(i > 0){
            positions[i] = Math.abs(lat%10*10)+Math.abs(lgt%10);
            lat = lat/10;
            lgt = lgt/10;
            i -=1;
        }
        positions[i] = (lat+90)*10000+(lgt+180);
        return  positions;
    }

    private void updatePosition(Location curLocation){
        prevLocation = curLocation;
        Toast.makeText(MainActivity.this, curLocation.getLatitude() + " : " + curLocation.getLongitude(),
                Toast.LENGTH_SHORT).show();
        int[] positions = convertPosition(curLocation.getLatitude(), curLocation.getLongitude());
        mPath = new String[SIZE];

        StringBuilder pathBuilder = new StringBuilder();
        for(int i=0; i<SIZE; i++){
            pathBuilder.append('/').append(positions[i]);
            mPath[i] = pathBuilder.toString();
            Log.d(TAG, mPath[i] + "|");
        }
        createView();
    }

    private void createView(){
        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final Fragment[] mFragments = new Fragment[]{
                    new PostListFragment(0,mPath),
                    new PostListFragment(1,mPath),
                    new PostListFragment(2,mPath),
                    new PostListFragment(3,mPath)
            };
            private final String[] mFragmentNames = getResources().getStringArray(R.array.post_types);

            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }

            @Override
            public int getCount() {
                return mFragments.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mFragmentNames[position];
            }
        };
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }
}
