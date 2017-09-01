# phy<b>NODE</b> Android App
This is an Android app demonstrating the implementation of a Bluetooth Low
Energy communication with phyNODE devices.

## Features
-   Writing the ePaper display of phyNODE devices and using it as a nametag
    with:
    -   Title
    -   Name
    -   Department
    -   Company Logo (simply place a bitmap containing your logo in directory
        on your smartphone, and read _Creating a Custom Logo_ for the image
        specifications)
-   Star your favourite devices so you can access them later on without a scan.

## Requirements
-   Android 5 "Lollipop" or newer
-   Bluetooth Low Energy compatible smartphone or tablet
-   compatible phyNODE device
    -   phyNODE-ePaper-1
    -   phyNODE-KW41Z (coming soon)

## Playstore
If you are not interested in building the phyNODE app yourself it is
recommended to
[download the latest version from the Google Playstore](https://play.google.com/store/apps/details?id=de.phytec.iot.phynode).

## Build
The Gradle build system is used to build this app. Import the project into
[Android Studio](https://developer.android.com/studio/index.html) as usual and
you should be able to `Build->Make Project` and `Run` it.

### Creating a Custom Logo
The phyNODE app allows you to use any logo to be written to the ePaper display.
The image file must have an alpha channel with full transparency. Any pixel
that is not fully transparent will be black on ePaper and any fully transparent
pixel will be white. Note that the logo will not be scaled meaning one pixel in
your image file translates to one pixel on the ePaper. To get the best results
your image should have a size of about 125x25 pixels.

## License
```
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
```
