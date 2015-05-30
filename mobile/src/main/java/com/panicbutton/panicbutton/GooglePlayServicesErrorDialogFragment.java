package com.panicbutton.panicbutton;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.google.android.gms.common.GooglePlayServicesUtil;

public class GooglePlayServicesErrorDialogFragment extends DialogFragment {

    private static final String EXTRA_ERROR_CODE = "com.useaurea.fleet.businesslocation.nearby.extra.ERROR_CODE";
    private static final String EXTRA_REQUEST_CODE = "com.useaurea.fleet.businesslocation.nearby.extra.REQUEST_CODE";
    // Bool to track whether the app is already resolving an error

    public static GooglePlayServicesErrorDialogFragment newInstance(int errorCode, int requestCode) {

        GooglePlayServicesErrorDialogFragment fragment = new GooglePlayServicesErrorDialogFragment();

        Bundle bundle = fragment.getArguments();
        if (bundle == null) {
            bundle = new Bundle();
        }

        // Add parameters to the argument bundle
        bundle.putInt(EXTRA_ERROR_CODE, errorCode);
        bundle.putInt(EXTRA_REQUEST_CODE, requestCode);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override @NonNull public Dialog onCreateDialog(Bundle savedInstanceState) {
        return GooglePlayServicesUtil.getErrorDialog(getArguments().getInt(EXTRA_ERROR_CODE),
                getActivity(), getArguments().getInt(EXTRA_REQUEST_CODE));
    }
}
