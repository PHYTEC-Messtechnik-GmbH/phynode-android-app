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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class AboutActivity extends AppCompatActivity {

    private AboutList mList = null;
    private AboutListAdapter mAdapter = null;
    private ListView mListView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle(getString(R.string.about));

        mList = new AboutList();
        mList.add(R.drawable.ic_info_outline,
                getString(R.string.app_version),
                BuildConfig.VERSION_NAME + (BuildConfig.DEBUG ? "-debug" : ""));
        mList.add(R.drawable.ic_gavel,
                getString(R.string.license),
                getString(R.string.license_name));
        mList.add(R.drawable.ic_code,
                getString(R.string.source_code),
                getString(R.string.source_code_github));
        mList.add(R.drawable.ic_bug_report,
                getString(R.string.issue_report),
                getString(R.string.issue_report_github));
        mList.add(R.drawable.ic_shopping_cart,
                getString(R.string.buy_devices),
                getString(R.string.phynode_epaper_1).concat(", ").concat(getString(R.string.phynode_athena)));

        mAdapter = new AboutListAdapter(this, R.layout.list_item_about, mList);

        mListView = (ListView) findViewById(R.id.list_view_about);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListViewItemClicked(position);
            }
        });
    }

    private void onListViewItemClicked(int position) {
        Integer itemIcon = mList.getIconList().get(position);
        switch (itemIcon) {
            case R.drawable.ic_info_outline:
                break;
            case R.drawable.ic_gavel:
                LicenseDialog licenseDialog = new LicenseDialog();
                licenseDialog.show(getFragmentManager(), "dialog_license");
                break;
            case R.drawable.ic_shopping_cart:
                ShoppingDialog shoppingDialog = new ShoppingDialog();
                shoppingDialog.show(getFragmentManager(), "shopping_dialog");
                break;
            case R.drawable.ic_code:
                Intent intentCode = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.source_code_link)));
                startActivity(intentCode);
                break;
            case R.drawable.ic_bug_report:
                Intent intentIssue = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.issue_report_link)));
                startActivity(intentIssue);
            default:
                break;
        }
    }
}
