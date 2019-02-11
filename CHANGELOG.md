# CHANGELOG

## 5.0.4
- Fix checks for empty strings/objects on iOS. Resolves [#216](https://github.com/dpa99c/phonegap-launch-navigator/issues/216).
- Make LNEmptyCoord static to scope it to the class. Prevents build failures with `cordova-ios@5`. Fixes [#217](https://github.com/dpa99c/phonegap-launch-navigator/issues/217).

## 5.0.3
- Pass `navigate=yes` to Waze on Android if input type is an address. Resolves [#213](https://github.com/dpa99c/phonegap-launch-navigator/issues/213).
- Pass the now required FLAG_ACTIVITY_NEW_TASK intent flag on Android. Fixes [#215](https://github.com/dpa99c/phonegap-launch-navigator/issues/215).

## 5.0.2
- Pin default OKHTTP version to 3.12.0 since 3.13.0 contains JDK 1.8 bytecode with which cordova-android@7.1.4 (and below) is incompatible so it causes build failure.

## 5.0.1
- Fix check for empty extras parameter on iOS. Fixes [#212](https://github.com/dpa99c/phonegap-launch-navigator/issues/212).

## 5.0.0
- Major rework of native code to separate Cordova plugin layer from native launch navigator functionality.
- Remove obsolete Windows Phone 8 platform.
- Add explicit `enableDebug()` API function to enable debug output.
- Add plugin variable to enable specification of Google API key for geocoding on Android. Fixes [#211](https://github.com/dpa99c/phonegap-launch-navigator/issues/211).

## v4.2.2
- Add minimum version restriction on Cordova CLI and platforms to prevent anomalous issues being reported due to outdated Cordova environment.

## v4.2.1
- Enable override of default OKHTTP version to prevent Gradle version collisions. Fixes [#193](https://github.com/dpa99c/phonegap-launch-navigator/issues/193).

## v4.2.0
- Make iOS call success/error callbacks wait for and return the result of calling openURL(), rather than just assuming success immediately.
- Add Gaode (Amap) support for Android and iOS. Resolves [#185](https://github.com/dpa99c/phonegap-launch-navigator/issues/185).
- Fix launching of Citymapper (both Android & iOS) by geocoding address input since native address input is no longer working in recent versions of Citymapper. Fixes [#180](https://github.com/dpa99c/phonegap-launch-navigator/issues/180). 
- Fix Cabify iOS URL Scheme. See [#155](https://github.com/dpa99c/phonegap-launch-navigator/issues/155).
- Replace deprecated iOS function `stringByAddingPercentEscapesUsingEncoding` with `stringByAddingPercentEncodingWithAllowedCharacters`.
- Add 99 Taxi support for Android and iOS. Resolves [#183](https://github.com/dpa99c/phonegap-launch-navigator/issues/183).

## v4.1.5
- Detect and handle disabled Android apps (i.e. Google Maps). Resolves [#182](https://github.com/dpa99c/phonegap-launch-navigator/issues/182).

## v4.1.4
- Add missing app name native iOS constants for Cabify and Baidu.

## v4.1.3
- Declare Objective-C constants as static. See [cordova-diagnostic-plugin#270](https://github.com/dpa99c/cordova-diagnostic-plugin/issues/270).

## v4.1.2
- Unpin version on cordova-plugin-dialogs dependency for Android (as well as iOS). Resolves [#176](https://github.com/dpa99c/phonegap-launch-navigator/issues/176) (and [#175](https://github.com/dpa99c/phonegap-launch-navigator/issues/175)).

## v4.1.1
- (Re-)open the database for storing user prefs every time before reading/writing. Resolves [#173](https://github.com/dpa99c/phonegap-launch-navigator/issues/173).
- Add extra checks for input types. Should resolve [#172](https://github.com/dpa99c/phonegap-launch-navigator/issues/172).
- Unpin version on cordova-plugin-dialogs dependency. Resolves [#175](https://github.com/dpa99c/phonegap-launch-navigator/issues/175).

## v4.1.0
- Add Baidu Maps support for Android and iOS.
- Fix "bicycling" transport mode parameter for MAPS.ME on iOS.

## v4.0.7
- Namespace kReachabilityChangedNotification to avoid collisions with other libraries. Fixes [#162](https://github.com/dpa99c/phonegap-launch-navigator/issues/162).

## v4.0.6
- Adds Cabify support for Android and iOS.

## v4.0.5 
- Handle edge case where preferred app is no longer available on device. Fixes [#153](https://github.com/dpa99c/phonegap-launch-navigator/issues/153).
- Namespace the Reachability class to avoid collisions with other libraries. Fixes [#152](https://github.com/dpa99c/phonegap-launch-navigator/issues/152).

## v4.0.4
- Fix Ionic Native navigate() options mapping in v4. Fixes [#145](https://github.com/dpa99c/phonegap-launch-navigator/issues/145).

## v4.0.3
- Replace bundled localforage with version that contains polyfill for promises API. Resolves [#142](https://github.com/dpa99c/phonegap-launch-navigator/issues/142).

## v4.0.2
- Fix infinite recursion when calling navigate() if only 1 supported app is present. Resolves [#141](https://github.com/dpa99c/phonegap-launch-navigator/issues/141).

## v4.0.1
- Fix references to enableGelocation vs enableGeocoding

## v4.0.0
- Add Lyft support to Android and iOS. Resolves [#130](https://github.com/dpa99c/phonegap-launch-navigator/issues/130)
- Add CHANGELOG
- Add empty LAUNCH_MODE object to iOS and Windows JS layers for cross-platform compatibility. Resolves [#133](https://github.com/dpa99c/phonegap-launch-navigator/issues/133).
- Rework Andoid and iOS implementations to: 
  - Only geocode when necessary, therefore support offline launching of apps if correct input location format supplied. Resolves [#93](https://github.com/dpa99c/phonegap-launch-navigator/issues/93).
  - Support optional disabling of geocoding.
- Fix Typescript declaration type.
- Windows: Rename options parameter from "disableAutoGeolocation" to "enableGeolocation" for consistency with Android and iOS.
- Remove backward compatibility shim for legacy v2 API.
- Update Typescript types for v4.
- Support specification of launch mode for Apple Maps on iOS: URI scheme or MapKit class. 
Default to URI scheme method in order to support address location types without geocoding.  Resolves [#106](https://github.com/dpa99c/phonegap-launch-navigator/issues/106) and [#107](https://github.com/dpa99c/phonegap-launch-navigator/issues/107).
- Rework app selection options structure for navigate().
NOTE: this is backwardly incompatible with v3 and below.
- Add mechanism to optionally offer to remember user choice of app.

## v3.2.2
- Rename typing files to use NPM package names instead of Github repo names.
- Reference type declaration in package.json.
- Add androidTheme option
- Document removing Apple Maps caveat. Resolves [#116](https://github.com/dpa99c/phonegap-launch-navigator/issues/116).

## v3.2.1
Add support for a callback to which the selected app in the native picker can be passed.

## v3.2.0
Bug fix: On Windows, isAppAvailable() should only return true if appName == APP.BING_MAPS. Fixes [#86](https://github.com/dpa99c/phonegap-launch-navigator/issues/86).

## v3.1.2
- Windows bugfix: cannot continue navigator when you dont pass options.
- Add callbacks as parameters to userSelect.
- Add Typescript definition
- Encode pipe character in Sygic URLs. Resolves [#83](https://github.com/dpa99c/phonegap-launch-navigator/issues/83).

## v3.1.1
- Bug fix: remove anomalous module declaration from Android JS implementation. Fixes [#72](https://github.com/dpa99c/phonegap-launch-navigator/issues/72).

## v3.1.0
- Add support for Sygic, HERE Maps and Moovit apps to Android & iOS
- Add ability to customise header title and cancel button text of app selection dialog
- Enable specification of app-specific extra parameters where possible

## v3.0.4
- When reversing geocoding on iOS, use original coordinate as result rather than geocoder result. Resolves [#61](https://github.com/dpa99c/phonegap-launch-navigator/issues/61).
- Add explicit support for Yandex Navigator on Android

## v3.0.3
- Fix issue [#58](https://github.com/dpa99c/phonegap-launch-navigator/issues/58)
- Merge pull request [#61](https://github.com/dpa99c/phonegap-launch-navigator/issues/61)

## v3.0.2
- Add debug logging of geocoding operations
- Fix issues [#13](https://github.com/dpa99c/phonegap-launch-navigator/issues/13), [#59](https://github.com/dpa99c/phonegap-launch-navigator/issues/59)
- Support alternate API signature: navigate(start, successCallback, errorCallback, options) - resolves [#55](https://github.com/dpa99c/phonegap-launch-navigator/issues/55).

## v3.0.1
Replaced legacy Apache HTTP client with OkHttp client on Android to prevent Gradle build issues

## v3.0.0
Version 3 is a complete rewrite of the plugin in order to support launching of 3rd party navigation apps on Android and iOS.
The plugin API has changed significantly, but is backwardly-compatible to support the version 2 API syntax.

If for any reason you have issues with v3, the final release of v2 is on this branch: [https://github.com/dpa99c/phonegap-launch-navigator/tree/v2](https://github.com/dpa99c/phonegap-launch-navigator/tree/v2). It can be specified in a Cordova project as `uk.co.workingedge.phonegap.plugin.launchnavigator@2`.

[cordova-plugin-actionsheet](https://github.com/EddyVerbruggen/cordova-plugin-actionsheet) has been introduced as a plugin dependency.
It is used to display a native picker (both on Android and iOS) to choose which available navigation app should be used. This is used if [app](#app) is specified as `launchnavigator.APP.USER_SELECT`, which is the default in v3 if an app is not explicitly specified.

## 2.9.11
Fix iOS bugs.

## 2.9.10
Fix iOS bug where if !disableAutoGeolocation and geolocation plugin returns error or plugin is not present, app can crash due to invalid start location.
Fix Android edge case.

## 2.9.9
Fix bug in Windows platform.

## 2.9.8
Change case of Cordova package in imports.


## 2.9.7
Make plugin ID lower case - fixes [#33](https://github.com/dpa99c/phonegap-launch-navigator/issues/33).

## 2.9.6
Add iOS 9 whitelisting for Google Maps URL scheme. Fixes [#32](https://github.com/dpa99c/phonegap-launch-navigator/issues/32).

## 2.9.5
Add missing iOS dependency on CoreLocation framework: fixes [#27](https://github.com/dpa99c/phonegap-launch-navigator/issues/27).

## 2.9.3
Use reverse geocoding to display address of start/destination in Apple Maps when specified as a latitude+longitude.


## 2.9.2
Fixes [#25](https://github.com/dpa99c/phonegap-launch-navigator/issues/25) - when "preferGoogleMaps" set to true,If Google Maps is not available, it "will not" fall back to Apple Maps.


## 2.9.1
Add functionality to check if Google Maps is available on iOS device.
Remove legacy interface functions.

## 2.9.0
Rewrite iOS implementation to use native MapKit to invoke Apple Maps.

## 2.8.0
Removed dependency on geolocation plugin, as this can cause issues when other plugins have dependencies.

## 2.7.0
Added dependency on geolocation plugin, which is used to obtain starting location if not specified.

## 2.6.0
Fix iOS merge issues.

## 2.4.0
Add support for Windows Phone 8.

## 2.3.0
Add support for Google Maps to iOS

## 2.2.0
Fixes issue on iOS 8.2+ where passing null as start location causes user to be prompted to choose, instead of using current location

## 2.1.0
Support optionally specifying start location for navigation and to support both lat/lon and place names interchangeably

## 2.0.0
Updated for Cordova/Phonegap 3.x.
Add support for launching Apple Maps on iOS and Bing Maps on Windows.

## 1.0.0
Initial plugin version for Cordova/Phonegap 2.x.
Supports launching Google Maps on Android to navigate from current location to a specific destination address/coordinate.
