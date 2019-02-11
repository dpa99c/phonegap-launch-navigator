/*
 * LN_LaunchNavigator Library
 *
 * Copyright (c) 2018 Dave Alden  (http://github.com/dpa99c)
 * Copyright (c) 2018 Working Edge Ltd. (http://www.workingedge.co.uk)
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

#import <MapKit/MapKit.h>
#import <CoreLocation/CoreLocation.h>
#import "WE_Logger.h"

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

static NSString*const LOG_TAG = @"LN_LaunchNavigator[native]";
static NSString*const LNLocTypeNone = @"none";
static NSString*const LNLocTypeBoth = @"both";
static NSString*const LNLocTypeAddress = @"name";
static NSString*const LNLocTypeCoords = @"coords";

/**
Indicates an empty coordinate
*/
static CLLocationCoordinate2D LNEmptyCoord;

/**
Indicates an empty latitude or longitude component
*/
static const CLLocationDegrees LNEmptyLocation = 0.000000;


@interface LN_LaunchNavigator : NSObject <CLLocationManagerDelegate> {}

typedef void (^NavigateSuccessBlock)(void);
typedef void (^NavigateFailBlock)(NSString* errorMsg);
typedef void(^LocationSuccessBlock)(CLLocation*);
typedef void(^LocationErrorBlock)(NSError*);

@property (nonatomic, strong) NavigateSuccessBlock navigateSuccess;
@property (nonatomic, strong) NavigateFailBlock navigateFail;
@property (nonatomic, strong) LocationSuccessBlock locationSuccess;
@property (nonatomic, strong) LocationErrorBlock locationError;
@property (retain, nonatomic) CLLocationManager* locationManager;

/*******************
* Public API
*******************/
- (id)init:(WE_Logger*) logger;
- (void)setLogger:(WE_Logger*) logger;
- (WE_Logger*)getLogger;
- (void) navigate:(NSDictionary*)params
          success:(NavigateSuccessBlock)success
             fail:(NavigateFailBlock)fail;
- (BOOL) isAppAvailable:(NSString*)appName;
- (NSDictionary*) availableApps;


@end
