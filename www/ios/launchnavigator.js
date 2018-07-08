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

var ln = {},
    common = launchnavigator;

// Launch modes for Apple Maps
ln.LAUNCH_MODE = {
    URI_SCHEME: "uri_scheme",
    MAPKIT: "mapkit"
};

/**
 * Enables debug log output from the plugin to the JS and native consoles. By default debug is disabled.
 * @param {boolean} enabled - Whether to enable debug.
 * @param {function} success - callback to invoke on successfully setting debug.
 * @param {function} error - callback to invoke on error while setting debug. Will be passed a single string argument containing the error message.
 */
ln.enableDebug = function(enabled, success, error){
    cordova.exec(success, error, 'LaunchNavigator', 'enableDebug', [enabled]);
};

/**
 * Determines if the given app is installed and available on the current device.
 * @param {string} appName - name of the app to check availability for. Define as a constant using ln.APP
 * @param {function} success - callback to invoke on successful determination of availability. Will be passed a single boolean argument indicating the availability of the app.
 * @param {function} error - callback to invoke on error while determining availability. Will be passed a single string argument containing the error message.
 */
ln.isAppAvailable = function(appName, success, error){
    common.util.validateApp(appName);
    cordova.exec(success, error, 'LaunchNavigator', 'isAppAvailable', [appName]);
};

/**
 * Returns a list indicating which apps are installed and available on the current device.
 * @param {function} success - callback to invoke on successful determination of availability. Will be passed a key/value object where the key is the app name and the value is a boolean indicating whether the app is available.
 * @param {function} error - callback to invoke on error while determining availability. Will be passed a single string argument containing the error message.
 */
ln.availableApps = function(success, error){
    cordova.exec(success, error, 'LaunchNavigator', 'availableApps', []);
};

/**
 * Opens navigator app to navigate to given destination, specified by either place name or lat/lon.
 * If a start location is not also specified, current location will be used for the start.
 *
 * @param {string/number[]} destination (required) - destination location to use for navigation.
 * Either:
 * - a {string} containing the address. e.g. "Buckingham Palace, London"
 * - an {array}, where the first element is the latitude and the second element is a longitude, as decimal numbers. e.g. [50.1, -4.0]
 *
 * @param {object} options (optional) - optional parameters:
 *
 * - {function} successCallback - A callback to invoke when the navigation app is successfully launched.
 *
 * - {function} errorCallback - A callback to invoke if an error is encountered while launching the app.
 * A single string argument containing the error message will be passed in.
 *
 * - {string} app - navigation app to use for directions, as a constant. e.g. launchnavigator.APP.GOOGLE_MAPS
 * If not specified, defaults to user selection via native picker UI.
 *
 * - {string} destinationName - nickname to display in app for destination. e.g. "Bob's House".
 *
 * - {mixed} start - start location to use for navigation. If not specified, the current location of the device will be used.
 * Either:
 *      - a {string} containing the address. e.g. "Buckingham Palace, London"
 *      - an {array}, where the first element is the latitude and the second element is a longitude, as decimal numbers. e.g. [50.1, -4.0]
 *
 * - {string} startName - nickname to display in app for start. e.g. "My Place".
 *
 * - {string} transportMode - transportation mode for navigation.
 * Defaults to "driving" if not specified.
 *
 * - {string} launchModeAppleMaps - method to use to open Apple Maps app:
 *   - `launchnavigator.LAUNCH_MODE.URI_SCHEME` or `launchnavigator.MAPKIT`
 *   - Defaults to `launchnavigator.LAUNCH_MODE.URI_SCHEME` if not specified.
 *
 * - {object} extras - a key/value map of extra app-specific parameters. For example, to tell Google Maps to display Satellite view in "maps" launch mode: `{"t": "k"}`
 *
 * - {string} appSelectionDialogHeader - text to display in the native picker which enables user to select which navigation app to launch.
 * Defaults to "Select app for navigation" if not specified.
 *
 * - {string} appSelectionCancelButton - text to display for the cancel button in the native picker which enables user to select which navigation app to launch.
 * Defaults to "Cancel" if not specified.
 *
 * - {array} appSelectionList - list of apps, defined as `launchnavigator.APP` constants, which should be displayed in the picker if the app is available.
 * This can be used to restrict which apps are displayed, even if they are installed. By default, all available apps will be displayed.
 *
 * - {boolean} enableGeocoding - if true, and input location type(s) doesn't match those required by the app, use geocoding to obtain the address/coords as required. Defaults to TRUE.
 */
ln.navigate = function(destination, options) {
    options = common.util.conformNavigateOptions(arguments);

    options.app = options.app || common.APP.USER_SELECT;

    // If app is user-selection
    if(options.app === common.APP.USER_SELECT){
        // Invoke user-selection UI and return (as it will re-invoke this method)
        return common.userSelect(destination, options);
    }

    // Set defaults
    options.transportMode = options.transportMode ? options.transportMode : common.TRANSPORT_MODE.DRIVING;
    options.enableGeocoding = typeof options.enableGeocoding !== "undefined" ? options.enableGeocoding : true;
    options.launchModeAppleMaps = typeof options.launchModeAppleMaps !== "undefined" ? options.launchModeAppleMaps : ln.LAUNCH_MODE.URI_SCHEME;

    // Input validation
    var throwError = function(errMsg){
        if(options.errorCallback){
            options.errorCallback(errMsg);
        }
        throw new Error(errMsg);
    };

    if(!destination){
        throwError("No destination was specified");
    }

    if(options.extras && typeof  options.extras !== "object"){
        throwError("'options.extras' must be a key/value object");
    }
    if(options.extras) options.extras = JSON.stringify(options.extras);

    common.util.validateApp(options.app);
    common.util.validateTransportMode(options.transportMode);

    // Process options
    destination = common.util.extractCoordsFromLocationString(destination);
    if(typeof(destination) === "object"){
        if(typeof destination.length === "undefined") throw "destination must be a string or an array";
        destination = destination.join(",");
        options.destType = "coords";
    }else{
        options.destType = "name";
    }

    options.start = common.util.extractCoordsFromLocationString(options.start);
    if(!options.start){
        options.startType = "none";
    }else if(typeof(options.start) === "object"){
        if(typeof options.start.length === "undefined") throw "start must be a string or an array";
        options.start = options.start.join(",");
        options.startType = "coords";
    }else{
        options.startType = "name";
    }

    cordova.exec(options.successCallback, options.errorCallback, 'LaunchNavigator', 'navigate', [
        destination,
        options.destType,
        options.destinationName,
        options.start,
        options.startType,
        options.startName,
        options.app,
        options.transportMode,
        options.launchModeAppleMaps,
        options.extras,
        options.enableGeocoding
    ]);

};

/*****************************
 * Platform-specific overrides
 *****************************/

/**
 * Indicates if an app on a given platform supports specification of a custom nickname for destination location.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.APPLE_MAPS`.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
 * @param {string} launchMode (optional) - only applies to Apple Maps on iOS. Specified as a constant in `launchnavigator.LAUNCH_MODE`. e.g. `launchnavigator.LAUNCH_MODE.MAPKIT`.
 * @return {boolean} - true if app/platform combination supports specification of destination location.
 */
ln.supportsDestName = function(app, platform, launchMode){
    common.util.validateApp(app);
    common.util.validatePlatform(platform);
    if(launchMode && platform === common.PLATFORM.IOS && app === common.APP.APPLE_MAPS){
        common.util.validateLaunchMode(launchMode);
        result = launchMode === ln.LAUNCH_MODE.MAPKIT;
    }else{
        result = common._supportsDestName(app, platform);
    }
    return result;
};
common._supportsDestName = common.supportsDestName;


/**
 * Indicates if an app on a given platform supports specification of a custom nickname for start location.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.APPLE_MAPS`.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g.
 * @param {string} launchMode (optional) - only applies to Apple Maps on iOS. Specified as a constant in `launchnavigator.LAUNCH_MODE`. e.g. `launchnavigator.LAUNCH_MODE.MAPKIT`.
 * @return {boolean} - true if app/platform combination supports specification of start location.
 */
ln.supportsStartName = function(app, platform, launchMode){
    common.util.validateApp(app);
    common.util.validatePlatform(platform);
    if(launchMode && platform === common.PLATFORM.IOS && app === common.APP.APPLE_MAPS){
        common.util.validateLaunchMode(launchMode);
        result = launchMode === ln.LAUNCH_MODE.MAPKIT;
    }else{
        result = common._supportsStartName(app, platform);
    }
    return result;
};
common._supportsStartName = common.supportsStartName;



module.exports = ln;