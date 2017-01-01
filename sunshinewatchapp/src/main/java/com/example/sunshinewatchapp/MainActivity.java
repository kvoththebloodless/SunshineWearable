package com.example.sunshinewatchapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.wearable.DataMap.TAG;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener,
        MessageApi.MessageListener,
        ResultCallback<NodeApi.GetConnectedNodesResult> {


    WearableWeatherFragment mToday, mTomorrow, mDayAfter;
    private GridViewPager mPager;
    private GoogleApiClient mGoogleApiClient;
    private boolean nodeConnected = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupViews();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)

                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        Log.d("connect", "connnnnnect");
    }

    @Override
    protected void onPause() {
        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
            nodeConnected = false;
        }
        super.onPause();
    }

    private void setupViews() {
        mPager = (GridViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageCount(2);
        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setDotSpacing((int) getResources().getDimension(R.dimen.dots_spacing));
        dotsPageIndicator.setPager(mPager);
        mToday = new WearableWeatherFragment();
        mTomorrow = new WearableWeatherFragment();
        mDayAfter = new WearableWeatherFragment();
        List<Fragment> pages = new ArrayList<>();
        pages.add(mToday);
        pages.add(mTomorrow);
        pages.add(mDayAfter);
        final MyPagerAdapter adapter = new MyPagerAdapter(getFragmentManager(), pages);
        mPager.setAdapter(adapter);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        nodeConnected = true;
        Log.d("onGo", "onConnected(): Successfully connected to Google API client");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        //  Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(this);


    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("connextion suspended", "" + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("connectionfailed", connectionResult + "");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d("onDataChanged()", "yay ");

        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                Asset photoAsset = dataMapItem.getDataMap()
                        .getAsset(SunshineWearableService.ASSET);
                if (SunshineWearableService.WEATHER_TODAY.equals(path)) {
                    new LoadBitmapAsyncTask(0, dataMapItem.getDataMap().getStringArrayList(SunshineWearableService.DATA)).execute(photoAsset);

                } else if (SunshineWearableService.WEATHER_TOMORROW.equals(path)) {
                    new LoadBitmapAsyncTask(1, dataMapItem.getDataMap().getStringArrayList(SunshineWearableService.DATA)).execute(photoAsset);

                } else {
                    new LoadBitmapAsyncTask(2, dataMapItem.getDataMap().getStringArrayList(SunshineWearableService.DATA)).execute(photoAsset);
                }

            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.d("eventtypedeleted", "yessss");
            } else {
                Log.d("unknown", "unknownnn");
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

    }


    @Override
    public void onResult(@NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
        if (getConnectedNodesResult == null || getConnectedNodesResult.getNodes().size() == 0)
            return;


        for (Node node : getConnectedNodesResult.getNodes()) {

            retrieveData(0, node);
            retrieveData(1, node);
            retrieveData(2, node);
        }

    }

    void retrieveData(final int i, Node node) {


        Uri uriToday = new Uri.Builder()
                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                .path(SunshineWearableService.PATH[i])
                .authority(node.getId())
                .build();


        Wearable.DataApi.getDataItem(mGoogleApiClient, uriToday)
                .setResultCallback(
                        new ResultCallback<DataApi.DataItemResult>() {
                            @Override
                            public void onResult(DataApi.DataItemResult dataItemResult) {

                                if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {

                                    DataMapItem d = DataMapItem.fromDataItem(dataItemResult.getDataItem());

                                    Asset photoAsset = d.getDataMap()
                                            .getAsset(SunshineWearableService.ASSET);


                                    ArrayList<String> data = d.getDataMap().getStringArrayList(SunshineWearableService.DATA);
                                    new LoadBitmapAsyncTask(i, data).execute(photoAsset);


                                }
                            }
                        }
                );
    }


    private class MyPagerAdapter extends FragmentGridPagerAdapter {

        private List<Fragment> mFragments;

        public MyPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            mFragments = fragments;
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public int getColumnCount(int row) {
            return mFragments == null ? 0 : mFragments.size();
        }

        @Override
        public Fragment getFragment(int row, int column) {
            return mFragments.get(column);
        }

    }

    private class LoadBitmapAsyncTask extends AsyncTask<Asset, Void, Bitmap> {
        private int i;
        private ArrayList<String> data;
        private WearableWeatherFragment frag;

        LoadBitmapAsyncTask(int i, ArrayList<String> data) {
            this.i = i;
            this.data = data;

        }

        @Override
        protected void onPreExecute() {

            switch (i) {
                case 0:
                    frag = mToday;
                    break;
                case 1:
                    frag = mTomorrow;
                    break;
                case 2:
                    frag = mDayAfter;
                    break;
            }

            frag.rePopulate(data.get(0), data.get(1), data.get(2), data.get(3));
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Asset... params) {

            if (params.length > 0) {

                Asset asset = params[0];

                InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                        mGoogleApiClient, asset).await().getInputStream();

                if (assetInputStream == null) {
                    Log.w("unknown asset", "Requested an unknown Asset.");
                    return null;
                }
                return BitmapFactory.decodeStream(assetInputStream);

            } else {
                Log.e("asset null", "Asset must be non-null");
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (bitmap != null) {
                Log.d("settingbackground", "Setting background image on second page..");

                frag.setBackgroundImage(bitmap);
            } else
                Log.d("settingbackground", "null");
        }
    }
}
