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
NSLog((@"[objc]: " fmt), ##__VA_ARGS__); \
}

/**
 * Actual implementation of the interface
 */
@implementation LaunchNavigator
@synthesize cordova_command;
@synthesize start_mapItem;
@synthesize dest_mapItem;

- (void) navigate:(CDVInvokedUrlCommand*)command;
{
    self.cordova_command = command;
    
    DLog(@"called navigate()");
    
    NSString* destination = [command.arguments objectAtIndex:0];
    BOOL enableDebug = [[command argumentAtIndex:2] boolValue];
    BOOL preferGoogleMaps = [[command argumentAtIndex:3] boolValue];

    if(enableDebug == TRUE){
        debugEnabled = enableDebug;
        DLog(@"Debug mode enabled");
    }
    
    if (![destination isKindOfClass:[NSString class]]) {
        DLog(@"Error: missing destination argument");
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Invalid arguments"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }
    
    BOOL googleMapsAvailable = [self isGoogleMapsAvailable];
    if (preferGoogleMaps && googleMapsAvailable) {
        [self openGoogleMaps];
    } else {
        if (preferGoogleMaps){
            DLog(@"Google Maps not supported on this device.");
        }
        [self openAppleMaps];
    }
}

- (void) googleMapsAvailable:(CDVInvokedUrlCommand*)command;
{
    BOOL googleMapsAvailable = [self isGoogleMapsAvailable];
    CDVPluginResult* pluginResult;
    if(googleMapsAvailable) {   
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:1];   
    }
    else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:0];
    }
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (BOOL) isGoogleMapsAvailable
{
    // Acquire a reference to the local UIApplication singleton
    UIApplication* app = [UIApplication sharedApplication];
    
    NSURL* testURL = [NSURL URLWithString:@"comgooglemaps://"];
    BOOL googleMapsAvailable = [app canOpenURL:testURL];
    return googleMapsAvailable;
}

- (void) openGoogleMaps
{
    CDVPluginResult* pluginResult = nil;
    NSString* destination = [self.cordova_command.arguments objectAtIndex:0];
    NSString* start = [self.cordova_command.arguments objectAtIndex:1];
    NSString* transportMode = [self.cordova_command.arguments objectAtIndex:4];
    NSString* urlScheme = [self.cordova_command.arguments objectAtIndex:7];
    NSString* backButtonText = [self.cordova_command.arguments objectAtIndex:8];

    NSString* directionsRequest = nil;
    NSString* protocol = nil;
    NSString* callbackParams = @"";


    if(![urlScheme isEqual:[NSNull null]]){
        protocol = @"comgooglemaps-x-callback://?";
        callbackParams = [NSString stringWithFormat:@"&x-success=%@://?resume=true&x-source=%@", urlScheme, backButtonText];

    }else{
        protocol = @"comgooglemaps://?";
    }

    directionsRequest = [NSString stringWithFormat:@"%@daddr=%@%@", protocol, destination, callbackParams];

    if(![start isEqual:[NSNull null]]){
        directionsRequest = [NSString stringWithFormat:@"%@&saddr=%@", directionsRequest, start];
    }

    if(![transportMode isEqual:[NSNull null]]){
        directionsRequest = [NSString stringWithFormat:@"%@&directionsmode=%@", directionsRequest, transportMode];
    }

    DLog(@"Opening URL: %@", directionsRequest);

    NSString* safeString = [directionsRequest stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    NSURL* directionsURL = [NSURL URLWithString:safeString];

    // Acquire a reference to the local UIApplication singleton
    UIApplication* app = [UIApplication sharedApplication];
    [app openURL:directionsURL];

    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cordova_command.callbackId];
}

- (void) openAppleMaps
{
    // Check for iOS 6
    Class mapItemClass = [MKMapItem class];
    if (mapItemClass && [mapItemClass respondsToSelector:@selector(openMapsWithItems:launchOptions:)])
    {
        [self setAppleDestination];
    }else{
        DLog(@"Error: iOS 5 and below not supported");
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"iOS 5 and below not supported"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cordova_command.callbackId];
    }
}

- (void) setAppleDestination
{
    DLog(@"Setting destination location");
    NSString* destination = [self.cordova_command.arguments objectAtIndex:0];
    NSString* destType = [self.cordova_command.arguments objectAtIndex:6];
    CLGeocoder* geocoder = [[CLGeocoder alloc] init];

    // Create an MKMapItem for destination
    if ([destType isEqualToString:@"coords"]){
        DLog(@"Destination location is coordinates");
        NSArray* coords = [destination componentsSeparatedByString:@","];
        NSString* lat = [coords objectAtIndex:0];
        NSString* lon = [coords objectAtIndex:1];
        CLLocationCoordinate2D dest_coordinate = CLLocationCoordinate2DMake([lat doubleValue], [lon doubleValue]);
        MKPlacemark* placemark = [[MKPlacemark alloc] initWithCoordinate:dest_coordinate addressDictionary:nil];
        self.dest_mapItem = [[MKMapItem alloc] initWithPlacemark:placemark];
        [self.dest_mapItem setName:@"Destination"];

        // Try to retrieve display address for start location via reverse geocoding
        CLLocation* location = [[CLLocation alloc]initWithLatitude:[lat doubleValue] longitude:[lon doubleValue]];
        [geocoder reverseGeocodeLocation:location completionHandler:^(NSArray* placemarks, NSError* error) {
            if (error == nil && [placemarks count] > 0) {
                CLPlacemark* geocodedPlacemark = [placemarks lastObject];
                NSString* address = [self getAddressFromPlacemark:geocodedPlacemark];
                DLog(@"Reverse geocoded destination: %@", address);
               [self.dest_mapItem setName:address];
            }
            [self setAppleStart];
        }];            
    }else{
        DLog(@"Destination location is place name - geocoding it");
        [geocoder geocodeAddressString:destination completionHandler:^(NSArray* placemarks, NSError* error) {

            // Convert the CLPlacemark to an MKPlacemark
            // Note: There's no error checking for a failed geocode
            CLPlacemark* geocodedPlacemark = [placemarks objectAtIndex:0];
            MKPlacemark* placemark = [[MKPlacemark alloc]
                initWithCoordinate:geocodedPlacemark.location.coordinate
                addressDictionary:geocodedPlacemark.addressDictionary];

            // Create a map item for the geocoded address to pass to Maps app
            self.dest_mapItem = [[MKMapItem alloc] initWithPlacemark:placemark];
            DLog(@"Geocoded destination: %@", geocodedPlacemark.name);
            [self.dest_mapItem setName:geocodedPlacemark.name];
            [self setAppleStart];
        }];
    }
}

- (void) setAppleStart
{
    DLog(@"Setting start location");
    NSString* start = [self.cordova_command.arguments objectAtIndex:1];
    NSString* startType = [self.cordova_command.arguments objectAtIndex:5];
    CLGeocoder* geocoder = [[CLGeocoder alloc] init];
    
    // Create an MKMapItem for start
    if ([startType isEqualToString:@"none"]){
        // Get the "Current User Location" MKMapItem
        DLog(@"No start location specified so using current position");
        self.start_mapItem = [MKMapItem mapItemForCurrentLocation];
        [self invokeAppleMaps];
    }else if ([startType isEqualToString:@"coords"]){
        DLog(@"Start location is coordinates");
        NSArray* coords = [start componentsSeparatedByString:@","];
        NSString* lat = [coords objectAtIndex:0];
        NSString* lon = [coords objectAtIndex:1];
        CLLocationCoordinate2D start_coordinate = CLLocationCoordinate2DMake([lat doubleValue], [lon doubleValue]);
        MKPlacemark* placemark = [[MKPlacemark alloc] initWithCoordinate:start_coordinate addressDictionary:nil];
        self.start_mapItem = [[MKMapItem alloc] initWithPlacemark:placemark];
        [self.start_mapItem setName:@"Start"];

        // Try to retrieve display address for start location via reverse geocoding
        CLLocation* location = [[CLLocation alloc]initWithLatitude:[lat doubleValue] longitude:[lon doubleValue]];
        [geocoder reverseGeocodeLocation:location completionHandler:^(NSArray* placemarks, NSError* error) {
            if (error == nil && [placemarks count] > 0) {
                CLPlacemark* geocodedPlacemark = [placemarks lastObject];
                NSString* address = [self getAddressFromPlacemark:geocodedPlacemark];
               DLog(@"Reverse geocoded start: %@", address);
               [self.start_mapItem setName:address];
            }
            [self invokeAppleMaps];
        }];
    }else{
        DLog(@"Start location is place name - geocoding it");
        [geocoder geocodeAddressString:start completionHandler:^(NSArray* placemarks, NSError* error) {

            // Convert the CLPlacemark to an MKPlacemark
            // Note: There's no error checking for a failed geocode
            CLPlacemark* geocodedPlacemark = [placemarks objectAtIndex:0];
            MKPlacemark* placemark = [[MKPlacemark alloc]
                initWithCoordinate:geocodedPlacemark.location.coordinate
                addressDictionary:geocodedPlacemark.addressDictionary];

            // Create a map item for the geocoded address to pass to Maps app
            self.start_mapItem = [[MKMapItem alloc] initWithPlacemark:placemark];
            DLog(@"Geocoded start: %@", geocodedPlacemark.name);
            [self.start_mapItem setName:geocodedPlacemark.name];
            [self invokeAppleMaps];
        }];
    }
}

- (void) invokeAppleMaps
{
    NSString* transportMode = [self.cordova_command.arguments objectAtIndex:4];

    // Set the directions mode
    NSDictionary* launchOptions = nil;
    if (transportMode != (id)[NSNull null] && [transportMode isEqualToString:@"walking"]){
        DLog(@"Transport mode is 'walking'");
        launchOptions = @{MKLaunchOptionsDirectionsModeKey : MKLaunchOptionsDirectionsModeWalking};
    }else{
    DLog(@"Transport mode is 'driving'");
        launchOptions = @{MKLaunchOptionsDirectionsModeKey : MKLaunchOptionsDirectionsModeDriving};
    }

    // Pass the start and destination map items and launchOptions to the Maps app
    DLog(@"Invoking Apple Maps...");
    [MKMapItem openMapsWithItems:@[self.start_mapItem, self.dest_mapItem] launchOptions:launchOptions];

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cordova_command.callbackId];
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