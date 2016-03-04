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

BOOL debugEnabled = FALSE;
#define DLog(fmt, ...) { \
if (debugEnabled) \
NSLog((@"LaunchNavigator[objc]: " fmt), ##__VA_ARGS__); \
}


@implementation LaunchNavigator
@synthesize cordova_command;
@synthesize start_mapItem;
@synthesize dest_mapItem;

MKPlacemark* start_placemark;
MKPlacemark* dest_placemark;

// Navigate JS args
NSString* destination;
NSString* destType;
NSString* destName;
NSString* start;
NSString* startType;
NSString* startName;
NSString* appName;
NSString* transportMode;
BOOL enableDebug;

/**************
 * Plugin API
 **************/

- (void) navigate:(CDVInvokedUrlCommand*)command;
{
    self.cordova_command = command;
    
    DLog(@"called navigate()");
    
    @try {
        // Get JS arguments
        destination = [command.arguments objectAtIndex:0];
        destType = [self.cordova_command.arguments objectAtIndex:1];
        destName = [self.cordova_command.arguments objectAtIndex:2];
        start = [self.cordova_command.arguments objectAtIndex:3];
        startType = [self.cordova_command.arguments objectAtIndex:4];
        startName = [self.cordova_command.arguments objectAtIndex:5];
        appName = [self.cordova_command.arguments objectAtIndex:6];
        transportMode = [self.cordova_command.arguments objectAtIndex:7];
        enableDebug = [[command argumentAtIndex:8] boolValue];


        if(enableDebug == TRUE){
            debugEnabled = enableDebug;
            DLog(@"Debug mode enabled");
            DLog(@"destination: %@", destination);
            DLog(@"destType: %@", destType);
            DLog(@"destName: %@", destName);
            DLog(@"start: %@", start);
            DLog(@"startType: %@", startType);
            DLog(@"startName: %@", startName);
            DLog(@"appName: %@", appName);
            DLog(@"transportMode: %@", transportMode);
        }
        
        if (![destination isKindOfClass:[NSString class]]) {
            [self sendPluginError:@"Missing destination argument"];
            return;
        }
        
        [self getDest:^{
            if([startType  isEqual: @"none"]){
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
        appName = [self.cordova_command.arguments objectAtIndex:0];
        CMMapApp app = [self mapAppName_lnToCmm:appName];
        BOOL result = [CMMapLauncher isMapAppInstalled:app];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:result];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }@catch (NSException *exception) {
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.reason] callbackId:command.callbackId];
    }
}

- (void) availableApps:(CDVInvokedUrlCommand*)command;{
    NSArray* supportedApps = @[@"apple_maps", @"citymapper", @"google_maps", @"navigon", @"transit_app", @"tomtom", @"uber", @"waze", @"yandex"];
    NSMutableDictionary* results = [NSMutableDictionary new];
    @try {
        for(id object in supportedApps){
            NSString* appName = object;
            CMMapApp app = [self mapAppName_lnToCmm:appName];
            BOOL result = [CMMapLauncher isMapAppInstalled:app];
            [results setObject:[NSNumber numberWithBool:result] forKey:appName];
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

- (void) launchApp{
    CMMapApp app = [self mapAppName_lnToCmm:appName];
    
    NSString* directionsMode = MKLaunchOptionsDirectionsModeDriving;
    if([transportMode isEqual: @"walking"]){
        directionsMode = MKLaunchOptionsDirectionsModeWalking;
    }else if([transportMode isEqual: @"transit"]){
        directionsMode = MKLaunchOptionsDirectionsModeTransit;
    }
    
    CMMapPoint* start_cmm;
    if([startType  isEqual: @"none"]){
        start_cmm = [CMMapPoint currentLocation];
    }else{
        start_cmm = [CMMapPoint
                     mapPointWithMapItem:start_mapItem
                     name:start_mapItem.name
                     address:[self getAddressFromPlacemark:start_placemark]
                     coordinate:start_placemark.coordinate];
    }
    
    CMMapPoint* dest_cmm = [CMMapPoint
                             mapPointWithMapItem:dest_mapItem
                             name:dest_mapItem.name
                             address:[self getAddressFromPlacemark:dest_placemark]
                             coordinate:dest_placemark.coordinate];

    
    [CMMapLauncher launchMapApp:app forDirectionsFrom:start_cmm to:dest_cmm directionsMode:directionsMode];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cordova_command.callbackId];
}

/**************
 * Utilities
 **************/

- (void) getDest:(void (^)(void))completeBlock{
    if([destType isEqual: @"coords"]){
        [self reverseGeocode:destination success:^(MKMapItem* destItem, MKPlacemark* destPlacemark) {
            dest_mapItem = destItem;
            dest_placemark = destPlacemark;
            if(destName != nil){
                [destItem setName:destName];
            }else{
                [destItem setName:destPlacemark.name];
            }
            completeBlock();
        } fail:^(NSString* failMsg) {
            [self sendPluginError:failMsg];
        }];
    }else{
        [self geocode:destination success:^(MKMapItem* destItem, MKPlacemark* destPlacemark) {
            dest_mapItem = destItem;
            dest_placemark = destPlacemark;
            if(destName != nil){
                [destItem setName:destName];
            }else{
                [destItem setName:destPlacemark.name];
            }
            completeBlock();
        }];
    }
    
}

- (void) getStart:(void (^)(void))completeBlock{
    if([startType isEqual: @"none"]){
        MKMapItem* startItem = [MKMapItem mapItemForCurrentLocation];
        start_mapItem = startItem;
        completeBlock();
    }else if([startType isEqual: @"coords"]){
        [self reverseGeocode:start success:^(MKMapItem* startItem, MKPlacemark* startPlacemark) {
            start_placemark = startPlacemark;
            start_mapItem = startItem;
            if(startName != nil){
                [startItem setName:startName];
            }else{
                [startItem setName:startPlacemark.name];
            }
            completeBlock();
        } fail:^(NSString* failMsg) {
            [self sendPluginError:failMsg];
        }];
    }else{
        [self geocode:start success:^(MKMapItem* startItem, MKPlacemark* startPlacemark) {
            start_placemark = startPlacemark;
            start_mapItem = startItem;
            if(startName != nil){
                [startItem setName:startName];
            }else{
                [startItem setName:startPlacemark.name];
            }
            completeBlock();
        }];
    }
}


- (void) sendPluginError:(NSString*)errorMessage{
    DLog("ERROR: %@",errorMessage);
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMessage];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cordova_command.callbackId];
}

- (NSString*) mapAppName_cmmToLn:(CMMapApp)cmmAppName{
    NSString* lnName = nil;
    
    switch(cmmAppName){
        case CMMapAppAppleMaps:
            lnName = @"apple_maps";
            break;
        case CMMapAppCitymapper:
            lnName = @"citymapper";
            break;
        case CMMapAppGoogleMaps:
            lnName = @"google_maps";
            break;
        case CMMapAppNavigon:
            lnName = @"navigon";
            break;
        case CMMapAppTheTransitApp:
            lnName = @"transit_app";
            break;
        case CMMapAppTomTom:
            lnName = @"tomtom";
            break;
        case CMMapAppUber:
            lnName = @"uber";
            break;
        case CMMapAppWaze:
            lnName = @"waze";
            break;
        case CMMapAppYandex:
            lnName = @"yandex";
            break;
        default:
            [NSException raise:NSGenericException format:@"Unexpected CMMapApp name"];
        
    }
    return lnName;
}

- (CMMapApp) mapAppName_lnToCmm:(NSString*)lnName{
    CMMapApp cmmName;
    
    if([lnName isEqual: @"apple_maps"]){
        cmmName = CMMapAppAppleMaps;
    }else if([lnName isEqual: @"citymapper"]){
        cmmName = CMMapAppCitymapper;
    }else if([lnName isEqual: @"google_maps"]){
        cmmName = CMMapAppGoogleMaps;
    }else if([lnName isEqual: @"navigon"]){
        cmmName = CMMapAppNavigon;
    }else if([lnName isEqual: @"transit_app"]){
        cmmName = CMMapAppTheTransitApp;
    }else if([lnName isEqual: @"tomtom"]){
        cmmName = CMMapAppTomTom;
    }else if([lnName isEqual: @"uber"]){
        cmmName = CMMapAppUber;
    }else if([lnName isEqual: @"waze"]){
        cmmName = CMMapAppWaze;
    }else if([lnName isEqual: @"yandex"]){
        cmmName = CMMapAppYandex;
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

    DLog(@"Geocoding address: %@",address);
    [geocoder geocodeAddressString:address completionHandler:^(NSArray* placemarks, NSError* error) {
        
        // Convert the CLPlacemark to an MKPlacemark
        // Note: There's no error checking for a failed geocode
        CLPlacemark* geocodedPlacemark = [placemarks objectAtIndex:0];
        DLog(@"Geocoded name: %@", geocodedPlacemark.name);
        
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
    CLLocationCoordinate2D start_coordinate = CLLocationCoordinate2DMake([lat doubleValue], [lon doubleValue]);
    MKPlacemark* placemark = [[MKPlacemark alloc] initWithCoordinate:start_coordinate addressDictionary:nil];
    MKMapItem* mapItem = [[MKMapItem alloc] initWithPlacemark:placemark];
    
    // Try to retrieve address via reverse geocoding
    DLog(@"Reverse geocoding coords: %@", coords);
    CLLocation* location = [[CLLocation alloc]initWithLatitude:[lat doubleValue] longitude:[lon doubleValue]];
    [geocoder reverseGeocodeLocation:location completionHandler:^(NSArray* placemarks, NSError* error) {
        if (error == nil && [placemarks count] > 0) {
            CLPlacemark* geocodedPlacemark = [placemarks lastObject];
            NSString* address = [self getAddressFromPlacemark:geocodedPlacemark];
            DLog(@"Reverse geocoded address: %@", address);
            [mapItem setName:address];
            
            MKPlacemark* placemark = [[MKPlacemark alloc]
                                      initWithCoordinate:geocodedPlacemark.location.coordinate
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
@end