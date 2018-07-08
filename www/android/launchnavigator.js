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

/**
 * Launch modes supported by Google Maps on Android
 * @type {object}
 */
ln.LAUNCH_MODE = {
    MAPS: "maps",
    TURN_BY_TURN: "turn-by-turn",
    GEO: "geo"
};

// Add the Android-specific option to pass in for geo: protocol, letting the native app chooser decide.
common.APP.GEO = "geo";
common.SUPPORTS_DEST_NAME[common.PLATFORM.ANDROID].push(common.APP.GEO);
common.APPS_BY_PLATFORM[common.PLATFORM.ANDROID].splice(1, 0, common.APP.GEO); //insert at [1] below [User select]
common.APP_NAMES[common.APP.GEO] = "[Geo intent chooser]";

// Add apps that support the geo: protocol
function onDiscoverSupportedApps(supportedApps){
    for(var appName in supportedApps){
        var packageName = supportedApps[appName];
        common.APP[appName.toUpperCase().replace(" ","_")] = packageName;
        common.APP_NAMES[packageName] = appName;
        common.APPS_BY_PLATFORM[common.PLATFORM.ANDROID].push(packageName);
        common.SUPPORTS_DEST_NAME[common.PLATFORM.ANDROID].push(packageName);
    }
}

function onDiscoverSupportedAppsError(error){
    console.error("Error discovering list of supported apps: "+error);
}

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
 * Returns a list indicating which apps are installed and available on the current device.
 * @param {function} success - callback to invoke on successful determination of availability. Will be passed a key/value object where the key is the app name and the value is a boolean indicating whether the app is available.
 * @param {function} error - callback to invoke on error while determining availability. Will be passed a single string argument containing the error message.
 */
ln.availableApps = function(success, error){
    cordova.exec(
        success,
        error,
        'LaunchNavigator',
        'availableApps',
        []
    );
};

/**
 * Determines if the given app is installed and available on the current device.
 * @param {string} appName - name of the app to check availability for. Define as a constant using ln.APP
 * @param {function} success - callback to invoke on successful determination of availability. Will be passed a single boolean argument indicating the availability of the app.
 * @param {function} error - callback to invoke on error while determining availability. Will be passed a single string argument containing the error message.
 */
ln.isAppAvailable = function(appName, success, error){
    common.util.validateApp(appName);
    cordova.exec(function(result){
        success(!!result);
    }, error, 'LaunchNavigator', 'isAppAvailable', [appName]);
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
 *      - a {string} containing a latitude/longitude coordinate. e.g. "50.1. -4.0"
 *      - an {array}, where the first element is the latitude and the second element is a longitude, as decimal numbers. e.g. [50.1, -4.0]
 *
 * - {string} startName - nickname to display in app for start. e.g. "My Place".
 *
 * - {string} transportMode - transportation mode for navigation.
 * Can only be specified if navigationMode == "turn-by-turn".
 * Accepted values are "driving", "walking", "bicycling" or "transit".
 * Defaults to "driving" if not specified.
 *
 * - {string} launchModeGoogleMaps - mode in which to open Google Maps app:
 *   - `launchnavigator.LAUNCH_MODE.MAPS` or `launchnavigator.LAUNCH_MODE.TURN_BY_TURN`
 *   - Defaults to `launchnavigator.LAUNCH_MODE.MAPS` if not specified.
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
 * - {boolean} enableGeocoding - if true, and input location type(s) doesn't match those required by the app, use geocoding to obtain the address/coords as required. Defaults to true.
 */
ln.navigate = function(destination, options) {
    options = common.util.conformNavigateOptions(arguments);

    var dType, sType = "none";

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

    options.app = options.app || common.APP.USER_SELECT;
    options.enableGeocoding = typeof options.enableGeocoding !== "undefined" ? options.enableGeocoding : true;

    // If app is user-selection
    if(options.app === common.APP.USER_SELECT){
        // Invoke user-selection UI and return (as it will re-invoke this method)
        return common.userSelect(destination, options);
    }

    destination = common.util.extractCoordsFromLocationString(destination);
    if(typeof(destination) === "object"){
        if(typeof destination.length === "undefined") throw "destination must be a string or an array";
        dType = "pos";
    }else{
        dType = "name";
    }
    if(options.start){
        options.start = common.util.extractCoordsFromLocationString(options.start);
        if(typeof(options.start) === "object"){
            if(typeof options.start.length === "undefined") throw "start must be a string or an array";
            sType = "pos";
        }else{
            sType = "name";
        }
    }

    var transportMode = null;
    if(options.transportMode){
        common.util.validateTransportMode(options.transportMode);
        transportMode = options.transportMode.charAt(0);
    }

    // Default to Google Maps if not specified
    if(!options.app) options.app = common.APP.GOOGLE_MAPS;
    common.util.validateApp(options.app);

    if(!options.launchModeGoogleMaps) options.launchModeGoogleMaps = ln.LAUNCH_MODE.MAPS;
    common.util.validateLaunchMode(options.launchModeGoogleMaps);

    if(options.extras) options.extras = JSON.stringify(options.extras);

    cordova.exec(
        options.successCallback,
        options.errorCallback,
        'LaunchNavigator',
        'navigate',
        [
            options.app,
            dType,
            destination,
            options.destinationName,
            sType,
            options.start,
            options.startName,
            transportMode,
            options.launchModeGoogleMaps,
            options.extras,
            options.enableGeocoding
        ]
    );
};

/*****************************
 * Platform-specific overrides
 *****************************/


/**
 * Indicates if an app on a given platform supports specification of transport mode.
 * Android-specific implementation supports additional specification of launch mode.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
 * @param {string} launchMode (optional) - only applies to Google Maps on Android. Specified as a constant in `launchnavigator.LAUNCH_MODE`. e.g. `launchnavigator.LAUNCH_MODE.MAPS`.
 * @return {boolean} - true if app/platform combination supports specification of transport mode.
 */
ln.supportsTransportMode = function(app, platform, launchMode){
    common.util.validateApp(app);
    common.util.validatePlatform(platform);

    var result;
    if(launchMode && platform === common.PLATFORM.ANDROID && app === common.APP.GOOGLE_MAPS){
        common.util.validateLaunchMode(launchMode);
        result = launchMode === ln.LAUNCH_MODE.TURN_BY_TURN;
    }else{
        result = common._supportsTransportMode(app, platform);
    }
    return result;
};
common._supportsTransportMode = common.supportsTransportMode;

/**
 * Returns the list of transport modes supported by an app on a given platform.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
 * @param {string} launchMode (optional) - only applies to Google Maps on Android. Specified as a constant in `launchnavigator.LAUNCH_MODE`. e.g. `launchnavigator.LAUNCH_MODE.MAPS`.
 * @return {string[]} - list of transports modes as constants in `launchnavigator.TRANSPORT_MODE`.
 * If app/platform combination doesn't support specification of transport mode, the list will be empty;
 */
ln.getTransportModes = function(app, platform, launchMode){
    if(ln.supportsTransportMode(app, platform, launchMode)){
        return common.TRANSPORT_MODES[platform][app];
    }
    return [];
};

/**
 * Indicates if an app on a given platform supports specification of start location.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
 * @param {string} launchMode (optional) - only applies to Google Maps on Android. Specified as a constant in `launchnavigator.LAUNCH_MODE`. e.g. `launchnavigator.LAUNCH_MODE.MAPS`.
 * @return {boolean} - true if app/platform combination supports specification of start location.
 */
ln.supportsStart = function(app, platform, launchMode){
    common.util.validateApp(app);
    common.util.validatePlatform(platform);

    var result;
    if(launchMode && platform === common.PLATFORM.ANDROID && app === common.APP.GOOGLE_MAPS){
        common.util.validateLaunchMode(launchMode);
        result = launchMode === ln.LAUNCH_MODE.MAPS;
    }else{
        result = common._supportsStart(app, platform);
    }
    return result;
};
common._supportsStart = common.supportsStart;

/**
 * Indicates if an app on a given platform supports specification of a custom nickname for destination location.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
 * @param {string} launchMode (optional) - only applies to Google Maps on Android. Specified as a constant in `launchnavigator.LAUNCH_MODE`. e.g. `launchnavigator.LAUNCH_MODE.MAPS`.
 * @return {boolean} - true if app/platform combination supports specification of destination location.
 */
ln.supportsDestName = function(app, platform, launchMode){
    common.util.validateApp(app);
    common.util.validatePlatform(platform);
    if(launchMode && platform === common.PLATFORM.ANDROID && app === common.APP.GOOGLE_MAPS){
        common.util.validateLaunchMode(launchMode);
        result = launchMode === ln.LAUNCH_MODE.GEO;
    }else{
        result = common._supportsDestName(app, platform);
    }
    return result;
};
common._supportsDestName = common.supportsDestName;


/************
 * Bootstrap
 ************/

// Discover supported apps
cordova.exec(
    onDiscoverSupportedApps,
    onDiscoverSupportedAppsError,
    'LaunchNavigator',
    'discoverSupportedApps',
    []
);

module.exports = ln;