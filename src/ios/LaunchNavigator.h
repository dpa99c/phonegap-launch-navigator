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
#ifdef CORDOVA_FRAMEWORK
#import <CORDOVA/CDVPlugin.h>
#else
#import "CORDOVA/CDVPlugin.h"
#endif

#import <MapKit/MapKit.h>


@interface LaunchNavigator :CDVPlugin {
    CDVInvokedUrlCommand* cordova_command;
    MKMapItem* start_mapItem;
    MKMapItem* dest_mapItem;
}
@property (nonatomic,retain) CDVInvokedUrlCommand* cordova_command;
@property (nonatomic,retain) MKMapItem* start_mapItem;
@property (nonatomic,retain) MKMapItem* dest_mapItem;

- (void) navigate:(CDVInvokedUrlCommand*)command;
- (void) googleMapsAvailable:(CDVInvokedUrlCommand*)command;


@end
