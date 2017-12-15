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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutListAdapter extends ArrayAdapter<Integer> {

    private Context mContext;
    private int mLayoutId;
    private AboutList mList;

    public AboutListAdapter(Context context, int layoutId, AboutList list) {
        super(context, layoutId);
        mContext = context;
        mLayoutId = layoutId;
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Integer getItem(int position) {
        return mList.getIconList().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(mLayoutId, parent, false);
        }

        ImageView icon = (ImageView) convertView.findViewById(R.id.item_icon);
        icon.setImageResource(mList.getIconList().get(position));

        TextView title = (TextView) convertView.findViewById(R.id.item_title);
        title.setText(mList.getTitleList().get(position));

        TextView description = (TextView) convertView.findViewById(R.id.item_description);
        description.setText(mList.getDescriptionList().get(position));

        return convertView;
    }
}
