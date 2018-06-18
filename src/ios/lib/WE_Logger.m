#import "WE_Logger.h"

@implementation WE_Logger
@synthesize enabled;

static BOOL enabled = FALSE;

/*******************
* Public API
*******************/
-(id)init{
    if(self = [super init]){
        
    }
    return self;
}

-(void)setEnabled:(BOOL)_enabled{
    enabled = _enabled;
}

-(BOOL)getEnabled{
    return enabled;
}

-(void)error:(NSString*)msg{
    [self throwException:@"error() must be overriden by subclass"];
}

-(void)warn:(NSString*)msg{
    [self throwException:@"warn() must be overriden by subclass"];
}

-(void)info:(NSString*)msg{
    [self throwException:@"info() must be overriden by subclass"];
}

-(void)debug:(NSString*)msg{
    [self throwException:@"debug() must be overriden by subclass"];
}

-(void)verbose:(NSString*)msg{
    [self throwException:@"verbose() must be overriden by subclass"];
}

/*********************
*  Internal functions
**********************/

-(void)throwException:(NSString*)reason{
    @throw([NSException exceptionWithName:reason reason:reason userInfo:nil]);
}

@end
