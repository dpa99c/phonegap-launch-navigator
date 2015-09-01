/*
 * Copyright (c) 2015 Dave Alden  (http://github.com/dpa99c)
 * Copyright (c) 2015 Oscar A. Padr� (https://github.com/opadro)
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *  
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *  
 */

var launchnavigator = {};

/**
 * Opens navigator app to navigate to given destination, specified by either place name or lat/lon.
 * If a start location is not also specified, current location will be used for the start. User will be requried to hit the enter key.
 *
 * @param {Mixed} destination (required) - destination location to use for navigation.
 * Either:
 * - a {String} containing the place name. e.g. "London"
 * - an {Array}, where the first element is the latitude and the second element is a longitude, as decimal numbers. e.g. [50.1, -4.0]
 * @param {Mixed} start (optional) - start location to use for navigation. If not specified, the current location of the device will be used.
 * Either:
 * - a {String} containing the place name. e.g. "London"
 * - an {Array}, where the first element is the latitude and the second element is a longitude, as decimal numbers. e.g. [50.1, -4.0]
 * @param {Function} successCallback (optional) - A callback which will be called when plugin call is successful.
 * @param {Function} errorCallback (optional) - A callback which will be called when plugin encounters an error.
 * This callback function have a string param with the error.
 * @param {Object} options (optional) - platform-specific options:
 * {String} transportMode - transportation mode for navigation: "driving", "walking" or "transit". Defaults to "driving" if not specified.
 * {Boolean} disableAutoGeolocation - if TRUE, the plugin will NOT attempt to use the geolocation plugin to determine the current device position when the start location parameter is omitted. Defaults to FALSE.
 */
launchnavigator.navigate = function(destination, start, successCallback, errorCallback, options) {
    options = options ? options : {};
    var url ="bingmaps:?rtp=";

    function doNavigate(url){
        url += "~";
        if(typeof(destination) == "object"){
            url += "pos." + destination[0] + "_" + destination[1];
        }else{
            url += "adr." + destination;
        }

        if(options.transportMode){
            url += "&mode=" + options.transportMode.charAt(0);
        }

        try{
            window.location = url;
            if(successCallback) successCallback();
        }catch(e){
            if(errorCallback) errorCallback(e);
        }
    }

    if(start){
        if(typeof(start) == "object"){
            url += "pos." + start[0] + "_" + start[1];
            doNavigate(url);
        }else{
            url += "adr." + start;
            doNavigate(url);
        }
    }else if(!options.disableAutoGeolocation && navigator.geolocation){ // if cordova-plugin-geolocation is available/enabled
        navigator.geolocation.getCurrentPosition(function(position){ // attempt to use current location as start position
            url += "pos." + position.coords.latitude + "_" + position.coords.longitude;
            doNavigate(url);
        },function(error){
            doNavigate(url);
        },{
            maxAge: 60000,
            timeout: 500
        });
    }else{
        doNavigate(url);
    }
};

module.exports = launchnavigator;