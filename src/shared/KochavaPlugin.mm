//
//  KochavaPlugin.mm
//  Kochava Plugin
//
//  Copyright (c) 2016 Corona Labs Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import "CoronaRuntime.h"
#import "CoronaAssert.h"
#import "CoronaEvent.h"
#import "CoronaLua.h"
#import "CoronaLibrary.h"
#import "CoronaLuaIOS.h"
#import "KochavaPlugin.h"

// Kochava
#import "KochavaCore.h"
#import "KochavaTracker.h"


// some macros to make life easier, and code more readable
#define UTF8StringWithFormat(format, ...) [[NSString stringWithFormat:format, ##__VA_ARGS__] UTF8String]
#define MsgFormat(format, ...) [NSString stringWithFormat:format, ##__VA_ARGS__]
#define UTF8IsEqual(utf8str1, utf8str2) (strcmp(utf8str1, utf8str2) == 0)

#define NoValue INT_MAX

// ----------------------------------------------------------------------------
// Plugin Constants
// ----------------------------------------------------------------------------
#define PLUGIN_NAME        "plugin.kochava"
#define PLUGIN_VERSION     "3.0.0"
#define PLUGIN_SDK_VERSION @"5.0.0"

static const char EVENT_NAME[]    = "analyticsRequest";
static const char PROVIDER_NAME[] = "kochava";

// analytics types
static NSString * const TYPE_STANDARD    = @"standard";
static NSString * const TYPE_CUSTOM      = @"custom";
static NSString * const TYPE_DEEPLINK    = @"deepLink";
static NSString * const TYPE_ATTRIBUTION = @"attribution";
static NSString * const TYPE_CONSENT     = @"consent";

// standard param types
static const char STANDARD_TYPE_ACHIEVEMENT[]          = "achievement";
static const char STANDARD_TYPE_ADVIEW[]               = "adView";
static const char STANDARD_TYPE_ADDTOCART[]            = "addToCart";
static const char STANDARD_TYPE_ADDTOWISHLIST[]        = "addToWishList";
static const char STANDARD_TYPE_CHECKOUTSTART[]        = "checkoutStart";
static const char STANDARD_TYPE_LEVELCOMPLETE[]        = "levelComplete";
static const char STANDARD_TYPE_PURCHASE[]             = "purchase";
static const char STANDARD_TYPE_PUSH_RECEIVED[]        = "pushReceived";
static const char STANDARD_TYPE_PUSH_OPENED[]          = "pushOpened";
static const char STANDARD_TYPE_RATING[]               = "rating";
static const char STANDARD_TYPE_REGISTRATIONCOMPLETE[] = "registrationComplete";
static const char STANDARD_TYPE_SEARCH[]               = "search";
static const char STANDARD_TYPE_TUTORIALCOMPLETE[]     = "tutorialComplete";
static const char STANDARD_TYPE_VIEW[]                 = "view";

// standard param properties
static const char STANDARD_PROPERTY_ACTION[]               = "action";
static const char STANDARD_PROPERTY_ADCAMPAIGNID[]         = "adCampaignId";
static const char STANDARD_PROPERTY_ADCAMPAIGNNAME[]       = "adCampaignName";
static const char STANDARD_PROPERTY_ADDEVICETYPE[]         = "adDeviceType";
static const char STANDARD_PROPERTY_ADGROUPID[]            = "adGroupId";
static const char STANDARD_PROPERTY_ADGROUPNAME[]          = "adGroupName";
static const char STANDARD_PROPERTY_ADMEDIATIONNAME[]      = "adMediationName";
static const char STANDARD_PROPERTY_ADNETWORKNAME[]        = "adNetworkName";
static const char STANDARD_PROPERTY_ADPLACEMENT[]          = "adPlacement";
static const char STANDARD_PROPERTY_ADSIZE[]               = "adSize";
static const char STANDARD_PROPERTY_ADTYPE[]               = "adType";
static const char STANDARD_PROPERTY_BACKGROUND[]           = "background";
static const char STANDARD_PROPERTY_CHECKOUTASGUEST[]      = "checkoutAsGuest";
static const char STANDARD_PROPERTY_COMPLETED[]            = "completed";
static const char STANDARD_PROPERTY_CONTENTID[]            = "contentId";
static const char STANDARD_PROPERTY_CONTENTTYPE[]          = "contentType";
static const char STANDARD_PROPERTY_CURRENCY[]             = "currency";
static const char STANDARD_PROPERTY_DATE[]                 = "date";
static const char STANDARD_PROPERTY_DESCRIPTION[]          = "description";
static const char STANDARD_PROPERTY_DESTINATION[]          = "destination";
static const char STANDARD_PROPERTY_DURATIONTIMEINTERVAL[] = "durationTimeInterval";
static const char STANDARD_PROPERTY_ENDDATE[]              = "endDate";
static const char STANDARD_PROPERTY_ITEMADDEDFROM[]        = "itemAddedFrom";
static const char STANDARD_PROPERTY_LEVEL[]                = "level";
static const char STANDARD_PROPERTY_MAXRATING[]            = "maxRating";
static const char STANDARD_PROPERTY_NAME[]                 = "name";
static const char STANDARD_PROPERTY_ORDERID[]              = "orderId";
static const char STANDARD_PROPERTY_ORIGIN[]               = "origin";
static const char STANDARD_PROPERTY_PAYLOAD[]              = "payload";
static const char STANDARD_PROPERTY_PRICE[]                = "price";
static const char STANDARD_PROPERTY_QUANTITY[]             = "quantity";
static const char STANDARD_PROPERTY_RATINGVALUE[]          = "ratingValue";
static const char STANDARD_PROPERTY_RECEIPTID[]            = "receiptId";
static const char STANDARD_PROPERTY_RECEIPTDATA[]          = "receiptData";
static const char STANDARD_PROPERTY_RECEIPTDATASIGNATURE[] = "receiptDataSignature";
static const char STANDARD_PROPERTY_REFERRALFROM[]         = "referralFrom";
static const char STANDARD_PROPERTY_REGISTRATIONMETHOD[]   = "registrationMethod";
static const char STANDARD_PROPERTY_RESULTS[]              = "results";
static const char STANDARD_PROPERTY_SCORE[]                = "score";
static const char STANDARD_PROPERTY_SEARCHTERM[]           = "searchTerm";
static const char STANDARD_PROPERTY_SPATIAL_X[]            = "spatialX";
static const char STANDARD_PROPERTY_SPATIAL_Y[]            = "spatialY";
static const char STANDARD_PROPERTY_SPATIAL_Z[]            = "spatialZ";
static const char STANDARD_PROPERTY_STARTDATE[]            = "startDate";
static const char STANDARD_PROPERTY_SUCCESS[]              = "success";
static const char STANDARD_PROPERTY_USERID[]               = "userId";
static const char STANDARD_PROPERTY_USERNAME[]             = "userName";
static const char STANDARD_PROPERTY_VALIDATED[]            = "validated";

// valid standard param types
static const NSArray *validStandardParamTypes = @[
  @(STANDARD_TYPE_ACHIEVEMENT),
  @(STANDARD_TYPE_ADVIEW),
  @(STANDARD_TYPE_ADDTOCART),
  @(STANDARD_TYPE_ADDTOWISHLIST),
  @(STANDARD_TYPE_CHECKOUTSTART),
  @(STANDARD_TYPE_LEVELCOMPLETE),
  @(STANDARD_TYPE_PURCHASE),
  @(STANDARD_TYPE_PUSH_RECEIVED),
  @(STANDARD_TYPE_PUSH_OPENED),
  @(STANDARD_TYPE_RATING),
  @(STANDARD_TYPE_REGISTRATIONCOMPLETE),
  @(STANDARD_TYPE_SEARCH),
  @(STANDARD_TYPE_TUTORIALCOMPLETE),
  @(STANDARD_TYPE_VIEW)
];

// valid standard param properties and expected type
static const NSDictionary *validStandardParamProperties = @{
  @(STANDARD_PROPERTY_ACTION):                @"String",
  @(STANDARD_PROPERTY_ADCAMPAIGNID):          @"String",
  @(STANDARD_PROPERTY_ADCAMPAIGNNAME):        @"String",
  @(STANDARD_PROPERTY_ADDEVICETYPE):          @"String",
  @(STANDARD_PROPERTY_ADGROUPID):             @"String",
  @(STANDARD_PROPERTY_ADGROUPNAME):           @"String",
  @(STANDARD_PROPERTY_ADMEDIATIONNAME):       @"String",
  @(STANDARD_PROPERTY_ADNETWORKNAME):         @"String",
  @(STANDARD_PROPERTY_ADPLACEMENT):           @"String",
  @(STANDARD_PROPERTY_ADSIZE):                @"String",
  @(STANDARD_PROPERTY_ADTYPE):                @"String",
  @(STANDARD_PROPERTY_BACKGROUND):            @"Boolean",
  @(STANDARD_PROPERTY_CHECKOUTASGUEST):       @"Boolean",
  @(STANDARD_PROPERTY_COMPLETED):             @"Boolean",
  @(STANDARD_PROPERTY_CONTENTID):             @"String",
  @(STANDARD_PROPERTY_CONTENTTYPE):           @"String",
  @(STANDARD_PROPERTY_CURRENCY):              @"String",
  @(STANDARD_PROPERTY_DATE):                  @"Date",
  @(STANDARD_PROPERTY_DESCRIPTION):           @"String",
  @(STANDARD_PROPERTY_DESTINATION):           @"String",
  @(STANDARD_PROPERTY_DURATIONTIMEINTERVAL):  @"Interval",
  @(STANDARD_PROPERTY_ENDDATE):               @"Date",
  @(STANDARD_PROPERTY_ITEMADDEDFROM):         @"String",
  @(STANDARD_PROPERTY_LEVEL):                 @"String",
  @(STANDARD_PROPERTY_MAXRATING):             @"Double",
  @(STANDARD_PROPERTY_NAME):                  @"String",
  @(STANDARD_PROPERTY_ORDERID):               @"String",
  @(STANDARD_PROPERTY_ORIGIN):                @"String",
  @(STANDARD_PROPERTY_PAYLOAD):               @"Table",
  @(STANDARD_PROPERTY_PRICE):                 @"Double",
  @(STANDARD_PROPERTY_QUANTITY):              @"Double",
  @(STANDARD_PROPERTY_RATINGVALUE):           @"Double",
  @(STANDARD_PROPERTY_RECEIPTID):             @"String",
  @(STANDARD_PROPERTY_RECEIPTDATA):           @"String",
  @(STANDARD_PROPERTY_RECEIPTDATASIGNATURE):  @"String",
  @(STANDARD_PROPERTY_REFERRALFROM):          @"String",
  @(STANDARD_PROPERTY_REGISTRATIONMETHOD):    @"String",
  @(STANDARD_PROPERTY_RESULTS):               @"String",
  @(STANDARD_PROPERTY_SCORE):                 @"String",
  @(STANDARD_PROPERTY_SEARCHTERM):            @"String",
  @(STANDARD_PROPERTY_SPATIAL_X):             @"Double",
  @(STANDARD_PROPERTY_SPATIAL_Y):             @"Double",
  @(STANDARD_PROPERTY_SPATIAL_Z):             @"Double",
  @(STANDARD_PROPERTY_STARTDATE):             @"Date",
  @(STANDARD_PROPERTY_SUCCESS):               @"String",
  @(STANDARD_PROPERTY_USERID):                @"String",
  @(STANDARD_PROPERTY_USERNAME):              @"String",
  @(STANDARD_PROPERTY_VALIDATED):             @"String"
};

// event phases
static NSString * const PHASE_INIT     = @"init";
static NSString * const PHASE_RECEIVED = @"received";
static NSString * const PHASE_RECORDED = @"recorded";
static NSString * const PHASE_FAILED   = @"failed";

// message constants
static NSString * const ERROR_MSG   = @"ERROR: ";
static NSString * const WARNING_MSG = @"WARNING: ";

// add missing keys
static const char EVENT_DATA_KEY[]  = "data";

static bool hasUserConsent = false;
static bool intelligentConsentManagement = false;

@implementation NSData (HexString)

// ----------------------------------------------------------------------------
// NSData extension to convert hex string to data
// ----------------------------------------------------------------------------

+ (NSData *)dataFromHexString:(NSString *)string
{
  string = [string lowercaseString];
  NSMutableData *data= [NSMutableData new];
  unsigned char whole_byte;
  char byte_chars[3] = {'\0','\0','\0'};
  NSUInteger i = 0;
  NSUInteger length = string.length;
  
  while (i < length-1) {
    char c = [string characterAtIndex:i++];
    
    if (c < '0' || (c > '9' && c < 'a') || c > 'f') {
      continue;
    }
    
    byte_chars[0] = c;
    byte_chars[1] = [string characterAtIndex:i++];
    whole_byte = strtol(byte_chars, NULL, 16);
    [data appendBytes:&whole_byte length:1];
  }
  
  return data;
}

@end

// ----------------------------------------------------------------------------
// plugin class and delegate definitions
// ----------------------------------------------------------------------------

@interface KochavaDelegate: NSObject

@property (nonatomic, assign) CoronaLuaRef coronaListener;             // Reference to the Lua listener
@property (nonatomic, assign) id<CoronaRuntime> coronaRuntime;         // Pointer to the Corona runtime

- (void)dispatchLuaEvent:(NSDictionary *)dict;

@end

// ----------------------------------------------------------------------------

class KochavaPlugin
{
public:
  typedef KochavaPlugin Self;
  
public:
  static const char kName[];
		
public:
  static int Open(lua_State *L);
  static int Finalizer(lua_State *L);
  static Self *ToLibrary(lua_State *L);
  
protected:
  KochavaPlugin();
  bool Initialize(void *platformContext);
		
public: // plugin API
  static int getAttributionData(lua_State *L);
  static int init(lua_State *L);
  static int limitAdTracking(lua_State *L);
  static int logDeeplinkEvent(lua_State *L);
  static int logEvent(lua_State *L);
  static int logCustomEvent(lua_State *L); // for backward compatibility only (use logEvent instead)
  static int logStandardEvent(lua_State *L); // for backward compatibility only (use logEvent instead)
  static int setIdentityLink(lua_State *L);
  static int getVersion(lua_State *L);
  static int setHasUserConsent(lua_State *L);
  
private: // internal helper functions
  static void logMsg(lua_State *L, NSString *msgType,  NSString *errorMsg);
  static bool isSDKInitialized(lua_State *L);
  
private:
  NSString *functionSignature;                                  // used in logMsg to identify function
  UIViewController *coronaViewController;                       // application's view controller
};

const char KochavaPlugin::kName[] = PLUGIN_NAME;
KochavaDelegate *kochavaDelegate;                                     // Kochava delegate

// ----------------------------------------------------------------------------
// helper functions
// ----------------------------------------------------------------------------

// log message to console
void
KochavaPlugin::logMsg(lua_State *L, NSString* msgType, NSString* errorMsg)
{
  Self *context = ToLibrary(L);
  
  if (context) {
    Self& library = *context;
    
    NSString *functionID = [library.functionSignature copy];
    if (functionID.length > 0) {
      functionID = [functionID stringByAppendingString:@", "];
    }
    
    CoronaLuaLogPrefix(L, [msgType UTF8String], UTF8StringWithFormat(@"%@%@", functionID, errorMsg));
  }
}

// check if SDK calls can be made
bool
KochavaPlugin::isSDKInitialized(lua_State *L)
{
  if (kochavaDelegate == nil) {
    logMsg(L, ERROR_MSG, @"kochava.init() must be called before calling other API methods.");
    return false;
  }
  
  return true;
}

// Corona beacon listener
static int
beaconListener(lua_State *L)
{
  // NOP (Debugging purposes only)
  // Listener called but the function body should be empty for public release
  return 0;
}


// ----------------------------------------------------------------------------
// plugin implementation
// ----------------------------------------------------------------------------

int
KochavaPlugin::Open( lua_State *L )
{
  // Register __gc callback
  const char kMetatableName[] = __FILE__; // Globally unique string to prevent collision
  CoronaLuaInitializeGCMetatable( L, kMetatableName, Finalizer );
  
  void *platformContext = CoronaLuaGetContext(L);
  
  // Set library as upvalue for each library function
  Self *library = new Self;
  
  if (library->Initialize(platformContext)) {
    // Functions in library
    static const luaL_Reg kFunctions[] = {
      {"getAttributionData", getAttributionData},
      {"init", init},
      {"limitAdTracking", limitAdTracking},
      {"logDeeplinkEvent", logDeeplinkEvent},
      {"logEvent", logEvent},
      {"logCustomEvent", logCustomEvent},
      {"logStandardEvent", logStandardEvent},
      {"setIdentityLink", setIdentityLink},
      {"getVersion", getVersion},
      {"setHasUserConsent", setHasUserConsent},
      {NULL, NULL}
    };
    
    // Register functions as closures, giving each access to the
    // 'library' instance via ToLibrary()
    {
      CoronaLuaPushUserdata(L, library, kMetatableName);
      luaL_openlib(L, kName, kFunctions, 1); // leave "library" on top of stack
    }
  }
  
  return 1;
}

int
KochavaPlugin::Finalizer( lua_State *L )
{
  Self *library = (Self *)CoronaLuaToUserdata(L, 1);
  
  // Free the Lua listener
  CoronaLuaDeleteRef(L, kochavaDelegate.coronaListener);
  kochavaDelegate = nil;
  
  delete library;
  
  return 0;
}

KochavaPlugin*
KochavaPlugin::ToLibrary( lua_State *L )
{
  // library is pushed as part of the closure
  Self *library = (Self *)CoronaLuaToUserdata( L, lua_upvalueindex( 1 ) );
  return library;
}

KochavaPlugin::KochavaPlugin()
: coronaViewController(nil)
{
}

bool
KochavaPlugin::Initialize( void *platformContext )
{
  bool shouldInit = (! coronaViewController);
  
  if (shouldInit) {
    id<CoronaRuntime> runtime = (__bridge id<CoronaRuntime>)platformContext;
    coronaViewController = runtime.appViewController;
    
    functionSignature = @"";
    
    kochavaDelegate = [KochavaDelegate new];
    kochavaDelegate.coronaRuntime = runtime;
  }
  
  return shouldInit;
}

// [Lua] getAttributionData()
int
KochavaPlugin::getAttributionData(lua_State *L)
{
  Self *context = ToLibrary(L);
  
  if (! context) { // abort if no valid context
    return 0;
  }
  
  Self& library = *context;
  
  library.functionSignature = @"kochava.getAttributionData()";
  
  if (! isSDKInitialized(L)) {
    return 0;
  }
  
  // check number or args
  int nargs = lua_gettop(L);
  if (nargs != 0) {
    logMsg(L, ERROR_MSG, MsgFormat(@"Expected no arguments, got %d", nargs));
    return 0;
  }
  
    // send the attribution result to the corona listener
    [KVATracker.shared.attribution retrieveResultWithCompletionHandler:^(KVAAttributionResult * _Nonnull attributionResult)
    {
        [kochavaDelegate tracker:KVATracker.shared didRetrieveAttributionDictionary:[attributionResult kva_asForContextObjectWithContext:KVAContext.log]];
    }];
    
    return 0;
}

// [Lua] init(listener, options)
int
KochavaPlugin::init(lua_State *L)
{
  Self *context = ToLibrary(L);
  
  if (! context) { // abort if no valid context
    return 0;
  }
  
  Self& library = *context;
  
  const char *appGUID = NULL;
  bool limitAdTracking = false;
  bool enableLogging = false;
  bool enableAttributionData = false;
  
  // prevent init from being called twice
  if (kochavaDelegate.coronaListener != NULL) {
    logMsg(L, ERROR_MSG, @"init should only be called once");
    return 0;
  }
  
  library.functionSignature = @"kochava.init(listener, options)";
  
  // check number or args
  int nargs = lua_gettop(L);
  if (nargs != 2) {
    logMsg(L, ERROR_MSG, MsgFormat(@"Expected 2 arguments, got %d", nargs));
    return 0;
  }
  
  // Get the listener (required)
  if (CoronaLuaIsListener(L, 1, PROVIDER_NAME)) {
    kochavaDelegate.coronaListener = CoronaLuaNewRef(L, 1);
  }
  else {
    logMsg(L, ERROR_MSG, MsgFormat(@"Listener expected, got: %s", luaL_typename(L, 1)));
    return 0;
  }
  
  // check for options table (required)
  if (lua_type(L, 2) == LUA_TTABLE) {
    // traverse and validate all the options
    for (lua_pushnil(L); lua_next(L, 2) != 0; lua_pop(L, 1)) {
      const char *key = lua_tostring(L, -2);
      
      // check for appId (for backward compatibility)
      if (UTF8IsEqual(key, "appId")) {
        if (lua_type(L, -1) == LUA_TSTRING) {
          appGUID = lua_tostring(L, -1);
        }
        else {
          logMsg(L, ERROR_MSG, MsgFormat(@"options.appGUID (string) expected, got %s", luaL_typename(L, -1)));
          return 0;
        }
      }
      // check for appGUID (required)
      else if (UTF8IsEqual(key, "appGUID")) {
        if (lua_type(L, -1) == LUA_TSTRING) {
          appGUID = lua_tostring(L, -1);
        }
        else {
          logMsg(L, ERROR_MSG, MsgFormat(@"options.appGUID (string) expected, got %s", luaL_typename(L, -1)));
          return 0;
        }
      }
      // limit ad tracking (optional) default false
      else if (UTF8IsEqual(key, "limitAdTracking")) {
        if (lua_type(L, -1) == LUA_TBOOLEAN) {
          limitAdTracking = lua_toboolean(L, -1);
        }
        else {
          logMsg(L, ERROR_MSG, MsgFormat(@"options.limitAdTracking (boolean) expected, got %s", luaL_typename(L, -1)));
          return 0;
        }
      }
      // enable console logging (optional) default false
      else if (UTF8IsEqual(key, "enableDebugLogging")) {
        if (lua_type(L, -1) == LUA_TBOOLEAN) {
          enableLogging = lua_toboolean(L, -1);
        }
        else {
          logMsg(L, ERROR_MSG, MsgFormat(@"options.enableDebugLogging (boolean) expected, got %s", luaL_typename(L, -1)));
          return 0;
        }
      }
      // enable attribution data retrieval (optional) default false
      else if (UTF8IsEqual(key, "enableAttributionData")) {
        if (lua_type(L, -1) == LUA_TBOOLEAN) {
          enableAttributionData = lua_toboolean(L, -1);
        }
        else {
          logMsg(L, ERROR_MSG, MsgFormat(@"options.enableAttributionData (boolean) expected, got %s", luaL_typename(L, -1)));
          return 0;
        }
      }
      else if (UTF8IsEqual(key, "hasUserConsent")) {
          if (lua_type(L, -1) == LUA_TBOOLEAN) {
              hasUserConsent = lua_toboolean(L, -1);
          }
          else {
              logMsg(L, ERROR_MSG, MsgFormat(@"options.hasUserConsent (boolean) expected, got %s", luaL_typename(L, -1)));
              return 0;
          }
      }
      else if (UTF8IsEqual(key, "intelligentConsentManagement")) {
          if (lua_type(L, -1) == LUA_TBOOLEAN) {
              intelligentConsentManagement = lua_toboolean(L, -1);
          }
          else {
              logMsg(L, ERROR_MSG, MsgFormat(@"options.intelligentConsentManagement (boolean) expected, got %s", luaL_typename(L, -1)));
              return 0;
          }
      }
      else {
        logMsg(L, ERROR_MSG, MsgFormat(@"Invalid option '%s'", key));
        return 0;
      }
    }
  }
  else {
    logMsg(L, ERROR_MSG, MsgFormat(@"options table expected, got %s", luaL_typename(L, 2)));
    return 0;
  }
  
  // check required params
  if (appGUID == NULL) {
    logMsg(L, ERROR_MSG, MsgFormat(@"options.appGUID is required"));
    return 0;
  }
       
    [KVATracker.shared startWithAppGUIDString: @(appGUID)];
    
    KVALog.shared.level = enableLogging ? KVALogLevel.debug : KVALogLevel.info;
    KVATracker.shared.appLimitAdTrackingBool = limitAdTracking;
    KVATracker.shared.consent.intelligentManagementBool = intelligentConsentManagement || hasUserConsent;


    [[KVATracker shared] consent].didUpdateBlock = ^(KVAConsent * _Nonnull consent){
        if (consent.shouldPromptBool) {
            if (!intelligentConsentManagement) {
                [[[KVATracker shared] consent] didPromptWithDidGrantBoolNumber:@(hasUserConsent)];
            } else {
                // send Corona Lua event
                NSDictionary *coronaEvent = @{
                                              @(CoronaEventPhaseKey()) : PHASE_RECEIVED,
                                              @(CoronaEventTypeKey()) : TYPE_CONSENT,
                                              @(EVENT_DATA_KEY) : @"Should prompt for user consent"
                                              };
                [kochavaDelegate dispatchLuaEvent:coronaEvent];
            }
        }
    };

    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 1 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
        
        NSString *deviceID = KVATracker.shared.deviceIdString ? KVATracker.shared.deviceIdString : @"not defined";
        // send Corona Lua event
        NSDictionary *coronaEvent = @{
                                      @(CoronaEventPhaseKey()) : PHASE_INIT,
                                      @(EVENT_DATA_KEY) : deviceID
                                      };
        [kochavaDelegate dispatchLuaEvent:coronaEvent];


        // Log plugin version to console
        NSString *version = PLUGIN_SDK_VERSION ? PLUGIN_SDK_VERSION : @"Plugin SDK version unknown";
        NSLog(@"%@", version);
    });
  
  return 0;
}

// [Lua] kochava.getVersion()
int
KochavaPlugin::getVersion(lua_State *L)
{
    Self *context = ToLibrary(L);

    if (! context) { // abort if no valid context
        return 0;
    }

    Self& library = *context;

    library.functionSignature = @"kochava.getVersion()";

    if (! isSDKInitialized(L)) {
        return 0;
    }

    logMsg(L, @"Data received:", MsgFormat(@"%s: %s (SDK: %@)", PLUGIN_NAME, PLUGIN_VERSION, PLUGIN_SDK_VERSION));

    // Create the reward event data
    NSDictionary *eventData = @{
                                @"pluginVersion": @PLUGIN_VERSION,
                                @"sdkVersion": PLUGIN_SDK_VERSION
                                };

    NSDictionary *coronaEvent = @{
                                  @(CoronaEventPhaseKey()): PHASE_INIT,
                                  @(CoronaEventDataKey()): eventData,
                                  };
    [kochavaDelegate dispatchLuaEvent:coronaEvent];

    return 0;
}

// [Lua] limitAdTracking(setting)
int
KochavaPlugin::limitAdTracking(lua_State *L)
{
  Self *context = ToLibrary(L);
  
  if (! context) { // abort if no valid context
    return 0;
  }
  
  Self& library = *context;
  
  bool limitAdTracking = false;
  
  library.functionSignature = @"kochava.limitAdTracking(setting)";
  
  if (! isSDKInitialized(L)) {
    return 0;
  }
  
  // check number or args
  int nargs = lua_gettop(L);
  if (nargs != 1) {
    logMsg(L, ERROR_MSG, MsgFormat(@"Expected 1 argument, got %d", nargs));
    return 0;
  }
  
  // get setting (required)
  if (lua_type(L, 1) == LUA_TBOOLEAN) {
    limitAdTracking = lua_toboolean(L, 1);
  }
  else {
    logMsg(L, ERROR_MSG, MsgFormat(@"'setting' (boolean) expected, got %s", luaL_typename(L, 1)));
    return 0;
  }
  
  // send the setting to Kochava
  [[KVATracker shared] setAppLimitAdTrackingBool:limitAdTracking];
  
  return 0;
}

// [Lua] logDeeplinkEvent(URL, sourceApp)
int
KochavaPlugin::logDeeplinkEvent(lua_State *L)
{
  Self *context = ToLibrary(L);
  
  if (! context) { // abort if no valid context
    return 0;
  }
  
  Self& library = *context;
  
  const char *URL = NULL;
  const char *sourceAppParams = NULL;
  
  library.functionSignature = @"kochava.logDeeplinkEvent(URL, sourceApp)";
  
  if (! isSDKInitialized(L)) {
    return 0;
  }
  
  // check number or args
  int nargs = lua_gettop(L);
  if (nargs != 2) {
    logMsg(L, ERROR_MSG, MsgFormat(@"Expected 2 arguments, got %d", nargs));
    return 0;
  }
  
  // get the app uri
  if (lua_type(L, 1) == LUA_TSTRING) {
    URL = lua_tostring(L, 1);
  }
  else {
    logMsg(L, ERROR_MSG, MsgFormat(@"URL (string) expected, got %s", luaL_typename(L, 1)));
    return 0;
  }
  
  // get the source app params
  if (lua_type(L, 2) == LUA_TSTRING) {
    sourceAppParams = lua_tostring(L, 2);
  }
  else {
    logMsg(L, ERROR_MSG, MsgFormat(@"sourceApp (string) expected, got %s", luaL_typename(L, 2)));
    return 0;
  }
  
  // send the deep link data to Kochava
    [KVAEvent sendCustomWithNameString:@"_Deeplink" infoDictionary:@{
        @"uri": @(URL),
        @"source":@(sourceAppParams)
    }];
  
  // send Corona Lua event
  NSDictionary *coronaEvent = @{
    @(CoronaEventPhaseKey()) : PHASE_RECORDED,
    @(CoronaEventTypeKey()) : TYPE_DEEPLINK
  };
  [kochavaDelegate dispatchLuaEvent:coronaEvent];
  
  return 0;
}

// DEPRECATED: use logEvent()
// remains here for backwards comapatibility
// --------------------------------------------------------------------------
// [Lua] logStandardEvent(event, options)
// --------------------------------------------------------------------------
int
KochavaPlugin::logStandardEvent(lua_State *L)
{
  NSLog(@"WARNING: logStandardEvent() is deprecated. Please use logEvent() instead");
  return logEvent(L);
}

// DEPRECATED: use logEvent()
// remains here for backwards comapatibility
// --------------------------------------------------------------------------
// [Lua] logCustomEvent(event [, details, receipt, receiptDataSignature])
// --------------------------------------------------------------------------
int
KochavaPlugin::logCustomEvent(lua_State *L)
{
  NSLog(@"WARNING: logCustomEvent() is deprecated. Please use logEvent() instead");
  
  Self *context = ToLibrary(L);
  
  if (! context) { // abort if no valid context
    return 0;
  }
  
  Self& library = *context;
  
  library.functionSignature = @"kochava.logCustomEvent(event [, details, receipt, receiptDataSignature])";
  
  if (! isSDKInitialized(L)) {
    return 0;
  }
  
  const char *eventName = NULL;
  const char *eventDetails = NULL;
  const char *receipt = NULL;
  
  // check number or args
  int nargs = lua_gettop(L);
  if ((nargs < 1) || (nargs > 4)) {
    logMsg(L, ERROR_MSG, MsgFormat(@"Expected 1 to 4 arguments, got %d", nargs));
    return 0;
  }
  
  // get event name
  if (lua_type(L, 1) == LUA_TSTRING) {
    eventName = lua_tostring(L, 1);
  }
  else {
    logMsg(L, ERROR_MSG, MsgFormat(@"eventName (string) expected, got %s", luaL_typename(L, 1)));
    return 0;
  }
  
  // get event details
  if (! lua_isnoneornil(L, 2)) {
    if (lua_type(L, 2) == LUA_TSTRING) {
      eventDetails = lua_tostring(L, 2);
    }
    else {
      logMsg(L, ERROR_MSG, MsgFormat(@"eventDetails (string) expected, got %s", luaL_typename(L, 2)));
      return 0;
    }
  }
  
  // get app store receipt
  if (! lua_isnoneornil(L, 3)) {
    if (lua_type(L, 3) == LUA_TSTRING) {
      receipt = lua_tostring(L, 3);
    }
    else {
      logMsg(L, ERROR_MSG, MsgFormat(@"receipt (string) expected, got %s", luaL_typename(L, 3)));
      return 0;
    }
  }
  
  // get store data signature (only used for Android, but checked for validity here)
  if (! lua_isnoneornil(L, 4)) {
    if (lua_type(L, 4) != LUA_TSTRING) {
      logMsg(L, ERROR_MSG, MsgFormat(@"receiptDataSignature (string) expected, got %s", luaL_typename(L, 4)));
      return 0;
    }
  }
  
  // validate event name (do not allow event names that begin with '_' (reserved for Kochava system))
  if (eventName[0] == '_') {
    logMsg(L, ERROR_MSG, MsgFormat(@"eventName can't start with '_' (reserved for Kochava system)"));
    return 0;
  }
  
  // validate event details. @() can't handle NULL strings
  // send an empty string as Android doesn't allow a nil value for eventDetails
  NSString *optionalEventDetails = (eventDetails == NULL) ? @"" : @(eventDetails);
  
  // track app store reciept event
  if (receipt != NULL) {
    NSData *receiptData = [NSData dataFromHexString:@(receipt)];
    NSString *encodedReceiptData = [receiptData base64EncodedStringWithOptions:0];
      KVAEvent *newEvent = [[KVAEvent alloc] init];
      [newEvent setNameString:@(eventName)];
      [newEvent setInfoString:optionalEventDetails];
      [newEvent setAppStoreReceiptBase64EncodedString:encodedReceiptData];
      [newEvent send];
  }
  else {
      [KVAEvent sendCustomWithNameString:@(eventName) infoString:optionalEventDetails];
  }
  
  // send Corona Lua event
  NSDictionary *coronaEvent = @{
    @(CoronaEventPhaseKey()) : PHASE_RECORDED,
    @(CoronaEventTypeKey()) : TYPE_CUSTOM
  };
  [kochavaDelegate dispatchLuaEvent:coronaEvent];
  
  return 0;
}

// [Lua] logEvent(eventType, options)
int
KochavaPlugin::logEvent(lua_State *L)
{
  Self *context = ToLibrary(L);
  
  if (! context) { // abort if no valid context
    return 0;
  }
  
  Self& library = *context;
  
  library.functionSignature = @"kochava.logEvent(eventType, options)";
  
  if (! isSDKInitialized(L)) {
    return 0;
  }
  
  // check number or args
  int nargs = lua_gettop(L);
  if ((nargs < 1) || (nargs > 2)) {
    logMsg(L, ERROR_MSG, MsgFormat(@"Expected 1 or 2 arguments, got %d", nargs));
    return 0;
  }
  
  const char *eventType = NULL;
  bool isCustomEvent = false;
  NSMutableDictionary *standardParams = [NSMutableDictionary new];
  
  // get event param type
  if (lua_type(L, 1) == LUA_TSTRING) {
    eventType = lua_tostring(L, 1);
  }
  else {
    logMsg(L, ERROR_MSG, MsgFormat(@"eventType (string) expected, got %s", luaL_typename(L, 1)));
    return 0;
  }
  
  // get event param properties
  if (! lua_isnoneornil(L, 2)) {
    if (lua_type(L, 2) == LUA_TSTRING) {
      // backwards compatibilty
      return logCustomEvent(L);
    }
    else if (lua_type(L, 2) == LUA_TTABLE) {
      // traverse and validate all the properties
      for (lua_pushnil(L); lua_next(L, 2) != 0; lua_pop(L, 1)) {
        const char *key = lua_tostring(L, -2);
        
        if (validStandardParamProperties[@(key)] != nil) {
          // check variable type
          if ([validStandardParamProperties[@(key)] isEqualToString:@"String"]) {
            if (lua_type(L, -1) == LUA_TSTRING) {
              standardParams[@(key)] = @(lua_tostring(L, -1));
            }
            else {
              logMsg(L, ERROR_MSG, MsgFormat(@"%s (string) expected, got %s", key, luaL_typename(L, -1)));
              return 0;
            }
          }
          else if ([validStandardParamProperties[@(key)] isEqualToString:@"Boolean"]) {
            if (lua_type(L, -1) == LUA_TBOOLEAN) {
              standardParams[@(key)] = @(lua_toboolean(L, -1));
            }
            else {
              logMsg(L, ERROR_MSG, MsgFormat(@"%s (boolean) expected, got %s", key, luaL_typename(L, -1)));
              return 0;
            }
          }
          else if ([validStandardParamProperties[@(key)] isEqualToString:@"Interval"]) {
            if (lua_type(L, -1) == LUA_TNUMBER) {
              standardParams[@(key)] = @(lua_tonumber(L, -1));
            }
            else {
              logMsg(L, ERROR_MSG, MsgFormat(@"%s (number) expected, got %s", key, luaL_typename(L, -1)));
              return 0;
            }
          }
          else if ([validStandardParamProperties[@(key)] isEqualToString:@"Double"]) {
            if (lua_type(L, -1) == LUA_TNUMBER) {
              standardParams[@(key)] = @(lua_tonumber(L, -1));
            }
            else {
              logMsg(L, ERROR_MSG, MsgFormat(@"%s (number) expected, got %s", key, luaL_typename(L, -1)));
              return 0;
            }
          }
          else if ([validStandardParamProperties[@(key)] isEqualToString:@"Date"]) {
            if (lua_type(L, -1) == LUA_TSTRING) {
              standardParams[@(key)] = @(lua_tostring(L, -1));
            }
            else {
              logMsg(L, ERROR_MSG, MsgFormat(@"%s (string) expected, got %s", key, luaL_typename(L, -1)));
              return 0;
            }
          }
        }
        else { // custom properties
          isCustomEvent = true;
          
          if (lua_type(L, -1) == LUA_TSTRING) {
            standardParams[@(key)] = @(lua_tostring(L, -1));
          }
          else if (lua_type(L, -1) == LUA_TBOOLEAN) {
            standardParams[@(key)] = @(lua_toboolean(L, -1));
          }
          else if (lua_type(L, -1) == LUA_TNUMBER) {
            standardParams[@(key)] = @(lua_tonumber(L, -1));
          }
          else {
            logMsg(L, ERROR_MSG, MsgFormat(@"options.%s unhandled type (%s)", key, luaL_typename(L, -1)));
            return 0;
          }
        }
      }
    }
    else {
      logMsg(L, ERROR_MSG, MsgFormat(@"options table expected, got %s", luaL_typename(L, 2)));
      return 0;
    }
  }
  
  // configure Kochava standard params
    KVAEvent *eventParams;
  NSMutableDictionary *customParams = [NSMutableDictionary new];
  
  if (UTF8IsEqual(eventType, STANDARD_TYPE_ACHIEVEMENT)) {
    eventParams = [KVAEvent eventWithType:KVAEventType.achievement];
  }
  else if (UTF8IsEqual(eventType, STANDARD_TYPE_ADVIEW)) {
      eventParams = [KVAEvent eventWithType:KVAEventType.adView];
  }
  else if (UTF8IsEqual(eventType, STANDARD_TYPE_ADDTOCART)) {
    eventParams = [KVAEvent eventWithType:KVAEventType.addToCart];
  }
  else if (UTF8IsEqual(eventType, STANDARD_TYPE_ADDTOWISHLIST)) {
      eventParams = [KVAEvent eventWithType:KVAEventType.addToWishList];
  }
  else if (UTF8IsEqual(eventType, STANDARD_TYPE_CHECKOUTSTART)) {
      eventParams = [KVAEvent eventWithType:KVAEventType.checkoutStart];
  }
  else if (UTF8IsEqual(eventType, STANDARD_TYPE_LEVELCOMPLETE)) {
      eventParams = [KVAEvent eventWithType:KVAEventType.levelComplete];
  }
  else if (UTF8IsEqual(eventType, STANDARD_TYPE_PURCHASE)) {
      eventParams = [KVAEvent eventWithType:KVAEventType.purchase];
  }
  else if (UTF8IsEqual(eventType, STANDARD_TYPE_PUSH_OPENED)) {
      eventParams = [KVAEvent eventWithType:KVAEventType.pushOpened];
  }
  else if (UTF8IsEqual(eventType, STANDARD_TYPE_PUSH_RECEIVED)) {
      eventParams = [KVAEvent eventWithType:KVAEventType.pushReceived];
  }
  else if (UTF8IsEqual(eventType, STANDARD_TYPE_RATING)) {
      eventParams = [KVAEvent eventWithType:KVAEventType.rating];
  }
  else if (UTF8IsEqual(eventType, STANDARD_TYPE_REGISTRATIONCOMPLETE)) {
      eventParams = [KVAEvent eventWithType:KVAEventType.registrationComplete];
  }
  else if (UTF8IsEqual(eventType, STANDARD_TYPE_SEARCH)) {
      eventParams = [KVAEvent eventWithType:KVAEventType.search];
  }
  else if (UTF8IsEqual(eventType, STANDARD_TYPE_TUTORIALCOMPLETE)) {
      eventParams = [KVAEvent eventWithType:KVAEventType.tutorialComplete];
  }
  else if (UTF8IsEqual(eventType, STANDARD_TYPE_VIEW)) {
      eventParams = [KVAEvent eventWithType:KVAEventType.view];
  }
  else { // custom type
    isCustomEvent = true;
      eventParams = [KVAEvent eventWithType:KVAEventType.custom];
    [eventParams setCustomEventNameString:@(eventType)];
  }
  
  // standard events must have properties
  if ((! isCustomEvent) && (standardParams.count == 0)) {
    logMsg(L, ERROR_MSG, @"Standard events must have parameters set");
    return 0;
  }
  
  for (NSString *key in standardParams) {
    if ([validStandardParamProperties[key] isEqualToString:@"Date"]) {
      NSDateFormatter *format = [[NSDateFormatter alloc] init];
      [format setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"GMT"]];
      [format setDateStyle:NSDateFormatterShortStyle];
      [format setDateFormat:@"yyyy-MM-dd"];
      NSDate *validateDate = [format dateFromString:standardParams[key]];
      
      if (validateDate != nil) {
        if ([key isEqualToString:@(STANDARD_PROPERTY_DATE)]) {
          [eventParams setDate:validateDate];
        }
        else if ([key isEqualToString:@(STANDARD_PROPERTY_ENDDATE)]) {
          [eventParams setEndDate:validateDate];
        }
        else if ([key isEqualToString:@(STANDARD_PROPERTY_STARTDATE)]) {
          [eventParams setStartDate:validateDate];
        }
      }
      else {
        logMsg(L, ERROR_MSG, MsgFormat(@"Invalid date '%@' (valid format 'yyyy-mm-dd')", standardParams[key]));
        return 0;
      }
    }
    else {
      if ([key isEqualToString:@(STANDARD_PROPERTY_ACTION)]) {
        [eventParams setActionString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_ADCAMPAIGNID)]) {
        [eventParams setAdCampaignIdString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_ADCAMPAIGNNAME)]) {
        [eventParams setAdCampaignNameString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_ADDEVICETYPE)]) {
        [eventParams setAdDeviceTypeString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_ADGROUPID)]) {
        [eventParams setAdGroupIdString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_ADGROUPNAME)]) {
        [eventParams setAdGroupNameString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_ADMEDIATIONNAME)]) {
        [eventParams setAdMediationNameString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_ADNETWORKNAME)]) {
        [eventParams setAdNetworkNameString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_ADPLACEMENT)]) {
        [eventParams setAdPlacementString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_ADSIZE)]) {
        [eventParams setAdSizeString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_ADTYPE)]) {
        [eventParams setAdTypeString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_BACKGROUND)]) {
        [eventParams setBackgroundBoolNumber:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_CHECKOUTASGUEST)]) {
        [eventParams setCheckoutAsGuestString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_COMPLETED)]) {
        [eventParams setCompletedBoolNumber:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_CONTENTID)]) {
        [eventParams setContentIdString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_CONTENTTYPE)]) {
        [eventParams setContentTypeString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_CURRENCY)]) {
        [eventParams setCurrencyString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_DESCRIPTION)]) {
        [eventParams setDescriptionString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_DESTINATION)]) {
        [eventParams setDestinationString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_DURATIONTIMEINTERVAL)]) {
        [eventParams setDurationTimeIntervalNumber:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_ITEMADDEDFROM)]) {
        [eventParams setItemAddedFromString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_LEVEL)]) {
        [eventParams setLevelString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_MAXRATING)]) {
        [eventParams setMaxRatingValueDoubleNumber:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_NAME)]) {
        [eventParams setNameString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_ORDERID)]) {
        [eventParams setOrderIdString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_ORIGIN)]) {
        [eventParams setOriginString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_PAYLOAD)]) {
        [eventParams setPayloadDictionary:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_PRICE)]) {
        [eventParams setPriceDoubleNumber:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_QUANTITY)]) {
        [eventParams setQuantityDoubleNumber:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_RATINGVALUE)]) {
        [eventParams setRatingValueDoubleNumber:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_RECEIPTID)]) {
        [eventParams setReceiptIdString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_RECEIPTDATA)]) {
        // format receipt
        NSData *receiptData = [NSData dataFromHexString:standardParams[key]];
        NSString *encodedReceiptData = [receiptData base64EncodedStringWithOptions:0];
        
        [eventParams setAppStoreReceiptBase64EncodedString:encodedReceiptData];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_RECEIPTDATASIGNATURE)]){
        // NOP (Google Play only)
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_REFERRALFROM)]) {
        [eventParams setReferralFromString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_REGISTRATIONMETHOD)]) {
        [eventParams setRegistrationMethodString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_RESULTS)]) {
        [eventParams setResultsString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_SCORE)]) {
        [eventParams setScoreString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_SEARCHTERM)]) {
        [eventParams setSearchTermString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_SPATIAL_X)]) {
        [eventParams setSpatialXDoubleNumber:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_SPATIAL_Y)]) {
        [eventParams setSpatialYDoubleNumber:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_SPATIAL_Z)]) {
        [eventParams setSpatialZDoubleNumber:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_SUCCESS)]) {
        [eventParams setSuccessString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_USERID)]) {
        [eventParams setUserIdString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_USERNAME)]) {
        [eventParams setUserNameString:standardParams[key]];
      }
      else if ([key isEqualToString:@(STANDARD_PROPERTY_VALIDATED)]) {
        [eventParams setValidatedString:standardParams[key]];
      }
      else {
        // add custom param to dictionary
        customParams[key] = standardParams[key];
      }
    }
  }
  
  // add custom parameters (if any)
  if (customParams.count > 0) {
    [eventParams setInfoDictionary:customParams];
  }
  
  // send event
    [eventParams send];
  
  // send Corona Lua event
  NSDictionary *coronaEvent = @{
    @(CoronaEventPhaseKey()) : PHASE_RECORDED,
    @(CoronaEventTypeKey()) : isCustomEvent ? TYPE_CUSTOM : TYPE_STANDARD
  };
  [kochavaDelegate dispatchLuaEvent:coronaEvent];
  
  return 0;
}

// [Lua] setIdentityLink(table)
int
KochavaPlugin::setIdentityLink(lua_State *L)
{
  Self *context = ToLibrary(L);
  
  if (! context) { // abort if no valid context
    return 0;
  }
  
  Self& library = *context;
  
  library.functionSignature = @"kochava.setIdentityLink(table)";
  
  if (! isSDKInitialized(L)) {
    return 0;
  }
  
  NSDictionary *dict;
  
  // check number or args
  int nargs = lua_gettop(L);
  if (nargs != 1) {
    logMsg(L, ERROR_MSG, MsgFormat(@"Expected 1 argument, got %d", nargs));
    return 0;
  }
  
  // check for key/value table (required)
  if (lua_type(L, 1) == LUA_TTABLE) {
    dict = CoronaLuaCreateDictionary(L, 1);
    int numEntries = (int)[dict count];
    if (numEntries > 2) {
      logMsg(L, ERROR_MSG, MsgFormat(@"key/value table can have a maximum of 2 key/value pairs, got %d", numEntries));
      return 0;
    }
  }
  else {
    logMsg(L, ERROR_MSG, MsgFormat(@"key/value table expected, got %s", luaL_typename(L, 1)));
    return 0;
  }
  
    // send link dictionary to Kochava
    [dict enumerateKeysAndObjectsUsingBlock:^(id key, id value, BOOL* stop) {
        [KVATracker.shared.identityLink registerWithNameString:key identifierString:value];
    }];
  
  return 0;
}

// [Lua] setHasUserConsent(boolean)
int
KochavaPlugin::setHasUserConsent(lua_State *L)
{
    Self *context = ToLibrary(L);

    if (! context) { // abort if no valid context
        return 0;
    }

    Self& library = *context;

    library.functionSignature = @"kochava.setHasUserConsent(boolean)";

    if (! isSDKInitialized(L)) {
        return 0;
    }

    bool localHasUserConsent = false;

    // check number or args
    int nargs = lua_gettop(L);
    if (nargs != 1) {
        logMsg(L, ERROR_MSG, MsgFormat(@"Expected 1 argument, got %d", nargs));
        return 0;
    }

    // check for consent boolean (required)
    if (lua_type(L, 1) == LUA_TBOOLEAN) {
        // send user consent to Kochava
        localHasUserConsent = lua_toboolean(L, -1);
        [[[KVATracker shared] consent] didPromptWithDidGrantBoolNumber:@(localHasUserConsent)];
        return 0;
    }
    else {
        logMsg(L, ERROR_MSG, MsgFormat(@"Boolean expected, got %s", luaL_typename(L, 1)));
        return 0;
    }
}

// ============================================================================
// delegate implementation
// ============================================================================

@implementation KochavaDelegate

- (instancetype)init {
  if (self = [super init]) {
    self.coronaListener = NULL;
    self.coronaRuntime = NULL;
  }
  
  return self;
}

// dispatch a new Lua event
- (void)dispatchLuaEvent:(NSDictionary *)dict
{
  [[NSOperationQueue mainQueue] addOperationWithBlock:^{
    lua_State *L = self.coronaRuntime.L;
    CoronaLuaRef coronaListener = self.coronaListener;
    bool hasErrorKey = false;
    
    // create new event
    CoronaLuaNewEvent(L, EVENT_NAME);
    
    for (NSString *key in dict) {
      CoronaLuaPushValue(L, [dict valueForKey:key]);
      lua_setfield(L, -2, key.UTF8String);
      
      if (! hasErrorKey) {
        hasErrorKey = [key isEqualToString:@(CoronaEventIsErrorKey())];
      }
    }
    
    // add error key if not in dict
    if (! hasErrorKey) {
      lua_pushboolean(L, false);
      lua_setfield(L, -2, CoronaEventIsErrorKey());
    }
    
    // add provider
    lua_pushstring(L, PROVIDER_NAME );
    lua_setfield(L, -2, CoronaEventProviderKey());
    
    CoronaLuaDispatchEvent(L, coronaListener, 0);
  }];
}

// delegate called when attribution result is available from kochava's server
- (void)tracker:(nonnull KVATracker *)tracker didRetrieveAttributionDictionary:(nonnull NSDictionary *)attributionDictionary
{
  if (! [NSJSONSerialization isValidJSONObject:attributionDictionary]) {
    NSLog(@"Kochava: attribution data cannot be converted to JSON object %@", attributionDictionary);
    // send Corona Lua event
    NSDictionary *coronaEvent = @{
      @(CoronaEventPhaseKey()) : PHASE_FAILED,
      @(CoronaEventTypeKey()) : TYPE_ATTRIBUTION,
      @(EVENT_DATA_KEY) : [NSString stringWithFormat:@"Cannot convert to JSON: %@", attributionDictionary]
    };
    [self dispatchLuaEvent:coronaEvent];
  }
  else {
    NSError *jsonError = nil;
  
    // convert data to json string
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:attributionDictionary options:0 error:&jsonError];
    
    if ((jsonData == nil) || (jsonError != nil)) {
      NSLog(@"Kochava JSON error %@", jsonError);
    }
    else {
      NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
      
      // send Corona Lua event
      NSDictionary *coronaEvent = @{
        @(CoronaEventPhaseKey()) : PHASE_RECEIVED,
        @(CoronaEventTypeKey()) : TYPE_ATTRIBUTION,
        @(EVENT_DATA_KEY) : jsonString
      };
      [self dispatchLuaEvent:coronaEvent];
    }
  }
}

@end

// ----------------------------------------------------------------------------

CORONA_EXPORT int luaopen_plugin_kochava(lua_State *L)
{
  return KochavaPlugin::Open(L);
}
