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

import java.util.ArrayList;

public class DeviceList {

    private static final String TAG = "DeviceList";

    private ArrayList<String> mListAddress;
    private ArrayList<String> mListName;
    private ArrayList<Integer> mListRssi;
    private ArrayList<Boolean> mListFavourite;

    DeviceList() {
        mListAddress = new ArrayList<>();
        mListName = new ArrayList<>();
        mListRssi = new ArrayList<>();
        mListFavourite = new ArrayList<>();
    }

    DeviceList(DeviceList list) {
        mListAddress = new ArrayList<>(list.getAddressList());
        mListName = new ArrayList<>(list.getNameList());
        mListRssi = new ArrayList<>(list.getRssiList());
        mListFavourite = new ArrayList<>(list.getFavouriteList());
    }

    public void clear() {
        if (mListAddress != null)
            mListAddress.clear();

        if (mListName != null)
            mListName.clear();

        if (mListRssi != null)
            mListRssi.clear();

        if (mListFavourite != null)
            mListFavourite.clear();
    }

    public void removeAllNonFavourites() {
        if (isNull()) {
            PhytecLog.e(TAG, "removeAllNonFavourites(): one or more lists are null");
            return;
        }

        if (!isValid()) {
            PhytecLog.e(TAG, "removeAllNonFavourites(): invalid lists");
            return;
        }

        int i = 0;
        while (mListFavourite.contains(false)) {
            if (mListFavourite.get(i) == false) {
                PhytecLog.i(TAG, "removeAllNonFavourites(): removing " + mListAddress.get(i));
                remove(mListAddress.get(i));
            } else {
                i++;
            }
        }
    }

    public int size() {
        if (isNull()) {
            PhytecLog.i(TAG, "size(): one or more lists are null");
            return -2;
        }

        if (!isValid()) {
            PhytecLog.e(TAG, "size(): invalid lists");
            return -1;
        }

        return mListAddress.size();
    }

    public boolean add(String address, String name, int rssi) {
        if (!isValid()) {
            PhytecLog.e(TAG, "add(): invalid lists");
            return false;
        }

        if (mListAddress.contains(address)) {
            // update device info
            this.set(address, name, rssi);
        } else {
            // add new device
            mListAddress.add(address);
            mListName.add(name);
            mListRssi.add(rssi);
            mListFavourite.add(false);
        }

        return true;
    }

    public boolean remove(String address) {
        if (isNull()) {
            PhytecLog.e(TAG, "remove(): one or more lists are null");
        }

        final int i = mListAddress.indexOf(address);

        if (i == -1 || !isValid()) {
            PhytecLog.e(TAG, "remove(): invalid lists or list item");
            return false;
        }

        PhytecLog.i(TAG, "remove(): " + Integer.toString(i));
        mListAddress.remove(i);
        mListName.remove(i);
        mListRssi.remove(i);
        mListFavourite.remove(i);

        return true;
    }

    public boolean set(String address, String name, int rssi) {
        final int i = mListAddress.indexOf(address);

        if (i == -1 || !isValid()) {
            PhytecLog.e(TAG, "set(): invalid lists");
            return false;
        }

        mListName.set(i, name);
        mListRssi.set(i, rssi);

        return true;
    }

    public ArrayList<String> getAddressList() {
        return mListAddress;
    }

    public ArrayList<String> getNameList() {
        return mListName;
    }

    public ArrayList<Integer> getRssiList() {
        return mListRssi;
    }

    public ArrayList<String> getRssiListAsStrings() {
        if (mListRssi == null)
            return null;

        ArrayList<String> rssiListAsStrings = new ArrayList<>();

        for (Integer i : mListRssi)
            rssiListAsStrings.add(Integer.toString(i));

        return rssiListAsStrings;
    }

    public ArrayList<Boolean> getFavouriteList() {
        return mListFavourite;
    }

    public ArrayList<String> getFavouriteListAsStrings() {
        if (mListFavourite == null)
            return null;

        ArrayList<String> favouriteListAsStrings = new ArrayList<>();

        for (Boolean b : mListFavourite)
            favouriteListAsStrings.add(Boolean.toString(b));

        return favouriteListAsStrings;
    }

    public void setLists(ArrayList<String> addressList, ArrayList<String> nameList,
                         ArrayList<Integer> rssiList, ArrayList<Boolean> favouriteList) {
        mListAddress = addressList;
        mListName = nameList;
        mListRssi = rssiList;
        mListFavourite = favouriteList;
    }

    public String toString() {
        return mListAddress.toString() + " " +
                mListName.toString() + " " +
                mListRssi.toString() + " " +
                mListFavourite.toString();
    }

    public boolean isNull() {
        return (mListAddress == null ||
                mListName == null ||
                mListRssi == null ||
                mListFavourite == null);
    }

    // private

    private boolean isValid() {
        // check if the size of all ArrayLists are the same,
        // they should never be of different sizes
        return (mListAddress.size() == mListName.size() &&
                mListName.size() == mListRssi.size() &&
                mListRssi.size() == mListFavourite.size());
    }
}