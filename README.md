PhoneGap Native Navigation Plugin
=================================

This PhoneGap Native Navigation Plugin provides a mechanism to launch the Google Navigator app on Android to get driving directions to a desired location. 
An iOS version of this plugin is unnecessary as the native navigation app on iOS 5.x and 6 can be launched using pure Javascript using the *magic* "maps" protocol (see below).


## Contents

* [Installing](#installing)
* [Using the Android plugin](#using-the-android-plugin)
* [Opening the native navigation app on iOS](#opening-the-native-navigation-app-on-ios)
* [Repository contents](#repository-contents)
* [License](#license)
 
# Installing

Phonegap 2.5.0 to 2.9.0 is required. The plugin has not yet been updated to work with Phonegap 3.0.0

## Java source code

Get the latest source code

    $ git clone https://github.com/dpa99c/phonegap-native-navigation.git

Copy the Java source files from src/android/src/ of phonegap-native-navigation project into the source directory of your Android project.

    $ cp -R phonegap-native-navigation/src/android/src/ $YOUR_PROJECT/src
    
For windows use xcopy

    c:\> xcopy phonegap-native-navigation\src\android\src %YOUR_PROJECT%\src /S

## config.xml 

Add the PhoneNavigator feature in res/xml/config.xml

### Phonegap 2.5.0 to 2.7.0
    <plugin name="PhoneNavigator" value="org.apache.cordova.plugin.PhoneNavigator"/>

### Phonegap 2.8.0 to 2.9.0    
    <feature name="PhoneNavigator">
          <param name="android-package" value="org.apache.cordova.plugin.PhoneNavigator" />
    </feature>
    
## JavaScript 

Copy phonenavigator.js into assets/www/js/

     $ cp phonegap-native-navigation/www/phonenavigator.js $YOUR_PROJECT/assets/www/js/
     
Windows     
     
     $ copy phonegap-native-navigation\www\phonenavigator.js %YOUR_PROJECT%\assets\www\js\
    
Include phonenavigator.js in index.html.  Ensure that phonenavigator.js is *after* cordova.js

    <script type="text/javascript" src="js/phonenavigator.js"></script>        


# Using the Android plugin

> The plugin has a single method, "doNavigate", which launches Google Navigator with the location specified by the supplied latitude and longitude as the destination. The method is called with the following signature:
    
    cordova.require('cordova/plugin/phonenavigator').doNavigate(lat, lon, successFn, errorFn);

## Parameters

- lat: destintation latitude as decimal number, e.g. "5.0349984534"
- lon: destintation longitude as decimal number, e.g. "-4.56463326"
- successFn: (Optional) The callback which will be called when plugin the call is successful.
- errorFn: (Optional) The callback which will be called when plugin encounters an error. This callback function will be passed an error message string as the first parameter.


# Opening the native navigation app on iOS

There's no need for an iOS version of this plugin because the native navigation app on iOS 5.x and 6 can be launched using pure Javascript using the *magic* "maps" protocol:

    window.location = "maps:daddr="+lat+","+lon;

On iOS 5.x this will launch Google Maps and on iOS 6 this will launch Apple Maps.

## Detecting the platform

You can detect whether the app is running on Android or iOS and therefore whether the plugin is required using the PhoneGap Device API:

    if(device.platform == "Android"){
        cordova.require('cordova/plugin/phonenavigator').doNavigate(lat, lon, successFn, errorFn);
    }else if(device.platform == "iOS"){
        window.location = "maps:daddr="+lat+","+lon;
    }else{
        console.error("Unknown platform");
    }

# Repository contents

The contents of this Git repository:

* [example/](example) - Eclipse test project illustrating how to use the plugin and the resulting compiled APK.
* [src/](src) - Java source code for the plugin
* [www/](www) - Javascript source for the plugin
* [README.md/](README.md) - the file you are currently reading

License
================

The MIT License

Copyright (c) 2013 Working Edge Ltd.

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
