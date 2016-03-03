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


var ln = {};

/******************
 * Public Constants
 ******************/

/**
 * Supported platforms
 * @type {object}
 */
ln.PLATFORM = {
    ANDROID: "android",
    IOS: "ios",
    WINDOWS: "windows"
};

/**
 * string constants, used to identify apps in native code
 * @type {object}
 */
ln.APP = {
    APPLE_MAPS: "apple_maps",
    GOOGLE_MAPS: "google_maps",
    WAZE: "waze",
    CITYMAPPER: "citymapper",
    NAVIGON: "navigon",
    TRANSIT_APP: "transit_app",
    YANDEX: "yandex",
    UBER: "uber",
    TOMTOM: "tomtom",
    BING_MAPS: "bing_maps"
};

/**
 * Supported apps by platform
 * @type {object}
 */
ln.APPS_BY_PLATFORM = {};
ln.APPS_BY_PLATFORM[ln.PLATFORM.ANDROID] = [
    ln.APP.GOOGLE_MAPS,
    ln.APP.WAZE,
    ln.APP.CITYMAPPER,
    ln.APP.UBER
];
ln.APPS_BY_PLATFORM[ln.PLATFORM.IOS] = [
    ln.APP.APPLE_MAPS,
    ln.APP.GOOGLE_MAPS,
    ln.APP.WAZE,
    ln.APP.CITYMAPPER,
    ln.APP.NAVIGON,
    ln.APP.TRANSIT_APP,
    ln.APP.YANDEX,
    ln.APP.UBER,
    ln.APP.TOMTOM
];
ln.APPS_BY_PLATFORM[ln.PLATFORM.WINDOWS] = [
    ln.APP.BING_MAPS
];


/**
 * Display names for supported apps
 * @type {object}
 */
ln.APP_NAMES = {};
ln.APP_NAMES[ln.APP.APPLE_MAPS] = "Apple Maps";
ln.APP_NAMES[ln.APP.GOOGLE_MAPS] = "Google Maps";
ln.APP_NAMES[ln.APP.WAZE] = "Waze";
ln.APP_NAMES[ln.APP.CITYMAPPER] = "Citymapper";
ln.APP_NAMES[ln.APP.NAVIGON] = "Navigon";
ln.APP_NAMES[ln.APP.TRANSIT_APP] = "Transit App";
ln.APP_NAMES[ln.APP.YANDEX] = "Yandex";
ln.APP_NAMES[ln.APP.UBER] = "Uber";
ln.APP_NAMES[ln.APP.TOMTOM] = "Tomtom";
ln.APP_NAMES[ln.APP.BING_MAPS] = "Bing Maps";

/**
 * All possible transport modes
 * @type {object}
 */
ln.TRANSPORT_MODE = {
    DRIVING: "driving",
    WALKING: "walking",
    BICYCLING: "bicycling",
    TRANSIT: "transit"
};

/**
 * Supported transport modes by apps and platform
 * @type {object}
 */
ln.TRANSPORT_MODES = {};
ln.TRANSPORT_MODES[ln.PLATFORM.ANDROID] = {};
ln.TRANSPORT_MODES[ln.PLATFORM.ANDROID][ln.APP.GOOGLE_MAPS] = [ // Only in turn-by-turn launch mode
    ln.TRANSPORT_MODE.DRIVING,
    ln.TRANSPORT_MODE.WALKING,
    ln.TRANSPORT_MODE.BICYCLING,
    ln.TRANSPORT_MODE.TRANSIT
];
ln.TRANSPORT_MODES[ln.PLATFORM.WINDOWS] = {};
ln.TRANSPORT_MODES[ln.PLATFORM.WINDOWS][ln.APP.BING_MAPS] = [
    ln.TRANSPORT_MODE.DRIVING,
    ln.TRANSPORT_MODE.WALKING,
    ln.TRANSPORT_MODE.TRANSIT
];
ln.TRANSPORT_MODES[ln.PLATFORM.IOS] = {};
ln.TRANSPORT_MODES[ln.PLATFORM.IOS][ln.APP.GOOGLE_MAPS] = [
    ln.TRANSPORT_MODE.DRIVING,
    ln.TRANSPORT_MODE.WALKING,
    ln.TRANSPORT_MODE.BICYCLING,
    ln.TRANSPORT_MODE.TRANSIT
];
ln.TRANSPORT_MODES[ln.PLATFORM.IOS][ln.APP.APPLE_MAPS] = [
    ln.TRANSPORT_MODE.DRIVING,
    ln.TRANSPORT_MODE.WALKING
];

/**
 * Apps by platform that support specifying a start location
 * @type {obect}
 */
ln.SUPPORTS_START = {};
ln.SUPPORTS_START[ln.PLATFORM.ANDROID] = [
    ln.APP.GOOGLE_MAPS, // Only in maps launch mode
    ln.APP.CITYMAPPER,
    ln.APP.UBER
];
ln.SUPPORTS_START[ln.PLATFORM.IOS] = [
    ln.APP.APPLE_MAPS,
    ln.APP.GOOGLE_MAPS,
    ln.APP.CITYMAPPER,
    ln.APP.TRANSIT_APP,
    ln.APP.YANDEX,
    ln.APP.UBER
];
ln.SUPPORTS_START[ln.PLATFORM.WINDOWS] = [
    ln.APP.BING_MAPS
];

/**
 * Apps by platform that support specifying a start nickname
 * @type {obect}
 */
ln.SUPPORTS_START_NAME = {};
ln.SUPPORTS_START_NAME[ln.PLATFORM.ANDROID] = [
    ln.APP.CITYMAPPER
];
ln.SUPPORTS_START_NAME[ln.PLATFORM.IOS] = [
    ln.APP.APPLE_MAPS,
    ln.APP.CITYMAPPER
];

/**
 * Apps by platform that support specifying a destination nickname
 * @type {obect}
 */
ln.SUPPORTS_DEST_NAME = {};
ln.SUPPORTS_DEST_NAME[ln.PLATFORM.ANDROID] = [
    ln.APP.CITYMAPPER,
    ln.APP.UBER
];
ln.SUPPORTS_DEST_NAME[ln.PLATFORM.IOS] = [
    ln.APP.APPLE_MAPS,
    ln.APP.CITYMAPPER,
    ln.APP.NAVIGON,
    ln.APP.UBER,
    ln.APP.TOMTOM
];

/**
 * Launch modes supported by Google Maps on Android
 * @type {object}
 */
ln.LAUNCH_MODE = {
    MAPS: "maps",
    TURN_BY_TURN: "turn_by_turn"
};

/**
 * Apps by platform that support specifying a launch mode
 * @type {obect}
 */
ln.SUPPORTS_LAUNCH_MODE = {};
ln.SUPPORTS_LAUNCH_MODE[ln.PLATFORM.ANDROID] = [ln.APP.GOOGLE_MAPS];


/******************
 * Internal functions
 ******************/
function validateApp(app){
    for(var APP in ln.APP){
        if(app == ln.APP[APP]) return;
    }
    throw new Error("'"+app+"' is not a recognised app");
}

function validatePlatform(platform){
    for(var PLATFORM in ln.PLATFORM){
        if(platform == ln.PLATFORM[PLATFORM]) return;
    }
    throw new Error("'"+platform+"' is not a recognised platform");
}

function validateLaunchMode(launchMode){
    for(var LAUNCH_MODE in ln.LAUNCH_MODE){
        if(launchMode == ln.LAUNCH_MODE[LAUNCH_MODE]) return;
    }
    throw new Error("'"+launchMode+"' is not a recognised launch mode");
}

function arrayContainsValue(a, obj) {
    var i = a.length;
    while (i--) {
        if (a[i] === obj) {
            return true;
        }
    }
    return false;
}

function objectContainsKey(o, key) {
    for(var k in o){
        if(k === key){
            return true;
        }
    }
    return false;
}

/******************
 * Public API
 ******************/

/**
 * Returns the display name of the specified app.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
 * @return {string} - app display name. e.g. "Google Maps".
 */
ln.getAppDisplayName = function(app){
    validateApp(app);
    return ln.APP_NAMES[app];
};

/**
 * Returns list of supported apps on a given platform.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
 * @return {array} - apps supported on specified platform as a list of `launchnavigator.APP` constants.
 */
ln.getAppsForPlatform = function(platform){
    validatePlatform(platform);
    return ln.APPS_BY_PLATFORM[platform];
};

/**
 * Indicates if an app on a given platform supports specification of transport mode.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
 * @param {string} launchMode (optional) - only applies to Google Maps on Android. Specified as a constant in `launchnavigator.LAUNCH_MODE`. e.g. `launchnavigator.LAUNCH_MODE.MAPS`.
 * @return {boolean} - true if app/platform combination supports specification of transport mode.
 */
ln.supportsTransportMode = function(app, platform, launchMode){
    validateApp(app);
    validatePlatform(platform);

    var result;
    if(launchMode && platform == ln.PLATFORM.ANDROID && app == ln.APP.GOOGLE_MAPS){
        validateLaunchMode(launchMode);
        result = launchMode === ln.LAUNCH_MODE.TURN_BY_TURN;
    }else{
        result = !!ln.TRANSPORT_MODES[platform] && objectContainsKey(ln.TRANSPORT_MODES[platform], app);
    }
    return result;
};

/**
 * Returns the list of transport modes supported by an app on a given platform.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
 * @param {string} launchMode (optional) - only applies to Google Maps on Android. Specified as a constant in `launchnavigator.LAUNCH_MODE`. e.g. `launchnavigator.LAUNCH_MODE.MAPS`.
 * @return {array} - list of transports modes as constants in `launchnavigator.TRANSPORT_MODE`.
 * If app/platform combination doesn't support specification of transport mode, the list will be empty;
 */
ln.getTransportModes = function(app, platform, launchMode){
    if(ln.supportsTransportMode(app, platform, launchMode)){
        return ln.TRANSPORT_MODES[platform][app];
    }
    return [];
};

/**
 * Indicates if an app on a given platform supports specification of launch mode.
 * Note that currently only Google Maps on Android does.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
 * @return {boolean} - true if app/platform combination supports specification of transport mode.
 */
ln.supportsLaunchMode = function(app, platform) {
    validateApp(app);
    validatePlatform(platform);
    return !!ln.SUPPORTS_LAUNCH_MODE[platform] && arrayContainsValue(ln.SUPPORTS_LAUNCH_MODE[platform], app);
};

/**
 * Indicates if an app on a given platform supports specification of start location.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
 * @param {string} launchMode (optional) - only applies to Google Maps on Android. Specified as a constant in `launchnavigator.LAUNCH_MODE`. e.g. `launchnavigator.LAUNCH_MODE.MAPS`.
 * @return {boolean} - true if app/platform combination supports specification of start location.
 */
ln.supportsStart = function(app, platform, launchMode){
    validateApp(app);
    validatePlatform(platform);

    var result;
    if(launchMode && platform == ln.PLATFORM.ANDROID && app == ln.APP.GOOGLE_MAPS){
        validateLaunchMode(launchMode);
        result = launchMode === ln.LAUNCH_MODE.MAPS;
    }else{
        result = !!ln.SUPPORTS_START[platform] && arrayContainsValue(ln.SUPPORTS_START[platform], app);
    }
    return result;
};

/**
 * Indicates if an app on a given platform supports specification of a custom nickname for start location.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
 * @return {boolean} - true if app/platform combination supports specification of start location.
 */
ln.supportsStartName = function(app, platform){
    validateApp(app);
    validatePlatform(platform);
    return !!ln.SUPPORTS_START_NAME[platform] && arrayContainsValue(ln.SUPPORTS_START_NAME[platform], app);
};

/**
 * Indicates if an app on a given platform supports specification of a custom nickname for destination location.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
 * @return {boolean} - true if app/platform combination supports specification of destination location.
 */
ln.supportsDestName = function(app, platform){
    validateApp(app);
    validatePlatform(platform);
    return !!ln.SUPPORTS_DEST_NAME[platform] && arrayContainsValue(ln.SUPPORTS_DEST_NAME[platform], app);
};

module.exports = ln;