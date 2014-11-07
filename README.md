Launch Navigator Phonegap Plugin
=================================

This PhoneGap Plugin provides a mechanism to launch the Google Navigator app on Android to get driving directions to a desired location. 

An iOS version of this plugin is unnecessary as the Apple Maps app on iOS can be launched using pure Javascript using the *magic* "maps" protocol ([see below](#opening)).

This is for Phonegap 3.x

## Contents

* [Installing](#installing)
* [Using the Android plugin](#using)
* [Opening the native navigation app on iOS](#opening)
* [Repository contents](#repository)
* [License](#license)
 
# Installing

## Automatically with CLI / Plugman
Launch Navigator can be installed with [Cordova Plugman](https://github.com/apache/cordova-plugman) and the [PhoneGap CLI](http://docs.phonegap.com/en/edge/guide_cli_index.md.html).

Here's how to install it with the CLI:


```
$ phonegap plugin add https://github.com/dpa99c/phonegap-launch-navigator.git
```

## Manually


1\. Get the source code
```
$ git clone https://github.com/dpa99c/phonegap-launch-navigator.git

```

2\. Add the feature to your `config.xml` in your project root directory:
```xml
<feature name="LaunchNavigator">
  <param name="android-package" value="uk.co.workingedge.phonegap.plugin.LaunchNavigator" />
</feature>
```

3\. Copy the Java source file from `src/android/uk/co/workingedge/phonegap/plugin/LaunchNavigator.java` into the Android source directory of your project 
    
    e.g. `$YOUR_PROJECT/platforms/android/src/uk/co/workingedge/phonegap/plugin/LaunchNavigator.java` (create the folders)
 

4\. Copy `www/launchnavigator.js` into your root www folder
    
    e.g. `$YOUR_PROJECT/www.launchnavigator.js`

    
5\. Include launchnavigator.js in index.html.  Ensure that launchnavigator.js is *after* cordova.js
```
<script type="text/javascript" src="launchnavigator.js"></script>        
```

# Using the plugin

The plugin has a two methods:

## navigateByLatLon

Launches Google Navigator with the location specified by the supplied latitude and longitude as the destination. 

```    
launchnavigator.navigateByLatLon(lat, lon, successFn, errorFn);
```

### Parameters

- lat: destintation latitude as decimal number, e.g. 5.0349984534
- lon: destintation longitude as decimal number, e.g. -4.56463326
- successFn: (Optional) The callback which will be called when plugin the call is successful.
- errorFn: (Optional) The callback which will be called when plugin encounters an error. This callback function will be passed an error message string as the first parameter.


## navigateByPlaceName

Launches Google Navigator with the location specified by a placename as the destination. 

```    
launchnavigator.navigateByPlaceName(name, successFn, errorFn);
```

### Parameters

- name: destintation place name as a string, e.g. "London"
- successFn: (Optional) The callback which will be called when plugin the call is successful.
- errorFn: (Optional) The callback which will be called when plugin encounters an error. This callback function will be passed an error message string as the first parameter.


# Opening the native navigation app on iOS

There's no need for an iOS version of this plugin because the native navigation app on iOS can be launched using pure Javascript using the *magic* "maps" protocol:

## Navigate to latitude/longitude
```
window.location = "maps:daddr=5.0349984534,-4.56463326";
```

## Navigate to place name
```
window.location = "maps:q=London";
```

# Detecting the platform

You can detect whether the app is running on Android or iOS and therefore whether the plugin is required using the [device plugin](https://github.com/apache/cordova-plugin-device/blob/master/doc/index.md):

    if(device.platform == "Android"){
	  launchnavigator.navigateByLatLon(lat, lon, successFn, errorFn);
    }else if(device.platform == "iOS"){
	  window.location = "maps:daddr="+lat+","+lon;
    }else{
	  console.error("Unknown platform");
    }


License
================

The MIT License

Copyright (c) 2014 Working Edge Ltd.

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