#import "WE_CordovaLogger.h"

@implementation WE_CordovaLogger
@synthesize commandDelegate;
@synthesize logTag;

/**********************
* Internal properties
**********************/


/*******************
* Public API
*******************/
- (id)initWithDelegate:(id <CDVCommandDelegate>)commandDelegate logTag:(NSString*)logTag{
    if(self = [super init]){
        self.commandDelegate = commandDelegate;
        self.logTag = logTag;
    }
    return self;
}

-(void)error:(NSString*) msg{
    [self log:msg jsLogLevel:@"error" nsLogLevel:@"error"];
}

-(void)warn:(NSString*) msg{
    [self log:msg jsLogLevel:@"warn" nsLogLevel:@"warn"];
}

-(void)info:(NSString*) msg{
    [self log:msg jsLogLevel:@"info" nsLogLevel:@"info"];
}

-(void)debug:(NSString*) msg{
    [self log:msg jsLogLevel:@"log" nsLogLevel:@"debug"];
}

-(void)verbose:(NSString*) msg{
    [self log:msg jsLogLevel:@"debug" nsLogLevel:@"verbose"];
}

/*********************
*  Internal functions
**********************/
- (void)executeGlobalJavascript: (NSString*)jsString
{
    [self.commandDelegate evalJs:jsString];
}

- (NSString*)escapeDoubleQuotes: (NSString*)str
{
    NSString *result =[str stringByReplacingOccurrencesOfString: @"\"" withString: @"\\\""];
    return result;
}

- (void)log: (NSString*)msg jsLogLevel:(NSString*)jsLogLevel nsLogLevel:(NSString*)nsLogLevel
{
    if(self.enabled){
        NSLog(@"%@[%@]: %@", self.logTag, nsLogLevel, msg);
        NSString* jsString = [NSString stringWithFormat:@"console.%@(\"%@: %@\")", jsLogLevel, self.logTag, [self escapeDoubleQuotes:msg]];
        [self executeGlobalJavascript:jsString];
    }
}
@end
