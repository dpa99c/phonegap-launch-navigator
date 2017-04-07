// CMMapLauncher.m
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

#import "CMMapLauncher.h"

@interface CMMapLauncher ()

+ (NSString*)urlPrefixForMapApp:(CMMapApp)mapApp;
+ (NSString*)urlEncode:(NSString*)queryParam;
+ (NSString*)googleMapsStringForMapPoint:(CMMapPoint*)mapPoint;

@end

static NSString*const LOG_TAG = @"CMMapLauncher";
static BOOL debugEnabled;


@implementation CMMapLauncher

+ (void)initialize {
    debugEnabled = FALSE;
    CMEmptyCoord = CLLocationCoordinate2DMake(CMEmptyLocation, CMEmptyLocation);
    CMEmptyAddress = nil;
}

+ (void)enableDebugLogging{
    debugEnabled = TRUE;
    [self logDebug:@"Debug logging enabled"];
}

+ (void)logDebug: (NSString*)msg
{
    if(debugEnabled){
        NSLog(@"%@: %@", LOG_TAG, msg);
    }
}

+ (void)logDebugURI: (NSString*)msg
{
    [self logDebug:[NSString stringWithFormat:@"Launching URI: %@", msg]];
}

+ (NSString*)urlPrefixForMapApp:(CMMapApp)mapApp {
    switch (mapApp) {
        case CMMapAppCitymapper:
            return @"citymapper://";

        case CMMapAppGoogleMaps:
            return @"comgooglemaps://";

        case CMMapAppNavigon:
            return @"navigon://";

        case CMMapAppTheTransitApp:
            return @"transit://";

        case CMMapAppWaze:
            return @"waze://";

        case CMMapAppYandex:
            return @"yandexnavi://";

        case CMMapAppUber:
            return @"uber://";
            
        case CMMapAppTomTom:
            return @"tomtomhome://";
        
        case CMMapAppSygic:
            return @"com.sygic.aura://";

        case CMMapAppHereMaps:
            return @"here-route://";

        case CMMapAppMoovit:
            return @"moovit://";

        default:
            return nil;
    }
}

+ (NSString*)urlEncode:(NSString*)queryParam {
    // Encode all the reserved characters, per RFC 3986
    // (<http://www.ietf.org/rfc/rfc3986.txt>)
    NSString* newString = (__bridge_transfer NSString*)CFURLCreateStringByAddingPercentEscapes(kCFAllocatorDefault, (__bridge CFStringRef)queryParam, NULL, (CFStringRef)@"!*'();:@&=+$,/?%#[]", kCFStringEncodingUTF8);

    if (newString) {
        return newString;
    }

    return @"";
}

+ (NSString*)extrasToQueryParams:(NSDictionary*)extras {
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

+ (NSString*)googleMapsStringForMapPoint:(CMMapPoint*)mapPoint {
    if (!mapPoint) {
        return @"";
    }

    if (mapPoint.isCurrentLocation && mapPoint.coordinate.latitude == 0.0 && mapPoint.coordinate.longitude == 0.0) {
        return @"";
    }

    if (mapPoint.name) {
        return [NSString stringWithFormat:@"%f,%f", mapPoint.coordinate.latitude, mapPoint.coordinate.longitude];
    }

    return [NSString stringWithFormat:@"%f,%f", mapPoint.coordinate.latitude, mapPoint.coordinate.longitude];
}

+ (BOOL)isMapAppInstalled:(CMMapApp)mapApp {
    if (mapApp == CMMapAppAppleMaps) {
        return YES;
    }

    NSString* urlPrefix = [CMMapLauncher urlPrefixForMapApp:mapApp];
    if (!urlPrefix) {
        return NO;
    }

    return [[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:urlPrefix]];
}

+ (BOOL)launchMapApp:(CMMapApp)mapApp forDirectionsTo:(CMMapPoint*)end {
    return [CMMapLauncher launchMapApp:mapApp forDirectionsTo:end directionsMode:nil];
}

+ (BOOL)launchMapApp:(CMMapApp)mapApp
     forDirectionsTo:(CMMapPoint*)end
      directionsMode:(NSString*)directionsMode {
    return [CMMapLauncher launchMapApp:mapApp forDirectionsFrom:[CMMapPoint currentLocation] to:end directionsMode:directionsMode];
}

+ (BOOL)launchMapApp:(CMMapApp)mapApp
   forDirectionsFrom:(CMMapPoint*)start
                  to:(CMMapPoint*)end {
    return [CMMapLauncher launchMapApp:mapApp forDirectionsFrom:start to:end directionsMode:nil];
}

+ (BOOL)launchMapApp:(CMMapApp)mapApp
   forDirectionsFrom:(CMMapPoint*)start
                  to:(CMMapPoint*)end
                  directionsMode:(NSString*)directionsMode{
    return [CMMapLauncher launchMapApp:mapApp forDirectionsFrom:start to:end directionsMode:directionsMode extras:nil];
}

// Main method
+ (BOOL)launchMapApp:(CMMapApp)mapApp
   forDirectionsFrom:(CMMapPoint*)start
                  to:(CMMapPoint*)end
      directionsMode:(NSString*)directionsMode
      extras:(NSDictionary*)extras{
    if (![CMMapLauncher isMapAppInstalled:mapApp]) {
        return NO;
    }
    
    Class mapItemClass = [MKMapItem class];
    bool gte_iOS6 = mapItemClass && [mapItemClass respondsToSelector:@selector(openMapsWithItems:launchOptions:)];

    if (mapApp == CMMapAppAppleMaps && gte_iOS6) {
        
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
        
        if(start.mapItem == nil){
            start.mapItem = [MKMapItem mapItemForCurrentLocation];
        }

            return [MKMapItem openMapsWithItems:@[start.mapItem, end.mapItem] launchOptions:launchOptions];
        
    } else if (mapApp == CMMapAppGoogleMaps || (mapApp == CMMapAppAppleMaps && !gte_iOS6)) {
        NSString* startStr;
        if([self isEmptyCoordinate:start.coordinate]){
            startStr = [start.address stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        }else{
            startStr = [CMMapLauncher googleMapsStringForMapPoint:start];
        }
        
        NSString* endStr;
        if([self isEmptyCoordinate:end.coordinate]){
            endStr = [end.address stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
        }else{
            endStr = [CMMapLauncher googleMapsStringForMapPoint:end];
        }
        
        NSMutableString* url = [[NSString stringWithFormat:@"%@?saddr=%@&daddr=%@",
                                 [self urlPrefixForMapApp:CMMapAppGoogleMaps],
                                 startStr,
                                 endStr
                                 ] mutableCopy];
        if (directionsMode) {
            [url appendFormat:@"&directionsmode=%@", directionsMode];
        }

        if(extras){
            [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
        }
        [self logDebugURI:url];
        return [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
    } else if (mapApp == CMMapAppCitymapper) {
        NSMutableArray* params = [NSMutableArray arrayWithCapacity:10];
        if (start && !start.isCurrentLocation) {
            if(![self isEmptyCoordinate:start.coordinate]){
                [params addObject:[NSString stringWithFormat:@"startcoord=%f,%f", start.coordinate.latitude, start.coordinate.longitude]];
            }
            if (start.name) {
                [params addObject:[NSString stringWithFormat:@"startname=%@", [CMMapLauncher urlEncode:start.name]]];
            }
            if (start.address) {
                [params addObject:[NSString stringWithFormat:@"startaddress=%@", [CMMapLauncher urlEncode:start.address]]];
            }
        }
        if (end && !end.isCurrentLocation) {
            if(![self isEmptyCoordinate:end.coordinate]){
                [params addObject:[NSString stringWithFormat:@"endcoord=%f,%f", end.coordinate.latitude, end.coordinate.longitude]];
            }
            if (end.name) {
                [params addObject:[NSString stringWithFormat:@"endname=%@", [CMMapLauncher urlEncode:end.name]]];
            }
            if (end.address) {
                [params addObject:[NSString stringWithFormat:@"endaddress=%@", [CMMapLauncher urlEncode:end.address]]];
            }
        }
        NSMutableString* url = [NSMutableString stringWithFormat:@"%@directions?%@",
            [self urlPrefixForMapApp:CMMapAppCitymapper],
            [params componentsJoinedByString:@"&"]];
        if(extras){
            [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
        }
        [self logDebugURI:url];
        return [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
    } else if (mapApp == CMMapAppTheTransitApp) {
        // http://thetransitapp.com/developers

        NSMutableArray* params = [NSMutableArray arrayWithCapacity:2];
        if (start && !start.isCurrentLocation) {
            [params addObject:[NSString stringWithFormat:@"from=%f,%f", start.coordinate.latitude, start.coordinate.longitude]];
        }
        if (end && !end.isCurrentLocation) {
            [params addObject:[NSString stringWithFormat:@"to=%f,%f", end.coordinate.latitude, end.coordinate.longitude]];
        }
        NSMutableString* url = [NSMutableString stringWithFormat:@"%@directions?%@",
            [self urlPrefixForMapApp:CMMapAppTheTransitApp],
            [params componentsJoinedByString:@"&"]];
        if(extras){
            [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
        }
        [self logDebugURI:url];
        return [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
    } else if (mapApp == CMMapAppNavigon) {
        // http://www.navigon.com/portal/common/faq/files/NAVIGON_AppInteract.pdf

        NSString* name = @"Destination";  // Doc doesn't say whether name can be omitted
        if (end.name) {
            name = end.name;
        }
        NSMutableString* url = [NSMutableString stringWithFormat:@"%@coordinate/%@/%f/%f",
            [self urlPrefixForMapApp:CMMapAppNavigon],
            [CMMapLauncher urlEncode:name], end.coordinate.longitude, end.coordinate.latitude];
        if(extras){
            [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
        }
        [self logDebugURI:url];
        return [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
    } else if (mapApp == CMMapAppWaze) {
        NSMutableString* url = [NSMutableString stringWithFormat:@"%@?ll=%f,%f&navigate=yes",
            [self urlPrefixForMapApp:CMMapAppWaze],
            end.coordinate.latitude, end.coordinate.longitude];
        if(extras){
            [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
        }
        [self logDebugURI:url];
        return [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
    } else if (mapApp == CMMapAppYandex) {
        NSMutableString* url = nil;
        if (start.isCurrentLocation) {
            url = [NSMutableString stringWithFormat:@"%@build_route_on_map?lat_to=%f&lon_to=%f",
                [self urlPrefixForMapApp:CMMapAppYandex],
                end.coordinate.latitude, end.coordinate.longitude];
        } else {
            url = [NSMutableString stringWithFormat:@"%@build_route_on_map?lat_to=%f&lon_to=%f&lat_from=%f&lon_from=%f",
                [self urlPrefixForMapApp:CMMapAppYandex],
                end.coordinate.latitude, end.coordinate.longitude, start.coordinate.latitude, start.coordinate.longitude];
        }
        if(extras){
            [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
        }
        [self logDebugURI:url];
        return [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
    } else if (mapApp == CMMapAppUber) {
        NSMutableString* url = nil;
        if (start.isCurrentLocation) {
            url = [NSMutableString stringWithFormat:@"%@?action=setPickup&pickup=my_location&dropoff[latitude]=%f&dropoff[longitude]=%f&dropoff[nickname]=%@",
                [self urlPrefixForMapApp:CMMapAppUber],
                end.coordinate.latitude,
                end.coordinate.longitude,
                [end.name stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
        } else {
            url = [NSMutableString stringWithFormat:@"%@?action=setPickup&pickup[latitude]=%f&pickup[longitude]=%f&pickup[nickname]=%@&dropoff[latitude]=%f&dropoff[longitude]=%f&dropoff[nickname]=%@",
                [self urlPrefixForMapApp:CMMapAppUber],
                start.coordinate.latitude,
                start.coordinate.longitude,
                [start.name stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding],
                end.coordinate.latitude,
                end.coordinate.longitude,
                [end.name stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
        }
        if(extras){
            [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
        }
        [self logDebugURI:url];
        return [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
    } else if (mapApp == CMMapAppTomTom) {
        NSMutableString* url = [NSMutableString stringWithFormat:@"tomtomhome:geo:action=navigateto&lat=%f&long=%f&name=%@",
            end.coordinate.latitude,
            end.coordinate.longitude,
            [end.name stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
        if(extras){
            [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
        }
        [self logDebugURI:url];
        return [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
    } else if (mapApp == CMMapAppSygic) {

         if([directionsMode isEqual: @"walking"]){
             directionsMode = @"walk";
         }else{
             directionsMode = @"drive";
         }
         NSString* separator = @"%7C";
         NSMutableString* url = [NSMutableString stringWithFormat:@"%@coordinate%@%f%@%f%@%@",
             [self urlPrefixForMapApp:CMMapAppSygic],
             separator,
             end.coordinate.longitude,
             separator,
             end.coordinate.latitude,
             separator,
             directionsMode];
         [self logDebugURI:url];
         return [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
    } else if (mapApp == CMMapAppHereMaps) {

        NSMutableString* startParam;
        if (start.isCurrentLocation) {
            startParam = (NSMutableString*) @"mylocation";
        } else {
            startParam = [NSMutableString stringWithFormat:@"%f,%f",
                start.coordinate.latitude, start.coordinate.longitude];

            if (start.name) {
                [startParam appendFormat:@",%@", [start.name stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
            }
        }

        NSMutableString* destParam = [NSMutableString stringWithFormat:@"%f,%f",
            end.coordinate.latitude, end.coordinate.longitude];

        if (end.name) {
            [destParam appendFormat:@",%@", [end.name stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
        }

        NSMutableString* url = [NSMutableString stringWithFormat:@"%@%@/%@",
            [self urlPrefixForMapApp:CMMapAppHereMaps],
             startParam,
             destParam];

        if(extras){
            [url appendFormat:@"?%@", [self extrasToQueryParams:extras]];
        }
        [self logDebugURI:url];
        return [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
     } else if (mapApp == CMMapAppMoovit) {

             NSMutableString* url = [NSMutableString stringWithFormat:@"%@directions", [self urlPrefixForMapApp:CMMapAppMoovit]];

             [url appendFormat:@"?dest_lat=%f&dest_lon=%f",
                  end.coordinate.latitude, end.coordinate.longitude];

              if (end.name) {
                  [url appendFormat:@"&dest_name=%@", [end.name stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
              }

              if (!start.isCurrentLocation) {
                  [url appendFormat:@"&orig_lat=%f&orig_lon=%f",
                      start.coordinate.latitude, start.coordinate.longitude];

                  if (start.name) {
                      [url appendFormat:@"&orig_name=%@", [start.name stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
                  }
              }

              if(extras){
                  [url appendFormat:@"%@", [self extrasToQueryParams:extras]];
              }
              [self logDebugURI:url];
              return [[UIApplication sharedApplication] openURL:[NSURL URLWithString:url]];
           }
    return NO;
}

+ (bool) isEmptyCoordinate:(CLLocationCoordinate2D)coordinate
{
    return coordinate.latitude == CMEmptyLocation && coordinate.longitude == CMEmptyLocation;
}

@end


///--------------------------
/// CMMapPoint (helper class)
///--------------------------

@implementation CMMapPoint

+ (CMMapPoint*)currentLocation {
    CMMapPoint* mapPoint = [[CMMapPoint alloc] init];
    mapPoint.isCurrentLocation = YES;
    return mapPoint;
}

+ (CMMapPoint*)mapPointWithCoordinate:(CLLocationCoordinate2D)coordinate {
    CMMapPoint* mapPoint = [[CMMapPoint alloc] init];
    mapPoint.coordinate = coordinate;
    return mapPoint;
}

+ (CMMapPoint*)mapPointWithName:(NSString*)name
                      coordinate:(CLLocationCoordinate2D)coordinate {
    CMMapPoint* mapPoint = [[CMMapPoint alloc] init];
    mapPoint.name = name;
    mapPoint.coordinate = coordinate;
    return mapPoint;
}

+ (CMMapPoint*)mapPointWithName:(NSString*)name
                         address:(NSString*)address
                      coordinate:(CLLocationCoordinate2D)coordinate {
    CMMapPoint* mapPoint = [[CMMapPoint alloc] init];
    mapPoint.name = name;
    mapPoint.address = address;
    mapPoint.coordinate = coordinate;
    return mapPoint;
}

+ (CMMapPoint*)mapPointWithAddress:(NSString*)address coordinate:(CLLocationCoordinate2D)coordinate {
    CMMapPoint* mapPoint = [[CMMapPoint alloc] init];
    mapPoint.address = address;
    mapPoint.coordinate = coordinate;
    return mapPoint;
}

- (NSString*)name {
    if (_isCurrentLocation) {
        return @"Current Location";
    }

    return _name;
}


+ (CMMapPoint*)mapPointWithMapItem:(MKMapItem*)mapItem
                               name:(NSString*)name
                            address:(NSString*)address
                         coordinate:(CLLocationCoordinate2D)coordinate{
    CMMapPoint* mapPoint = [[CMMapPoint alloc] init];
    mapPoint.mapItem = mapItem;
    mapPoint.name = name;
    mapPoint.address = address;
    mapPoint.coordinate = coordinate;
    return mapPoint;
}

@end

