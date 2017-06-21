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

NSString*const LOG_TAG = @"LaunchNavigator[native]";

NSArray* supportedApps;

NSString*const LNLocTypeNone = @"none";
NSString*const LNLocTypeBoth = @"both";
NSString*const LNLocTypeAddress = @"name";
NSString*const LNLocTypeCoords = @"coords";


// Valid input location types for apps
static NSDictionary* AppLocationTypes;

@implementation LaunchNavigator
@synthesize debugEnabled;
@synthesize cordova_command;

@synthesize start_mapItem;
@synthesize dest_mapItem;

BOOL enableGeocoding = TRUE;

NSString* destAddress = CMEmptyAddress;
CLLocationCoordinate2D destCoord = CMEmptyCoord;
NSString* startAddress = CMEmptyAddress;
CLLocationCoordinate2D startCoord = CMEmptyCoord;


MKPlacemark* start_placemark;
MKPlacemark* dest_placemark;

CMMapApp cmmApp;
CMMapPoint* start_cmm;
CMMapPoint* dest_cmm;

NSString* logMsg;

NSString* destName = nil;
NSString* startName = nil;

// Navigate JS args
NSString* jsDestination;
NSString* jsDestType;
NSString* jsDestName;
NSString* jsStart;
NSString* jsStartType;
NSString* jsStartName;
NSString* jsAppName;
NSString* jsTransportMode;
NSString* jsExtra;
BOOL jsEnableDebug;

/**************
 * Plugin API
 **************/

+ (void)initialize{
    supportedApps = @[@"apple_maps", @"citymapper", @"google_maps", @"navigon", @"transit_app", @"tomtom", @"uber", @"waze", @"yandex", @"sygic", @"here_maps", @"moovit", @"lyft"];
    AppLocationTypes = @{
        @(CMMapAppAppleMaps): LNLocTypeBoth,
		@(CMMapAppCitymapper): LNLocTypeBoth,
        @(CMMapAppGoogleMaps): LNLocTypeBoth,
        @(CMMapAppNavigon): LNLocTypeCoords,
        @(CMMapAppTheTransitApp): LNLocTypeCoords,
        @(CMMapAppWaze): LNLocTypeCoords,
        @(CMMapAppYandex): LNLocTypeCoords,
        @(CMMapAppUber): LNLocTypeCoords,
        @(CMMapAppTomTom): LNLocTypeCoords,
        @(CMMapAppSygic): LNLocTypeCoords,
        @(CMMapAppHereMaps): LNLocTypeCoords,
        @(CMMapAppMoovit): LNLocTypeCoords,
        @(CMMapAppLyft): LNLocTypeCoords
    };
}

- (void) navigate:(CDVInvokedUrlCommand*)command;
{
    self.cordova_command = command;
    
    @try {
        // Get JS arguments
        jsDestination = [command.arguments objectAtIndex:0];
        jsDestType = [command.arguments objectAtIndex:1];
        jsDestName = [command.arguments objectAtIndex:2];
        jsStart = [command.arguments objectAtIndex:3];
        jsStartType = [command.arguments objectAtIndex:4];
        jsStartName = [command.arguments objectAtIndex:5];
        jsAppName = [command.arguments objectAtIndex:6];
        jsTransportMode = [command.arguments objectAtIndex:7];
        jsEnableDebug = [[command argumentAtIndex:8] boolValue];
        jsExtra = [command.arguments objectAtIndex:9];
		jsEnableGeocoding = [command.arguments objectAtIndex:10];
        
        if(jsEnableDebug == TRUE){
            self.debugEnabled = jsEnableDebug;
            [CMMapLauncher enableDebugLogging];
        }else{
            self.debugEnabled = FALSE;
        }
        
        [self logDebug:[NSString stringWithFormat:@"Called navigate() with args: destination=%@; destType=%@; destName=%@; start=%@; startType=%@; startName=%@; appName=%@; transportMode=%@; extras=%@", jsDestination, jsDestType, jsDestName, jsStart, jsStartType, jsStartName, jsAppName, jsTransportMode, jsExtra]];
        
        CMMapApp app = [self mapAppName_lnToCmm:jsAppName];
        BOOL isAvailable = [CMMapLauncher isMapAppInstalled:app];
        if(!isAvailable){
            [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:[NSString stringWithFormat:@"%@ is not installed on the device", jsAppName]] callbackId:self.cordova_command.callbackId];
            return;
        }
        
        if (![jsDestination isKindOfClass:[NSString class]]) {
            [self sendPluginError:@"Missing destination argument"];
            return;
        }

		if(![self isNull:jsEnableGeocoding]){
			self.enableGeocoding = [[command argumentAtIndex:10] boolValue];
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
        CMMapApp app = [self mapAppName_lnToCmm:[command.arguments objectAtIndex:0]];
        BOOL result = [CMMapLauncher isMapAppInstalled:app];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:result];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }@catch (NSException *exception) {
        [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:exception.reason] callbackId:command.callbackId];
    }
}

- (void) availableApps:(CDVInvokedUrlCommand*)command;{
    NSMutableDictionary* results = [NSMutableDictionary new];
    @try {
        for(id object in supportedApps){
            NSString* jsAppName = object;
            CMMapApp app = [self mapAppName_lnToCmm:jsAppName];
            BOOL result = [CMMapLauncher isMapAppInstalled:app];
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

- (void) launchApp{
    cmmApp = [self mapAppName_lnToCmm:jsAppName];
    
    // Destination
    dest_cmm = [CMMapPoint
        mapPointWithMapItem:dest_mapItem
        name:destName
        address:destAddress
        coordinate:destCoord];
    
    // Start        
    start_cmm = [CMMapPoint
        mapPointWithMapItem:start_mapItem
        name:startName
        address:startAddress
        coordinate:startCoord];
    
    // Extras
    NSDictionary* dExtras = nil;
    if(![self isNull:jsExtra]){
        NSError* error;
        dExtras = [NSJSONSerialization JSONObjectWithData:[jsExtra dataUsingEncoding:NSUTF8StringEncoding] options:kNilOptions error:&error];
        if (error != nil){
            [self logError:@"Failed to parse extras parameter as valid JSON"];
            dExtras = nil;
        }else{
            logMsg = [NSString stringWithFormat:@"%@ - extras=%@", logMsg, jsExtra];
        }
    }
	
    [self logDebug:logMsg];
    
    // Launch
    [CMMapLauncher launchMapApp:cmmApp forDirectionsFrom:start_cmm to:dest_cmm directionsMode:jsTransportMode extras:dExtras];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.cordova_command.callbackId];
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
		

		if([AppLocationTypes objectForKey:@(cmmApp)] == LNLocTypeAddress){
           if([self isGeocodingEnabled]){
			   if([self isNetworkAvailable]){
				   [self reverseGeocode:jsDestination success:^(MKMapItem* destItem, MKPlacemark* destPlacemark) {
						dest_mapItem = destItem;
						dest_placemark = destPlacemark;
						destAddress = [self getAddressFromPlacemark:dest_placemark];
						logMsg = [NSString stringWithFormat:@"%@ [%@]", logMsg, destAddress];

						if(![self isNull:destName]){
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
				   [self sendPluginError:[NSString stringWithFormat:@"Failed to reverse geocode: no internet connection. %@ requires destination as address but plugin was passed a lat,lon", app]];
			   }
		   }else{
			   [self sendPluginError:[NSString stringWithFormat:@"Failed to reverse geocode: geocoding disabled. %@ requires destination as address but plugin was passed a lat,lon", app]];
		   }
        }else{
			completeBlock();
		}
    }else{ // [jsDestType isEqual: LNLocTypeAddress]
		destAddress = jsDestination;

		if([AppLocationTypes objectForKey:@(cmmApp)] == LNLocTypeCoords){
           if([self isGeocodingEnabled]){
			   if([self isNetworkAvailable]){
				   [self geocode:jsDestination success:^(MKMapItem* destItem, MKPlacemark* destPlacemark) {
						dest_mapItem = destItem;
						dest_placemark = destPlacemark;
						destCoord = dest_placemark.coordinate;
						logMsg = [NSString stringWithFormat:@"%@ [%@]", logMsg, [coordsToString destCoord]];

						if(![self isNull:destName]){
							[dest_mapItem setName:destName];
						}else{
							[dest_mapItem setName:destPlacemark.name];
							destName = destPlacemark.name;
						}
						completeBlock();
					}];
			   }else{
				   [self sendPluginError:[NSString stringWithFormat:@"Failed to geocode: no internet connection. %@ requires destination as lat,lon but plugin was passed an address", app]];
			   }
		   }else{
		   	   [self sendPluginError:[NSString stringWithFormat:@"Failed to geocode: geocoding disabled. %@ requires destination as lat,lon but plugin was passed an address", app]];
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
        MKMapItem* startItem = [MKMapItem mapItemForCurrentLocation];
        start_mapItem = startItem;
		logMsg = [NSString stringWithFormat:@"%@ from current location", logMsg];
		if([self isNull:startName]){
			startName = "Current location";
		}
        completeBlock();
    }else if([jsStartType isEqual: LNLocTypeCoords]){
		startCoord = [self stringToCoords:jsStart];
		logMsg = [NSString stringWithFormat:@"%@ from %@", logMsg, jsStart];

		if([AppLocationTypes objectForKey:@(cmmApp)] == LNLocTypeAddress){
			if([self isGeocodingEnabled]){
			   if([self isNetworkAvailable]){
				   [self reverseGeocode:jsStart success:^(MKMapItem* startItem, MKPlacemark* startPlacemark) {
						start_placemark = startPlacemark;
						start_mapItem = startItem;
						startAddress = [self getAddressFromPlacemark:start_placemark];
						logMsg = [NSString stringWithFormat:@"%@ [%@]", logMsg, startAddress];

						if(![self isNull:startName]){
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
				   [self sendPluginError:[NSString stringWithFormat:@"Failed to reverse geocode: no internet connection. %@ requires start as address but plugin was passed a lat,lon", app]];
			   }
		   }else{
			   [self sendPluginError:[NSString stringWithFormat:@"Failed to reverse geocode: geocoding disabled. %@ requires start as address but plugin was passed a lat,lon", app]];
		   }		
		}else{
			completeBlock();
		}

    }else{ //[jsStartType isEqual: LNLocTypeAddress]
		startAddress = jsStart;
		logMsg = [NSString stringWithFormat:@"%@ from %@", logMsg, jsStart];

        if([AppLocationTypes objectForKey:@(cmmApp)] == LNLocTypeCoords){
			if([self isGeocodingEnabled]){
			   if([self isNetworkAvailable]){
					[self geocode:jsStart success:^(MKMapItem* startItem, MKPlacemark* startPlacemark) {
						start_placemark = startPlacemark;
						start_mapItem = startItem;
						startCoord = start_placemark.coordinate;
						logMsg = [NSString stringWithFormat:@"%@ [%@]", logMsg, [coordsToString startCoord]];

						if(![self isNull:startName]){
							[start_mapItem setName:startName];
						}else{
							[start_mapItem setName:startPlacemark.name];
							startName = startPlacemark.name;
						}
						completeBlock();
					}];
			   }else{
				   [self sendPluginError:[NSString stringWithFormat:@"Failed to geocode: no internet connection. %@ requires start as lat,lon but plugin was passed an address", app]];
			   }
		   }else{
		   	   [self sendPluginError:[NSString stringWithFormat:@"Failed to geocode: geocoding disabled. %@ requires start as lat,lon but plugin was passed an address", app]];
		   }
		}else{
			completeBlock();
		}
    }
}


- (void) sendPluginError:(NSString*)errorMessage{
    [self logError:errorMessage];
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
        case CMMapAppSygic:
            lnName = @"sygic";
            break;
        case CMMapAppHereMaps:
            lnName = @"here_maps";
            break;
        case CMMapAppMoovit:
            lnName = @"moovit";
            break;
        case CMMapAppLyft:
            lnName = @"lyft";
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
    }else if([lnName isEqual: @"sygic"]){
        cmmName = CMMapAppSygic;
    }else if([lnName isEqual: @"here_maps"]){
        cmmName = CMMapAppHereMaps;
    }else if([lnName isEqual: @"moovit"]){
        cmmName = CMMapAppMoovit;
    }else if([lnName isEqual: @"lyft"]){
        cmmName = CMMapAppLyft;
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
    return self.enableGeocoding;
}

+(bool)isNetworkAvailable
{
    SCNetworkReachabilityFlags flags;
    SCNetworkReachabilityRef address;
    address = SCNetworkReachabilityCreateWithName(NULL, "www.appleiphonecell.com" );
    Boolean success = SCNetworkReachabilityGetFlags(address, &flags);
    CFRelease(address);

    bool canReach = success
                    && !(flags & kSCNetworkReachabilityFlagsConnectionRequired)
                    && (flags & kSCNetworkReachabilityFlagsReachable);

    return canReach;
}
@end
