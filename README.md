Launch Navigator Cordova/Phonegap Plugin
=================================

This Cordova/PhoneGap Plugin provides a mechanism to launch the native navigation app on iOS (Apple Maps) and Android (Google Navigator) to get driving directions to a desired location. 

This is for Cordova/Phonegap 3+

## Contents

* [Installing](#installing)
* [Using the plugin](#using-the-plugin)
* [License](#license)
 
# Installing

## Automatically with CLI / Plugman
Launch Navigator can be installed with [Cordova Plugman](https://github.com/apache/cordova-plugman) and the [PhoneGap CLI](http://docs.phonegap.com/en/edge/guide_cli_index.md.html).

Here's how to install it with the CLI:


```
$ cordova plugin add https://github.com/dpa99c/phonegap-launch-navigator.git
OR
$ phonegap plugin add https://github.com/dpa99c/phonegap-launch-navigator.git
```


# Using the plugin

The plugin has a two methods:

## navigateByLatLon

Launches navigation app with the location specified by the supplied latitude and longitude as the destination. 

```    
launchnavigator.navigateByLatLon(lat, lon, successFn, errorFn);
```

### Parameters

- lat: destintation latitude as decimal number, e.g. 5.0349984534
- lon: destintation longitude as decimal number, e.g. -4.56463326
- successFn: (Optional) The callback which will be called when plugin the call is successful.
- errorFn: (Optional) The callback which will be called when plugin encounters an error. This callback function will be passed an error message string as the first parameter.


## navigateByPlaceName

Launches navigator app with the location specified by a placename as the destination. 

```    
launchnavigator.navigateByPlaceName(name, successFn, errorFn);
```

### Parameters

- name: destintation place name as a string, e.g. "London"
- successFn: (Optional) The callback which will be called when plugin the call is successful.
- errorFn: (Optional) The callback which will be called when plugin encounters an error. This callback function will be passed an error message string as the first parameter.


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