package com.panicbutton.panicbutton;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.wearable.view.DismissOverlayView;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by douglasritter on 5/30/15.
 */
public class MapActivity extends FragmentActivity
        implements OnMapReadyCallback, GoogleMap.OnMapClickListener,
        GoogleMap.OnMapLongClickListener {

        private DismissOverlayView mDismissOverlay;
        private static final LatLng SYDNEY = new LatLng(-33.85704, 151.21522);
        private GoogleMap mMap;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
//                setContentView(R.layout.activity_map);
                setContentView(R.layout.activity_activate);

                /*final FrameLayout topFrameLayout = (FrameLayout) findViewById(R.id.root_container);
                final FrameLayout mapFrameLayout = (FrameLayout) findViewById(R.id.map_container);

                // Set the system view insets on the containers when they become available.
                topFrameLayout.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                        @Override
                        public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                                // Call through to super implementation and apply insets
                                insets = topFrameLayout.onApplyWindowInsets(insets);

                                FrameLayout.LayoutParams params =
                                        (FrameLayout.LayoutParams) mapFrameLayout.getLayoutParams();

                                // Add Wearable insets to FrameLayout container holding map as margins
                                params.setMargins(
                                        insets.getSystemWindowInsetLeft(),
                                        insets.getSystemWindowInsetTop(),
                                        insets.getSystemWindowInsetRight(),
                                        insets.getSystemWindowInsetBottom());
                                mapFrameLayout.setLayoutParams(params);

                                return insets;
                        }
                });*/

                // Obtain the DismissOverlayView and display the intro help text.
                /*mDismissOverlay = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
                mDismissOverlay.setIntroText(R.string.message_intro_map);
                mDismissOverlay.showIntroIfNecessary();

                // Obtain the MapFragment and set the async listener to be notified when the map is ready.
                SupportMapFragment mapFragment =
                        (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);*/

        }


        @Override
        public void onMapClick(LatLng latLng) {

        }

        @Override
        public void onMapLongClick(LatLng latLng) {
                mDismissOverlay.show();
        }

        @Override
        public void onMapReady(GoogleMap map) {

                Log.e("MAP", "ON MAP READY");
                mMap = map;

                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

                /*mMap.addMarker(new MarkerOptions().position(SYDNEY)
                        .title("Sydney Opera House"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SYDNEY, 16));*/
                mMap.setOnMapLongClickListener(this);
        }

}
