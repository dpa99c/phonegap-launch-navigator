/*
 * LN_LaunchNavigator Plugin for Phonegap
 *
 * Copyright (c) 2018 Dave Alden  (http://github.com/dpa99c)
 * Copyright (c) 2018 Working Edge Ltd. (http://www.workingedge.co.uk)
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
#import "LaunchNavigatorPlugin.h"

@implementation LaunchNavigatorPlugin
@synthesize logger;
@synthesize launchNavigator;
@synthesize cordova_command;


- (void)pluginInitialize {
    self.logger = [[WE_CordovaLogger alloc] initWithDelegate:self.commandDelegate logTag:@"LaunchNavigatorPlugin"];
    self.launchNavigator = [[LN_LaunchNavigator alloc] init:[[WE_CordovaLogger alloc] initWithDelegate:self.commandDelegate logTag:@"LN_LaunchNavigator"]];
    [super pluginInitialize];
}

/**************
 * Plugin API
 **************/
 - (void) enableDebug:(CDVInvokedUrlCommand*)command;{
     @try {
         bool debugEnabled = [[command argumentAtIndex:0] boolValue];
         [self.logger setEnabled:debugEnabled];
         [[self.launchNavigator getLogger] setEnabled:debugEnabled];
         [self sendPluginSuccessWithCommand:command];
     }@catch (NSException* exception) {
         [self handleExceptionWithCommand:exception command:command];
     }
 }

- (void) navigate:(CDVInvokedUrlCommand*)command;
{
    @try {
        self.cordova_command = command;

        NSMutableDictionary* params = [NSMutableDictionary new];
        [params setValue:[command.arguments objectAtIndex:0] forKey:@"dest"];
        [params setValue:[command.arguments objectAtIndex:1] forKey:@"destType"];
        [params setValue:[command.arguments objectAtIndex:2] forKey:@"destName"];
        [params setValue:[command.arguments objectAtIndex:3] forKey:@"start"];
        [params setValue:[command.arguments objectAtIndex:4] forKey:@"startType"];
        [params setValue:[command.arguments objectAtIndex:5] forKey:@"startName"];
        [params setValue:[command.arguments objectAtIndex:6] forKey:@"appName"];
        [params setValue:[command.arguments objectAtIndex:7] forKey:@"transportMode"];
        [params setValue:[command.arguments objectAtIndex:8] forKey:@"launchMode"];
        [params setValue:[command.arguments objectAtIndex:9] forKey:@"extras"];
        [params setObject:[NSNumber numberWithBool:[[command argumentAtIndex:10] boolValue]] forKey:@"enableGeocoding"];

        [self.logger debug:[NSString stringWithFormat:@"Called navigate() with args: destination=%@; destType=%@; destName=%@; start=%@; startType=%@; startName=%@; appName=%@; transportMode=%@; launchMode=%@; extras=%@", params[@"dest"], params[@"destType"], params[@"destName"], params[@"start"], params[@"startType"], params[@"startName"], params[@"appName"], params[@"transportMode"], params[@"launchMode"], params[@"extras"]]];

        [launchNavigator navigate:params
            success:^(void) {
                [self sendPluginSuccess];
            }
            fail:^(NSString* errorMsg) {
                [self sendPluginError:errorMsg];
            }
        ];
    }@catch (NSException* exception) {
        [self handleExceptionWithCommand:exception command:command];
    }
}

- (void) isAppAvailable:(CDVInvokedUrlCommand*)command;{
    @try {
        BOOL result = [launchNavigator isAppAvailable:[command.arguments objectAtIndex:0]];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsBool:result];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }@catch (NSException* exception) {
        [self handleExceptionWithCommand:exception command:command];
    }
}

- (void) availableApps:(CDVInvokedUrlCommand*)command;{
    @try {
        NSDictionary* results = [launchNavigator availableApps];
        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:results];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }@catch (NSException* exception) {
        [self handleExceptionWithCommand:exception command:command];
    }
}

/*********************
 * Internal functions
 *********************/
- (void) sendPluginSuccessWithCommand:(CDVInvokedUrlCommand*)command{
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) sendPluginSuccess{
    [self sendPluginSuccessWithCommand:self.cordova_command];
}

- (void) sendPluginErrorWithCommand:(NSString*)errorMessage command:(CDVInvokedUrlCommand*)command{
    [self.logger error:errorMessage];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorMessage];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) sendPluginError:(NSString*)errorMessage{
    [self sendPluginErrorWithCommand:errorMessage command:self.cordova_command];
}

- (void) handleExceptionWithCommand:(NSException*)exception command:(CDVInvokedUrlCommand*)command{
    [self sendPluginErrorWithCommand:exception.reason command:command];
}

- (void) handleException:(NSException*)exception{
    [self sendPluginError:exception.reason];
}
@end
