package com.example.sunshinewatchapp;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by Radha_acharya on 12/31/2016.
 */

public class WearableWeatherFragment extends Fragment {
    private TextView mDesc, mTempMax, mTempMin, mDate;
    private ImageButton mWeatherPic;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_activity_weather, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDesc = (TextView) view.findViewById(R.id.desc);
        mTempMax = (TextView) view.findViewById(R.id.high_temperature);
        mTempMin = (TextView) view.findViewById(R.id.low_temperature);
        mDate = (TextView) view.findViewById(R.id.date);
        mWeatherPic = (ImageButton) view.findViewById(R.id.weatherpic);
    }

    void rePopulate(String Desc, String TempMax, String TempMin, String Date) {
        mDesc.setText(Desc);
        mTempMax.setText(TempMax);
        mTempMin.setText(TempMin);
        mDate.setText(Date);

    }

    void setBackgroundImage(Bitmap bitmap) {
        mWeatherPic.setImageBitmap(bitmap);
    }
}
