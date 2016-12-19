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
    USER_SELECT: "user_select",
    APPLE_MAPS: "apple_maps",
    GOOGLE_MAPS: "google_maps",
    WAZE: "waze",
    CITYMAPPER: "citymapper",
    NAVIGON: "navigon",
    TRANSIT_APP: "transit_app",
    YANDEX: "yandex",
    UBER: "uber",
    TOMTOM: "tomtom",
    BING_MAPS: "bing_maps",
    SYGIC: "sygic",
    HERE_MAPS: "here_maps",
    MOOVIT: "moovit"
};

/**
 * Explicitly supported apps by platform
 * @type {object}
 */
ln.APPS_BY_PLATFORM = {};
ln.APPS_BY_PLATFORM[ln.PLATFORM.ANDROID] = [
    ln.APP.USER_SELECT,
    ln.APP.GOOGLE_MAPS,
    ln.APP.CITYMAPPER,
    ln.APP.UBER,
    ln.APP.WAZE,
    ln.APP.YANDEX,
    ln.APP.SYGIC,
    ln.APP.HERE_MAPS,
    ln.APP.MOOVIT
];
ln.APPS_BY_PLATFORM[ln.PLATFORM.IOS] = [
    ln.APP.USER_SELECT,
    ln.APP.APPLE_MAPS,
    ln.APP.GOOGLE_MAPS,
    ln.APP.WAZE,
    ln.APP.CITYMAPPER,
    ln.APP.NAVIGON,
    ln.APP.TRANSIT_APP,
    ln.APP.YANDEX,
    ln.APP.UBER,
    ln.APP.TOMTOM,
    ln.APP.SYGIC,
    ln.APP.HERE_MAPS,
    ln.APP.MOOVIT
];
ln.APPS_BY_PLATFORM[ln.PLATFORM.WINDOWS] = [
    ln.APP.BING_MAPS
];

/**
 * Stock maps app that is always present on each platform
 * @type {object}
 */
ln.STOCK_APP = {};
ln.STOCK_APP[ln.PLATFORM.ANDROID] = ln.APP.GOOGLE_MAPS;
ln.STOCK_APP[ln.PLATFORM.IOS] = ln.APP.APPLE_MAPS;
ln.STOCK_APP[ln.PLATFORM.WINDOWS] = ln.APP.BING_MAPS;

/**
 * Display names for supported apps
 * @type {object}
 */
ln.APP_NAMES = {};
ln.APP_NAMES[ln.APP.USER_SELECT] = "[User select]";
ln.APP_NAMES[ln.APP.APPLE_MAPS] = "Apple Maps";
ln.APP_NAMES[ln.APP.GOOGLE_MAPS] = "Google Maps";
ln.APP_NAMES[ln.APP.WAZE] = "Waze";
ln.APP_NAMES[ln.APP.CITYMAPPER] = "Citymapper";
ln.APP_NAMES[ln.APP.NAVIGON] = "Navigon";
ln.APP_NAMES[ln.APP.TRANSIT_APP] = "Transit App";
ln.APP_NAMES[ln.APP.YANDEX] = "Yandex Navigator";
ln.APP_NAMES[ln.APP.UBER] = "Uber";
ln.APP_NAMES[ln.APP.TOMTOM] = "Tomtom";
ln.APP_NAMES[ln.APP.BING_MAPS] = "Bing Maps";
ln.APP_NAMES[ln.APP.SYGIC] = "Sygic";
ln.APP_NAMES[ln.APP.HERE_MAPS] = "HERE Maps";
ln.APP_NAMES[ln.APP.MOOVIT] = "Moovit";

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
// Android
ln.TRANSPORT_MODES[ln.PLATFORM.ANDROID] = {};
ln.TRANSPORT_MODES[ln.PLATFORM.ANDROID][ln.APP.USER_SELECT] = [ // Allow all
    ln.TRANSPORT_MODE.DRIVING,
    ln.TRANSPORT_MODE.WALKING,
    ln.TRANSPORT_MODE.BICYCLING,
    ln.TRANSPORT_MODE.TRANSIT
];
ln.TRANSPORT_MODES[ln.PLATFORM.ANDROID][ln.APP.GOOGLE_MAPS] = [ // Only launchMode=turn-by-turn
    ln.TRANSPORT_MODE.DRIVING,
    ln.TRANSPORT_MODE.WALKING,
    ln.TRANSPORT_MODE.BICYCLING,
    ln.TRANSPORT_MODE.TRANSIT
];
ln.TRANSPORT_MODES[ln.PLATFORM.ANDROID][ln.APP.SYGIC] = [
    ln.TRANSPORT_MODE.DRIVING,
    ln.TRANSPORT_MODE.WALKING
];
// Windows
ln.TRANSPORT_MODES[ln.PLATFORM.WINDOWS] = {};
ln.TRANSPORT_MODES[ln.PLATFORM.WINDOWS][ln.APP.BING_MAPS] = [
    ln.TRANSPORT_MODE.DRIVING,
    ln.TRANSPORT_MODE.WALKING,
    ln.TRANSPORT_MODE.TRANSIT
];

// iOS
ln.TRANSPORT_MODES[ln.PLATFORM.IOS] = {};
ln.TRANSPORT_MODES[ln.PLATFORM.IOS][ln.APP.USER_SELECT] = [ // Allow all
    ln.TRANSPORT_MODE.DRIVING,
    ln.TRANSPORT_MODE.WALKING,
    ln.TRANSPORT_MODE.BICYCLING,
    ln.TRANSPORT_MODE.TRANSIT
];
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
ln.TRANSPORT_MODES[ln.PLATFORM.IOS][ln.APP.SYGIC] = [
    ln.TRANSPORT_MODE.DRIVING,
    ln.TRANSPORT_MODE.WALKING
];

/**
 * Apps by platform that support specifying a start location
 * @type {obect}
 */
ln.SUPPORTS_START = {};
ln.SUPPORTS_START[ln.PLATFORM.ANDROID] = [
    ln.APP.USER_SELECT,
    ln.APP.GOOGLE_MAPS, // Only launchMode=maps
    ln.APP.CITYMAPPER,
    ln.APP.UBER,
    ln.APP.YANDEX,
    ln.APP.HERE_MAPS,
    ln.APP.MOOVIT
];
ln.SUPPORTS_START[ln.PLATFORM.IOS] = [
    ln.APP.USER_SELECT,
    ln.APP.APPLE_MAPS,
    ln.APP.GOOGLE_MAPS,
    ln.APP.CITYMAPPER,
    ln.APP.TRANSIT_APP,
    ln.APP.YANDEX,
    ln.APP.UBER,
    ln.APP.HERE_MAPS,
    ln.APP.MOOVIT
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
    ln.APP.USER_SELECT,
    ln.APP.CITYMAPPER,
    ln.APP.UBER,
    ln.APP.HERE_MAPS,
    ln.APP.MOOVIT
];
ln.SUPPORTS_START_NAME[ln.PLATFORM.IOS] = [
    ln.APP.USER_SELECT,
    ln.APP.APPLE_MAPS,
    ln.APP.CITYMAPPER,
    ln.APP.UBER,
    ln.APP.HERE_MAPS,
    ln.APP.MOOVIT
];

/**
 * Apps by platform that support specifying a destination nickname
 * @type {obect}
 */
ln.SUPPORTS_DEST_NAME = {};
ln.SUPPORTS_DEST_NAME[ln.PLATFORM.ANDROID] = [
    ln.APP.USER_SELECT,
    ln.APP.GOOGLE_MAPS, // only launchMode=geo
    ln.APP.CITYMAPPER,
    ln.APP.UBER,
    ln.APP.HERE_MAPS,
    ln.APP.MOOVIT
];
ln.SUPPORTS_DEST_NAME[ln.PLATFORM.IOS] = [
    ln.APP.USER_SELECT,
    ln.APP.APPLE_MAPS,
    ln.APP.CITYMAPPER,
    ln.APP.NAVIGON,
    ln.APP.UBER,
    ln.APP.TOMTOM,
    ln.APP.HERE_MAPS,
    ln.APP.MOOVIT
];

/**
 * Apps by platform that support specifying a launch mode
 * @type {obect}
 */
ln.SUPPORTS_LAUNCH_MODE = {};
ln.SUPPORTS_LAUNCH_MODE[ln.PLATFORM.ANDROID] = [
    ln.APP.USER_SELECT,
    ln.APP.GOOGLE_MAPS
];

ln.COORDS_REGEX = /^[-\d.]+,[\s]*[-\d.]+$/;



/******************
 * Public API
 ******************/

/**
 * Delegation shim to determine by arguments if API call is v2 or v3 and delegate accordingly.
 */
ln.navigate = function(){
    if(arguments.length <= 2 && (typeof(arguments[1]) == "undefined" || typeof(arguments[1]) == "object")){
        ln.v3.navigate.apply(this, arguments);
    }else if(typeof(arguments[1]) == "function" && (typeof(arguments[2]) == "undefined" || typeof(arguments[2]) == "function")){
        // called with v3 signature: navigate(start, successCallback, errorCallback, options)
        var options = typeof(arguments[3]) == "object" ? arguments[3] : {};
        options.successCallback = arguments[1];
        options.errorCallback = arguments[2];
        ln.v3.navigate.call(this, arguments[0], options);
    }else{
        ln.v2.navigate.apply(this, arguments);
    }
};

/**
 * Returns the display name of the specified app.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
 * @return {string} - app display name. e.g. "Google Maps".
 */
ln.getAppDisplayName = function(app){
    ln.util.validateApp(app);
    return ln.APP_NAMES[app];
};

/**
 * Returns list of supported apps on a given platform.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
 * @return {array} - apps supported on specified platform as a list of `launchnavigator.APP` constants.
 */
ln.getAppsForPlatform = function(platform){
    ln.util.validatePlatform(platform);
    return ln.APPS_BY_PLATFORM[platform];
};

/**
 * Indicates if an app on a given platform supports specification of transport mode.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
 * @return {boolean} - true if app/platform combination supports specification of transport mode.
 */
ln.supportsTransportMode = function(app, platform){
    ln.util.validateApp(app);
    ln.util.validatePlatform(platform);
    return !!ln.TRANSPORT_MODES[platform] && ln.util.objectContainsKey(ln.TRANSPORT_MODES[platform], app);
};

/**
 * Returns the list of transport modes supported by an app on a given platform.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
 * @return {array} - list of transports modes as constants in `launchnavigator.TRANSPORT_MODE`.
 * If app/platform combination doesn't support specification of transport mode, the list will be empty;
 */
ln.getTransportModes = function(app, platform){
    if(ln.supportsTransportMode(app, platform)){
        return ln.TRANSPORT_MODES[platform][app];
    }
    return [];
};

/**
 * Indicates if an app on a given platform supports specification of launch mode.
 * Note that currently only Google Maps on Android does.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.ANDROID`.
 * @return {boolean} - true if app/platform combination supports specification of transport mode.
 */
ln.supportsLaunchMode = function(app, platform) {
    ln.util.validateApp(app);
    ln.util.validatePlatform(platform);
    return !!ln.SUPPORTS_LAUNCH_MODE[platform] && ln.util.arrayContainsValue(ln.SUPPORTS_LAUNCH_MODE[platform], app);
};

/**
 * Indicates if an app on a given platform supports specification of start location.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
 * @return {boolean} - true if app/platform combination supports specification of start location.
 */
ln.supportsStart = function(app, platform){
    ln.util.validateApp(app);
    ln.util.validatePlatform(platform);
    return !!ln.SUPPORTS_START[platform] && ln.util.arrayContainsValue(ln.SUPPORTS_START[platform], app);
};

/**
 * Indicates if an app on a given platform supports specification of a custom nickname for start location.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
 * @return {boolean} - true if app/platform combination supports specification of start location.
 */
ln.supportsStartName = function(app, platform){
    ln.util.validateApp(app);
    ln.util.validatePlatform(platform);
    return !!ln.SUPPORTS_START_NAME[platform] && ln.util.arrayContainsValue(ln.SUPPORTS_START_NAME[platform], app);
};

/**
 * Indicates if an app on a given platform supports specification of a custom nickname for destination location.
 * @param {string} app - specified as a constant in `launchnavigator.APP`. e.g. `launchnavigator.APP.GOOGLE_MAPS`.
 * @param {string} platform - specified as a constant in `launchnavigator.PLATFORM`. e.g. `launchnavigator.PLATFORM.IOS`.
 * @return {boolean} - true if app/platform combination supports specification of destination location.
 */
ln.supportsDestName = function(app, platform){
    ln.util.validateApp(app);
    ln.util.validatePlatform(platform);
    return !!ln.SUPPORTS_DEST_NAME[platform] && ln.util.arrayContainsValue(ln.SUPPORTS_DEST_NAME[platform], app);
};


/**
 *
 * @param {mixed} destination (required) - destination location to use for navigation - see launchnavigator.navigate()
 * @param {object} options (optional) - optional parameters - see launchnavigator.navigate()
 */
ln.userSelect = function(destination, options, successCallback, errorCallback){
    options = options ? options : {};
    options.errorCallback = options.errorCallback ? options.errorCallback : errorCallback || function(){};
    options.successCallback = options.successCallback ? options.successCallback : successCallback || function(){};
    options.appSelectionCallback = options.appSelectionCallback ? options.appSelectionCallback : function(){};
    
    var buttonList = [], buttonMap = {};

    if(launchnavigator.userSelectDisplayed) return;

    function launchApp(app){
        options.app = app;
        launchnavigator.navigate(destination, options);
    }

    function onChooseApp(btnNumber){
        delete launchnavigator.userSelectDisplayed;
        var idx = btnNumber - 1,
            app = buttonMap[idx];
        if(app != "cancel"){
            launchApp(app);
            options.appSelectionCallback(app);
        } else {
            options.errorCallback('cancelled');
        }
    }

    function displayChooser(){
        launchnavigator.userSelectDisplayed = true;
        window.plugins.actionsheet.show({
            'androidTheme': options.androidTheme || window.plugins.actionsheet.ANDROID_THEMES.THEME_HOLO_LIGHT,
            'title': options.appSelectionDialogHeader || DEFAULT_appSelectionDialogHeader,
            'buttonLabels': buttonList,
            'androidEnableCancelButton' : true, // default false
            //'winphoneEnableCancelButton' : true, // default false
            'addCancelButtonWithLabel': options.appSelectionCancelButton || DEFAULT_appSelectionCancelButton,
            'position': [550, 500] // for iPad pass in the [x, y] position of the popover
        }, onChooseApp);
    }

    // Get list of available apps
    launchnavigator.availableApps(function(apps){
        for(var app in apps){
            var isAvailable = apps[app];
            if(!isAvailable) continue;
            if(options.appSelectionList && options.appSelectionList.length > 0 && !ln.util.arrayContainsValue(options.appSelectionList, app)) continue;
            buttonList.push(ln.getAppDisplayName(app));
            buttonMap[buttonList.length-1] = app;
        }

        if(buttonList.length == 0){
            return options.errorCallback('No apps in selection list are available');
        }

        if(buttonList.length == 1){
            return launchApp(buttonMap[0]);
        }

        buttonMap[buttonList.length] = "cancel"; // Add an entry for cancel button

        displayChooser();

    }, options.errorCallback);
};

/*******************
 * Utility functions
 *******************/
ln.util = {};
ln.util.arrayContainsValue = function (a, obj) {
    var i = a.length;
    while (i--) {
        if (a[i] === obj) {
            return true;
        }
    }
    return false;
};

ln.util.objectContainsKey = function (o, key) {
    for(var k in o){
        if(k === key){
            return true;
        }
    }
    return false;
};

ln.util.objectContainsValue = function (o, value) {
    for(var k in o){
        if(o[k] === value){
            return true;
        }
    }
    return false;
};

ln.util.countKeysInObject = function (o){
    var count = 0;
    for(var k in o){
        count++;
    }
    return count;
};

ln.util.isValidApp = function(app){
    if(app == "none") return true; // native chooser
    return ln.util.objectContainsValue(ln.APP, app);
};

ln.util.isValidPlatform = function(platform){
    return ln.util.objectContainsValue(ln.PLATFORM, platform);
};

ln.util.isValidTransportMode = function(transportMode) {
    return ln.util.objectContainsValue(ln.TRANSPORT_MODE, transportMode);
};

ln.util.validateApp = function(app){
    if(!ln.util.isValidApp(app)){
        throw new Error("'"+app+"' is not a recognised app");
    }
};

ln.util.validatePlatform = function(platform){
    if(!ln.util.isValidPlatform(platform)){
        throw new Error("'"+platform+"' is not a recognised platform");
    }
};

ln.util.validateTransportMode = function(transportMode){
    if(!ln.util.isValidTransportMode(transportMode)){
        throw new Error("'"+transportMode+"' is not a recognised transport mode");
    }
};

ln.util.extractCoordsFromLocationString = function(location){
    if(location && typeof(location) == "string" && location.match(ln.COORDS_REGEX)){
        location = location.replace(/\s*/g,'');
        var parts = location.split(",");
        location = [parts[0], parts[1]];
    }
    return location;
};

/*********************
 * Internal properties
 *********************/

var DEFAULT_appSelectionDialogHeader = "Select app for navigation",
    DEFAULT_appSelectionCancelButton = "Cancel";

/********************
 * Internal functions
 ********************/

module.exports = ln;
