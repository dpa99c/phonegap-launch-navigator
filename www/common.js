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

/*********************
 * Internal properties
 *********************/

var DEFAULT_appSelectionDialogHeaderText = "Select app for navigation",
    DEFAULT_appSelectionCancelButtonText = "Cancel",
    DEFAULT_rememberChoicePromptDialogHeaderText = "Remember your choice?",
    DEFAULT_rememberChoicePromptDialogBodyText = "Use the same app for navigating next time?",
    DEFAULT_rememberChoicePromptDialogYesButtonText = "Yes",
    DEFAULT_rememberChoicePromptDialogNoButtonText = "No";

var store;

var emptyFn = function(){};

/********************
 * Internal functions
 ********************/

var ensureStore = function(){
    store = localforage.createInstance({
        name: "launchnavigator"
    });
};

var setItem = function(key, value, callback){
    ensureStore();
    return store.setItem(key, value, callback);
};

var getItem = function(key, callback){
    ensureStore();
    return store.getItem(key, function(err, value){
        callback(value);
    });
};

var removeItem = function(key, callback){
    ensureStore();
    return store.removeItem(key, callback);
};

var itemExists = function(key, callback){
    ensureStore();
    return store.getItem(key, function(err, value){
        callback(value !== null);
    });
};

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
    MOOVIT: "moovit",
    LYFT: "lyft",
    MAPS_ME: "maps_me",
    CABIFY: "cabify",
    BAIDU: "baidu",
    TAXIS_99: "taxis_99",
    GAODE: "gaode"
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
    ln.APP.MOOVIT,
    ln.APP.LYFT,
    ln.APP.MAPS_ME,
    ln.APP.CABIFY,
    ln.APP.BAIDU,
    ln.APP.TAXIS_99,
    ln.APP.GAODE
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
    ln.APP.MOOVIT,
    ln.APP.LYFT,
    ln.APP.MAPS_ME,
    ln.APP.CABIFY,
    ln.APP.BAIDU,
    ln.APP.TAXIS_99,
    ln.APP.GAODE
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
ln.APP_NAMES[ln.APP.LYFT] = "Lyft";
ln.APP_NAMES[ln.APP.MAPS_ME] = "MAPS.ME";
ln.APP_NAMES[ln.APP.CABIFY] = "Cabify";
ln.APP_NAMES[ln.APP.BAIDU] = "Baidu Maps";
ln.APP_NAMES[ln.APP.TAXIS_99] = "99 Taxi";
ln.APP_NAMES[ln.APP.GAODE] = "Gaode Maps (Amap)";

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
ln.TRANSPORT_MODES[ln.PLATFORM.ANDROID][ln.APP.MAPS_ME] = [
    ln.TRANSPORT_MODE.DRIVING,
    ln.TRANSPORT_MODE.WALKING,
    ln.TRANSPORT_MODE.BICYCLING,
    ln.TRANSPORT_MODE.TRANSIT
];
ln.TRANSPORT_MODES[ln.PLATFORM.ANDROID][ln.APP.BAIDU] = [
    ln.TRANSPORT_MODE.DRIVING,
    ln.TRANSPORT_MODE.WALKING,
    ln.TRANSPORT_MODE.BICYCLING,
    ln.TRANSPORT_MODE.TRANSIT
];
ln.TRANSPORT_MODES[ln.PLATFORM.ANDROID][ln.APP.GAODE] = [
    ln.TRANSPORT_MODE.DRIVING,
    ln.TRANSPORT_MODE.WALKING,
    ln.TRANSPORT_MODE.BICYCLING,
    ln.TRANSPORT_MODE.TRANSIT
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
    ln.TRANSPORT_MODE.WALKING,
    ln.TRANSPORT_MODE.TRANSIT
];
ln.TRANSPORT_MODES[ln.PLATFORM.IOS][ln.APP.SYGIC] = [
    ln.TRANSPORT_MODE.DRIVING,
    ln.TRANSPORT_MODE.WALKING
];
ln.TRANSPORT_MODES[ln.PLATFORM.IOS][ln.APP.MAPS_ME] = [
    ln.TRANSPORT_MODE.DRIVING,
    ln.TRANSPORT_MODE.WALKING,
    ln.TRANSPORT_MODE.BICYCLING,
    ln.TRANSPORT_MODE.TRANSIT
];
ln.TRANSPORT_MODES[ln.PLATFORM.IOS][ln.APP.BAIDU] = [
    ln.TRANSPORT_MODE.DRIVING,
    ln.TRANSPORT_MODE.WALKING,
    ln.TRANSPORT_MODE.BICYCLING,
    ln.TRANSPORT_MODE.TRANSIT
];
ln.TRANSPORT_MODES[ln.PLATFORM.IOS][ln.APP.GAODE] = [
    ln.TRANSPORT_MODE.DRIVING,
    ln.TRANSPORT_MODE.WALKING,
    ln.TRANSPORT_MODE.BICYCLING,
    ln.TRANSPORT_MODE.TRANSIT
];

/**
 * Apps by platform that support specifying a start location
 * @type {object}
 */
ln.SUPPORTS_START = {};
ln.SUPPORTS_START[ln.PLATFORM.ANDROID] = [
    ln.APP.USER_SELECT,
    ln.APP.GOOGLE_MAPS, // Only launchMode=maps
    ln.APP.CITYMAPPER,
    ln.APP.UBER,
    ln.APP.YANDEX,
    ln.APP.HERE_MAPS,
    ln.APP.MOOVIT,
    ln.APP.LYFT,
    ln.APP.MAPS_ME,
    ln.APP.CABIFY,
    ln.APP.BAIDU,
    ln.APP.TAXIS_99,
    ln.APP.GAODE
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
    ln.APP.MOOVIT,
    ln.APP.LYFT,
    ln.APP.MAPS_ME,
    ln.APP.CABIFY,
    ln.APP.BAIDU,
    ln.APP.TAXIS_99,
    ln.APP.GAODE
];
ln.SUPPORTS_START[ln.PLATFORM.WINDOWS] = [
    ln.APP.BING_MAPS
];

/**
 * Apps by platform that support specifying a start nickname
 * @type {object}
 */
ln.SUPPORTS_START_NAME = {};
ln.SUPPORTS_START_NAME[ln.PLATFORM.ANDROID] = [
    ln.APP.USER_SELECT,
    ln.APP.CITYMAPPER,
    ln.APP.UBER,
    ln.APP.HERE_MAPS,
    ln.APP.MOOVIT,
    ln.APP.CABIFY,
    ln.APP.BAIDU,
    ln.APP.TAXIS_99,
    ln.APP.GAODE
];
ln.SUPPORTS_START_NAME[ln.PLATFORM.IOS] = [
    ln.APP.USER_SELECT,
    ln.APP.APPLE_MAPS, // Only launchMode=mapkit
    ln.APP.CITYMAPPER,
    ln.APP.UBER,
    ln.APP.HERE_MAPS,
    ln.APP.MOOVIT,
    ln.APP.CABIFY,
    ln.APP.BAIDU,
    ln.APP.TAXIS_99,
    ln.APP.GAODE
];

/**
 * Apps by platform that support specifying a destination nickname
 * @type {object}
 */
ln.SUPPORTS_DEST_NAME = {};
ln.SUPPORTS_DEST_NAME[ln.PLATFORM.ANDROID] = [
    ln.APP.USER_SELECT,
    ln.APP.GOOGLE_MAPS, // only launchMode=geo
    ln.APP.CITYMAPPER,
    ln.APP.UBER,
    ln.APP.HERE_MAPS,
    ln.APP.MOOVIT,
    ln.APP.CABIFY,
    ln.APP.BAIDU,
    ln.APP.TAXIS_99,
    ln.APP.GAODE
];
ln.SUPPORTS_DEST_NAME[ln.PLATFORM.IOS] = [
    ln.APP.USER_SELECT,
    ln.APP.APPLE_MAPS, // Only launchMode=mapkit
    ln.APP.CITYMAPPER,
    ln.APP.NAVIGON,
    ln.APP.UBER,
    ln.APP.TOMTOM,
    ln.APP.HERE_MAPS,
    ln.APP.MOOVIT,
    ln.APP.CABIFY,
    ln.APP.BAIDU,
    ln.APP.TAXIS_99,
    ln.APP.GAODE
];

/**
 * Apps by platform that support specifying a launch mode
 * @type {object}
 */
ln.SUPPORTS_LAUNCH_MODE = {};
ln.SUPPORTS_LAUNCH_MODE[ln.PLATFORM.ANDROID] = [
    ln.APP.USER_SELECT,
    ln.APP.GOOGLE_MAPS
];
ln.SUPPORTS_LAUNCH_MODE[ln.PLATFORM.IOS] = [
    ln.APP.USER_SELECT,
    ln.APP.APPLE_MAPS
];

ln.COORDS_REGEX = /^[-\d.]+,[\s]*[-\d.]+$/;



/******************
 * Public API
 ******************/

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
 * @return {string[]} - apps supported on specified platform as a list of `launchnavigator.APP` constants.
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
 * @return {string[]} - list of transports modes as constants in `launchnavigator.TRANSPORT_MODE`.
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
 * @param {string/number[]} destination (required) - destination location to use for navigation - see launchnavigator.navigate()
 * @param {object} [options={}] - optional parameters - see launchnavigator.navigate()
 * @param {function} successCallback - function executed in case of success
 * @param {function} errorCallback - function executed in case of error
 */
ln.userSelect = function(destination, options, successCallback, errorCallback){
    var userSelectDisplayed, app;

    options = options || {};
    options.errorCallback = options.errorCallback || errorCallback || emptyFn;
    options.successCallback = options.successCallback || successCallback || emptyFn;
    
    // app selection
    options.appSelection = options.appSelection || {};
    options.appSelection.dialogPositionX = options.appSelection.dialogPositionX || 550;
    options.appSelection.dialogPositionY = options.appSelection.dialogPositionY || 500;
    options.appSelection.callback = options.appSelection.callback || emptyFn;
    options.appSelection.rememberChoice = options.appSelection.rememberChoice || {};
    options.appSelection.rememberChoice.enabled = typeof options.appSelection.rememberChoice.enabled !== "undefined" ? options.appSelection.rememberChoice.enabled : "prompt";
    options.appSelection.rememberChoice.prompt = options.appSelection.rememberChoice.prompt || {};
    options.appSelection.rememberChoice.prompt.callback = options.appSelection.rememberChoice.prompt.callback || emptyFn;
    
    var buttonList = [], buttonMap = {};

    if(userSelectDisplayed) return;

    var launchApp = function(){
        options.app = app;
        launchnavigator.navigate(destination, options);
    };

    var displayChooser = function(){
        userSelectDisplayed = true;
        window.plugins.actionsheet.show({
            'androidTheme': options.appSelection.androidTheme || window.plugins.actionsheet.ANDROID_THEMES.THEME_HOLO_LIGHT,
            'title': options.appSelection.dialogHeaderText || DEFAULT_appSelectionDialogHeaderText,
            'buttonLabels': buttonList,
            'androidEnableCancelButton' : true, // default false
            //'winphoneEnableCancelButton' : true, // default false
            'addCancelButtonWithLabel': options.appSelection.cancelButtonText || DEFAULT_appSelectionCancelButtonText,
            'position': [options.appSelection.dialogPositionX, options.appSelection.dialogPositionY] // for iPad pass in the [x, y] position of the popover
        }, onChooseApp);
    };

    var onChooseApp = function (btnNumber){
        userSelectDisplayed = false;
        var idx = btnNumber - 1;
        app = buttonMap[idx];
        if(app !== "cancel"){
            options.appSelection.callback(app);
            if(options.appSelection.rememberChoice.enabled === true || options.appSelection.rememberChoice.enabled === "true"){
                rememberUserChoiceAndLaunch();
            }else if(options.appSelection.rememberChoice.enabled === false || options.appSelection.rememberChoice.enabled === "false"){
                // Don't remember, just launch app
                launchApp();
            }else{
                // Default
                checkIfAlreadyPrompted();
            }
        } else {
            options.errorCallback('cancelled');
        }
    };

    var rememberUserChoiceAndLaunch = function(){
        setItem("choice", app, function(){
            launchApp();
        });
    };

    var checkForChoice = function(availableApps){
        getItem("choice", function(choice){
            if(choice){
                if(availableApps[choice]){
                    app = choice;
                    launchApp();
                }else{
                    // Chosen app is no longer available on device
                    ln.appSelection.userChoice.clear(function(){
                        ln.appSelection.userPrompted.clear(function(){
                            displayChooser();
                        });
                    })
                }
            }else{
                displayChooser();
            }
        })
    };

    // Check if user has already been prompted whether to remember their choice
    var checkIfAlreadyPrompted = function(){
        getItem("prompted", function(prompted){
            if(prompted){
                launchApp();
            }else{
                promptUser();
            }
        });
    };

    // Prompt user whether to remember their choice
    var promptUser = function(){
        if(options.appSelection.rememberChoice.promptFn){
            options.appSelection.rememberChoice.promptFn(handleUserPromptChoice);
        }else{
            // Show prompt using cordova dialogs
            navigator.notification.confirm(
                options.appSelection.rememberChoice.prompt.bodyText || DEFAULT_rememberChoicePromptDialogBodyText,
                function(idx){
                    handleUserPromptChoice(idx === 1);
                },
                options.appSelection.rememberChoice.prompt.headerText || DEFAULT_rememberChoicePromptDialogHeaderText,
                [
                    options.appSelection.rememberChoice.prompt.yesButtonText || DEFAULT_rememberChoicePromptDialogYesButtonText,
                    options.appSelection.rememberChoice.prompt.noButtonText || DEFAULT_rememberChoicePromptDialogNoButtonText
                ]
            );
        }
    };

    var handleUserPromptChoice = function(shouldRemember){
        options.appSelection.rememberChoice.prompt.callback(shouldRemember);
        setItem("prompted", true, function(){
            if(shouldRemember){
                rememberUserChoiceAndLaunch();
            }else{
                launchApp();
            }
        });
    };


    // Get list of available apps
    launchnavigator.availableApps(function(apps){
        for(var _app in apps){
            var isAvailable = apps[_app];
            if(!isAvailable) continue;
            if(options.appSelection.list && options.appSelection.list.length > 0 && !ln.util.arrayContainsValue(options.appSelection.list, _app)) continue;
            buttonList.push(ln.getAppDisplayName(_app));
            buttonMap[buttonList.length-1] = _app;
        }

        if(buttonList.length === 0){
            return options.errorCallback("No supported navigation apps are available on the device");
        }

        if(buttonList.length === 1){
            app = buttonMap[0];
            return launchApp();
        }

        buttonMap[buttonList.length] = "cancel"; // Add an entry for cancel button

        // Check if a user choice exists
        checkForChoice(apps);

    }, options.errorCallback);
};

/*****************************
 * App selection API functions
 *****************************/
ln.appSelection = {
    userChoice: {},
    userPrompted: {}
};

/**
 * Indicates whether a user choice exists for a preferred navigator app.
 * @param {function} cb - function to pass result to: will receive a boolean argument.
 */
ln.appSelection.userChoice.exists = function(cb){
    itemExists("choice", cb);
};

/**
 * Returns current user choice of preferred navigator app.
 * @param {function} cb - function to pass result to: will receive a string argument indicating the app, which is a constant in `launchnavigator.APP`.
 * If no current choice exists, value will be null.
 */
ln.appSelection.userChoice.get = function(cb){
    getItem("choice", cb);
};

/**
 * Sets the current user choice of preferred navigator app.
 * @param {string} app - app to set as preferred choice as a constant in `launchnavigator.APP`.
 * @param {function} cb - function to call once operation is complete.
 */
ln.appSelection.userChoice.set = function(app, cb){
    setItem("choice", app, cb);
};

/**
 * Clears current user choice of preferred navigator app.
 * @param {function} cb - function to call once operation is complete.
 */
ln.appSelection.userChoice.clear = function(cb){
    removeItem("choice", cb);
};

/**
 * Indicates whether user has already been prompted whether to remember their choice a preferred navigator app.
 * @param {function} cb - function to pass result to: will receive a boolean argument.
 */
ln.appSelection.userPrompted.get = function(cb){
    itemExists("prompted", cb);
};

/**
 * Sets flag indicating user has already been prompted whether to remember their choice a preferred navigator app.
 * @param {function} cb - function to call once operation is complete.
 */
ln.appSelection.userPrompted.set = function(cb){
    setItem("prompted", true, cb);
};

/**
 * Clears flag which indicates if user has already been prompted whether to remember their choice a preferred navigator app.
 * @param {function} cb - function to call once operation is complete.
 */
ln.appSelection.userPrompted.clear = function(cb){
    removeItem("prompted", cb);
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
    if(app === "none") return true; // native chooser
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
    if(location && typeof(location) === "string" && location.match(ln.COORDS_REGEX)){
        location = location.replace(/\s*/g,'');
        var parts = location.split(",");
        location = [parts[0], parts[1]];
    }
    return location;
};

ln.util.isValidLaunchMode = function(launchMode){
    for(var LAUNCH_MODE in ln.LAUNCH_MODE){
        if(launchMode === ln.LAUNCH_MODE[LAUNCH_MODE]) return true;
    }
    return false;
};

ln.util.validateLaunchMode = function(launchMode){
    if(!ln.util.isValidLaunchMode(launchMode)){
        throw new Error("'"+launchMode+"' is not a recognised launch mode");
    }
};

ln.util.conformNavigateOptions = function(args){
    var options;
    if(args.length > 1 && typeof args[1] === "function"){
        // assume (dest, success, error, opts)
        options = (args.length > 3 && typeof args[3] === "object") ? args[3] : {};
        options.successCallback = args[1];
        if(args.length > 2 && typeof args[2] === "function"){
            options.errorCallback = args[2];
        }
    }else{
        // assume (dest, opts)
        options = (args.length > 1 && typeof args[1] === "object") ? args[1] : {};
    }
    return options;
};


module.exports = ln;
