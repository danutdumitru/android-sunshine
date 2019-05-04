package eu.bidtrans.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

public class DetailsFragment extends Fragment {
    private static final String TAG = DetailsFragment.class.getSimpleName();

    private TextView txtData;
    private ShareActionProvider shareActionProvider;

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
        MenuItem shareItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        String data = txtData.getText().toString() + "#SunshineApp";
        shareActionProvider.setShareIntent(Intent.createChooser( getShareIntent(data), "Send weather to"));
    }

    private Intent getShareIntent(String data) {
        Intent myShareIntent = new Intent(Intent.ACTION_SEND);
        myShareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        myShareIntent.setType("text/plain");
        myShareIntent.putExtra(Intent.EXTRA_TEXT, data );
        return myShareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent settingsIntent = new Intent(getContext(), SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
