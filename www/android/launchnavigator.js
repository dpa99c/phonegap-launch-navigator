/*
 * Copyright (c) 2014 Dave Alden  (http://github.com/dpa99c)
 * Copyright (c) 2014 Working Edge Ltd. (http://www.workingedge.co.uk)
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
 * If a start location is not also specified, current location will be used for the start.
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
 * {Boolean} disableAutoGeolocation - if TRUE, the plugin will NOT attempt to use the geolocation plugin to determine the current device position when the start location parameter is omitted. Defaults to FALSE.
 * {String} navigationMode - navigation mode in which to open Google Maps app: "maps" or "turn-by-turn". Defaults to "maps" if not specified.
 * In "turn-by-turn" mode, transportMode can be specified but start location cannot be specified (defaults to current location).
 * In "maps" mode, transportMode cannot be specified but start location can be specified.
 * {String} transportMode - transportation mode for navigation. Can only be specified if navigationMode == "turn-by-turn". Accepted values are "driving", "walking", "bicycling" or "transit". Defaults to "driving" if not specified.
 */
launchnavigator.navigate = function(destination, start, successCallback, errorCallback, options) {
    options = options ? options : {};
    var dType, sType = "none";
    if(typeof(destination) == "object"){
        dType = "pos";
    }else{
        dType = "name";
    }

    var transportMode = null;
    if(options.transportMode){
        transportMode = options.transportMode.charAt(0);
    }

    var turnByTurnMode = options.navigationMode === "turn-by-turn";

    function doNavigate(sType){

        cordova.exec(
            successCallback,
            errorCallback,
            'LaunchNavigator',
            'navigate',
            [dType, destination, sType, start, transportMode, turnByTurnMode]
        );
    }

    if(start){
        if(typeof(start) == "object"){
            sType = "pos";
        }else{
            sType = "name";
        }
        doNavigate(sType);
    }else if(!options.disableAutoGeolocation && navigator.geolocation){ // if cordova-plugin-geolocation is available/enabled
        navigator.geolocation.getCurrentPosition(function(position){ // attempt to use current location as start position
            start = [position.coords.latitude, position.coords.longitude];
            doNavigate("pos");
        },function(error){
            doNavigate(sType); // Fallback to default current location on error
        },{
            maxAge: 60000,
            timeout: 500
        });
    }else{
        doNavigate(sType);
    }
};
module.exports = launchnavigator;