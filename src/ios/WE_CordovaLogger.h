#import "WE_Logger.h"
#import "Cordova/CDVCommandDelegate.h"

@interface WE_CordovaLogger : WE_Logger

@property (nonatomic,retain)CDVCommandDelegate* commandDelegate;
@property (nonatomic, retain) NSString* logTag;

- (void)init:(CDVCommandDelegate*)commandDelegate logTag:(NSString*)logTag;
-(void)error(NSString* msg);
-(void)warn(NSString* msg);
-(void)info(NSString* msg);
-(void)debug(NSString* msg);
-(void)verbose(NSString* msg);

@end