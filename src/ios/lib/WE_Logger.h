#import <Foundation/Foundation.h>

@interface WE_Logger : NSObject

@property (nonatomic) BOOL enabled;

-(id)init;
-(void)setEnabled:(BOOL)enabled;
-(BOOL)getEnabled;
-(void)error:(NSString*)msg;
-(void)warn:(NSString*)msg;
-(void)info:(NSString*)msg;
-(void)debug:(NSString*)msg;
-(void)verbose:(NSString*)msg;

@end
