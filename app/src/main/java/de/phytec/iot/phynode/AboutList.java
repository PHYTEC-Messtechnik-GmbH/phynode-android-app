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

public class AboutList {

    private ArrayList<Integer> mListIcon;
    private ArrayList<String> mListTitle;
    private ArrayList<String> mListDescription;

    public AboutList() {
        mListIcon = new ArrayList<>();
        mListTitle = new ArrayList<>();
        mListDescription = new ArrayList<>();
    }

    // public

    public void clear() {
        mListIcon.clear();
        mListTitle.clear();
        mListDescription.clear();
    }

    public int size() {
        if (!isValid())
            return -1;

        return mListTitle.size();
    }

    public boolean add(Integer icon, String title, String description) {
        if (!isValid())
            return false;

        mListIcon.add(icon);
        mListTitle.add(title);
        mListDescription.add(description);

        return true;
    }

    public boolean remove(Integer icon) {
        int i = mListTitle.indexOf(icon);

        if (i == -1 || !isValid())
            return false;

        mListIcon.remove(i);
        mListTitle.remove(i);
        mListDescription.remove(i);

        return true;
    }

    public ArrayList<Integer> getIconList() {
        return mListIcon;
    }

    public ArrayList<String> getTitleList() {
        return mListTitle;
    }

    public ArrayList<String> getDescriptionList() {
        return mListDescription;
    }

    // private

    private boolean isValid() {
        return (mListIcon.size() == mListTitle.size() &&
                mListTitle.size() == mListDescription.size());
    }
}
