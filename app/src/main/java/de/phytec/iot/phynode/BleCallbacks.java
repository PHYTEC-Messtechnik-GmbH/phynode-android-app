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

import android.bluetooth.BluetoothGattService;
import java.util.List;

interface BleCallbacks {

    public void deviceFound(String address, String name, int rssi);
    public void scanStarted();
    public void scanStopped();
    public void servicesFound(final List<BluetoothGattService> services);
    public void sendingStarted();
    public void sendingStatusChanged();
    public void sendingStopped();
    public void connected();
    public void disconnected();
}
