/*
 * Copyright (c) 2015 opadro  (https://github.com/opadro)
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
 * Opens navigator app to navigate to given lat/lon destination
 *
 * @param {Number} lat - destintation latitude as decimal number
 * @param {Number} lon - destintation longitude as decimal number 
 * @param {Function} successCallback - The callback which will be called when plugin call is successful.
 * @param {Function} errorCallback - The callback which will be called when plugin encounters an error.
 * @param {Number} lat_start - start latitude as decimal number
 * @param {Number} lon_start - start longitude as decimal number 
 * This callback function have a string param with the error.     
 */
launchnavigator.navigateByLatLon = function (lat, lon, successCallback, errorCallback, lat_start, lon_start) {
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
    successCallback();

    var start = "";
    if (lat_start && lon_start)
        start = "pos." + lat_start + "_" + lon_start;

    window.location = "bingmaps:?rtp=" + start + "~adr." + name;
};
module.exports = launchnavigator;