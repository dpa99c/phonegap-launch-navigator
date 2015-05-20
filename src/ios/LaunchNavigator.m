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

- (void) navigate:(CDVInvokedUrlCommand*)command;
{
    CDVPluginResult* pluginResult = nil;
    
    DLog(@"called navigate()");
    
    NSString *destination = [command.arguments objectAtIndex:0];
    NSString *start = [command.arguments objectAtIndex:1];
    BOOL preferGoogleMaps = [[command argumentAtIndex:2] boolValue];
    NSString *urlScheme = [command.arguments objectAtIndex:3];
    NSString *backButtonText = [command.arguments objectAtIndex:4];
    BOOL enableDebug = [[command argumentAtIndex:5] boolValue];
    
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
    
    NSString *directionsRequest = nil;
    NSString *protocol = nil;
    NSString *callbackParams = @"";
    
    NSURL *testURL = [NSURL URLWithString:@"comgooglemaps://"];
    BOOL googleMapsSupported = [app canOpenURL:testURL];
   
    
    if (preferGoogleMaps && googleMapsSupported) {
        
        if(![urlScheme isEqual:[NSNull null]]){
            protocol = @"comgooglemaps-x-callback://?";
            callbackParams = [NSString stringWithFormat:@"&x-success=%@://?resume=true&x-source=%@", urlScheme, backButtonText];

        }else{
            protocol = @"comgooglemaps://?";
        }
        
    } else {
        if (preferGoogleMaps){
            DLog(@"Google Maps not supported on this device.");
        }
        
        protocol = @"http://maps.apple.com/?";
    }
    
    directionsRequest = [NSString stringWithFormat:@"%@daddr=%@%@", protocol, destination, callbackParams];
    
    if(![start isEqual:[NSNull null]]){
        directionsRequest = [NSString stringWithFormat:@"%@&saddr=%@", directionsRequest, start];
    }
    
    DLog(@"Opening URL: %@", directionsRequest);

    NSString *safeString = [directionsRequest stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
    NSURL *directionsURL = [NSURL URLWithString:safeString];
    [app openURL:directionsURL];
  
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
