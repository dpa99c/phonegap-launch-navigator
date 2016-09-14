// CMMapLauncher.h
// Last updated 2013-08-26
//
// Copyright (c) 2013 Citymapper Ltd. All rights reserved.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

// README
//
// This pair of classes simplifies the process of launching various mapping
// applications to display directions.  Here's the simplest use case:
//
// CLLocationCoordinate2D bigBen =
//     CLCLLocationCoordinate2DMake(51.500755, -0.124626);
// [CMMapLauncher launchMapApp:CMMapAppAppleMaps
//             forDirectionsTo:[CMMapPoint mapPointWithName:@"Big Ben"
//                                               coordinate:bigBen]];

#import <Foundation/Foundation.h>
#import <MapKit/MapKit.h>

@class CMMapPoint;

///---------------------------
/// CMMapLauncher (main class)
///---------------------------

// This enumeration identifies the mapping apps
// that this launcher knows how to support.
typedef NS_ENUM(NSUInteger, CMMapApp) {
    CMMapAppAppleMaps = 0,  // Preinstalled Apple Maps
    CMMapAppCitymapper,     // Citymapper
    CMMapAppGoogleMaps,     // Standalone Google Maps App
    CMMapAppNavigon,        // Navigon
    CMMapAppTheTransitApp,  // The Transit App
    CMMapAppWaze,           // Waze
    CMMapAppYandex,         // Yandex Navigator
    CMMapAppUber,           // Uber
    CMMapAppTomTom,         // TomTom
    CMMapAppSygic,          // Sygic
    CMMapAppHereMaps,       // HERE Maps
    CMMapAppMoovit          // Moovit
    
};

/**
 Indicates an empty address
 */
NSString* CMEmptyAddress;

/**
 Indicates an empty coordinate
 */
CLLocationCoordinate2D CMEmptyCoord;

/**
 Indicates an empty latitude or longitude component
 */
static const CLLocationDegrees CMEmptyLocation = -1000.0;

@interface CMMapLauncher : NSObject

/**
 Enables debug logging which logs resulting URL scheme to console
 */
+ (void)enableDebugLogging;

/**
 Determines whether the given mapping app is installed.

 @param mapApp An enumeration value identifying a mapping application.

 @return YES if the specified app is installed, NO otherwise.
 */
+ (BOOL)isMapAppInstalled:(CMMapApp)mapApp;

/**
 Launches the specified mapping application with directions
 from the user's current location to the specified endpoint.

 @param mapApp An enumeration value identifying a mapping application.
 @param end The destination of the desired directions.

 @return YES if the mapping app could be launched, NO otherwise.
 */
+ (BOOL)launchMapApp:(CMMapApp)mapApp
     forDirectionsTo:(CMMapPoint *)end;


/**
 Launches the specified mapping application with directions
 from the user's current location to the specified endpoint
 and using the specified transport mode.
 
 @param mapApp An enumeration value identifying a mapping application.
 @param end The destination of the desired directions.
 @param directionsMode transport mode to use when getting directions.
 
 @return YES if the mapping app could be launched, NO otherwise.
 */
+ (BOOL)launchMapApp:(CMMapApp)mapApp
     forDirectionsTo:(CMMapPoint *)end
      directionsMode:(NSString *)directionsMode;

/**
 Launches the specified mapping application with directions
 between the two specified endpoints.

 @param mapApp An enumeration value identifying a mapping application.
 @param start The starting point of the desired directions.
 @param end The destination of the desired directions.

 @return YES if the mapping app could be launched, NO otherwise.
 */
+ (BOOL)launchMapApp:(CMMapApp)mapApp
   forDirectionsFrom:(CMMapPoint *)start
                  to:(CMMapPoint *)end;

/**
 Launches the specified mapping application with directions
 between the two specified endpoints
 and using the specified transport mode.
 
 @param mapApp An enumeration value identifying a mapping application.
 @param start The starting point of the desired directions.
 @param end The destination of the desired directions.
 @param directionsMode transport mode to use when getting directions.

 @return YES if the mapping app could be launched, NO otherwise.
 */
+ (BOOL)launchMapApp:(CMMapApp)mapApp
   forDirectionsFrom:(CMMapPoint *)start
                  to:(CMMapPoint *)end
      directionsMode:(NSString *)directionsMode;

/**
 Launches the specified mapping application with directions
 between the two specified endpoints
 and using the specified transport mode
 and including app-specific extra parameters

 @param mapApp An enumeration value identifying a mapping application.
 @param start The starting point of the desired directions.
 @param end The destination of the desired directions.
 @param directionsMode transport mode to use when getting directions.
 @param extras key/value map of app-specific extra parameters to pass to launched app

 @return YES if the mapping app could be launched, NO otherwise.
 */
+ (BOOL)launchMapApp:(CMMapApp)mapApp
   forDirectionsFrom:(CMMapPoint *)start
                  to:(CMMapPoint *)end
      directionsMode:(NSString *)directionsMode
      extras:(NSDictionary *)extras;

@end


///--------------------------
/// CMMapPoint (helper class)
///--------------------------

@interface CMMapPoint : NSObject

/**
 Determines whether this map point represents the user's current location.
 */
@property (nonatomic, assign) BOOL isCurrentLocation;

/**
 The geographical coordinate of the map point.
 */
@property (nonatomic, assign) CLLocationCoordinate2D coordinate;

/**
 The user-visible name of the given map point (optional, may be nil).
 */
@property (nonatomic, copy) NSString *name;

/**
 The address of the given map point (optional, may be nil).
 */
@property (nonatomic, copy) NSString *address;

/**
 Gives an MKMapItem corresponding to this map point object.
 */
@property (nonatomic, retain) MKMapItem *mapItem;

/**
 Creates a new CMMapPoint that signifies the current location.
 */
+ (CMMapPoint *)currentLocation;

/**
 Creates a new CMMapPoint with the given geographical coordinate.

 @param coordinate The geographical coordinate of the new map point.
 */
+ (CMMapPoint *)mapPointWithCoordinate:(CLLocationCoordinate2D)coordinate;

/**
 Creates a new CMMapPoint with the given name and coordinate.

 @param name The user-visible name of the new map point.
 @param coordinate The geographical coordinate of the new map point.
 */
+ (CMMapPoint *)mapPointWithName:(NSString *)name
                      coordinate:(CLLocationCoordinate2D)coordinate;

/**
 Creates a new CMMapPoint with the given name, address, and coordinate.

 @param name The user-visible name of the new map point.
 @param address The address string of the new map point.
 @param coordinate The geographical coordinate of the new map point.
 */
+ (CMMapPoint *)mapPointWithName:(NSString *)name
                         address:(NSString *)address
                      coordinate:(CLLocationCoordinate2D)coordinate;

/**
 Creates a new CMMapPoint with the given name, address, and coordinate.

 @param address The address string of the new map point.
 @param coordinate The geographical coordinate of the new map point.
 */
+ (CMMapPoint *)mapPointWithAddress:(NSString *)address
                         coordinate:(CLLocationCoordinate2D)coordinate;


+ (CMMapPoint *)mapPointWithMapItem:(MKMapItem *)mapItem
                               name:(NSString *)name
                            address:(NSString *)address
                         coordinate:(CLLocationCoordinate2D)coordinate;

@end
