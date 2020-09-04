Launch Navigator Cordova/Phonegap Plugin [![Latest Stable Version](https://img.shields.io/npm/v/uk.co.workingedge.phonegap.plugin.launchnavigator.svg)](https://www.npmjs.com/package/uk.co.workingedge.phonegap.plugin.launchnavigator) [![Total Downloads](https://img.shields.io/npm/dt/uk.co.workingedge.phonegap.plugin.launchnavigator.svg)](https://npm-stat.com/charts.html?package=uk.co.workingedge.phonegap.plugin.launchnavigator)
=================================

Cordova/Phonegap plugin for launching today's most popular navigation/ride apps to navigate to a destination.

Platforms: Android, iOS and Windows.

Key features:

- Single, clean API to abstract away the gory details of each 3rd party app's custom URI scheme
- Detects which supported apps are installed/available on the user's device
- API to detect which features are supported by which apps on which platforms
- Out-of-the-box UI for app selection which remembers user choice
- Growing list of [supported apps](#supported-navigation-apps)

Launch Navigator is also available as a [React Native module](https://github.com/dpa99c/react-native-launch-navigator).

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

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
**Table of Contents**

- [General concepts](#general-concepts)
  - [App detection, selection and launching](#app-detection-selection-and-launching)
  - [Geocoding and input format of start/destination locations](#geocoding-and-input-format-of-startdestination-locations)
  - [Remember user's choice of navigation app](#remember-users-choice-of-navigation-app)
- [Supported navigation apps](#supported-navigation-apps)
  - [Adding support for more apps](#adding-support-for-more-apps)
- [Installing](#installing)
  - [Using the CLI](#using-the-cli)
  - [PhoneGap Build](#phonegap-build)
  - [Google API key for Android](#google-api-key-for-android)
  - [OKHTTP Library](#okhttp-library)
- [Usage examples](#usage-examples)
  - [Simple usage](#simple-usage)
    - [Navigate to a destination address from current location.](#navigate-to-a-destination-address-from-current-location)
    - [Navigate to a destination with specified start location](#navigate-to-a-destination-with-specified-start-location)
    - [Navigate using latitude/longitude coordinates](#navigate-using-latitudelongitude-coordinates)
  - [Advanced usage](#advanced-usage)
    - [Navigate using a specific app](#navigate-using-a-specific-app)
    - [List all of the apps supported by the current platform](#list-all-of-the-apps-supported-by-the-current-platform)
    - [List apps available on the current device](#list-apps-available-on-the-current-device)
- [Reporting issues](#reporting-issues)
  - [Reporting a bug or problem](#reporting-a-bug-or-problem)
  - [Requesting a new feature](#requesting-a-new-feature)
- [Supported parameters](#supported-parameters)
  - [Transport modes](#transport-modes)
- [Plugin API](#plugin-api)
  - [Constants](#constants)
    - [PLATFORM](#platform)
    - [APP](#app)
    - [APP_NAMES](#app_names)
    - [TRANSPORT_MODE](#transport_mode)
    - [LAUNCH_MODE](#launch_mode)
  - [API methods](#api-methods)
    - [navigate()](#navigate)
      - [Parameters](#parameters)
    - [enableDebug()](#enabledebug)
      - [Parameters](#parameters-1)
    - [isAppAvailable()](#isappavailable)
      - [Parameters](#parameters-2)
    - [availableApps()](#availableapps)
      - [Parameters](#parameters-3)
    - [getAppDisplayName()](#getappdisplayname)
      - [Parameters](#parameters-4)
    - [getAppsForPlatform()](#getappsforplatform)
      - [Parameters](#parameters-5)
    - [supportsTransportMode()](#supportstransportmode)
      - [Parameters](#parameters-6)
    - [getTransportModes()](#gettransportmodes)
      - [Parameters](#parameters-7)
    - [supportsDestName()](#supportsdestname)
      - [Parameters](#parameters-8)
    - [supportsStart()](#supportsstart)
      - [Parameters](#parameters-9)
    - [supportsStartName()](#supportsstartname)
      - [Parameters](#parameters-10)
    - [supportsLaunchMode()](#supportslaunchmode)
      - [Parameters](#parameters-11)
    - [appSelection.userChoice.exists()](#appselectionuserchoiceexists)
      - [Parameters](#parameters-12)
    - [appSelection.userChoice.get()](#appselectionuserchoiceget)
      - [Parameters](#parameters-13)
    - [appSelection.userChoice.set()](#appselectionuserchoiceset)
      - [Parameters](#parameters-14)
    - [appSelection.userChoice.clear()](#appselectionuserchoiceclear)
      - [Parameters](#parameters-15)
    - [appSelection.userPrompted.get()](#appselectionuserpromptedget)
      - [Parameters](#parameters-16)
    - [appSelection.userPrompted.set()](#appselectionuserpromptedset)
      - [Parameters](#parameters-17)
    - [appSelection.userPrompted.clear()](#appselectionuserpromptedclear)
      - [Parameters](#parameters-18)
    - [setApiKey()](#setapikey)
      - [Parameters](#parameters-19)
- [Example projects](#example-projects)
- [Platform-specifics](#platform-specifics)
  - [Android](#android)
  - [Windows](#windows)
  - [iOS](#ios)
    - ["Removing" Apple Maps](#removing-apple-maps)
    - [Apple Maps launch method](#apple-maps-launch-method)
      - [URI scheme launch method](#uri-scheme-launch-method)
      - [MapKit class launch method](#mapkit-class-launch-method)
- [App-specifics](#app-specifics)
  - [Lyft](#lyft)
  - [99 Taxi](#99-taxi)
- [Credits](#credits)
- [License](#license)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

# General concepts

## App detection, selection and launching
- The plugin will detect which supported navigation apps are available on the device.
- By default, where this is more than one choice, it will display a list of these to the user to select for navigation.
    - This is done using native UI elements
- However, the plugin API allows you to programmatically:
    - check which apps are available on the current device
    - check which apps support which navigation options
    - launch a specific app for navigation

## Geocoding and input format of start/destination locations
- Some navigation apps require that destination/start locations be specified as coordinates, and others require an address.
    - See [App location support type wiki page](https://github.com/dpa99c/phonegap-launch-navigator/wiki/App-location-type-support) for details of which apps support which location types.
- By default, this plugin will appropriately geocode or reverse-geocode the locations you provide to ensure the app receives the location in the required format.
- However, geocoding requires use of a remote service, so an internet connection is required.
- If `navigate()` is passed a location type which the selected app doesn't support, the error callback will be invoked if:
  - geocoding is disabled by passing `enableGeocoding: false` in the options object
  - there is no internet connection to perform the remote geocode operation
  - geocoding fails (e.g. an address cannot be found for the given lat/long coords)
- Note that for geocoding to work on Android, a Google API key must be specified in order to use the Google Geocoder API service (see installation instructions below).
  
## Remember user's choice of navigation app

- If the built-in app selection mechanism is used, the plugin enables the user's choice of app to be locally persisted, meaning they don't have to choose every time.
- By default, this as the user to confirm they wish their choice to be remembered.
- See the `appSelection` section of options for the [`navigate()`](#navigate) function for more details.
- See the "Advanced Example" project] in the [example repo](https://github.com/dpa99c/phonegap-launch-navigator-example) for an illustrated example.

# Supported navigation apps

The plugin currently supports launching the following navigation apps:

Android

* [Google Maps](https://play.google.com/store/apps/details?id=com.google.android.apps.maps)
* [Waze](https://play.google.com/store/apps/details?id=com.waze)
* [Citymapper](https://play.google.com/store/apps/details?id=com.citymapper.app.release)
* [Uber](https://play.google.com/store/apps/details?id=com.ubercab)
* [Yandex Navigator](https://play.google.com/store/apps/details?id=ru.yandex.yandexnavi)
* [Sygic](https://play.google.com/store/apps/details?id=com.sygic.aura)
* [HERE Maps](https://play.google.com/store/apps/details?id=com.here.app.maps)
* [Moovit](https://play.google.com/store/apps/details?id=com.tranzmate)
* [Lyft](https://play.google.com/store/apps/details?id=me.lyft.android)
* [MAPS.ME](https://play.google.com/store/apps/details?id=com.mapswithme.maps.pro)
* [Cabify](https://play.google.com/store/apps/details?id=com.cabify.rider)
* [99 Taxi](https://play.google.com/store/apps/details?id=com.taxis99&hl=en)
* [Baidu Maps](https://play.google.com/store/apps/details?id=com.baidu.BaiduMap)
* [Gaode](https://play.google.com/store/apps/details?id=com.autonavi.minimap&hl=en)


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
* [Lyft](https://itunes.apple.com/us/app/lyft/id529379082?mt=8)
* [MAPS.ME](https://itunes.apple.com/us/app/maps-me-offline-map-with-navigation-directions/id510623322?mt=8)
* [Cabify](https://itunes.apple.com/us/app/cabify-enjoy-the-ride/id476087442?mt=8)
* [99 Taxi](https://itunes.apple.com/gb/app/99-taxi-and-private-drivers/id553663691?mt=8)
* [Baidu Maps](https://itunes.apple.com/us/app/%E7%99%BE%E5%BA%A6%E5%9C%B0%E5%9B%BE-%E5%85%AC%E4%BA%A4%E5%9C%B0%E9%93%81%E5%87%BA%E8%A1%8C%E5%BF%85%E5%A4%87%E7%9A%84%E6%99%BA%E8%83%BD%E5%AF%BC%E8%88%AA/id452186370?mt=8)
* [Gaode](https://itunes.apple.com/cn/app/%E9%AB%98%E5%BE%B7%E5%9C%B0%E5%9B%BE-%E7%B2%BE%E5%87%86%E5%9C%B0%E5%9B%BE-%E5%AF%BC%E8%88%AA%E5%BF%85%E5%A4%87/id461703208?mt=8)


Windows

* [Bing Maps](https://www.microsoft.com/en-us/store/apps/maps/9wzdncrfj224)

## Adding support for more apps

This plugin is a work in progress. I'd like it to support launching of as many popular navigation apps as possible.

If there's another navigation app which you think should be explicitly supported and **it provides a mechanism to externally launch it**,
open an issue containing a link or details of how the app should be invoked.

**Don't** just open an issue saying "Add support for Blah" without first finding out if/how it can be externally launched.
I don't have time to research launch mechanisms for every suggested app, so I will close such issues immediately.

# Installing

The plugin is registered on [npm](https://www.npmjs.com/package/uk.co.workingedge.phonegap.plugin.launchnavigator) as `uk.co.workingedge.phonegap.plugin.launchnavigator`

**IMPORTANT:** Note that the plugin will **NOT** work in a browser-emulated Cordova environment, for example by running `cordova serve` or using the [Ripple emulator](https://github.com/ripple-emulator/ripple).
This plugin is intended to launch **native** navigation apps and therefore will only work on native mobile platforms (i.e. Android/iOS/Windows).


## Using the CLI

    $ cordova plugin add uk.co.workingedge.phonegap.plugin.launchnavigator --variable GOOGLE_API_KEY_FOR_ANDROID="{your_api_key}"
    $ phonegap plugin add uk.co.workingedge.phonegap.plugin.launchnavigator --variable GOOGLE_API_KEY_FOR_ANDROID="{your_api_key}"
    $ ionic cordova plugin add uk.co.workingedge.phonegap.plugin.launchnavigator --variable GOOGLE_API_KEY_FOR_ANDROID="{your_api_key}"

## PhoneGap Build

Add the following xml to your config.xml to use the latest version of this plugin from [npm](https://www.npmjs.com/package/uk.co.workingedge.phonegap.plugin.launchnavigator):

    <plugin name="uk.co.workingedge.phonegap.plugin.launchnavigator" source="npm" >
        <variable name="GOOGLE_API_KEY_FOR_ANDROID" value="{your_api_key}" />
    </plugin>

## Google API key for Android
- On Android, this plugin uses [Google's Geocoding API](https://developers.google.com/maps/documentation/geocoding/intro) to geocode input addresses to lat/lon coordinates in order to support navigation apps which only allow input locations to be specified as lat/lon coordinates.
- Google now requires that an API key be specified in order to use the Geocoding API
    - For more information on how to obtain an API key, see the [Google documentation](https://developers.google.com/maps/documentation/geocoding/get-api-key).
    - Don't place any application restrictions on the key, otherwise geocoding will fail.
- You'll need to provide your Google API key to the plugin by either:
    - setting the `GOOGLE_API_KEY_FOR_ANDROID` plugin variable during plugin installation
        - Note: this method places your API in the `AndroidManifest.xml` in cleartext so carries the possible security risk of a malicious party decompiling your app to obtain your API key (see [#249](https://github.com/dpa99c/phonegap-launch-navigator/issues/249))
    - setting it at runtime by calling the [setApiKey()](#setapikey) function
        - this method is secure from a security perspective.
        - you must call this method in each app session (e.g. at app startup) before attempting to use the plugin's geocoding features on Android.

## OKHTTP Library
- This plugin uses the [OKHTTP library](https://square.github.io/okhttp/) on Android to access Google's remote Geocoding API service
- The library is included at Android build time via Gradle
- If another plugin in your Cordova project specifies a different version of the OKHTTP library than this plugin, this can cause a Gradle version collision leading to build failure. [See #193](https://github.com/dpa99c/phonegap-launch-navigator/issues/193).
- You can override the default version of the library specified by this plugin by specifying the `OKHTTP_VERSION` plugin variable during plugin installation:
    - `cordova plugin add uk.co.workingedge.phonegap.plugin.launchnavigator --variable GOOGLE_API_KEY_FOR_ANDROID="{your_api_key}" --variable OKHTTP_VERSION=1.2.3`
- You can find the version of the library currently specified by this plugin [in the plugin.xml](https://github.com/dpa99c/phonegap-launch-navigator/blob/master/plugin.xml)

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
    });

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
    
# Reporting issues
**IMPORTANT:** Please read the following carefully. 
Failure to follow the issue template guidelines below will result in the issue being immediately closed.

## Reporting a bug or problem
Before [opening a bug issue](https://github.com/dpa99c/phonegap-launch-navigator/issues/new?assignees=&labels=&template=bug_report.md&title=), please do the following:
- *DO NOT* open issues asking for support in using/integrating the plugin into your project
    - Only open issues for suspected bugs/issues with the plugin that are generic and will affect other users
    - I don't have time to offer free technical support: this is free open-source software
    - Ask for help on StackOverflow, Ionic Forums, etc.
    - Use the [example project](https://github.com/dpa99c/phonegap-launch-navigator-example) as a known working reference
    - Any issues requesting support will be closed immediately.
- *DO NOT* open issues related to the  [Ionic Typescript wrapper for this plugin](https://github.com/ionic-team/ionic-native/blob/master/src/%40ionic-native/plugins/launch-navigator/index.ts)
    - This is owned/maintained by [Ionic](https://github.com/ionic-team) and is not part of this plugin
    - Please raise such issues/PRs against [Ionic Native](https://github.com/ionic-team/ionic-native/) instead.
	- To verify an if an issue is caused by this plugin or its Typescript wrapper, please re-test using the vanilla Javascript plugin interface (without the Ionic Native wrapper).
	- Any issue opened here which is obviously an Ionic Typescript wrapper issue will be closed immediately.
- Read the above documentation thoroughly
- Check your target country is supported for turn-by-turn by the native navigation app
  - [Apple Maps country list for iOS](https://www.apple.com/ios/feature-availability/#maps-turn-by-turn-navigation)
  - [Google Maps country list for Android](https://support.google.com/gmm/answer/3137767?hl=en-GB)
  - [Bing Maps country list for Windows Phone](https://msdn.microsoft.com/en-us/library/dd435699.aspx)
- Check the [CHANGELOG](https://github.com/dpa99c/phonegap-launch-navigator/blob/master/CHANGELOG.md) for any breaking changes that may be causing your issue.
- Check a similar issue (open or closed) does not already exist against this plugin.
	- Duplicates or near-duplicates will be closed immediately.
- When [creating a new issue](https://github.com/dpa99c/phonegap-launch-navigator/issues/new/choose)
    - Choose the "Bug report" template
    - Fill out the relevant sections of the template and delete irrelevant sections
    - *WARNING:* Failure to complete the issue template will result in the issue being closed immediately. 
- Reproduce the issue using the [example project](https://github.com/dpa99c/phonegap-launch-navigator-example)
	- This will eliminate bugs in your code or conflicts with other code as possible causes of the issue
	- This will also validate your development environment using a known working codebase
	- If reproducing the issue using the example project is not possible, create an isolated test project that you are able to share
- Include full verbose console output when reporting build issues
    - If the full console output is too large to insert directly into the Github issue, then post it on an external site such as [Pastebin](https://pastebin.com/) and link to it from the issue 
    - Often the details of an error causing a build failure is hidden away when building with the CLI
        - To get the full detailed console output, append the `--verbose` flag to CLI build commands
        - e.g. `cordova build ios --verbose`
    - Failure to include the full console output will result in the issue being closed immediately
- If the issue relates to the plugin documentation (and not the code), please of a [documentation issue](https://github.com/dpa99c/phonegap-launch-navigator/issues/new?assignees=&labels=&template=documentation-issue.md&title=)

## Requesting a new feature
Before [opening a feature request issue](https://github.com/dpa99c/phonegap-launch-navigator/issues/new?assignees=&labels=&template=feature_request.md&title=), please do the following:
- Check the above documentation to ensure the feature you are requesting doesn't already exist
- Check the list if open/closed issues to check if there's a reason that feature hasn't been included already
- Ensure the feature you are requesting is actually possible to implement and generically useful to other users than yourself
- Where possible, post a link to the documentation related to the feature you are requesting
- Include other relevant links, e.g.
    - Stack Overflow post illustrating a solution
    - Code within another Github repo that illustrates a solution 
    

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
| Android  | Lyft                           |   X  |           |   X   |            |                |   X  |
| Android  | MAPS.ME                        |   X  |           |   X   |            |        X       |   X  |
| Android  | _Geo: URI scheme_              |   X  |     X     |       |            |                |  N/A |
| Android  | Cabify                         |   X  |     X     |   X   |      X     |                |   X  |
| Android  | Baidu Maps                     |   X  |     X<sup>[\[1\]](#apple_baidu_maps_nicknames_uri)</sup>     |   X   |      X<sup>[\[1\]](#apple_baidu_maps_nicknames_uri)</sup>     |        X       |   X  |
| Android  | 99 Taxi                        |   X  |     X     |   X   |      X     |                |   X  |
| Android  | Gaode Maps                     |   X  |     X     |   X   |      X     |        X       |   X  |
| iOS      | Apple Maps - URI scheme        |   X  |           |   X   |            |        X       |   X  |
| iOS      | Apple Maps - MapKit class      |   X  |     X     |   X   |      X     |        X       |   X  |
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
| iOS      | Lyft                           |   X  |           |   X   |            |                |   X  |
| iOS      | MAPS.ME                        |   X  |           |   X   |            |        X       |   X  |
| iOS      | Cabify                         |   X  |     X     |   X   |      X     |                |   X  |
| iOS      | Baidu Maps                     |   X  |     X<sup>[\[1\]](#apple_baidu_maps_nicknames_uri)</sup>     |   X   |      X<sup>[\[1\]](#apple_baidu_maps_nicknames_uri)</sup>     |        X       |   X  |
| iOS      | 99 Taxi                        |   X  |     X     |   X   |      X     |                |   X  |
| iOS      | Gaode Maps                     |   X  |     X     |   X   |      X     |        X       |   X  |
| Windows  | Bing Maps                      |   X  |     X     |   X   |      X     |        X       |   X  |

<a name="baidu_maps_nicknames">[1]</a>: Only supported when Start or Dest is specified as lat/lon (e.g. "50,-4")

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
| Android  | MAPS.ME                        |    X    |    X    |     X     |    X    |
| Android  | Baidu Maps                     |    X    |    X    |     X     |    X    |
| Android  | Gaode Maps                     |    X    |    X    |     X     |    X    |
| iOS      | Apple Maps                     |    X    |    X    |           |         |
| iOS      | Google Maps                    |    X    |    X    |     X     |    X    |
| iOS      | Sygic                          |    X    |    X    |           |         |
| iOS      | MAPS.ME                        |    X    |    X    |     X     |    X    |
| iOS      | Baidu Maps                     |    X    |    X    |     X     |    X    |
| iOS      | Gaode Maps                     |    X    |    X    |     X     |    X    |
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
- `launchnavigator.APP.LYFT` (Android & iOS)
- `launchnavigator.APP.MAPS_ME` (Android & iOS)
- `launchnavigator.APP.CABIFY` (Android & iOS)
- `launchnavigator.APP.BAIDU` (Android & iOS)
- `launchnavigator.APP.TAXIS_99` (Android & iOS)
- `launchnavigator.APP.GAODE` (Android & iOS)

### APP_NAMES

Display names for supported apps, referenced by `launchnavigator.APP`.

e.g. `launchnavigator.APP_NAMES[launchnavigator.APP.GOOGLE_MAPS] == "Google Maps"`
x
### TRANSPORT_MODE

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
    - {boolean} enableGeocoding - (Android and iOS only) if true, and input location type(s) doesn't match those required by the app, use geocoding to obtain the address/coords as required. Defaults to true.
    - {boolean} enableGeolocation - (Windows only) if false, the plugin will NOT attempt to use the geolocation plugin to determine the current device position when the start location parameter is omitted. Defaults to true. 
    - {object} extras - a key/value map of extra app-specific parameters. For example, to tell Google Maps on Android to display Satellite view in "maps" launch mode: `{"t": "k"}`
        - These will be appended to the URL used to invoke the app, e.g. `google_maps://?t=k&...`
        - See [Supported app URL scheme documentation wiki page](https://github.com/dpa99c/phonegap-launch-navigator/wiki/Supported-app-URL-scheme-documentation) for links to find app-specific parameters.
    - {string} launchModeGoogleMaps - (Android only) mode in which to open Google Maps app
        - `launchnavigator.LAUNCH_MODE.MAPS` or `launchnavigator.LAUNCH_MODE.TURN_BY_TURN`
        - Defaults to `launchnavigator.LAUNCH_MODE.MAPS` if not specified.
    - {string} launchModeAppleMaps - (iOS only) method to use to open Apple Maps app
            - `launchnavigator.LAUNCH_MODE.URI_SCHEME` or `launchnavigator.LAUNCH_MODE.MAPKIT`
            - Defaults to `launchnavigator.LAUNCH_MODE.URI_SCHEME` if not specified.
    - {object} - appSelection - options related to the default native actionsheet picker which enables user to select which navigation app to launch if `app` is not specified.
        - {string} dialogHeaderText - text to display in the native picker body, above the app buttons.
            - Defaults to "Select app for navigation" if not specified.
        - {string} cancelButtonText - text to display for the cancel button.
            - Defaults to "Cancel" if not specified.
        - {number} dialogPositionX - [iPad only] x position for the dialog
            - Defaults to 550 if not specified
        - {number} dialogPositionY - [iPad only] y position for the dialog
            - Defaults to 500 if not specified
        - {array} list - list of apps, defined as `launchnavigator.APP` constants, which should be displayed in the picker if the app is available.
        This can be used to restrict which apps are displayed, even if they are installed. By default, all available apps will be displayed.
        - {function} callback - a callback to invoke when the user selects an app in the native picker.
            - A single string argument is passed which is the app what was selected defined as a `launchnavigator.APP` constant.
        - {integer} androidTheme - (Android only) native picker theme. Specify using `actionsheet.ANDROID_THEMES` constants. Default `actionsheet.ANDROID_THEMES.THEME_HOLO_LIGHT`
        - {object} - rememberChoice - options related to whether to remember user choice of app for next time, instead of asking again for user choice.
            - {string/boolean} enabled - whether to remember user choice of app for next time, instead of asking again for user choice.
                - `"prompt"` - Prompt user to decide whether to remember choice.
                    - Default value if unspecified.
                    - If `promptFn` is defined, this will be used for user confirmation.
                    - Otherwise (by default), a native dialog will be displayed to ask user.
                - `false` - Do not remember user choice.
                - `true` - Remember user choice.
            - {function} promptFn - a function which asks the user whether to remember their choice of app.
                - If this is defined, then the default dialog prompt will not be shown, allowing for a custom UI for asking the user.
                - This will be passed a callback function which should be invoked with a single boolean argument which indicates the user's decision to remember their choice.
            - {object} - prompt - options related to the default dialog prompt used to ask the user whether to remember their choice of app.
                - {function} callback - a function to pass the user's decision whether to remember their choice of app.
                    - This will be passed a single boolean value indicating the user's decision.
                - {string} headerText - text to display in the native prompt header asking user whether to remember their choice.
                    - Defaults to "Remember your choice?" if not specified.
                - {string} bodyText - text to display in the native prompt body asking user whether to remember their choice.
                    - Defaults to "Use the same app for navigating next time?" if not specified.
                - {string} yesButtonText - text to display for the Yes button.
                    - Defaults to "Yes" if not specified.
                - {string} noButtonText - text to display for the No button.
                    - Defaults to "No" if not specified.

### enableDebug()

Enables debug log output from the plugin to the JS and native consoles. By default debug is disabled.

    launchnavigator.enableDebug(true, success, error);

#### Parameters
- {boolean} enabled - Whether to enable debug.
- {function} success - callback to invoke on successfully setting debug.
- {function} error - callback to invoke on error while setting debug. Will be passed a single string argument containing the error message.

### isAppAvailable()

Determines if the given app is installed and available on the current device.

    launchnavigator.isAppAvailable(appName, function(isAvailable){
        console.log(appName + " is available: " + isAvaialble);
    }, error);

#### Parameters
- {string} appName - name of the app to check availability for. Define as a constant using `ln.APP`.
- {function} success - callback to invoke on successful determination of availability. Will be passed a single boolean argument indicating the availability of the app.
- {function} error - callback to invoke on error while determining availability. Will be passed a single string argument containing the error message.


### availableApps()

Returns a list indicating which apps are installed and available on the current device.

    launchnavigator.availableApps(function(apps){
        apps.forEach(function(app){
            console.log(app + " is available");
        });
    }, error);

#### Parameters
- {function} success - callback to invoke on successful determination of availability. Will be passed a key/value object where the key is the app name and the value is a boolean indicating whether the app is available.
- {function} error - callback to invoke on error while determining availability. Will be passed a single string argument containing the error message.



### getAppDisplayName()

Returns the display name of the specified app.

    let displayName = launchnavigator.getAppDisplayName(app);

#### Parameters
- {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`. whether the app is available.
- returns {string} - app display name. e.g. "Google Maps".



### getAppsForPlatform()

Returns list of supported apps on a given platform.

    let apps = launchnavigator.getAppsForPlatform(platform);

#### Parameters
- {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
- returns {array} of {string} - apps supported on specified platform as a list of `launchnavigator.APP` constants.


### supportsTransportMode()

Indicates if an app on a given platform supports specification of transport mode.

    let transportModeIsSupported = launchnavigator.supportsTransportMode(app, platform, launchMode);

#### Parameters
- {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
- {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
- {string} launchMode (optional) - Android only. Only applies to Google Maps on Android. Specified as a constant in `launchnavigator.LAUNCH_MODE`. e.g. `launchnavigator.LAUNCH_MODE.MAPS`.
- returns {boolean} - true if app/platform combination supports specification of transport mode.



### getTransportModes()

Returns the list of transport modes supported by an app on a given platform.

    let modes = launchnavigator.getTransportModes(app, platform, launchMode);

#### Parameters
- {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
- {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
- {string} launchMode (optional) - Only applies to Google Maps on Android. Specified as a constant in `launchnavigator.LAUNCH_MODE`. e.g. `launchnavigator.LAUNCH_MODE.MAPS`.
- returns {array} of {string} - list of transports modes as constants in `launchnavigator.TRANSPORT_MODE`.
If app/platform combination doesn't support specification of transport mode, the list will be empty;



### supportsDestName()

Indicates if an app on a given platform supports specification of a custom nickname for destination location.

    let destNameIsSupported = launchnavigator.supportsDestName(app, platform, launchMode);

#### Parameters
- {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
- {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
- {string} launchMode (optional) - Only applies to Google Maps on Android and Apple Maps on iOS. Specified as a constant in `launchnavigator.LAUNCH_MODE`. e.g. `launchnavigator.LAUNCH_MODE.MAPS`.
- returns {boolean} - true if app/platform combination supports specification of a custom nickname for destination location.


### supportsStart()

Indicates if an app on a given platform supports specification of start location.

    let startIsSupported =  launchnavigator.supportsStart(app, platform, launchMode);

#### Parameters
- {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
- {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
- {string} launchMode (optional) - Only applies to Google Maps on Android. Specified as a constant in `launchnavigator.LAUNCH_MODE`. e.g. `launchnavigator.LAUNCH_MODE.MAPS`.
- returns {boolean} - true if app/platform combination supports specification of start location.


### supportsStartName()

Indicates if an app on a given platform supports specification of a custom nickname for start location.

    let startNameIsSupported = launchnavigator.supportsStartName(app, platform);

#### Parameters
- {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
- {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
- {string} launchMode (optional) - Only applies to Apple Maps on iOS. Specified as a constant in `launchnavigator.LAUNCH_MODE`. e.g. `launchnavigator.LAUNCH_MODE.MAPKIT`.
- returns {boolean} - {boolean} - true if app/platform combination supports specification of a custom nickname for start location.



### supportsLaunchMode()

Indicates if an app on a given platform supports specification of launch mode.
- Currently only Google Maps on Android and Apple Maps on iOS does.

    let launchModeIsSupported = launchnavigator.supportsLaunchMode(app, platform);

#### Parameters
- {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
- {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.ANDROID`.
- returns {boolean} - true if app/platform combination supports specification of transport mode.

### appSelection.userChoice.exists()
Indicates whether a user choice exists for a preferred navigator app.

    launchnavigator.appSelection.userChoice.exists(function(exists){
        console.log("User preference of app: " + (exists ? "exists" : "doesn't exist"));
    });
#### Parameters
- [function} cb - function to pass result to: will receive a boolean argument.

### appSelection.userChoice.get()
Returns current user choice of preferred navigator app.

    launchnavigator.appSelection.userChoice.get(function(app){
        console.log("User preferred app is: " + launchnavigator.getAppDisplayName(app));
    });
#### Parameters
- [function} cb - function to pass result to: will receive a string argument indicating the app, which is a constant in `launchnavigator.APP`.

### appSelection.userChoice.set()
Sets the current user choice of preferred navigator app.

    launchnavigator.appSelection.userChoice.set(launchnavigator.APP.GOOGLE_MAPS, function(){
        console.log("User preferred app is set");
    });
#### Parameters
- {string} app - app to set as preferred choice as a constant in `launchnavigator.APP`.
- [function} cb - function to call once operation is complete.

### appSelection.userChoice.clear()
Clears the current user choice of preferred navigator app.

    launchnavigator.appSelection.userChoice.clear(function(){
        console.log("User preferred app is cleared");
    });
#### Parameters
- [function} cb - function to call once operation is complete.

### appSelection.userPrompted.get()
Indicates whether user has already been prompted whether to remember their choice a preferred navigator app.

    launchnavigator.appSelection.userPrompted.get(function(alreadyPrompted){
        console.log("User " + (alreadyPrompted ? "has" : "hasn't") + " already been asked whether to remember their choice of navigator app");
    });
#### Parameters
- [function} cb - function to pass result to: will receive a boolean argument.

### appSelection.userPrompted.set()
Sets flag indicating user has already been prompted whether to remember their choice a preferred navigator app.

    launchnavigator.appSelection.userPrompted.set(function(){
        console.log("Flag set to indicate user chose to remember their choice of navigator app");
    });
#### Parameters
- [function} cb - function to call once operation is complete.

### appSelection.userPrompted.clear()
Clears flag which indicates if user has already been prompted whether to remember their choice a preferred navigator app.

    launchnavigator.appSelection.userPrompted.clear(function(){
        console.log("Clear flag indicating whether user chose to remember their choice of navigator app");
    });
#### Parameters
- [function} cb - function to call once operation is complete.

### setApiKey()
Sets the [Google API key for Android](#google-api-key-for-android).
Note: This function is also available on iOS but it does nothing. This is to keep the interface consistent between the platforms.

    launchnavigator.setApiKey(api_key, success, error);
#### Parameters
- {String} apiKey - Google API Key.
- {function} success - callback to invoke on successfully setting api key.
- {function} error - callback to invoke on error while setting api key. Will be passed a single string argument containing the error message.

# Example projects

There are several example projects in the [example repo](https://github.com/dpa99c/phonegap-launch-navigator-example) which illustrate usage of this plugin:
- SimpleExample - illustrates basic usage of the plugin
- AdvancedExample - illustrates advanced usage of the plugin
- IonicExample - illustrates usage of the plugin with the Ionic v1 framework
- Ionic2Example - illustrates usage of the plugin with the Ionic v2 framework and Ionic Native plugin wrapper.

# Platform-specifics

## Android

- Running on Android, in addition to discovering which explicitly supported apps are installed, the plugin will also detect which installed apps support using the `geo:` URI scheme for use in navigation. These are returned in the list of available apps.

- By specifying the `app` option as `launchnavigator.APP.GEO`, the plugin will invoke a native Android chooser, to allow the user to select an app which supports the `geo:` URI scheme for navigation.

- Google Maps on Android can be launched in 3 launch modes:
    - Maps mode (`launchnavigator.LAUNCH_MODE.MAPS`) - launches in Map view. Enables start location to be specified, but not transport mode or destination name.
    - Turn-by-turn mode (`launchnavigator.LAUNCH_MODE.TURN_BY_TURN`) - launches in Navigation view. Enables transport mode to be specified, but not start location or destination name.
    - Geo mode (`launchnavigator.LAUNCH_MODE.GEO`) - invokes Navigation view via `geo`: URI scheme. Enables destination name to be specified, but not start location or transport mode.
    - Launch mode can be specified via the `launchModeGoogleMaps` option, but defaults to Maps mode if not specified.


## Windows

- The plugin is compatible with Windows 10 on any PC or Windows 10 Mobile on a phone/tablet using the Universal .Net project generated by Cordova: `cordova platform add windows`

- Bing Maps requires the user to press the enter key to initiate navigation if you don't provide the start location.
Therefore, if a start location is not going to be passed to the plugin from your app, you should install the [Geolocation plugin](https://github.com/apache/cordova-plugin-geolocation) into your project.
By default, if the geolocation plugin is detected, the plugin will attempt to retrieve the current device location using it, and use this as the start location.
This can be disabled via the `enableGeolocation` option.


## iOS

### "Removing" Apple Maps
  - Since iOS 10, it is possible to "remove" built-in Apple apps, including Maps, from the Home screen.
  - Not that removing is not the same as uninstalling - the app is still actually present on the device, just the icon is removed from the Home screen.
  - Therefore it's not possible detect if Apple Maps is unavailable - `launchnavigator.availableApps()` will always report it as present.
  - The best that can be done is to gracefully handle the error when attempting to open Apple Maps using `launchnavigator.navigate()`
  - For reference, see [this SO question](http://stackoverflow.com/questions/39603120/how-to-check-if-apple-maps-is-installed) and the [Apple documentation](https://support.apple.com/en-gb/HT204221).
  
### Apple Maps launch method

This plugin supports 2 different launch methods for launching the Apple Maps app on iOS.

- Specified by passing the `launchModeAppleMaps` option as a `launchnavigator.LAUNCH_MODE` constant to `navigate()`
    - `launchnavigator.LAUNCH_MODE.URI_SCHEME`: use the URI scheme launch method. Default if not specified.
    - `launchnavigator.LAUNCH_MODE.MAPKIT`: use the MapKit class launch method.

#### URI scheme launch method
- Launches the app using the [Apple Maps URI scheme](https://developer.apple.com/library/content/featuredarticles/iPhoneURLScheme_Reference/MapLinks/MapLinks.html)
- The default method used by the plugin.
- Supports input location types of both coordinates and address string without requiring remote geocoding service (works offline)
- Doesn't support specifying nicknames for start/destination locations.

#### MapKit class launch method
- Launches the app using the [MapKit class](https://developer.apple.com/documentation/mapkit/mkmapitem/1452207-openmapswithitems?language=objc) to launch Apple Maps
- Only supports input location type of coordinates without requiring remote geocoding service (works offline)
- An input location type of an address (formatted as a single string) requires use of remote geocoding service (requires internet connection)
    - MapKit class input requires an address which is formatted as an [address dictionary](https://developer.apple.com/documentation/contacts/cnpostaladdress), in which the address is split into known fields such as street, city and state.  
- Support specifying nicknames for start/destination locations.
- Provides [additional launch options](https://developer.apple.com/documentation/mapkit/mkmapitem/launch_options_dictionary_keys?language=objc) which are not available via the URI scheme launch method.

# App-specifics

## Lyft

On both Android and iOS, the "ride type" will default to "Lyft" unless otherwise specified in the `extras` list as `id`. 

See the [Lyft documentation](https://developer.lyft.com/v1/docs/deeplinking) for URL scheme details and other supported ride types.

## 99 Taxi
On both Android and iOS, the extra parameters `client_id` and `deep_link_product_id` are required by 99 Taxi

- `client_id` should follow the pattern `MAP_***` where `***` is the client name given by the 99 Team.
    - If not specified defaults to `client_id=MAP_123`
- `deep_link_product_id` identifies the ride category
    - Currently supported values are:
        - `316` - POP ride
        - `326` - TOP ride
        - `327` - Taxis ride
    - If not specified defaults to `deep_link_product_id=316`     

On Android, 99 Taxi is currently the only app where `options.start` is a **required** parameter when calling `navigate()`
- If `navigate()` is called without a start location and the selected app is 99 Taxi, the error callback will be invoked and the 99 Taxi app will not be launched
- In order for this plugin to automatically provide start location to 99 Taxi (if it's not already specified), the native Android implementation needs to be enhanced to:
    - check/request runtime permission to use location
    - add the necessary permission entries to the `AndroidManifest.xml`
    - check/request high accuracy location is enabled (no point in requesting a low-accuracy city-level position if you want a pickup at your exact current address)
    - request a high accuracy position to determine the user's current location
    - handle errors cases such as:
        - User denies location permission
        - User denies high accuracy mode permission
        - Location cannot be retrieved
- Currently, I don't have time to do all of the above just for the case of 99 Taxi
    - However I'm willing to accept a PR request which implements the necessary native Android features.
- Otherwise/until then, you'll need to manually specify the start location for 99 Taxi
    - If the current user location is required, you can use `cordova-plugin-geolocation` to find this.


# Credits

Thanks to:

- [opadro](https://github.com/opadro) for Windows implementation
- [Eddy Verbruggen](https://github.com/EddyVerbruggen) for [cordova-plugin-actionsheet](https://github.com/EddyVerbruggen/cordova-plugin-actionsheet)

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
