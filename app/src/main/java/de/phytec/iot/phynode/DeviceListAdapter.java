package de.phytec.iot.phynode;

/*
    Copyright 2017  PHYTEC Messtechnik GmbH

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class DeviceListAdapter extends ArrayAdapter<String> {

    private MainActivity mActivity;
    private DeviceList mData;
    private int mLayoutId;

    public DeviceListAdapter(MainActivity context, int layoutId, DeviceList data) {
        super(context, layoutId);
        mActivity = context;
        mLayoutId = layoutId;
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public String getItem(int position) {
        return mData.getAddressList().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = mActivity.getLayoutInflater();
            convertView = inflater.inflate(mLayoutId, parent, false);
        }

        convertView.findViewById(R.id.item_device).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.onDeviceSelected(position);
            }
        });

        final ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.item_device_favourite);

        if (mData.getFavouriteList().get(position))
            imageButton.setImageResource(R.drawable.ic_star);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean wasFavourite = mData.getFavouriteList().get(position);
                mData.getFavouriteList().set(position, !wasFavourite);
                imageButton.setImageResource(wasFavourite ? R.drawable.ic_star_border : R.drawable.ic_star);
            }
        });

        TextView textView;
        String string;

        textView = (TextView) convertView.findViewById(R.id.item_device_address);
        string = mData.getAddressList().get(position);
        textView.setText(string);

        textView = (TextView) convertView.findViewById(R.id.item_device_name);
        string = mData.getNameList().get(position);
        textView.setText(string);

        textView = (TextView) convertView.findViewById(R.id.item_device_rssi);
        string = mData.getRssiList().get(position).toString();
        textView.setText(string);

        return convertView;
    }
}
