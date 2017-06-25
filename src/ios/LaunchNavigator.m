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
#import "LaunchNavigator.h"
#import <SystemConfiguration/SCNetworkReachability.h>
#import "Reachability.h"

NSString*const LOG_TAG = @"LaunchNavigator[native]";

NSArray* appNames;

NSString*const LNLocTypeNone = @"none";
NSString*const LNLocTypeBoth = @"both";
NSString*const LNLocTypeAddress = @"name";
NSString*const LNLocTypeCoords = @"coords";


// Valid input location types for apps
static NSDictionary* AppLocationTypes;

@implementation LaunchNavigator
@synthesize debugEnabled;
@synthesize cordova_command;

BOOL enableGeocoding;
BOOL useMapKit;

LNApp app;
NSString* logMsg;

// Navigate JS args
NSString* jsDestination;
NSString* jsDestType;
NSString* jsDestName;
NSString* jsStart;
NSString* jsStartType;
NSString* jsStartName;
NSString* jsAppName;
NSString* jsTransportMode;
NSString* jsLaunchMode;
NSString* jsExtras;
BOOL jsEnableDebug;

// App inputs
BOOL startIsCurrentLocation;
MKMapItem* start_mapItem;
MKMapItem* dest_mapItem;
MKPlacemark* start_placemark;
MKPlacemark* dest_placemark;
CLLocationCoordinate2D destCoord;
CLLocationCoordinate2D startCoord;
NSString* destAddress;
NSString* startAddress;
NSString* destName;
NSString* startName;
NSString* directionsMode;
NSDictionary* extras;

/**************
 * Plugin API
 **************/

+ (void)initialize{
    appNames = @[@"apple_maps", @"citymapper", @"google_maps", @"navigon", @"transit_app", @"tomtom", @"uber", @"waze", @"yandex", @"sygic", @"here_maps", @"moovit", @"lyft"];
    AppLocationTypes = @{
                         @(LNAppAppleMaps): LNLocTypeBoth,
                         @(LNAppCitymapper): LNLocTypeBoth,
                         @(LNAppGoogleMaps): LNLocTypeBoth,
                         @(LNAppNavigon): LNLocTypeCoords,
                         @(LNAppTheTransitApp): LNLocTypeCoords,
                         @(LNAppWaze): LNLocTypeCoords,
                         @(LNAppYandex): LNLocTypeCoords,
                         @(LNAppUber): LNLocTypeCoords,
                         @(LNAppTomTom): LNLocTypeCoords,
                         @(LNAppSygic): LNLocTypeCoords,
                         @(LNAppHereMaps): LNLocTypeCoords,
                         @(LNAppMoovit): LNLocTypeCoords,
                         @(LNAppLyft): LNLocTypeCoords
                         };
    LNEmptyCoord = CLLocationCoordinate2DMake(LNEmptyLocation, LNEmptyLocation);
}

- (void) navigate:(CDVInvokedUrlCommand*)command;
{
    self.cordova_command = command;
    
    @try {
        // Reset state
        startIsCurrentLocation = FALSE;
        destAddress = nil;
        destCoord = LNEmptyCoord;
        startAddress = nil;
        startCoord = LNEmptyCoord;
        destName = nil;
        startName = nil;
        directionsMode = nil;
        extras = nil;
        
        // Get JS arguments
        jsDestination = [command.arguments objectAtIndex:0];
        jsDestType = [command.arguments objectAtIndex:1];
        jsDestName = [command.arguments objectAtIndex:2];
        jsStart = [command.arguments objectAtIndex:3];
        jsStartType = [command.arguments objectAtIndex:4];
        jsStartName = [command.arguments objectAtIndex:5];
        jsAppName = [command.arguments objectAtIndex:6];
        jsTransportMode = [command.arguments objectAtIndex:7];
        jsLaunchMode = [command.arguments objectAtIndex:8];
        jsEnableDebug = [[command argumentAtIndex:9] boolValue];
        jsExtras = [command.arguments objectAtIndex:10];
        enableGeocoding = [[command argumentAtIndex:11] boolValue];
        
        if([jsLaunchMode isEqual: @"mapkit"]){
            useMapKit = TRUE;
        }else{
            useMapKit = FALSE;
        }
        
        if(jsEnableDebug == TRUE){
            self.debugEnabled = jsEnableDebug;
        }else{
            self.debugEnabled = FALSE;
        }
        
        [self logDebug:[NSString stringWithFormat:@"Called navigate() with args: destination=%@; destType=%@; destName=%@; start=%@; startType=%@; startName=%@; appName=%@; transportMode=%@; extras=%@", jsDestination, jsDestType, jsDestName, jsStart, jsStartType, jsStartName, jsAppName, jsTransportMode, jsExtras]];
        
        LNApp app = [self mapApp_NameToLN:jsAppName];
        BOOL isAvailable = [self isMapAppInstalled:app];
        if(!isAvailable){
            [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[NSString stringWithFormat:@"%@ is not installed on the device", jsAppName]] callbackId:self.cordova_command.callbackId];
            return;
        }
        
        if (![jsDestination isKindOfClass:[NSString class]]) {
            [self sendPluginError:@"Missing destination argument"];
            return;
        }
        
        if(![self isNull:jsTransportMode]){
            directionsMode = jsTransportMode;
        }
        
        
        logMsg = [NSString stringWithFormat:@"Using %@ to navigate", jsAppName];
        [self getDest:^{
            if([jsStartType isEqual: LNLocTypeNone]){
                [self launchApp];
            }else{
                [self getStart:^{
                    [self launchApp];
                }];
            }
            
        }];
    }@catch (NSException *exception) {
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.reason] callbackId:self.cordova_command.callbackId];
    }
    
    
}

- (void) isAppAvailable:(CDVInvokedUrlCommand*)command;{
    @try {
        LNApp app = [self mapApp_NameToLN:[command.arguments objectAtIndex:0]];
        BOOL result = [self isMapAppInstalled:app];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:result];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }@catch (NSException *exception) {
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.reason] callbackId:command.callbackId];
    }
}

- (void) availableApps:(CDVInvokedUrlCommand*)command;{
    NSMutableDictionary* results = [NSMutableDictionary new];
    @try {
        for(id object in appNames){
            NSString* jsAppName = object;
            LNApp app = [self mapApp_NameToLN:jsAppName];
            BOOL result = [self isMapAppInstalled:app];
            [results setObject:[NSNumber numberWithBool:result] forKey:jsAppName];
        }
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:results];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }@catch (NSException *exception) {
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.reason] callbackId:command.callbackId];
    }
}

/**************
 * Apps
 **************/
-(void)launchAppleMaps {
    if(useMapKit){
        [self launchAppleMapsWithMapKit];
    }else{
        [self launchAppleMapsWithURI];
    }
}

-(void)launchAppleMapsWithURI {
    
    NSMutableString* url = [[NSString stringWithFormat:@"%@?",
                             [self urlPrefixForMapApp:LNAppAppleMaps]
                             ] mutableCopy];
    
    if([self isEmptyCoordinate:destCoord]){
        [url appendFormat:@"daddr=%@", [destAddress stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    }else{
        [url appendFormat:@"daddr=%@", [self stringForCoord:destCoord]];
    }
    
    if(![jsStartType isEqual: LNLocTypeNone]){
        if([self isEmptyCoordinate:startCoord]){
            [url appendFormat:@"&saddr=%@", [startAddress stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
        }else{
            [url appendFormat:@"&saddr=%@", [self stringForCoord:startCoord]];
        }
    }
    
    if (directionsMode) {
        if([directionsMode isEqual: @"walking"]){
            [url appendFormat:@"&dirflg=w"];
        }else if([directionsMode isEqual: @"transit"]){
            [url appendFormat:@"&dirflg=r"];
        }else{
            [url appendFormat:@"&dirflg=d"];
        }
    }
    
    if(extras){
        [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
    }
    [self logDebugURI:url];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
}

-(void)launchAppleMapsWithMapKit {
    NSDictionary* launchOptions;
    if (directionsMode) {
        if([directionsMode isEqual: @"walking"]){
            directionsMode = MKLaunchOptionsDirectionsModeWalking;
        }else if([directionsMode isEqual: @"transit"]){
            directionsMode = MKLaunchOptionsDirectionsModeTransit;
        }else{
            directionsMode = MKLaunchOptionsDirectionsModeDriving;
        }
        launchOptions = @{MKLaunchOptionsDirectionsModeKey: directionsMode};
    } else {
        launchOptions = @{MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeDriving};
    }
    
    if(extras){
        NSEnumerator* keyEnum = [extras keyEnumerator];
        id key;
        while ((key = [keyEnum nextObject]))
        {
            launchOptions = @{key: [extras objectForKey:key]};
        }
    }
    
    if(!destAddress){
        MKPlacemark* placemark = [[MKPlacemark alloc] initWithCoordinate:destCoord];
        dest_mapItem = [[MKMapItem alloc] initWithPlacemark:placemark];
        if(destName){
            [dest_mapItem setName:destName];
        }
    }
    
    if([jsStartType isEqual: LNLocTypeNone]){
        [MKMapItem openMapsWithItems:@[dest_mapItem] launchOptions:launchOptions];
    }else{
        if(!startAddress){
            start_mapItem = [[MKMapItem alloc] initWithPlacemark:[[MKPlacemark alloc] initWithCoordinate:startCoord]];
            if(startName){
                [start_mapItem setName:startName];
            }
        }
        [MKMapItem openMapsWithItems:@[start_mapItem, dest_mapItem] launchOptions:launchOptions];
    }
}

-(void)launchGoogleMaps {
    
    NSMutableString* url = [[NSString stringWithFormat:@"%@?",
                             [self urlPrefixForMapApp:LNAppGoogleMaps]
                             ] mutableCopy];
    
    if([self isEmptyCoordinate:destCoord]){
        [url appendFormat:@"daddr=%@", [destAddress stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    }else{
        [url appendFormat:@"daddr=%@", [self stringForCoord:destCoord]];
    }
    
    if(![jsStartType isEqual: LNLocTypeNone]){
        if([self isEmptyCoordinate:startCoord]){
            [url appendFormat:@"&saddr=%@", [startAddress stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
        }else{
            [url appendFormat:@"&saddr=%@", [self stringForCoord:startCoord]];
        }
    }
    
    if (directionsMode) {
        [url appendFormat:@"&directionsmode=%@", directionsMode];
    }
    
    if(extras){
        [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
    }
    [self logDebugURI:url];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
}

-(void)launchCitymapper {
    NSMutableArray* params = [NSMutableArray arrayWithCapacity:10];
    if (!startIsCurrentLocation) {
        if(![self isEmptyCoordinate:startCoord]){
            [params addObject:[NSString stringWithFormat:@"startcoord=%f,%f", startCoord.latitude, startCoord.longitude]];
        }
        if(startName){
            [params addObject:[NSString stringWithFormat:@"startname=%@", [self urlEncode:startName]]];
        }
        if(startAddress){
            [params addObject:[NSString stringWithFormat:@"startaddress=%@", [self urlEncode:startAddress]]];
        }
    }
    
    if(![self isEmptyCoordinate:destCoord]){
        [params addObject:[NSString stringWithFormat:@"endcoord=%f,%f", destCoord.latitude, destCoord.longitude]];
    }
    if(destName){
        [params addObject:[NSString stringWithFormat:@"endname=%@", [self urlEncode:destName]]];
    }
    if(destAddress){
        [params addObject:[NSString stringWithFormat:@"endaddress=%@", [self urlEncode:destAddress]]];
    }
    
    NSMutableString* url = [NSMutableString stringWithFormat:@"%@directions?%@",
                            [self urlPrefixForMapApp:LNAppCitymapper],
                            [params componentsJoinedByString:@"&"]];
    if(extras){
        [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
    }
    [self logDebugURI:url];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
}

-(void)launchTheTransitApp {
    // http://thetransitapp.com/developers
    
    NSMutableArray* params = [NSMutableArray arrayWithCapacity:2];
    if (!startIsCurrentLocation) {
        [params addObject:[NSString stringWithFormat:@"from=%f,%f", startCoord.latitude, startCoord.longitude]];
    }
    
    [params addObject:[NSString stringWithFormat:@"to=%f,%f", destCoord.latitude, destCoord.longitude]];
    
    NSMutableString* url = [NSMutableString stringWithFormat:@"%@directions?%@",
                            [self urlPrefixForMapApp:LNAppTheTransitApp],
                            [params componentsJoinedByString:@"&"]];
    if(extras){
        [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
    }
    [self logDebugURI:url];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
}

-(void)launchNavigon {
    // http://www.navigon.com/portal/common/faq/files/NAVIGON_AppInteract.pdf
    
    NSString* name = @"Destination";  // Doc doesn't say whether name can be omitted
    if (destName) {
        name = destName;
    }
    NSMutableString* url = [NSMutableString stringWithFormat:@"%@coordinate/%@/%f/%f",
                            [self urlPrefixForMapApp:LNAppNavigon],
                            [self urlEncode:name], destCoord.longitude, destCoord.latitude];
    if(extras){
        [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
    }
    [self logDebugURI:url];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
}

-(void)launchWaze {
    NSMutableString* url = [NSMutableString stringWithFormat:@"%@?ll=%f,%f&navigate=yes",
                            [self urlPrefixForMapApp:LNAppWaze],
                            destCoord.latitude, destCoord.longitude];
    if(extras){
        [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
    }
    [self logDebugURI:url];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
}

-(void)launchYandex {
    NSMutableString* url = nil;
    if (startIsCurrentLocation) {
        url = [NSMutableString stringWithFormat:@"%@build_route_on_map?lat_to=%f&lon_to=%f",
               [self urlPrefixForMapApp:LNAppYandex],
               destCoord.latitude, destCoord.longitude];
    } else {
        url = [NSMutableString stringWithFormat:@"%@build_route_on_map?lat_to=%f&lon_to=%f&lat_from=%f&lon_from=%f",
               [self urlPrefixForMapApp:LNAppYandex],
               destCoord.latitude, destCoord.longitude, startCoord.latitude, startCoord.longitude];
    }
    if(extras){
        [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
    }
    [self logDebugURI:url];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
}

-(void)launchUber {
    NSMutableString* url = nil;
    if (startIsCurrentLocation) {
        url = [NSMutableString stringWithFormat:@"%@?action=setPickup&pickup=my_location&dropoff[latitude]=%f&dropoff[longitude]=%f",
               [self urlPrefixForMapApp:LNAppUber],
               destCoord.latitude,
               destCoord.longitude];
        
        if(destName){
            url = [NSMutableString stringWithFormat:@"%@&dropoff[nickname]=%@",url,[destName stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
        }
        
    } else {
        url = [NSMutableString stringWithFormat:@"%@?action=setPickup&pickup[latitude]=%f&pickup[longitude]=%f&dropoff[latitude]=%f&dropoff[longitude]=%f",
               [self urlPrefixForMapApp:LNAppUber],
               startCoord.latitude,
               startCoord.longitude,
               destCoord.latitude,
               destCoord.longitude];
        
        if(destName){
            url = [NSMutableString stringWithFormat:@"%@&dropoff[nickname]=%@",url,[destName stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
        }
        
        if(startName){
            url = [NSMutableString stringWithFormat:@"%@&pickup[nickname]=%@",url,[startName stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
        }
    }
    if(extras){
        [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
    }
    [self logDebugURI:url];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
}

-(void)launchTomTom {
    NSMutableString* url = [NSMutableString stringWithFormat:@"tomtomhome:geo:action=navigateto&lat=%f&long=%f",
                            destCoord.latitude,
                            destCoord.longitude];
    if(destName){
        url = [NSMutableString stringWithFormat:@"%@&name=%@",url,[destName stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    }
    if(extras){
        [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
    }
    [self logDebugURI:url];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
}

-(void)launchSygic {
    if([directionsMode isEqual: @"walking"]){
        directionsMode = @"walk";
    }else{
        directionsMode = @"drive";
    }
    NSString* separator = @"%7C";
    NSMutableString* url = [NSMutableString stringWithFormat:@"%@coordinate%@%f%@%f%@%@",
                            [self urlPrefixForMapApp:LNAppSygic],
                            separator,
                            destCoord.longitude,
                            separator,
                            destCoord.latitude,
                            separator,
                            directionsMode];
    [self logDebugURI:url];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
}

-(void)launchHereMaps {
    NSMutableString* startParam;
    if (startIsCurrentLocation) {
        startParam = (NSMutableString*) @"mylocation";
    } else {
        startParam = [NSMutableString stringWithFormat:@"%f,%f",
                      startCoord.latitude, startCoord.longitude];
        
        if (startName) {
            [startParam appendFormat:@",%@", [startName stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
        }
    }
    
    NSMutableString* destParam = [NSMutableString stringWithFormat:@"%f,%f",
                                  destCoord.latitude, destCoord.longitude];
    
    if (destName) {
        [destParam appendFormat:@",%@", [destName stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    }
    
    NSMutableString* url = [NSMutableString stringWithFormat:@"%@%@/%@",
                            [self urlPrefixForMapApp:LNAppHereMaps],
                            startParam,
                            destParam];
    
    if(extras){
        [url appendFormat:@"?%@", [self extrasToQueryParams:extras]];
    }
    [self logDebugURI:url];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
}

-(void)launchMoovit {
    NSMutableString* url = [NSMutableString stringWithFormat:@"%@directions", [self urlPrefixForMapApp:LNAppMoovit]];
    
    [url appendFormat:@"?dest_lat=%f&dest_lon=%f",
     destCoord.latitude, destCoord.longitude];
    
    if (destName) {
        [url appendFormat:@"&dest_name=%@", [destName stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    }
    
    if (!startIsCurrentLocation) {
        [url appendFormat:@"&orig_lat=%f&orig_lon=%f",
         startCoord.latitude, startCoord.longitude];
        
        if (startName) {
            [url appendFormat:@"&orig_name=%@", [startName stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
        }
    }
    
    if(extras){
        [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
    }
    [self logDebugURI:url];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
}

-(void)launchLyft {
    NSMutableString* url = [NSMutableString stringWithFormat:@"%@ridetype?", [self urlPrefixForMapApp:LNAppLyft]];
    
    if(extras){
        [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
    }
    if(!extras || ![extras objectForKey:@"id"]){
        [url appendFormat:@"%@", @"id=lyft"];
    }
    
    [url appendFormat:@"&destination[latitude]=%f&destination[longitude]=%f",
     destCoord.latitude, destCoord.longitude];
    
    if (!startIsCurrentLocation) {
        [url appendFormat:@"&pickup[latitude]=%f&pickup[longitude]=%f",
         startCoord.latitude, startCoord.longitude];
    }
    
    [self logDebugURI:url];
    [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
}

/**************
 * Utilities
 **************/

- (void) getDest:(void (^)(void))completeBlock{
    
    logMsg = [NSString stringWithFormat:@"%@ to %@", logMsg, jsDestination];
    
    if(![self isNull:jsDestName]){
        destName = jsDestName;
    }
    
    if([jsDestType isEqual: LNLocTypeCoords]){
        destCoord = [self stringToCoords:jsDestination];
        
        
        if([AppLocationTypes objectForKey:@(app)] == LNLocTypeAddress){
            if([self isGeocodingEnabled]){
                if([self isNetworkAvailable]){
                    [self reverseGeocode:jsDestination success:^(MKMapItem* destItem, MKPlacemark* destPlacemark) {
                        dest_mapItem = destItem;
                        dest_placemark = destPlacemark;
                        destAddress = [self getAddressFromPlacemark:dest_placemark];
                        logMsg = [NSString stringWithFormat:@"%@ [%@]", logMsg, destAddress];
                        
                        if(destName){
                            [dest_mapItem setName:destName];
                        }else{
                            [dest_mapItem setName:destPlacemark.name];
                            destName = destPlacemark.name;
                        }
                        completeBlock();
                    } fail:^(NSString* failMsg) {
                        [self sendPluginError:failMsg];
                    }
                     ];
                }else{
                    [self sendPluginError:[NSString stringWithFormat:@"Failed to reverse geocode: no internet connection. %@ requires destination as address but plugin was passed a lat,lon", jsAppName]];
                }
            }else{
                [self sendPluginError:[NSString stringWithFormat:@"Failed to reverse geocode: geocoding disabled. %@ requires destination as address but plugin was passed a lat,lon", jsAppName]];
            }
        }else{
            completeBlock();
        }
    }else{ // [jsDestType isEqual: LNLocTypeAddress]
        destAddress = jsDestination;
        
        if([AppLocationTypes objectForKey:@(app)] == LNLocTypeCoords || (app == LNAppAppleMaps && useMapKit)){
            if([self isGeocodingEnabled]){
                if([self isNetworkAvailable]){
                    [self geocode:jsDestination success:^(MKMapItem* destItem, MKPlacemark* destPlacemark) {
                        dest_mapItem = destItem;
                        dest_placemark = destPlacemark;
                        destCoord = dest_placemark.coordinate;
                        logMsg = [NSString stringWithFormat:@"%@ [%@]", logMsg, [self coordsToString:destCoord]];
                        
                        if(destName){
                            [dest_mapItem setName:destName];
                        }else{
                            [dest_mapItem setName:destPlacemark.name];
                            destName = destPlacemark.name;
                        }
                        completeBlock();
                    }];
                }else{
                    [self sendPluginError:[NSString stringWithFormat:@"Failed to geocode: no internet connection. %@ requires destination as lat,lon but plugin was passed an address", jsAppName]];
                }
            }else{
                [self sendPluginError:[NSString stringWithFormat:@"Failed to geocode: geocoding disabled. %@ requires destination as lat,lon but plugin was passed an address", jsAppName]];
            }
        }else{
            completeBlock();
        }
    }
}

- (void) getStart:(void (^)(void))completeBlock{
    if(![self isNull:jsStartName]){
        startName = jsStartName;
    }
    if([jsStartType isEqual: LNLocTypeNone]){
        start_mapItem = [MKMapItem mapItemForCurrentLocation];
        startIsCurrentLocation = TRUE;
        logMsg = [NSString stringWithFormat:@"%@ from current location", logMsg];
        if([self isNull:startName]){
            startName = @"Current location";
        }
        completeBlock();
    }else if([jsStartType isEqual: LNLocTypeCoords]){
        startCoord = [self stringToCoords:jsStart];
        logMsg = [NSString stringWithFormat:@"%@ from %@", logMsg, jsStart];
        
        if([AppLocationTypes objectForKey:@(app)] == LNLocTypeAddress){
            if([self isGeocodingEnabled]){
                if([self isNetworkAvailable]){
                    [self reverseGeocode:jsStart success:^(MKMapItem* startItem, MKPlacemark* startPlacemark) {
                        start_placemark = startPlacemark;
                        start_mapItem = startItem;
                        startAddress = [self getAddressFromPlacemark:start_placemark];
                        logMsg = [NSString stringWithFormat:@"%@ [%@]", logMsg, startAddress];
                        
                        if(startName){
                            [start_mapItem setName:startName];
                        }else{
                            [start_mapItem setName:startPlacemark.name];
                            startName = startPlacemark.name;
                        }
                        completeBlock();
                    } fail:^(NSString* failMsg) {
                        [self sendPluginError:failMsg];
                    }];
                }else{
                    [self sendPluginError:[NSString stringWithFormat:@"Failed to reverse geocode: no internet connection. %@ requires start as address but plugin was passed a lat,lon", jsAppName]];
                }
            }else{
                [self sendPluginError:[NSString stringWithFormat:@"Failed to reverse geocode: geocoding disabled. %@ requires start as address but plugin was passed a lat,lon", jsAppName]];
            }
        }else{
            completeBlock();
        }
        
    }else{ //[jsStartType isEqual: LNLocTypeAddress]
        startAddress = jsStart;
        logMsg = [NSString stringWithFormat:@"%@ from %@", logMsg, jsStart];
        
        if([AppLocationTypes objectForKey:@(app)] == LNLocTypeCoords || (app == LNAppAppleMaps && useMapKit)){
            if([self isGeocodingEnabled]){
                if([self isNetworkAvailable]){
                    [self geocode:jsStart success:^(MKMapItem* startItem, MKPlacemark* startPlacemark) {
                        start_placemark = startPlacemark;
                        start_mapItem = startItem;
                        startCoord = start_placemark.coordinate;
                        logMsg = [NSString stringWithFormat:@"%@ [%@]", logMsg, [self coordsToString:startCoord]];
                        
                        if(startName){
                            [start_mapItem setName:startName];
                        }else{
                            [start_mapItem setName:startPlacemark.name];
                            startName = startPlacemark.name;
                        }
                        completeBlock();
                    }];
                }else{
                    [self sendPluginError:[NSString stringWithFormat:@"Failed to geocode: no internet connection. %@ requires start as lat,lon but plugin was passed an address", jsAppName]];
                }
            }else{
                [self sendPluginError:[NSString stringWithFormat:@"Failed to geocode: geocoding disabled. %@ requires start as lat,lon but plugin was passed an address", jsAppName]];
            }
        }else{
            completeBlock();
        }
    }
}

- (void) launchApp{
    app = [self mapApp_NameToLN:jsAppName];
    
    
    // Extras
    if(![self isNull:jsExtras]){
        NSError* error;
        extras = [NSJSONSerialization JSONObjectWithData:[jsExtras dataUsingEncoding:NSUTF8StringEncoding] options:kNilOptions error:&error];
        if (error != nil){
            [self logError:@"Failed to parse extras parameter as valid JSON"];
            extras = nil;
        }else{
            logMsg = [NSString stringWithFormat:@"%@ - extras=%@", logMsg, jsExtras];
        }
    }
    
    [self logDebug:logMsg];
    
    // Launch
    if(app == LNAppAppleMaps){
        [self launchAppleMaps];
    }else if(app == LNAppGoogleMaps){
        [self launchGoogleMaps];
    }else if(app == LNAppCitymapper){
        [self launchCitymapper];
    }else if(app == LNAppTheTransitApp){
        [self launchTheTransitApp];
    }else if(app == LNAppNavigon){
        [self launchNavigon];
    }else if(app == LNAppWaze){
        [self launchWaze];
    }else if(app == LNAppYandex){
        [self launchYandex];
    }else if(app == LNAppUber){
        [self launchUber];
    }else if(app == LNAppTomTom){
        [self launchTomTom];
    }else if(app == LNAppSygic){
        [self launchSygic];
    }else if(app == LNAppHereMaps){
        [self launchHereMaps];
    }else if(app == LNAppMoovit){
        [self launchMoovit];
    }else if(app == LNAppLyft){
        [self launchLyft];
    }
    
    [self sendPluginSuccess];
}

- (void) sendPluginSuccess{
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cordova_command.callbackId];
}

- (void) sendPluginError:(NSString*)errorMessage{
    [self logError:errorMessage];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMessage];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cordova_command.callbackId];
}

- (NSString*) mapApp_LNtoName:(LNApp)appName{
    NSString* name = nil;
    
    switch(appName){
        case LNAppAppleMaps:
        name = @"apple_maps";
        break;
        case LNAppCitymapper:
        name = @"citymapper";
        break;
        case LNAppGoogleMaps:
        name = @"google_maps";
        break;
        case LNAppNavigon:
        name = @"navigon";
        break;
        case LNAppTheTransitApp:
        name = @"transit_app";
        break;
        case LNAppTomTom:
        name = @"tomtom";
        break;
        case LNAppUber:
        name = @"uber";
        break;
        case LNAppWaze:
        name = @"waze";
        break;
        case LNAppYandex:
        name = @"yandex";
        break;
        case LNAppSygic:
        name = @"sygic";
        break;
        case LNAppHereMaps:
        name = @"here_maps";
        break;
        case LNAppMoovit:
        name = @"moovit";
        break;
        case LNAppLyft:
        name = @"lyft";
        break;
        default:
        [NSException raise:NSGenericException format:@"Unexpected app name"];
        
    }
    return name;
}

- (LNApp) mapApp_NameToLN:(NSString*)lnName{
    LNApp cmmName;
    
    if([lnName isEqual: @"apple_maps"]){
        cmmName = LNAppAppleMaps;
    }else if([lnName isEqual: @"citymapper"]){
        cmmName = LNAppCitymapper;
    }else if([lnName isEqual: @"google_maps"]){
        cmmName = LNAppGoogleMaps;
    }else if([lnName isEqual: @"navigon"]){
        cmmName = LNAppNavigon;
    }else if([lnName isEqual: @"transit_app"]){
        cmmName = LNAppTheTransitApp;
    }else if([lnName isEqual: @"tomtom"]){
        cmmName = LNAppTomTom;
    }else if([lnName isEqual: @"uber"]){
        cmmName = LNAppUber;
    }else if([lnName isEqual: @"waze"]){
        cmmName = LNAppWaze;
    }else if([lnName isEqual: @"yandex"]){
        cmmName = LNAppYandex;
    }else if([lnName isEqual: @"sygic"]){
        cmmName = LNAppSygic;
    }else if([lnName isEqual: @"here_maps"]){
        cmmName = LNAppHereMaps;
    }else if([lnName isEqual: @"moovit"]){
        cmmName = LNAppMoovit;
    }else if([lnName isEqual: @"lyft"]){
        cmmName = LNAppLyft;
    }else{
        [NSException raise:NSGenericException format:@"Unexpected app name: %@", lnName];
    }
    return cmmName;
}


// Get coords given address
- (void) geocode:(NSString*)address
         success:(void (^)(MKMapItem* resultItem, MKPlacemark* placemark))successBlock
{
    CLGeocoder* geocoder = [[CLGeocoder alloc] init];
    [self logDebug:[NSString stringWithFormat:@"Attempting to geocode address: %@", address]];
    [geocoder geocodeAddressString:address completionHandler:^(NSArray* placemarks, NSError* error) {
        
        // Convert the CLPlacemark to an MKPlacemark
        // Note: There's no error checking for a failed geocode
        CLPlacemark* geocodedPlacemark = [placemarks objectAtIndex:0];
        [self logDebug:[NSString stringWithFormat:@"Geocoded address '%@' to coord '%@'", address, [self coordsToString:geocodedPlacemark.location.coordinate]]];
        
        MKPlacemark* placemark = [[MKPlacemark alloc]
                                  initWithCoordinate:geocodedPlacemark.location.coordinate
                                  addressDictionary:geocodedPlacemark.addressDictionary];
        
        MKMapItem* mapItem = [[MKMapItem alloc] initWithPlacemark:placemark];
        
        successBlock(mapItem, placemark);
    }];
}

// Get address given coords
- (void) reverseGeocode:(NSString*)coords
                success:(void (^)(MKMapItem* resultItem, MKPlacemark* placemark))successBlock
                   fail:(void (^)(NSString* failMsg))failBlock
{
    CLGeocoder* geocoder = [[CLGeocoder alloc] init];
    
    NSArray* latlon = [coords componentsSeparatedByString:@","];
    NSString* lat = [latlon objectAtIndex:0];
    NSString* lon = [latlon objectAtIndex:1];
    CLLocationCoordinate2D coordinate = CLLocationCoordinate2DMake([lat doubleValue], [lon doubleValue]);
    
    MKPlacemark* placemark = [[MKPlacemark alloc] initWithCoordinate:coordinate addressDictionary:nil];
    MKMapItem* mapItem = [[MKMapItem alloc] initWithPlacemark:placemark];
    
    // Try to retrieve address via reverse geocoding
    [self logDebug:[NSString stringWithFormat:@"Attempting to reverse geocode coords: %@", coords]];
    CLLocation* location = [[CLLocation alloc]initWithLatitude:[lat doubleValue] longitude:[lon doubleValue]];
    [geocoder reverseGeocodeLocation:location completionHandler:^(NSArray* placemarks, NSError* error) {
        if (error == nil && [placemarks count] > 0) {
            CLPlacemark* geocodedPlacemark = [placemarks lastObject];
            NSString* address = [self getAddressFromPlacemark:geocodedPlacemark];
            [self logDebug:[NSString stringWithFormat:@"Reverse geocoded coords '%@' to address '%@'", [self coordsToString:coordinate], address]];
            [mapItem setName:address];
            
            MKPlacemark* placemark = [[MKPlacemark alloc]
                                      initWithCoordinate:coordinate
                                      addressDictionary:geocodedPlacemark.addressDictionary];
            
            successBlock(mapItem, placemark);
        }else if (error != nil){
            failBlock([error localizedDescription]);
        }else{
            failBlock(@"No address found at given coordinates");
        }
    }];
}


- (NSString*) getAddressFromPlacemark:(CLPlacemark*)placemark;
{
    NSString* address = @"";
    
    if (placemark.subThoroughfare){
        address = [NSString stringWithFormat:@"%@%@, ", address, placemark.subThoroughfare];
    }
    
    if (placemark.thoroughfare){
        address = [NSString stringWithFormat:@"%@%@, ", address, placemark.thoroughfare];
    }
    
    if (placemark.locality){
        address = [NSString stringWithFormat:@"%@%@, ", address, placemark.locality];
    }
    
    if (placemark.subAdministrativeArea){
        address = [NSString stringWithFormat:@"%@%@, ", address, placemark.subAdministrativeArea];
    }
    
    if (placemark.administrativeArea){
        address = [NSString stringWithFormat:@"%@%@, ", address, placemark.administrativeArea];
    }
    
    if (placemark.postalCode){
        address = [NSString stringWithFormat:@"%@%@, ", address, placemark.postalCode];
    }
    
    if (placemark.country){
        address = [NSString stringWithFormat:@"%@%@, ", address, placemark.country];
    }
    
    return address;
}

- (NSString*)coordsToString: (CLLocationCoordinate2D) coords
{
    NSString* lat = [[NSString alloc] initWithFormat:@"%g", coords.latitude];
    NSString* lon = [[NSString alloc] initWithFormat:@"%g", coords.longitude];
    return [NSString stringWithFormat:@"%@, %@", lat, lon];
}

- (CLLocationCoordinate2D)stringToCoords: (NSString*) coordString
{
    NSArray* latlon = [coordString componentsSeparatedByString:@","];
    NSString* lat = [latlon objectAtIndex:0];
    NSString* lon = [latlon objectAtIndex:1];
    CLLocationCoordinate2D coordinate = CLLocationCoordinate2DMake([lat doubleValue], [lon doubleValue]);
    return coordinate;
}

- (void)executeGlobalJavascript: (NSString*)jsString
{
    [self.commandDelegate evalJs:jsString];
    
}

- (void)logDebug: (NSString*)msg
{
    if(self.debugEnabled){
        NSLog(@"%@: %@", LOG_TAG, msg);
        NSString* jsString = [NSString stringWithFormat:@"console.log(\"%@: %@\")", LOG_TAG, [self escapeDoubleQuotes:msg]];
        [self executeGlobalJavascript:jsString];
    }
}

- (void)logDebugURI: (NSString*)msg
{
    [self logDebug:[NSString stringWithFormat:@"Launching URI: %@", msg]];
}

- (void)logError: (NSString*)msg
{
    NSLog(@"%@ ERROR: %@", LOG_TAG, msg);
    if(self.debugEnabled){
        NSString* jsString = [NSString stringWithFormat:@"console.error(\"%@: %@\")", LOG_TAG, [self escapeDoubleQuotes:msg]];
        [self executeGlobalJavascript:jsString];
    }
}

- (NSString*)escapeDoubleQuotes: (NSString*)str
{
    NSString *result =[str stringByReplacingOccurrencesOfString: @"\"" withString: @"\\\""];
    return result;
}

- (bool)isNull: (NSString*)str
{
    return str == nil || str == (id)[NSNull null] || str.length == 0 || [str isEqual: @"<null>"];
}

- (bool)isGeocodingEnabled
{
    return enableGeocoding;
}

-(bool)isNetworkAvailable
{
    Reachability *reachability = [Reachability reachabilityForInternetConnection];
    NetworkStatus networkStatus = [reachability currentReachabilityStatus];
    return networkStatus != NotReachable;
}

- (BOOL)isMapAppInstalled:(LNApp)mapApp {
    if (mapApp == LNAppAppleMaps) {
        return YES;
    }
    
    NSString* urlPrefix = [self urlPrefixForMapApp:mapApp];
    if (!urlPrefix) {
        return NO;
    }
    
    return [[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:urlPrefix]];
}

- (NSString*)urlPrefixForMapApp:(LNApp)mapApp {
    switch (mapApp) {
        case LNAppAppleMaps:
        return @"http://maps.apple.com/";
        
        case LNAppCitymapper:
        return @"citymapper://";
        
        case LNAppGoogleMaps:
        return @"comgooglemaps://";
        
        case LNAppNavigon:
        return @"navigon://";
        
        case LNAppTheTransitApp:
        return @"transit://";
        
        case LNAppWaze:
        return @"waze://";
        
        case LNAppYandex:
        return @"yandexnavi://";
        
        case LNAppUber:
        return @"uber://";
        
        case LNAppTomTom:
        return @"tomtomhome://";
        
        case LNAppSygic:
        return @"com.sygic.aura://";
        
        case LNAppHereMaps:
        return @"here-route://";
        
        case LNAppMoovit:
        return @"moovit://";
        
        case LNAppLyft:
        return @"lyft://";
        
        default:
        return nil;
    }
}

- (NSString*)urlEncode:(NSString*)queryParam {
    // Encode all the reserved characters, per RFC 3986
    // (<http://www.ietf.org/rfc/rfc3986.txt>)
    NSString* newString = (__bridge_transfer NSString*)CFURLCreateStringByAddingPercentEscapes(kCFAllocatorDefault, (__bridge CFStringRef)queryParam, NULL, (CFStringRef)@"!*'();:@&=+$,/?%#[]", kCFStringEncodingUTF8);
    
    if (newString) {
        return newString;
    }
    
    return @"";
}

- (NSString*)extrasToQueryParams:(NSDictionary*)extras {
    NSString* queryParams = @"";
    NSEnumerator* keyEnum = [extras keyEnumerator];
    id key;
    while ((key = [keyEnum nextObject]))
    {
        id value = [extras objectForKey:key];
        queryParams = [NSString stringWithFormat:@"%@&%@=%@)", queryParams, key, [self urlEncode:value]];
    }
    return queryParams;
}

- (NSString*)stringForCoord:(CLLocationCoordinate2D)coordinate {
    if ([self isEmptyCoordinate:coordinate]) {
        return @"";
    }
    
    return [NSString stringWithFormat:@"%f,%f", coordinate.latitude, coordinate.longitude];
}

- (bool) isEmptyCoordinate:(CLLocationCoordinate2D)coordinate
{
    return coordinate.latitude == LNEmptyLocation && coordinate.longitude == LNEmptyLocation;
}

@end
