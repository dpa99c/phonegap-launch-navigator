#ifdef CORDOVA_FRAMEWORK
#import <CORDOVA/CDVPlugin.h>
#else
#import "CORDOVA/CDVPlugin.h"
#endif


@interface LaunchNavigator :CDVPlugin {
}

- (void) navigate:(CDVInvokedUrlCommand*)command;


@end
