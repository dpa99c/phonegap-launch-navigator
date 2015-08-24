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
@synthesize start_mapItem;
@synthesize dest_mapItem;

- (void) navigate:(CDVInvokedUrlCommand*)command;
{
    
    DLog(@"called navigate()");
    
    NSString *destination = [command.arguments objectAtIndex:0];
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
    
    // Acquire a reference to the local UIApplication singleton
    UIApplication* app = [UIApplication sharedApplication];
    
    NSURL *testURL = [NSURL URLWithString:@"comgooglemaps://"];
    BOOL googleMapsSupported = [app canOpenURL:testURL];

    if (preferGoogleMaps && googleMapsSupported) {
        [self openGoogleMaps:command];
    } else {
        if (preferGoogleMaps){
            DLog(@"Google Maps not supported on this device.");
        }
        [self openAppleMaps:command];
    }
}

- (void) openGoogleMaps:(CDVInvokedUrlCommand*)command;
{
    CDVPluginResult* pluginResult = nil;
    NSString *destination = [command.arguments objectAtIndex:0];
    NSString *start = [command.arguments objectAtIndex:1];
    NSString *transportMode = [command.arguments objectAtIndex:4];
    NSString *urlScheme = [command.arguments objectAtIndex:5];
    NSString *backButtonText = [command.arguments objectAtIndex:6];

    NSString *directionsRequest = nil;
    NSString *protocol = nil;
    NSString *callbackParams = @"";


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

    NSString *safeString = [directionsRequest stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    NSURL *directionsURL = [NSURL URLWithString:safeString];

    // Acquire a reference to the local UIApplication singleton
    UIApplication* app = [UIApplication sharedApplication];
    [app openURL:directionsURL];

    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) openAppleMaps:(CDVInvokedUrlCommand*)command;
{
    CDVPluginResult* pluginResult = nil;
    NSString *destination = [command.arguments objectAtIndex:0];
    NSString *start = [command.arguments objectAtIndex:1];
    NSString *transportMode = [command.arguments objectAtIndex:4];
    NSString *startType = [command.arguments objectAtIndex:5];
    NSString *destType = [command.arguments objectAtIndex:6];

    // Check for iOS 6
    Class mapItemClass = [MKMapItem class];
    if (mapItemClass && [mapItemClass respondsToSelector:@selector(openMapsWithItems:launchOptions:)])
    {
        // Create an MKMapItem for destination
        if ([destType isEqualToString:@"coords"]){
            NSArray* coords = [destination componentsSeparatedByString:@","];
            NSString* lat = [coords objectAtIndex:0];
            NSString* lon = [coords objectAtIndex:1];
            CLLocationCoordinate2D dest_coordinate = CLLocationCoordinate2DMake([lat doubleValue], [lon doubleValue]);
            MKPlacemark *placemark = [[MKPlacemark alloc] initWithCoordinate:dest_coordinate addressDictionary:nil];
            self.dest_mapItem = [[MKMapItem alloc] initWithPlacemark:placemark];
            [self.dest_mapItem setName:@"Destination"];
        }else{
            CLGeocoder *geocoder = [[CLGeocoder alloc] init];
            [geocoder geocodeAddressString:destination completionHandler:^(NSArray *placemarks, NSError *error) {

                // Convert the CLPlacemark to an MKPlacemark
                // Note: There's no error checking for a failed geocode
                CLPlacemark *geocodedPlacemark = [placemarks objectAtIndex:0];
                MKPlacemark *placemark = [[MKPlacemark alloc]
                    initWithCoordinate:geocodedPlacemark.location.coordinate
                    addressDictionary:geocodedPlacemark.addressDictionary];
    
                // Create a map item for the geocoded address to pass to Maps app
                self.dest_mapItem = [[MKMapItem alloc] initWithPlacemark:placemark];
                [self.dest_mapItem setName:geocodedPlacemark.name];
            }];
        }


        // Create an MKMapItem for start
        if ([startType isEqualToString:@"none"]){
            // Get the "Current User Location" MKMapItem
            self.start_mapItem = [MKMapItem mapItemForCurrentLocation];
        }else if ([startType isEqualToString:@"coords"]){
            NSArray *coords = [start componentsSeparatedByString:@","];
            NSString* lat = [coords objectAtIndex:0];
            NSString* lon = [coords objectAtIndex:1];
            CLLocationCoordinate2D start_coordinate = CLLocationCoordinate2DMake([lat doubleValue], [lon doubleValue]);
            MKPlacemark *placemark = [[MKPlacemark alloc] initWithCoordinate:start_coordinate addressDictionary:nil];
            self.start_mapItem = [[MKMapItem alloc] initWithPlacemark:placemark];
            [self.start_mapItem setName:@"Start"];
        }else{
            CLGeocoder *geocoder = [[CLGeocoder alloc] init];
            [geocoder geocodeAddressString:start completionHandler:^(NSArray *placemarks, NSError *error) {

                // Convert the CLPlacemark to an MKPlacemark
                // Note: There's no error checking for a failed geocode
                CLPlacemark *geocodedPlacemark = [placemarks objectAtIndex:0];
                MKPlacemark *placemark = [[MKPlacemark alloc]
                    initWithCoordinate:geocodedPlacemark.location.coordinate
                    addressDictionary:geocodedPlacemark.addressDictionary];

                // Create a map item for the geocoded address to pass to Maps app
                self.start_mapItem = [[MKMapItem alloc] initWithPlacemark:placemark];
                [self.start_mapItem setName:geocodedPlacemark.name];
            }];
        }

        // Set the directions mode
        NSDictionary *launchOptions = nil;
        if (transportMode != (id)[NSNull null] && [transportMode isEqualToString:@"walking"]){
            launchOptions = @{MKLaunchOptionsDirectionsModeKey : MKLaunchOptionsDirectionsModeWalking};
        }else{
            launchOptions = @{MKLaunchOptionsDirectionsModeKey : MKLaunchOptionsDirectionsModeDriving};
        }

        // Pass the start and destination map items to the Maps app
        // Set the direction mode in the launchOptions dictionary
        [MKMapItem openMapsWithItems:@[self.start_mapItem, self.dest_mapItem] launchOptions:launchOptions];
        
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }else{
        DLog(@"Error: iOS 5 and below not supported");
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"iOS 5 and below not supported"];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}
@end