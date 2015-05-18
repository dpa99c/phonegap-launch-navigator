#import "LaunchNavigator.h"

#define DLog(fmt, ...) { \
NSLog((@"[LaunchNavigator objc]: " fmt), ##__VA_ARGS__); \
}

/**
 * Actual implementation of the interface
 */
@implementation LaunchNavigator

- (void) navigate:(CDVInvokedUrlCommand*)command;
{
    CDVPluginResult* pluginResult = nil;
    
    NSString *destination = [command.arguments objectAtIndex:0];
    NSString *start = [command.arguments objectAtIndex:1];
    BOOL preferGoogleMaps = [[command argumentAtIndex:2] boolValue];
    NSString *urlScheme = [command.arguments objectAtIndex:3];
    NSString *backButtonText = [command.arguments objectAtIndex:4];
    
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
        if(urlScheme != nil){
            protocol = @"comgooglemaps-x-callback://?";
            callbackParams = [NSString stringWithFormat:@"%@/%@/%@/%@", @"&x-success=", urlScheme, @"://?resume=true&x-source=", backButtonText];

        }else{
            protocol = @"comgooglemaps://?";
        }
        
    } else {
        if (preferGoogleMaps){
            DLog(@"Google Maps not supported on this device.");
        }
        
        protocol = @"http://maps.apple.com/?";
    }
    
    directionsRequest = [NSString stringWithFormat:@"%@/%@/%@/%@", protocol, @"daddr=", destination, callbackParams];
    
    if(start != nil){
        directionsRequest = [NSString stringWithFormat:@"%@/%@/%@", directionsRequest, @"&saddr=", start];
    }

    
    NSURL *directionsURL = [NSURL URLWithString:directionsRequest];
    [app openURL:directionsURL];
  
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end
