package com.panicbutton.panicbutton;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.wearable.view.DismissOverlayView;

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
                setContentView(R.layout.activity_map);

                mDismissOverlay =
                        (DismissOverlayView) findViewById(R.id.dismiss_overlay);
                mDismissOverlay.setIntroText(R.string.message_intro_map);
                mDismissOverlay.showIntroIfNecessary();

                SupportMapFragment mapFragment =
                        (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);
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
                mMap = map;
                mMap.addMarker(new MarkerOptions().position(SYDNEY)
                        .title("Sydney Opera House"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SYDNEY, 10));
                mMap.setOnMapLongClickListener(this);
        }
}
