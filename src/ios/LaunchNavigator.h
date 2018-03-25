/*
 * LaunchNavigator Plugin for Phonegap
 *
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
#ifdef CORDOVA_FRAMEWORK
#import <Cordova/CDVPlugin.h>
#else
#import "Cordova/CDVPlugin.h"
#endif

#import <MapKit/MapKit.h>
#import <CoreLocation/CoreLocation.h>

 // This enumeration identifies the mapping apps
 // that this launcher knows how to support.
typedef NS_ENUM(NSUInteger, LNApp) {
	LNAppAppleMaps = 0,  // Preinstalled Apple Maps
	LNAppCitymapper,     // Citymapper
	LNAppGoogleMaps,     // Standalone Google Maps App
	LNAppNavigon,        // Navigon
	LNAppTheTransitApp,  // The Transit App
	LNAppWaze,           // Waze
	LNAppYandex,         // Yandex Navigator
	LNAppUber,           // Uber
	LNAppTomTom,         // TomTom
	LNAppSygic,          // Sygic
	LNAppHereMaps,       // HERE Maps
	LNAppMoovit,         // Moovit
	LNAppLyft,           // Lyft
    LNAppMapsMe,          // MAPS.ME
    LNAppCabify,          // Cabify
    LNAppBaidu,           // Baidu
	LNAppTaxis99,           // 99 Taxi
	LNAppGaode           // Gaode (Amap)
};

static NSString*const LOG_TAG = @"LaunchNavigator[native]";
static NSString*const LNLocTypeNone = @"none";
static NSString*const LNLocTypeBoth = @"both";
static NSString*const LNLocTypeAddress = @"name";
static NSString*const LNLocTypeCoords = @"coords";

/**
Indicates an empty coordinate
*/
CLLocationCoordinate2D LNEmptyCoord;

/**
Indicates an empty latitude or longitude component
*/
static const CLLocationDegrees LNEmptyLocation = 0.000000;


@interface LaunchNavigator :CDVPlugin <CLLocationManagerDelegate> {
    BOOL debugEnabled;
    CDVInvokedUrlCommand* cordova_command;    
}
@property (nonatomic) BOOL debugEnabled;
@property (nonatomic,retain) CDVInvokedUrlCommand* cordova_command;
@property (retain, nonatomic) CLLocationManager* locationManager;

typedef void(^locationSuccess)(CLLocation*);
typedef void(^locationError)(NSError*);

@property (nonatomic, strong) locationSuccess _locationSuccess;
@property (nonatomic, strong) locationError _locationError;

- (void) navigate:(CDVInvokedUrlCommand*)command;
- (void) isAppAvailable:(CDVInvokedUrlCommand*)command;
- (void) availableApps:(CDVInvokedUrlCommand*)command;


@end
