Launch Navigator Cordova/Phonegap Plugin
=================================

This Cordova/Phonegap plugin can be used to navigate to a destination using the native navigation app on:

- Android: Google Navigator
- iOS: Apple Maps/Google Maps<sup>[[1]](#fn1)</sup>
- Windows Phone: Bing Maps

<sub><a id="fn1">[1]</a>: on iOS, you can choose to [prefer Google Maps](#ios) over Apple Maps if it's installed on the user's device; if not, Apple Maps will be used instead.</sub>

The plugin is registered on [npm](https://www.npmjs.com/package/uk.co.workingedge.phonegap.plugin.launchnavigator) as `uk.co.workingedge.phonegap.plugin.launchnavigator`

## Contents

* [Installing](#installing)
* [Using the plugin](#using-the-plugin)
    * [Example usage](#example-usage)
    * [Example project](#example-project)
* [Caveats](#caveats)
    * [Android](#android)
    * [Windows](#windows)
    * [iOS](#ios)
* [Reporting issues](#reporting-issues)
* [Credits](#credits)
* [License](#license)
 
# Installing

## Using the Cordova/Phonegap [CLI](http://docs.phonegap.com/en/edge/guide_cli_index.md.html)

    $ cordova plugin add uk.co.workingedge.phonegap.plugin.launchnavigator
    $ phonegap plugin add uk.co.workingedge.phonegap.plugin.launchnavigator

**NOTE**: Make sure your Cordova CLI version is 5.0.0+ (check with `cordova -v`). Cordova 4.x and below uses the now deprecated [Cordova Plugin Registry](http://plugins.cordova.io) as its plugin repository, so using a version of Cordova 4.x or below will result in installing an [old version](http://plugins.cordova.io/#/package/uk.co.workingedge.phonegap.plugin.launchnavigator) of this plugin.

## Using [Cordova Plugman](https://github.com/apache/cordova-plugman)

    $ plugman install --plugin=uk.co.workingedge.phonegap.plugin.launchnavigator --platform=<platform> --project=<project_path> --plugins_dir=plugins

For example, to install for the Android platform

    $ plugman install --plugin=uk.co.workingedge.phonegap.plugin.launchnavigator --platform=android --project=platforms/android --plugins_dir=plugins

## PhoneGap Build

Add the following xml to your config.xml to use the latest version of this plugin from [npm](https://www.npmjs.com/package/uk.co.workingedge.phonegap.plugin.launchnavigator):

    <gap:plugin name="uk.co.workingedge.phonegap.plugin.launchnavigator" source="npm" />


# Using the plugin

The plugin has single function which launches the navigation app with the location specified as the destination.

    launchnavigator.navigate(destination, start, successCallback, errorCallback, options);

## Parameters

- destination (required): destination location to use for navigation, either as a String specifying the place name, or as an Array specifying latitude/longitude.
- start (optional): start location to use for navigation, either as a String specifying the place name, or as an Array specifying latitude/longitude. If not specified, the current device location will be used.
- successFn (optional): Called when plugin the call is successful.
- errorFn (optional): Called when plugin encounters an error. This callback function will be passed an error message string as the first parameter.
- options (optional): Platform-specific options. See [Caveats](#caveats) for details on each platform.

## Example usage

Navigate by place name:

    launchnavigator.navigate(
      "London, UK",
      "Manchester, UK",
      function(){
          alert("Plugin success");
      },
      function(error){
          alert("Plugin error: "+ error);
      });

Navigate by latitude/longitude:

    launchnavigator.navigate(
      [50.279306, -5.163158],
      [50.342847, -4.749904],
      function(){
          alert("Plugin success");
      },
      function(error){
          alert("Plugin error: "+ error);
      });

Navigate from current location:

    launchnavigator.navigate(
      "London, UK",
      null,
      function(){
          alert("Plugin success");
      },
      function(error){
          alert("Plugin error: "+ error);
      });

## Example project

https://github.com/dpa99c/phonegap-launch-navigator-example

The above link is to an example Cordova 3 project which demonstrates usage of this plugin.
The examples currently run on Android, iOS, Windows Phone 8.1, and Windows 8.1 (PC) platforms.


# Caveats

## Android

- Start location will be ignored if `transportMode` is "turn-by-turn" (defaults to current location even if defined).
- The Android version of the plugin supports the following platform-specific options:
  - {String} navigationMode - navigation mode in which to open Google Maps app: "maps" or "turn-by-turn". Defaults to "maps" if not specified.
  - {String} transportMode - transportation mode for navigation: "driving", "walking", "bicycling" or "transit". Defaults to "driving" if not specified. Only works when `transportMode` is "turn-by-turn".
  - {Boolean} disableAutoGeolocation - if true, the plugin will NOT attempt to use the geolocation plugin to determine the current device position when the start location parameter is omitted. Defaults to false.

For example:

    launchnavigator.navigate(
      "London, UK",
      null,
      function(){
        alert("Plugin success");
      },
      function(error){
        alert("Plugin error: "+ error);
      },
      {
        navigationMode: "turn-by-turn",
        transportMode: "bicycling",
        disableAutoGeolocation: true
      }
    );

## Windows

- The plugin is compatible with Windows 8.1 on any PC and on Windows Phone 8.0/8.1 using the Universal .Net project generated by Cordova: `cordova platform add windows` or `cordova platform add wp8` (for windows phone 8.0)

- Bing Maps requires the user to press the enter key to initiate navigation if you don't provide the start location.
Therefore, if a start location is not going to be passed to the plugin from your app, you should install the [Geolocation plugin](https://github.com/apache/cordova-plugin-geolocation) into your project.
If the geolocation plugin is detected, the plugin will attempt to retrieve the current device location using it, and use this as the start location.
This can be disabled via the `disableAutoGeolocation` option.

For example:

    launchnavigator.navigate(
      "London, UK",
      "Manchester, UK",
      function(){
        alert("Plugin success");
      },
      function(error){
        alert("Plugin error: "+ error);
      },
      {
        disableAutoGeolocation: true,
        transportMode: "walking"
      }
    );

- The Windows version of the plugin supports the following platform-specific options:
  - {String} transportMode - transportation mode for navigation: "driving", "walking" or "transit". Defaults to "driving" if not specified.
  - {Boolean} disableAutoGeolocation - if true, the plugin will NOT attempt to use the geolocation plugin to determine the current device position when the start location parameter is omitted. Defaults to false.

## iOS

- The iOS version of the plugin supports the following platform-specific options:
  - {Boolean} preferGoogleMaps - if true, plugin will attempt to launch Google Maps instead of Apple Maps. If Google Maps is not available, it will fall back to Apple Maps.
  - {Boolean} disableAutoGeolocation - if true, the plugin will NOT attempt to use the geolocation plugin to determine the current device position when the start location parameter is omitted. Defaults to false.
  - {String} transportMode - transportation mode for navigation. For Apple Maps, valid options are "driving" or "walking". For Google Maps, valid options are "driving", "walking", "bicycling" or "transit". Defaults to "driving" if not specified.
  - {String} urlScheme - if using Google Maps and the app has a URL scheme, passing this to Google Maps will display a button which returns to the app
  - {String} backButtonText - if using Google Maps with a URL scheme, this specifies the text of the button in Google Maps which returns to the app. Defaults to "Back" if not specified.
  - {Boolean} enableDebug - if true, debug log output will be generated by the plugin. Defaults to false.

For example:

    launchnavigator.navigate(
      "London, UK",
      "Manchester, UK",
      function(){
        alert("Plugin success");
      },
      function(error){
        alert("Plugin error: "+ error);
      },
      {
        preferGoogleMaps: true,
        transportMode: "transit",
        enableDebug: true,
        disableAutoGeolocation: true
    });

The iOS plugin interface also provides the additional function to check if Google Maps is installed and available on the iOS device:

    launchnavigator.isGoogleMapsAvailable(successFn);

Where `successFn` is a callback function which is passed a single parameter which is a boolean indicating if Google Maps is available. For example:

    launchnavigator.isGoogleMapsAvailable(
      function(available){
        if(available){
          alert("Google Maps is available");
        }else{
          alert("Google Maps is NOT available");
        }
      });

# Reporting issues

Before reporting issues with this plugin, please first do the following:

- Check the existing lists of [open issues](https://github.com/dpa99c/phonegap-launch-navigator/issues) and [closed issues](https://github.com/dpa99c/phonegap-launch-navigator/issues?q=is%3Aissue+is%3Aclosed)
- Check your target country is supported for turn-by-turn by the native navigation app
  - [Apple Maps country list for iOS](https://www.apple.com/ios/feature-availability/#maps-turn-by-turn-navigation)
  - [Google Maps country list for Android](https://support.google.com/gmm/answer/3137767?hl=en-GB)
  - [Bing Maps country list for Windows Phone](https://msdn.microsoft.com/en-us/library/dd435699.aspx)
- If possible, test using the [example project](https://github.com/dpa99c/phonegap-launch-navigator-example) to eliminate the possibility of a bug in your code rather than the plugin.


When reporting issues, please give the following information:

- A clear description of the problem

- OS version(s) and device (or emulator) model(s) on which the problem was observed

- Code example of calling the plugin which results in the observed issue

- Example parameters (locations or place names) which results in the observed issue


# Credits

Thanks to [opadro](https://github.com/opadro) for Windows implementation

License
================

The MIT License

Copyright (c) 2015 Working Edge Ltd.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.