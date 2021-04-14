#import "TesseractPlugin.h"
#if __has_include(<tesseract/tesseract-Swift.h>)
#import <tesseract/tesseract-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "tesseract-Swift.h"
#endif

@implementation TesseractPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftTesseractPlugin registerWithRegistrar:registrar];
}
@end
