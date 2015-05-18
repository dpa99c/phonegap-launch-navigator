/*
 * Copyright (c) 2015 Dave Alden  (http://github.com/dpa99c)
 * Copyright (c) 2015 Oscar A. Padró (https://github.com/opadro)
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
 */
launchnavigator.navigate = function(destination, start, successCallback, errorCallback) {
    var url ="bingmaps:?rtp=";
    if(start){
        if(typeof(start) == "object"){
            url += "pos." + start[0] + "_" + start[1];
        }else{
            url += "adr." + start;
        }
    }

    url += "~";
    if(typeof(destination) == "object"){
        url += "pos." + destination[0] + "_" + destination[1];
    }else{
        url += "adr." + destination;
    }

    try{
        window.location = url;
        if(successCallback) successCallback();
    }catch(e){
        if(errorCallback) errorCallback(e);
    }

};


/**
 * Opens navigator app to navigate to given lat/lon destination
 *
 * @param {Number} lat - destination latitude as decimal number
 * @param {Number} lon - destination longitude as decimal number 
 * @param {Function} successCallback - The callback which will be called when plugin call is successful.
 * @param {Function} errorCallback - The callback which will be called when plugin encounters an error.
 * @param {Number} lat_start - start latitude as decimal number
 * @param {Number} lon_start - start longitude as decimal number 
 * This callback function have a string param with the error.     
 */
launchnavigator.navigateByLatLon = function (lat, lon, successCallback, errorCallback, lat_start, lon_start) {
    if(typeof(console) != "undefined") console.warn("launchnavigator.navigateByLatLon() has been deprecated and will be removed in a future version of this plugin. Please use launchnavigator.navigate()");
    successCallback();
    var start = "";
    if (lat_start && lon_start)
        start = "pos." + lat_start + "_" + lon_start;

    window.open("bingmaps:?rtp=" + start + "~pos." + lat + "_" + lon);
};

/**
 * Opens navigator app to navigate to given place name destination
 *
 * @param {String} name - place name to navigate to
 * @param {Function} successCallback - The callback which will be called when plugin call is successful.
 * @param {Function} errorCallback - The callback which will be called when plugin encounters an error.
 * @param {Number} lat_start - start latitude as decimal number
 * @param {Number} lon_start - start longitude as decimal number 
 * This callback function have a string param with the error.     
 */
launchnavigator.navigateByPlaceName = function (name, successCallback, errorCallback, lat_start, lon_start) {
    if(typeof(console) != "undefined") console.warn("launchnavigator.navigateByPlaceName() has been deprecated and will be removed in a future version of this plugin. Please use launchnavigator.navigate()");
    successCallback();

    var start = "";
    if (lat_start && lon_start)
        start = "pos." + lat_start + "_" + lon_start;

    window.location = "bingmaps:?rtp=" + start + "~adr." + name;
};
module.exports = launchnavigator;