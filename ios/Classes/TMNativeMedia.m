#import "TMNativeMedia.h"
#if __has_include(<tm_native_media/tm_native_media-Swift.h>)
#import <tm_native_media/tm_native_media-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "tm_native_media-Swift.h"
#endif

@implementation TMNativeMedia
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftTMNativeMedia registerWithRegistrar:registrar];
}
@end
