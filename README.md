Launch Navigator Cordova/Phonegap Plugin [![Latest Stable Version](https://img.shields.io/npm/v/uk.co.workingedge.phonegap.plugin.launchnavigator.svg)](https://www.npmjs.com/package/uk.co.workingedge.phonegap.plugin.launchnavigator) [![Total Downloads](https://img.shields.io/npm/dt/uk.co.workingedge.phonegap.plugin.launchnavigator.svg)](https://npm-stat.com/charts.html?package=uk.co.workingedge.phonegap.plugin.launchnavigator)
=================================

This Cordova/Phonegap plugin can be used to navigate to a destination by launching native navigation apps on Android, iOS and Windows Phone.

The plugin is registered on [npm](https://www.npmjs.com/package/uk.co.workingedge.phonegap.plugin.launchnavigator) as `uk.co.workingedge.phonegap.plugin.launchnavigator`

<p align="center">
  <img src="http://i.imgur.com/v96FhpZ.gif" />
  <span>&nbsp;</span>
  <img src="http://i.imgur.com/mUg9WqO.gif" />
</p>

<!-- DONATE -->
[![donate](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG_global.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=ZRD3W47HQ3EMJ)

I dedicate a considerable amount of my free time to developing and maintaining this Cordova plugin, along with my other Open Source software.
To help ensure this plugin is kept updated, new features are added and bugfixes are implemented quickly, please donate a couple of dollars (or a little more if you can stretch) as this will help me to afford to dedicate time to its maintenance. Please consider donating if you're using this plugin in an app that makes you money, if you're being paid to make the app, if you're asking for new features or priority bug fixes.
<!-- END DONATE -->

<!-- START table-of-contents -->
**Table of Contents**

- [General concepts](#general-concepts)
  - [App detection, selection and launching](#app-detection-selection-and-launching)
  - [Format of start/destination locations](#format-of-startdestination-locations)
- [Supported navigation apps](#supported-navigation-apps)
  - [Adding support for more apps](#adding-support-for-more-apps)
- [Installing](#installing)
  - [Using the Cordova/Phonegap CLI](#using-the-cordovaphonegap-cli)
  - [Using Cordova Plugman](#using-cordova-plugman)
  - [PhoneGap Build](#phonegap-build)
- [Usage examples](#usage-examples)
  - [Simple usage](#simple-usage)
  - [Advanced usage](#advanced-usage)
- [Supported parameters](#supported-parameters)
  - [Transport modes](#transport-modes)
- [Plugin API](#plugin-api)
  - [Constants](#constants)
  - [API methods](#api-methods)
- [Example project](#example-project)
- [Legacy v2 API](#legacy-v2-api)
- [Platform-specifics](#platform-specifics)
  - [Android](#android)
  - [Windows](#windows)
  - [iOS](#ios)
- [Release notes](#release-notes)
- [Reporting issues](#reporting-issues)
- [Credits](#credits)
- [License](#license)

<!-- END table-of-contents -->

# General concepts

## App detection, selection and launching
- The plugin will detect which supported navigation apps are available on the device.
- By default, where this is more than one choice, it will display a list of these to the user to select for navigation.
    - This is done using native UI elements
- However, the plugin API allows you to programmatically:
    - check which apps are available on the current device
    - check which apps support which navigation options
    - launch a specific app for navigation

## Format of start/destination locations
- Some navigation apps require that destination/start locations be specified as coordinates, and others require an address.
- This plugin will appropriately geocode or reverse-geocode the locations you provide to ensure the app receives the location in the required format.
- So you can supply location as an address or as coordinates and the plugin will take care of getting it into the correct format for a particular app.
- However, geocoding requires use of a remote service, so an internet connection is required.

<!-- - Hence if `navigate()` is called and no internet connection is detected, an error will be returned. -->


# Supported navigation apps

The plugin currently supports launching the following navigation apps:

Android

* [Google Maps](https://play.google.com/store/apps/details?id=com.google.android.apps.maps)
* [Waze](https://play.google.com/store/apps/details?id=com.waze)
* [Citymapper](https://play.google.com/store/apps/details?id=com.citymapper.app.release)
* [Uber](https://play.google.com/store/apps/details?id=com.ubercab)
* [Yandex Navigator](https://play.google.com/store/apps/details?id=ru.yandex.yandexnavi)
* [Sygic](https://play.google.com/store/apps/details?id=com.sygic.aura)
* [HERE Maps](https://play.google.com/store/apps/details?id=com.here.app.maps&hl=en_GB)
* [Moovit](https://play.google.com/store/apps/details?id=com.tranzmate&hl=en_GB)
* _Any installed app that supports the [`geo:` URI scheme](http://developer.android.com/guide/components/intents-common.html#Maps)_

iOS

* [Apple Maps](http://www.apple.com/uk/ios/maps/)
* [Google Maps](https://itunes.apple.com/gb/app/google-maps/id585027354?mt=8)
* [Waze](https://itunes.apple.com/gb/app/waze-gps-maps-social-traffic/id323229106?mt=8)
* [Citymapper](https://itunes.apple.com/gb/app/citymapper-london-hong-kong/id469463298?mt=8)
* [Garmin Navigon](https://itunes.apple.com/us/developer/garmin-wuerzburg-gmbh/id320198400)
* [Transit App](https://itunes.apple.com/us/app/transit-app-real-time-tracker/id498151501?mt=8)
* [Yandex Navigator](https://itunes.apple.com/gb/app/yandex.navigator/id474500851?mt=8)
* [Uber](https://itunes.apple.com/gb/app/uber/id368677368?mt=8)
* [Tomtom](https://itunes.apple.com/gb/developer/tomtom/id326055452)
* [Sygic](https://itunes.apple.com/gb/app/sygic-gps-navigation-offline/id585193266?mt=8)
* [HERE Maps](https://itunes.apple.com/gb/app/here-maps-offline-navigation/id955837609?mt=8)
* [Moovit](https://itunes.apple.com/us/app/moovit-your-local-transit/id498477945?mt=8)

Windows

* [Bing Maps](https://www.microsoft.com/en-us/store/apps/maps/9wzdncrfj224)

## Adding support for more apps

This plugin is a work in progress. I'd like it to support launching of as many popular navigation apps as possible.

If there's another navigation app which you think should be explicitly supported and **it provides a mechanism to externally launch it**,
open an issue containing a link or details of how the app should be invoked.

**Don't** just open an issue saying "Add support for Blah" without first find out if/how it can be externally launched.
I don't have time to research launch mechanisms for every suggested app, so I will close such issues immediately.

# Installing

**IMPORTANT:** Note that the plugin will **NOT** work in a browser-emulated Cordova environment, for example by running `cordova serve` or using the [Ripple emulator](https://github.com/ripple-emulator/ripple).
This plugin is intended to launch **native** navigation apps and therefore will only work on native mobile platforms (i.e. Android/iOS/Windows).


## Using the Cordova/Phonegap CLI

    $ cordova plugin add uk.co.workingedge.phonegap.plugin.launchnavigator
    $ phonegap plugin add uk.co.workingedge.phonegap.plugin.launchnavigator

## Using Cordova Plugman

    $ plugman install --plugin=uk.co.workingedge.phonegap.plugin.launchnavigator --platform=<platform> --project=<project_path> --plugins_dir=plugins

For example, to install for the Android platform

    $ plugman install --plugin=uk.co.workingedge.phonegap.plugin.launchnavigator --platform=android --project=platforms/android --plugins_dir=plugins

## PhoneGap Build

Add the following xml to your config.xml to use the latest version of this plugin from [npm](https://www.npmjs.com/package/uk.co.workingedge.phonegap.plugin.launchnavigator):

    <plugin name="uk.co.workingedge.phonegap.plugin.launchnavigator" source="npm" />

# Usage examples

## Simple usage

### Navigate to a destination address from current location.

User is prompted to choose from available installed navigation apps.

    launchnavigator.navigate("London, UK");

### Navigate to a destination with specified start location

    launchnavigator.navigate("London, UK", {
        start: "Manchester, UK"
    });

### Navigate using latitude/longitude coordinates

Coordinates can be specified as a string or array

    launchnavigator.navigate([50.279306, -5.163158], {
        start: "50.342847, -4.749904"
    };

## Advanced usage

### Navigate using a specific app

    launchnavigator.isAppAvailable(launchnavigator.APP.GOOGLE_MAPS, function(isAvailable){
        var app;
        if(isAvailable){
            app = launchnavigator.APP.GOOGLE_MAPS;
        }else{
            console.warn("Google Maps not available - falling back to user selection");
            app = launchnavigator.APP.USER_SELECT;
        }
        launchnavigator.navigate("London, UK", {
            app: app
        });
    });

### List all of the apps supported by the current platform

    var platform = device.platform.toLowerCase();
    if(platform == "android"){
        platform = launchnavigator.PLATFORM.ANDROID;
    }else if(platform == "ios"){
        platform = launchnavigator.PLATFORM.IOS;
    }else if(platform.match(/win/)){
        platform = launchnavigator.PLATFORM.WINDOWS;
    }

    launchnavigator.getAppsForPlatform(platform).forEach(function(app){
        console.log(launchnavigator.getAppDisplayName(app) + " is supported");
    });

### List apps available on the current device

    launchnavigator.availableApps(function(results){
        for(var app in results){
            console.log(launchnavigator.getAppDisplayName(app) + (results[app] ? " is" : " isn't") +" available");
        }
    });

# Supported parameters

Different apps support different input parameters on different platforms.
Any input parameters not supported by a specified app will be ignored.


The following table enumerates which apps support which parameters.

| Platform | App                            | Dest | Dest name | Start | Start name | Transport mode | Free |
|----------|--------------------------------|:----:|:---------:|:-----:|:----------:|:--------------:|:----:|
| Android  | Google Maps (Map mode)         |   X  |           |   X   |            |                |   X  |
| Android  | Google Maps (Turn-by-turn mode)|   X  |           |       |            |        X       |   X  |
| Android  | Waze                           |   X  |           |       |            |                |   X  |
| Android  | CityMapper                     |   X  |     X     |   X   |      X     |                |   X  |
| Android  | Uber                           |   X  |     X     |   X   |      X     |                |   X  |
| Android  | Yandex                         |   X  |           |   X   |            |                |   X  |
| Android  | Sygic                          |   X  |           |       |            |        X       |   X  |
| Android  | HERE Maps                      |   X  |     X     |   X   |      X     |                |   X  |
| Android  | Moovit                         |   X  |     X     |   X   |      X     |                |   X  |
| Android  | _Geo: URI scheme_              |   X  |     X     |       |            |                |  N/A |
| iOS      | Apple Maps                     |   X  |     X     |   X   |      X     |        X       |   X  |
| iOS      | Google Maps                    |   X  |           |   X   |            |        X       |   X  |
| iOS      | Waze                           |   X  |           |       |            |                |   X  |
| iOS      | Citymapper                     |   X  |     X     |   X   |      X     |                |   X  |
| iOS      | Navigon                        |   X  |     X     |       |            |                |      |
| iOS      | Transit App                    |   X  |           |   X   |            |                |   X  |
| iOS      | Yandex                         |   X  |           |   X   |            |                |   X  |
| iOS      | Uber                           |   X  |     X     |   X   |            |                |   X  |
| iOS      | Tomtom                         |   X  |     X     |       |            |                |      |
| iOS      | Sygic                          |   X  |           |       |            |        X       |   X  |
| iOS      | HERE Maps                      |   X  |     X     |   X   |      X     |                |   X  |
| iOS      | Moovit                         |   X  |     X     |   X   |      X     |                |   X  |
| Windows  | Bing Maps                      |   X  |     X     |   X   |      X     |        X       |   X  |

Table columns:

* Dest - destination location specified as lat/lon (e.g. "50,-4") or address (e.g. "London")
* Dest name - nickname for destination location (e.g. "Bob's house")
* Start - start location specified as lat/lon (e.g. "50,-4") or address (e.g. "London")
* Start name - nickname for start location (e.g. "Bob's house")
* Transport mode - mode of transport to use for route planning (e.g. "walking")
* Free - is the app free or does it cost money?


## Transport modes

Apps that support specifying transport mode.

| Platform | App                            | Driving | Walking | Bicycling | Transit |
|----------|--------------------------------|:-------:|:-------:|:---------:|:-------:|
| Android  | Google Maps (Turn-by-turn mode)|    X    |    X    |     X     |    X    |
| Android  | Sygic                          |    X    |    X    |           |         |
| iOS      | Apple Maps                     |    X    |    X    |           |         |
| iOS      | Google Maps                    |    X    |    X    |     X     |    X    |
| iOS      | Sygic                          |    X    |    X    |           |         |
| Windows  | Bing Maps                      |    X    |    X    |           |    X    |


# Plugin API

All of the following constants and functions should be referenced from the global `launchnavigator` namespace. For example:

    launchnavigator.PLATFORM.ANDROID

## Constants

### PLATFORM

Supported platforms:

- `launchnavigator.PLATFORM.ANDROID`
- `launchnavigator.PLATFORM.IOS`
- `launchnavigator.PLATFORM.WINDOWS`

### APP

Supported apps:

- `launchnavigator.APP.USER_SELECT` (Android & iOS) - invokes native UI for user to select available navigation app
- `launchnavigator.APP.GEO` (Android) - invokes a native chooser, allowing users to select an app which supports the `geo:` URI scheme for navigation
- `launchnavigator.APP.GOOGLE_MAPS` (Android & iOS)
- `launchnavigator.APP.WAZE` (Android & iOS)
- `launchnavigator.APP.CITYMAPPER` (Android & iOS)
- `launchnavigator.APP.UBER` (Android & iOS)
- `launchnavigator.APP.APPLE_MAPS` (iOS)
- `launchnavigator.APP.NAVIGON` (iOS)
- `launchnavigator.APP.TRANSIT_APP` (iOS)
- `launchnavigator.APP.YANDEX` (Android & iOS)
- `launchnavigator.APP.TOMTOM` (iOS)
- `launchnavigator.APP.BING_MAPS` (Windows)
- `launchnavigator.APP.SYGIC` (Android & iOS)
- `launchnavigator.APP.HERE_MAPS` (Android & iOS)
- `launchnavigator.APP.MOOVIT` (Android & iOS)

### APP_NAMES

Display names for supported apps, referenced by `launchnavigator.APP`.

e.g. `launchnavigator.APP_NAMES[launchnavigator.APP.GOOGLE_MAPS] == "Google Maps"`

### `launchnavigator.TRANSPORT_MODE`

Transport modes for navigation:

- `launchnavigator.TRANSPORT_MODE.DRIVING`
- `launchnavigator.TRANSPORT_MODE.WALKING`
- `launchnavigator.TRANSPORT_MODE.BICYCLING`
- `launchnavigator.TRANSPORT_MODE.TRANSIT`

### LAUNCH_MODE

Android only: launch modes supported by Google Maps on Android

- `launchnavigator.LAUNCH_MODE.MAPS` - Maps view
- `launchnavigator.LAUNCH_MODE.TURN_BY_TURN` - Navigation view
- `launchnavigator.LAUNCH_MODE.GEO` - Navigation view via `geo:` URI scheme

## API methods

### navigate()

The plugin's primary API method.
Launches a navigation app with a specified destination.
Also takes optional parameters.

    launchnavigator.navigate(destination, options);

OR

    launchnavigator.navigate(destination, successCallback, errorCallback, options);

#### Parameters

- destination (required): destination location to use for navigation.
Either:
    - a {string} containing the address. e.g. "Buckingham Palace, London"
    - a {string} containing a latitude/longitude coordinate. e.g. "50.1. -4.0"
    - an {array}, where the first element is the latitude and the second element is a longitude, as decimal numbers. e.g. [50.1, -4.0]
- options - optional parameters:
    - successCallback (optional): A callback to invoke when the navigation app is successfully launched.
    - errorCallback (optional): A callback to invoke if an error is encountered while launching the app. A single string argument containing the error message will be passed in.
    - {string} app - name of the navigation app to use for directions.
    Specify using `launchnavigator.APP` constants.  e.g. `launchnavigator.APP.GOOGLE_MAPS`.
    If not specified, defaults to user selection via native picker UI.
    - {string} destinationName - nickname to display in app for destination. e.g. "Bob's House".
    - start (optional): start location to use for navigation.
    If not specified, the current device location will be used.
    Either:
        - a {string} containing the address. e.g. "Buckingham Palace, London"
        - a {string} containing a latitude/longitude coordinate. e.g. "50.1. -4.0"
        - an {array}, where the first element is the latitude and the second element is a longitude, as decimal numbers. e.g. [50.1, -4.0]
    - {string} startName - nickname to display in app for start. e.g. "My Place".
    - {string} transportMode - transportation mode for navigation.
    Defaults to "driving" if not specified.
    Specify using `launchnavigator.TRANSPORT_MODE` constants.
    - {boolean} enableDebug - if true, debug log output will be generated by the plugin. Defaults to false.
    - {object} extras - a key/value map of extra app-specific parameters. For example, to tell Google Maps on Android to display Satellite view in "maps" launch mode: `{"t": "k"}`
    - {string} launchMode - (Android only) mode in which to open Google Maps app: "maps" or "turn-by-turn".
    Defaults to "maps" if not specified.
    Specify using `launchnavigator.LAUNCH_MODE` constants.
    - {string} appSelectionDialogHeader - text to display in the native picker which enables user to select which navigation app to launch.
    Defaults to "Select app for navigation" if not specified.
    - {string} appSelectionCancelButton - text to display for the cancel button in the native picker which enables user to select which navigation app to launch.
    Defaults to "Cancel" if not specified.
    - {array} appSelectionList - list of apps, defined as `launchnavigator.APP` constants, which should be displayed in the picker if the app is available.
    This can be used to restrict which apps are displayed, even if they are installed. By default, all available apps will be displayed.
    - {function} appSelectionCallback - a callback to invoke when the user selects an app in the native picker.
    A single string argument is passed which is the app what was selected defined as a `launchnavigator.APP` constant.
    - {integer} androidTheme - (Android only) native picker theme. Specify using `actionsheet.ANDROID_THEMES` constants. Default `actionsheet.ANDROID_THEMES.THEME_HOLO_LIGHT`

### isAppAvailable()

Determines if the given app is installed and available on the current device.

    launchnavigator.isAppAvailable(appName, success, error);

#### Parameters
- {string} appName - name of the app to check availability for. Define as a constant using `ln.APP`.
- {function} success - callback to invoke on successful determination of availability. Will be passed a single boolean argument indicating the availability of the app.
- {function} error - callback to invoke on error while determining availability. Will be passed a single string argument containing the error message.


### availableApps()

Returns a list indicating which apps are installed and available on the current device.

    launchnavigator.availableApps(success, error);

#### Parameters
- {function} success - callback to invoke on successful determination of availability. Will be passed a key/value object where the key is the app name and the value is a boolean indicating whether the app is available.
- {function} error - callback to invoke on error while determining availability. Will be passed a single string argument containing the error message.



### getAppDisplayName()

Returns the display name of the specified app.

    launchnavigator.getAppDisplayName(app);

#### Parameters
- {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`. whether the app is available.
- returns {string} - app display name. e.g. "Google Maps".



### getAppsForPlatform()

Returns list of supported apps on a given platform.

    launchnavigator.getAppsForPlatform(platform);

#### Parameters
- {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
- returns {string} -  {array} - apps supported on specified platform as a list of `launchnavigator.APP` constants.


### supportsTransportMode()

Indicates if an app on a given platform supports specification of transport mode.

    launchnavigator.supportsTransportMode(app, platform, launchMode);

#### Parameters
- {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
- {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
- {string} launchMode (optional) - Android only. Only applies to Google Maps on Android. Specified as a constant in `launchnavigator.LAUNCH_MODE`. e.g. `launchnavigator.LAUNCH_MODE.MAPS`.
- returns {boolean} - true if app/platform combination supports specification of transport mode.



### getTransportModes()

Returns the list of transport modes supported by an app on a given platform.

    launchnavigator.getTransportModes(app, platform, launchMode);

#### Parameters
- {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
- {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
- {string} launchMode (optional) - Android only. Only applies to Google Maps on Android. Specified as a constant in `launchnavigator.LAUNCH_MODE`. e.g. `launchnavigator.LAUNCH_MODE.MAPS`.
- returns {boolean} - {array} - list of transports modes as constants in `launchnavigator.TRANSPORT_MODE`.
If app/platform combination doesn't support specification of transport mode, the list will be empty;



### supportsDestName()

Indicates if an app on a given platform supports specification of a custom nickname for destination location.

    launchnavigator.supportsDestName(app, platform, launchMode);

#### Parameters
- {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
- {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
- {string} launchMode (optional) - Android only. Only applies to Google Maps on Android. Specified as a constant in `launchnavigator.LAUNCH_MODE`. e.g. `launchnavigator.LAUNCH_MODE.MAPS`.
- returns {boolean} - true if app/platform combination supports specification of a custom nickname for destination location.


### supportsStart()

Indicates if an app on a given platform supports specification of start location.

    launchnavigator.supportsStart(app, platform, launchMode);

#### Parameters
- {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
- {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
- {string} launchMode (optional) - Android only. Only applies to Google Maps on Android. Specified as a constant in `launchnavigator.LAUNCH_MODE`. e.g. `launchnavigator.LAUNCH_MODE.MAPS`.
- returns {boolean} - true if app/platform combination supports specification of start location.



### supportsStartName()

Indicates if an app on a given platform supports specification of a custom nickname for start location.

    launchnavigator.supportsStartName(app, platform);

#### Parameters
- {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
- {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
- returns {boolean} - {boolean} - true if app/platform combination supports specification of a custom nickname for start location.



### supportsLaunchMode()

Indicates if an app on a given platform supports specification of launch mode.
Note that currently only Google Maps on Android does.

    launchnavigator.supportsLaunchMode(app, platform);

#### Parameters
- {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
- {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.ANDROID`.
- returns {boolean} - true if app/platform combination supports specification of transport mode.


# Example project

There is an [example Cordova project](https://github.com/dpa99c/phonegap-launch-navigator-example) which demonstrates usage of this plugin.
The example currently runs on Android, iOS, Windows Phone 8.1, Windows 8.1 (PC), and Windows 10 (PC) platforms.

# Legacy v2 API

The plugin API has changed in v3, but the v2 API is still supported, although deprecated and will be removed in a future version, so plugin users are urged to migrate to the new API.
Calls to the plugin which use the v2 API syntax will display a deprecation warning message in the JS console.


# Platform-specifics

## Android

- Running on Android, in addition to discovering which explicitly supported apps are installed, the plugin will also detect which installed apps support using the `geo:` URI scheme for use in navigation. These are returned in the list of available apps.

- By specifying the `app` option as `launchnavigator.APP.GEO`, the plugin will invoke a native Android chooser, to allow the user to select an app which supports the `geo:` URI scheme for navigation.

- Google Maps on Android can be launched in 3 launch modes:
    - Maps mode (`launchnavigator.LAUNCH_MODE.MAPS`) - launches in Map view. Enables start location to be specified, but not transport mode or destination name.
    - Turn-by-turn mode (`launchnavigator.LAUNCH_MODE.TURN_BY_TURN`) - launches in Navigation view. Enables transport mode to be specified, but not start location or destination name.
    - Geo mode (`launchnavigator.LAUNCH_MODE.GEO`) - invokes Navigation view via `geo`: URI scheme. Enables destination name to be specified, but not start location or transport mode.
    - Launch mode can be specified via the `launchMode` option, but defaults to Maps mode if not specified.


## Windows

- The plugin is compatible with Windows 8.1 or Windows 10 on any PC and on Windows Phone 8.0/8.1 using the Universal .Net project generated by Cordova: `cordova platform add windows` or `cordova platform add wp8` (for windows phone 8.0)

- Bing Maps requires the user to press the enter key to initiate navigation if you don't provide the start location.
Therefore, if a start location is not going to be passed to the plugin from your app, you should install the [Geolocation plugin](https://github.com/apache/cordova-plugin-geolocation) into your project.
If the geolocation plugin is detected, the plugin will attempt to retrieve the current device location using it, and use this as the start location.
This can be disabled via the `disableAutoGeolocation` option.


## iOS

- The iOS implementation uses a [forked version](https://github.com/dpa99c/CMMapLauncher) of the [CMMapLauncher library](https://github.com/citymapper/CMMapLauncher) to invoke apps.
- "Removing" Apple Maps
  - Since iOS 10, it is possible to "remove" built-in Apple apps, including Maps, from the Home screen.
  - Not that removing is not the same as uninstalling - the app is still actually present on the device, just the icon is removed from the Home screen.
  - Therefore it's not possible detect if Apple Maps is unavailable - `launchnavigator.availableApps()` will always report it as present.
  - The best that can be done is to gracefully handle the error when attempting to open Apple Maps using `launchnavigator.navigate()`
  - For reference, see [this SO question](http://stackoverflow.com/questions/39603120/how-to-check-if-apple-maps-is-installed) and the [Apple documentation](https://support.apple.com/en-gb/HT204221).

# Release notes
**v3.0.1**
Replaced legacy Apache HTTP client with OkHttp client on Android to prevent Gradle build issues

**v3.0.0**

Version 3 is a complete rewrite of the plugin in order to support launching of 3rd party navigation apps on Android and iOS.
The plugin API has changed significantly, but is backwardly-compatible to support the version 2 API syntax.

If for any reason you have issues with v3, the final release of v2 is on this branch: [https://github.com/dpa99c/phonegap-launch-navigator/tree/v2](https://github.com/dpa99c/phonegap-launch-navigator/tree/v2). It can be specified in a Cordova project as `uk.co.workingedge.phonegap.plugin.launchnavigator@2`.

[cordova-plugin-actionsheet](https://github.com/EddyVerbruggen/cordova-plugin-actionsheet) has been introduced as a plugin dependency.
It is used to display a native picker (both on Android and iOS) to choose which available navigation app should be used. This is used if [app](#app) is specified as `launchnavigator.APP.USER_SELECT`, which is the default in v3 if an app is not explicitly specified.

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

**Issues which fail to give a clear description of the problem as described above will be closed immediately**

# Credits

Thanks to:

- [opadro](https://github.com/opadro) for Windows implementation
- [Eddy Verbruggen](https://github.com/EddyVerbruggen) for [cordova-plugin-actionsheet](https://github.com/EddyVerbruggen/cordova-plugin-actionsheet)
- [Citymapper](http://citymapper.com/) for [CMMapLauncher iOS library](https://github.com/citymapper/CMMapLauncher)

License
================

The MIT License

Copyright (c) 2016 Dave Alden (Working Edge Ltd.)

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
