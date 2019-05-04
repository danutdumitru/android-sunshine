package eu.bidtrans.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailsFragment extends Fragment {
    private static final String TAG = DetailsFragment.class.getSimpleName();

    private TextView txtData;

    public DetailsFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.details_fragment, container, false);
        txtData = (TextView) fragment.findViewById(R.id.txtData);

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            Log.d(TAG, "Data received " + intent.getExtras().toString());
            String weather = intent.getStringExtra(ForecastFragment.KEY_WEATHER);
            Log.d(TAG, "Weather is " + weather);
            txtData.setText(weather);
        }
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.details, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Log.d(TAG, "Settings pressed");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
