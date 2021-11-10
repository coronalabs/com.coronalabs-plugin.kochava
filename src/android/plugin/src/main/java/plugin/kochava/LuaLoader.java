//
// LuaLoader.java
// Kochava Plugin
//
// Copyright (c) 2016 CoronaLabs inc. All rights reserved.
//

// @formatter:off

package plugin.kochava;

import com.ansca.corona.CoronaActivity;
import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaLuaEvent;
import com.ansca.corona.CoronaRuntimeTask;
import com.ansca.corona.CoronaRuntimeTaskDispatcher;
import com.ansca.corona.CoronaEnvironment;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeListener;

import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaType;
import com.naef.jnlua.NamedJavaFunction;
import com.naef.jnlua.LuaState;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.os.Bundle;
import android.util.Log;

// Kochava imports
import com.kochava.base.Tracker;
import com.kochava.base.Tracker.IdentityLink;
import com.kochava.base.Tracker.Event;
import com.kochava.base.Tracker.Configuration;
import com.kochava.base.AttributionUpdateListener;
import com.kochava.base.ConsentStatusChangeListener;

/**
 * Implements the Lua interface for the Kochava Plugin.
 * <p>
 * Only one instance of this class will be created by Corona for the lifetime of the application.
 * This instance will be re-used for every new Corona activity that gets created.
 */
public class LuaLoader implements JavaFunction, CoronaRuntimeListener
{
  private static final String PLUGIN_NAME        = "plugin.kochava";
  private static final String PLUGIN_VERSION     = "3.0.0";
  private static final String PLUGIN_SDK_VERSION = Tracker.getVersion();

  private static final String EVENT_NAME    = "analyticsRequest";
  private static final String PROVIDER_NAME = "kochava";

  // analytics types
  private static final String TYPE_CUSTOM      = "custom";
  private static final String TYPE_STANDARD    = "standard";
  private static final String TYPE_DEEPLINK    = "deepLink";
  private static final String TYPE_ATTRIBUTION = "attribution";
  private static final String TYPE_CONSENT     = "consent";

  private static final String STANDARD_TYPE_ACHIEVEMENT          = "achievement";
  private static final String STANDARD_TYPE_ADVIEW               = "adView";
  private static final String STANDARD_TYPE_ADDTOCART            = "addToCart";
  private static final String STANDARD_TYPE_ADDTOWISHLIST        = "addToWishList";
  private static final String STANDARD_TYPE_CHECKOUTSTART        = "checkoutStart";
  private static final String STANDARD_TYPE_LEVELCOMPLETE        = "levelComplete";
  private static final String STANDARD_TYPE_PURCHASE             = "purchase";
  private static final String STANDARD_TYPE_PUSH_RECEIVED        = "pushReceived";
  private static final String STANDARD_TYPE_PUSH_OPENED          = "pushOpened";
  private static final String STANDARD_TYPE_RATING               = "rating";
  private static final String STANDARD_TYPE_REGISTRATIONCOMPLETE = "registrationComplete";
  private static final String STANDARD_TYPE_SEARCH               = "search";
  private static final String STANDARD_TYPE_TUTORIALCOMPLETE     = "tutorialComplete";
  private static final String STANDARD_TYPE_VIEW                 = "view";

  // standard param properties
  private static final String STANDARD_PROPERTY_ACTION               = "action";
  private static final String STANDARD_PROPERTY_ADCAMPAIGNID         = "adCampaignId";
  private static final String STANDARD_PROPERTY_ADCAMPAIGNNAME       = "adCampaignName";
  private static final String STANDARD_PROPERTY_ADDEVICETYPE         = "adDeviceType";
  private static final String STANDARD_PROPERTY_ADGROUPID            = "adGroupId";
  private static final String STANDARD_PROPERTY_ADGROUPNAME          = "adGroupName";
  private static final String STANDARD_PROPERTY_ADMEDIATIONNAME      = "adMediationName";
  private static final String STANDARD_PROPERTY_ADNETWORKNAME        = "adNetworkName";
  private static final String STANDARD_PROPERTY_ADPLACEMENT          = "adPlacement";
  private static final String STANDARD_PROPERTY_ADSIZE               = "adSize";
  private static final String STANDARD_PROPERTY_ADTYPE               = "adType";
  private static final String STANDARD_PROPERTY_BACKGROUND           = "background";
  private static final String STANDARD_PROPERTY_CHECKOUTASGUEST      = "checkoutAsGuest";
  private static final String STANDARD_PROPERTY_COMPLETED            = "completed";
  private static final String STANDARD_PROPERTY_CONTENTID            = "contentId";
  private static final String STANDARD_PROPERTY_CONTENTTYPE          = "contentType";
  private static final String STANDARD_PROPERTY_CURRENCY             = "currency";
  private static final String STANDARD_PROPERTY_DATE                 = "date";
  private static final String STANDARD_PROPERTY_DESCRIPTION          = "description";
  private static final String STANDARD_PROPERTY_DESTINATION          = "destination";
  private static final String STANDARD_PROPERTY_DURATIONTIMEINTERVAL = "durationTimeInterval";
  private static final String STANDARD_PROPERTY_ENDDATE              = "endDate";
  private static final String STANDARD_PROPERTY_ITEMADDEDFROM        = "itemAddedFrom";
  private static final String STANDARD_PROPERTY_LEVEL                = "level";
  private static final String STANDARD_PROPERTY_MAXRATING            = "maxRating";
  private static final String STANDARD_PROPERTY_NAME                 = "name";
  private static final String STANDARD_PROPERTY_ORDERID              = "orderId";
  private static final String STANDARD_PROPERTY_ORIGIN               = "origin";
  private static final String STANDARD_PROPERTY_PAYLOAD              = "payload";
  private static final String STANDARD_PROPERTY_PRICE                = "price";
  private static final String STANDARD_PROPERTY_QUANTITY             = "quantity";
  private static final String STANDARD_PROPERTY_RATINGVALUE          = "ratingValue";
  private static final String STANDARD_PROPERTY_RECEIPTID            = "receiptId";
  private static final String STANDARD_PROPERTY_RECEIPTDATA          = "receiptData";
  private static final String STANDARD_PROPERTY_RECEIPTDATASIGNATURE = "receiptDataSignature";
  private static final String STANDARD_PROPERTY_REFERRALFROM         = "referralFrom";
  private static final String STANDARD_PROPERTY_REGISTRATIONMETHOD   = "registrationMethod";
  private static final String STANDARD_PROPERTY_RESULTS              = "results";
  private static final String STANDARD_PROPERTY_SCORE                = "score";
  private static final String STANDARD_PROPERTY_SEARCHTERM           = "searchTerm";
  private static final String STANDARD_PROPERTY_SPATIAL_X            = "spatialX";
  private static final String STANDARD_PROPERTY_SPATIAL_Y            = "spatialY";
  private static final String STANDARD_PROPERTY_SPATIAL_Z            = "spatialZ";
  private static final String STANDARD_PROPERTY_STARTDATE            = "startDate";
  private static final String STANDARD_PROPERTY_SUCCESS              = "success";
  private static final String STANDARD_PROPERTY_USERID               = "userId";
  private static final String STANDARD_PROPERTY_USERNAME             = "userName";
  private static final String STANDARD_PROPERTY_VALIDATED            = "validated";

  // valid standard param types
  private static final List<String> validStandardParamTypes = new ArrayList<>();

  // valid standard param properties and expected type
  private static final Map<String, Object> validStandardParamProperties = new HashMap<>();

  // event phases
  private static final String PHASE_INIT     = "init";
  private static final String PHASE_RECEIVED = "received";
  private static final String PHASE_RECORDED = "recorded";

  // add missing event keys
  private static final String EVENT_PHASE_KEY = "phase";
  private static final String EVENT_DATA_KEY  = "data";
  private static final String EVENT_TYPE_KEY  = "type";

  // message constants
  private static final String CORONA_TAG  = "Corona";
  private static final String ERROR_MSG   = "ERROR: ";
  private static final String WARNING_MSG = "WARNING: ";

  private static int coronaListener = CoronaLua.REFNIL;
  private static CoronaRuntimeTaskDispatcher coronaRuntimeTaskDispatcher = null;

  private static String functionSignature = "";
  private static AttributionUpdateListener kochavaDelegate = null;
  private static ConsentStatusChangeListener kochavaConsentDelegate = null;

  private static Boolean hasUserConsent = false;
  private static Boolean intelligentConsentManagement = false;

  // -------------------------------------------------------
  // Plugin lifecycle events
  // -------------------------------------------------------

  /**
   * <p>
   * Note that a new LuaLoader instance will not be created for every CoronaActivity instance.
   * That is, only one instance of this class will be created for the lifetime of the application process.
   * This gives a plugin the option to do operations in the background while the CoronaActivity is destroyed.
   */
  @SuppressWarnings("unused")
  public LuaLoader() {
    // Set up this plugin to listen for Corona runtime events to be received by methods
    // onLoaded(), onStarted(), onSuspended(), onResumed(), and onExiting().

    CoronaEnvironment.addRuntimeListener(this);
  }

  /**
   * Called when this plugin is being loaded via the Lua require() function.
   * <p>
   * Note that this method will be called every time a new CoronaActivity has been launched.
   * This means that you'll need to re-initialize this plugin here.
   * <p>
   * Warning! This method is not called on the main UI thread.
   * @param L Reference to the Lua state that the require() function was called from.
   * @return Returns the number of values that the require() function will return.
   *         <p>
   *         Expected to return 1, the library that the require() function is loading.
   */
  @Override
  public int invoke(LuaState L) {
    // Register this plugin into Lua with the following functions.
    NamedJavaFunction[] luaFunctions = new NamedJavaFunction[] {
      new GetAttributionData(),
      new Init(),
      new LimitAdTracking(),
      new LogDeeplinkEvent(),
      new LogEvent(),
      new LogCustomEvent(),    // for backwards compatibility (use logEvent() instead)
      new LogStandardEvent(),  // for backwards compatibility (use logEvent() instead)
      new SetIdentityLink(),
            new GetVersion(),
            new SetHasUserConsent()
    };
    String libName = L.toString(1);
    L.register(libName, luaFunctions);

    // Returning 1 indicates that the Lua require() function will return the above Lua library
    return 1;
  }

  /**
   * Called after the Corona runtime has been created and just before executing the "main.lua" file.
   * <p>
   * Warning! This method is not called on the main thread.
   * @param runtime Reference to the CoronaRuntime object that has just been loaded/initialized.
   *                Provides a LuaState object that allows the application to extend the Lua API.
   */
  @Override
  public void onLoaded(CoronaRuntime runtime) {
    // Note that this method will not be called the first time a Corona activity has been launched.
    // This is because this listener cannot be added to the CoronaEnvironment until after
    // this plugin has been required-in by Lua, which occurs after the onLoaded() event.
    // However, this method will be called when a 2nd Corona activity has been created.

    if (coronaRuntimeTaskDispatcher == null) {
      coronaRuntimeTaskDispatcher = new CoronaRuntimeTaskDispatcher(runtime);

      validStandardParamTypes.add(STANDARD_TYPE_ACHIEVEMENT);
      validStandardParamTypes.add(STANDARD_TYPE_ADVIEW);
      validStandardParamTypes.add(STANDARD_TYPE_ADDTOCART);
      validStandardParamTypes.add(STANDARD_TYPE_ADDTOWISHLIST);
      validStandardParamTypes.add(STANDARD_TYPE_CHECKOUTSTART);
      validStandardParamTypes.add(STANDARD_TYPE_LEVELCOMPLETE);
      validStandardParamTypes.add(STANDARD_TYPE_PURCHASE);
      validStandardParamTypes.add(STANDARD_TYPE_PUSH_RECEIVED);
      validStandardParamTypes.add(STANDARD_TYPE_PUSH_OPENED);
      validStandardParamTypes.add(STANDARD_TYPE_RATING);
      validStandardParamTypes.add(STANDARD_TYPE_REGISTRATIONCOMPLETE);
      validStandardParamTypes.add(STANDARD_TYPE_SEARCH);
      validStandardParamTypes.add(STANDARD_TYPE_TUTORIALCOMPLETE);
      validStandardParamTypes.add(STANDARD_TYPE_VIEW);

      validStandardParamProperties.put(STANDARD_PROPERTY_ACTION,               "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_ADCAMPAIGNID,         "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_ADCAMPAIGNNAME,       "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_ADDEVICETYPE,         "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_ADGROUPID,            "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_ADGROUPNAME,          "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_ADMEDIATIONNAME,      "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_ADNETWORKNAME,        "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_ADPLACEMENT,          "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_ADSIZE,               "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_ADTYPE,               "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_BACKGROUND,           "Boolean");
      validStandardParamProperties.put(STANDARD_PROPERTY_CHECKOUTASGUEST,      "Boolean");
      validStandardParamProperties.put(STANDARD_PROPERTY_COMPLETED,            "Boolean");
      validStandardParamProperties.put(STANDARD_PROPERTY_CONTENTID,            "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_CONTENTTYPE,          "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_CURRENCY,             "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_DATE,                 "Date");
      validStandardParamProperties.put(STANDARD_PROPERTY_DESCRIPTION,          "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_DESTINATION,          "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_DURATIONTIMEINTERVAL, "Interval");
      validStandardParamProperties.put(STANDARD_PROPERTY_ENDDATE,              "Date");
      validStandardParamProperties.put(STANDARD_PROPERTY_ITEMADDEDFROM,        "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_LEVEL,                "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_MAXRATING,            "Double");
      validStandardParamProperties.put(STANDARD_PROPERTY_NAME,                 "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_ORDERID,              "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_ORIGIN,               "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_PAYLOAD,              "Table");
      validStandardParamProperties.put(STANDARD_PROPERTY_PRICE,                "Double");
      validStandardParamProperties.put(STANDARD_PROPERTY_QUANTITY,             "Double");
      validStandardParamProperties.put(STANDARD_PROPERTY_RATINGVALUE,          "Double");
      validStandardParamProperties.put(STANDARD_PROPERTY_RECEIPTID,            "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_RECEIPTDATA,          "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_RECEIPTDATASIGNATURE, "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_REFERRALFROM,         "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_REGISTRATIONMETHOD,   "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_RESULTS,              "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_SCORE,                "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_SEARCHTERM,           "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_SPATIAL_X,            "Double");
      validStandardParamProperties.put(STANDARD_PROPERTY_SPATIAL_Y,            "Double");
      validStandardParamProperties.put(STANDARD_PROPERTY_SPATIAL_Z,            "Double");
      validStandardParamProperties.put(STANDARD_PROPERTY_STARTDATE,            "Date");
      validStandardParamProperties.put(STANDARD_PROPERTY_SUCCESS,              "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_USERID,               "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_USERNAME,             "String");
      validStandardParamProperties.put(STANDARD_PROPERTY_VALIDATED,            "String");
    }
  }

  /**
   * Called just after the Corona runtime has executed the "main.lua" file.
   * <p>
   * Warning! This method is not called on the main thread.
   * @param runtime Reference to the CoronaRuntime object that has just been started.
   */
  @Override
  public void onStarted(CoronaRuntime runtime) {
  }

  /**
   * Called just after the Corona runtime has been suspended which pauses all rendering, audio, timers,
   * and other Corona related operations. This can happen when another Android activity (ie: window) has
   * been displayed, when the screen has been powered off, or when the screen lock is shown.
   * <p>
   * Warning! This method is not called on the main thread.
   * @param runtime Reference to the CoronaRuntime object that has just been suspended.
   */
  @Override
  public void onSuspended(CoronaRuntime runtime) {
  }

  /**
   * Called just after the Corona runtime has been resumed after a suspend.
   * <p>
   * Warning! This method is not called on the main thread.
   * @param runtime Reference to the CoronaRuntime object that has just been resumed.
   */
  @Override
  public void onResumed(CoronaRuntime runtime) {
  }

  /**
   * Called just before the Corona runtime terminates.
   * <p>
   * This happens when the Corona activity is being destroyed which happens when the user presses the Back button
   * on the activity, when the native.requestExit() method is called in Lua, or when the activity's finish()
   * method is called. This does not mean that the application is exiting.
   * <p>
   * Warning! This method is not called on the main thread.
   * @param runtime Reference to the CoronaRuntime object that is being terminated.
   */
  @Override
  public void onExiting(final CoronaRuntime runtime) {
    // reset class variables
    CoronaLua.deleteRef(runtime.getLuaState(), coronaListener);
    coronaListener = CoronaLua.REFNIL;

    validStandardParamTypes.clear();
    validStandardParamProperties.clear();

    kochavaDelegate = null;
    kochavaConsentDelegate = null;
    coronaRuntimeTaskDispatcher = null;
    functionSignature = "";
  }

  // --------------------------------------------------------------------------
  // helper functions
  // --------------------------------------------------------------------------

  // log message to console
  private void logMsg(String msgType, String errorMsg)
  {
    String functionID = functionSignature;
    if (!functionID.isEmpty()) {
      functionID += ", ";
    }

    Log.i(CORONA_TAG, msgType + functionID + errorMsg);
  }

  // return true if SDK is properly initialized
  private boolean isSDKInitialized() {
    if (kochavaDelegate == null) {
      logMsg(ERROR_MSG, "kochava.init() must be called before calling other API functions");
      return false;
    }

    return true;
  }

  // dispatch a Lua event to our callback (dynamic handling of properties through map)
  private void dispatchLuaEvent(final Map<String, Object> event) {
    if (coronaRuntimeTaskDispatcher != null) {
      coronaRuntimeTaskDispatcher.send(new CoronaRuntimeTask() {
        public void executeUsing(CoronaRuntime runtime) {
          try {
            LuaState L = runtime.getLuaState();
            CoronaLua.newEvent(L, EVENT_NAME);
            boolean hasErrorKey = false;

            // add event parameters from map
            for (String key: event.keySet()) {
              CoronaLua.pushValue(L, event.get(key));           // push value
              L.setField(-2, key);                              // push key

              if (! hasErrorKey) {
                hasErrorKey = key.equals(CoronaLuaEvent.ISERROR_KEY);
              }
            }

            // add error key if not in map
            if (! hasErrorKey) {
              L.pushBoolean(false);
              L.setField(-2, CoronaLuaEvent.ISERROR_KEY);
            }

            // add provider
            L.pushString(PROVIDER_NAME);
            L.setField(-2, CoronaLuaEvent.PROVIDER_KEY);

            CoronaLua.dispatchEvent(L, coronaListener, 0);
          }
          catch (Exception ex) {
            ex.printStackTrace();
          }
        }
      });
    }
  }

  private IdentityLink makeIdentityLink(Map<Object, Object> map) {
    IdentityLink link = new IdentityLink();

    for (Object key: map.keySet()) {
      String keyString;
      String valueString;

      Object value = map.get(key);

      if (key instanceof String) {
        keyString = (String)key;
      }
      else {
        keyString = key.toString();
      }

      if (value instanceof String) {
        valueString = (String)value;
      }
      else {
        valueString = value.toString();
      }

      link = link.add(keyString, valueString);
    }

    return link;
  }


  // -------------------------------------------------------
  // plugin implementation
  // -------------------------------------------------------

  // [Lua] getAttributionData()
  private class GetAttributionData implements NamedJavaFunction {
    /**
     * Gets the name of the Lua function as it would appear in the Lua script.
     * @return Returns the name of the custom Lua function.
     */
    @Override
    public String getName() {
      return "getAttributionData";
    }

    /**
     * This method is called when the Lua function is called.
     * <p>
     * Warning! This method is not called on the main UI thread.
     * @param luaState Reference to the Lua state.
     *                 Needed to retrieve the Lua function's parameters and to return values back to Lua.
     * @return Returns the number of values to be returned by the Lua function.
     */
    @Override
    public int invoke( LuaState luaState ) {
      functionSignature = "kochava.getAttributionData()";

      if (!isSDKInitialized()) {
        return 0;
      }

      final CoronaActivity coronaActivity = CoronaEnvironment.getCoronaActivity();

      if (coronaActivity == null) {
        // bail of no valid activity
        return 0;
      }

      // check number of args
      int nargs = luaState.getTop();
      if (nargs != 0) {
        logMsg(ERROR_MSG, "Expected no arguments, got " + nargs);
        return 0;
      }

      // Create a new runnable object to invoke our activity
      Runnable runnableActivity = new Runnable() {
        public void run() {
          // get attribution data
          String data = Tracker.getAttribution();

          // send Corona Lua event
          Map<String, Object> coronaEvent = new HashMap<>();
          coronaEvent.put(EVENT_PHASE_KEY, PHASE_RECEIVED);
          coronaEvent.put(EVENT_TYPE_KEY, TYPE_ATTRIBUTION);
          coronaEvent.put(EVENT_DATA_KEY, data);
          dispatchLuaEvent(coronaEvent);
        }
      };

      // Run the activity on the uiThread
      coronaActivity.runOnUiThread(runnableActivity);

      return 0;
    }
  }

  // [Lua] init(listener, params)
  private class Init implements NamedJavaFunction {
    /**
     * Gets the name of the Lua function as it would appear in the Lua script.
     * @return Returns the name of the custom Lua function.
     */
    @Override
    public String getName() {
      return "init";
    }

    /**
     * This method is called when the Lua function is called.
     * <p>
     * Warning! This method is not called on the main UI thread.
     * @param luaState Reference to the Lua state.
     *                 Needed to retrieve the Lua function's parameters and to return values back to Lua.
     * @return Returns the number of values to be returned by the Lua function.
     */
    @Override
    public int invoke(final LuaState luaState) {
      // Parameters
      String appGUID = null;
      boolean limitAdTracking = false;
      boolean enableDebug = false;
      boolean enableAttributionData = false;

      // prevent init from being called twice
      if (kochavaDelegate != null) {
        return 0;
      }

      functionSignature = "kochava.init(listener, options)";

      // check number of args
      int nargs = luaState.getTop();
      if (nargs != 2) {
        logMsg(ERROR_MSG, "Expected 2 arguments, got " + nargs);
        return 0;
      }

      // Get the listener (required)
      if (CoronaLua.isListener(luaState, 1, PROVIDER_NAME)) {
        coronaListener = CoronaLua.newRef(luaState, 1);
      }
      else {
        logMsg(ERROR_MSG, "Listener expected, got: " + luaState.typeName(1));
        return 0;
      }

      // check for options table (required)
      if (luaState.type(2) == LuaType.TABLE) {
        // traverse and verify all options
        for (luaState.pushNil(); luaState.next(2); luaState.pop(1)) {
          String key = luaState.toString(-2);

          if (key.equals("appId")) { // (for backward compatibility only. NEW: appGUID)
            if (luaState.type(-1) == LuaType.STRING) {
              appGUID = luaState.toString(-1);
            }
            else {
              logMsg(ERROR_MSG, "options.appGUID (string) expected, got " + luaState.typeName(-1));
              return 0;
            }
          }
          else if (key.equals("appGUID")) {
            if (luaState.type(-1) == LuaType.STRING) {
              appGUID = luaState.toString(-1);
            }
            else {
              logMsg(ERROR_MSG, "options.appGUID (string) expected, got " + luaState.typeName(-1));
              return 0;
            }
          }
          else if (key.equals("limitAdTracking")) {
            if (luaState.type(-1) == LuaType.BOOLEAN) {
              limitAdTracking = luaState.toBoolean(-1);
            }
            else {
              logMsg(ERROR_MSG, "options.limitAdTracking (boolean) expected, got " + luaState.typeName(-1));
              return 0;
            }
          }
          else if (key.equals("enableDebugLogging")) {
            if (luaState.type(-1) == LuaType.BOOLEAN) {
              enableDebug = luaState.toBoolean(-1);
            }
            else {
              logMsg(ERROR_MSG, "options.enableDebugLogging (boolean) expected, got " + luaState.typeName(-1));
              return 0;
            }
          }
          else if (key.equals("enableAttributionData")) {
            // This option is not used anymore as the new SDK automatically sets this by
            // checking for the presence of an event listener.
            // It remains here since iOS still requires it and we need to maintain cross-platform compatibility
            if (luaState.type(-1) == LuaType.BOOLEAN) {
              enableAttributionData = luaState.toBoolean(-1);
            }
            else {
              logMsg(ERROR_MSG, "options.enableAttributionData (boolean) expected, got " + luaState.typeName(-1));
              return 0;
            }
          }
          else if (key.equals("hasUserConsent")) {
            if (luaState.type(-1) == LuaType.BOOLEAN) {
              hasUserConsent = luaState.toBoolean(-1);
            }
            else {
              logMsg(ERROR_MSG, "options.hasUserConsent (boolean) expected, got " + luaState.typeName(-1));
              return 0;
            }
          }
          else if (key.equals("intelligentConsentManagement")) {
            if (luaState.type(-1) == LuaType.BOOLEAN) {
              intelligentConsentManagement = luaState.toBoolean(-1);
            }
            else {
              logMsg(ERROR_MSG, "options.intelligentConsentManagement (boolean) expected, got " + luaState.typeName(-1));
              return 0;
            }
          }
          else {
            logMsg(ERROR_MSG, "Invalid option '" + key + "'");
            return 0;
          }
        }
      }
      else {
        logMsg(ERROR_MSG, "options table expected, got " + luaState.typeName(2));
        return 0;
      }

      // check required params
      if (appGUID == null) {
        logMsg(ERROR_MSG, "options.appGUID is required");
        return 0;
      }

      final CoronaActivity coronaActivity = CoronaEnvironment.getCoronaActivity();

      // make values final
      final String fAppGUID = appGUID;
      final boolean fLimitAdTracking = limitAdTracking;
      final boolean fEnableDebug = enableDebug;

      if (coronaActivity != null) {
        coronaActivity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            // set plugin traffic detection
            Tracker.executeAdvancedInstruction("CoronaPlugin " + PLUGIN_VERSION, "");

            kochavaDelegate = new KochavaDelegate();
            kochavaConsentDelegate = new KochavaConsentDelegate();

            Configuration config = new Configuration(coronaActivity.getApplicationContext())
              .setAppGuid(fAppGUID)
              .setAttributionUpdateListener(kochavaDelegate)
              .setAppLimitAdTracking(fLimitAdTracking)
            .setIntelligentConsentManagement(intelligentConsentManagement || hasUserConsent)
            .setConsentStatusChangeListener(kochavaConsentDelegate);

            if (fEnableDebug) {
              config = config.setLogLevel(Tracker.LOG_LEVEL_DEBUG);
            }

            // configure SDK
            Tracker.configure(config);

            // Log plugin version to device log
            Log.i(CORONA_TAG, PLUGIN_NAME + ": " + PLUGIN_VERSION + " (SDK: " + PLUGIN_SDK_VERSION + ")");

            // send Corona Lua event
            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_INIT);
            coronaEvent.put(EVENT_DATA_KEY, Tracker.getDeviceId());
            dispatchLuaEvent(coronaEvent);


            // Check if we know our consent status from a previous launch.
            if(!Tracker.isConsentGrantedOrNotRequired()) {
              if(Tracker.isConsentShouldPrompt()) {
                if (!intelligentConsentManagement) {
                  Tracker.setConsentGranted(hasUserConsent);
                  Tracker.clearConsentShouldPrompt();
                } else {
                  // send Corona Lua event
                  Map<String, Object> coronaConsentEvent = new HashMap<>();
                  coronaConsentEvent.put(EVENT_PHASE_KEY, PHASE_RECEIVED);
                  coronaConsentEvent.put(EVENT_TYPE_KEY, TYPE_CONSENT);
                  coronaConsentEvent.put(EVENT_DATA_KEY, "Should prompt for user consent");
                  dispatchLuaEvent(coronaConsentEvent);
                }
              }
            }
          }
        });
      }

      return 0;
    }
  }

  // [Lua] kochava.getVersion()
  private class GetVersion implements NamedJavaFunction
  {
    // Gets the name of the Lua function as it would appear in the Lua script
    @Override
    public String getName()
    {
      return "getVersion";
    }

    // This method is executed when the Lua function is called
    @Override
    public int invoke(LuaState luaState)
    {
      functionSignature = "kochava.getVersion()";

      if (! isSDKInitialized()) {
        return 0;
      }

      // declare final vars for inner loop
      final CoronaActivity coronaActivity = CoronaEnvironment.getCoronaActivity();

      if (coronaActivity != null) {
        coronaActivity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Log.i(CORONA_TAG, PLUGIN_NAME + ": " + PLUGIN_VERSION + " (SDK: " + PLUGIN_SDK_VERSION + ")");
            // Dispatch the Lua event
            HashMap<String, Object> event = new HashMap<>();
            event.put("pluginVersion", PLUGIN_VERSION);
            event.put("sdkVersion", PLUGIN_SDK_VERSION);
            dispatchLuaEvent(event);
          }
        });
      }

      return 0;
    }
  }

  // [Lua] limitAdTracking(setting)
  private class LimitAdTracking implements NamedJavaFunction {
    /**
     * Gets the name of the Lua function as it would appear in the Lua script.
     * @return Returns the name of the custom Lua function.
     */
    @Override
    public String getName() {
      return "limitAdTracking";
    }

    /**
     * This method is called when the Lua function is called.
     * <p>
     * Warning! This method is not called on the main UI thread.
     * @param luaState Reference to the Lua state.
     *                 Needed to retrieve the Lua function's parameters and to return values back to Lua.
     * @return Returns the number of values to be returned by the Lua function.
     */
    @Override
    public int invoke( LuaState luaState ) {
      final boolean limitTracking;

      functionSignature = "kochava.limitAdTracking(setting)";

      if (!isSDKInitialized()) {
        return 0;
      }

      // check number or args
      int nargs = luaState.getTop();
      if (nargs != 1) {
        logMsg(ERROR_MSG, "Expected 1 argument, got " + nargs);
        return 0;
      }

      // get setting (required)
      if (luaState.type(1) == LuaType.BOOLEAN) {
        limitTracking = luaState.toBoolean(1);
      }
      else {
        logMsg(ERROR_MSG, "'setting' (boolean) expected, got " + luaState.typeName(1));
        return 0;
      }

      final CoronaActivity coronaActivity = CoronaEnvironment.getCoronaActivity();

      if (coronaActivity != null) {
        coronaActivity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            // send adTracking setting to Kochava
            Tracker.setAppLimitAdTracking(limitTracking);
          }
        });
      }

      return 0;
    }
  }

  // [Lua] logDeeplinkEvent(URL, sourceApp)
  private class LogDeeplinkEvent implements NamedJavaFunction {
    /**
     * Gets the name of the Lua function as it would appear in the Lua script.
     * @return Returns the name of the custom Lua function.
     */
    @Override
    public String getName() {
      return "logDeeplinkEvent";
    }

    /**
     * This method is called when the Lua function is called.
     * <p>
     * Warning! This method is not called on the main UI thread.
     * @param luaState Reference to the Lua state.
     *                 Needed to retrieve the Lua function's parameters and to return values back to Lua.
     * @return Returns the number of values to be returned by the Lua function.
     */
    @Override
    public int invoke(LuaState luaState) {
      final String URL;
      final String sourceAppParams;

      functionSignature = "kochava.logDeeplinkEvent(URL, sourceApp)";

      if (!isSDKInitialized()) {
        return 0;
      }

      // check number or args
      int nargs = luaState.getTop();
      if (nargs != 2) {
        logMsg(ERROR_MSG, "Expected 2 arguments, got " + nargs);
        return 0;
      }

      // get the app url
      if (luaState.type(1) == LuaType.STRING) {
        URL = luaState.toString(1);
      }
      else {
        logMsg(ERROR_MSG, "URL (string) expected, got " + luaState.typeName(1));
        return 0;
      }

      // get the source app params
      if (luaState.type(2) == LuaType.STRING) {
        sourceAppParams = luaState.toString(2);
      }
      else {
        logMsg(ERROR_MSG, "sourceApp (string) expected, got " + luaState.typeName(2));
        return 0;
      }

      final CoronaActivity coronaActivity = CoronaEnvironment.getCoronaActivity();

      if (coronaActivity != null) {
        coronaActivity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            // send deep link event to Kochava
            Tracker.sendEventDeepLink(URL); // sourceAppParams not supported on Android

            // send Corona Lua event
            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_RECORDED);
            coronaEvent.put(EVENT_TYPE_KEY, TYPE_DEEPLINK);
            dispatchLuaEvent(coronaEvent);
          }
        });
      }

      return 0;
    }
  }

  // DEPRECATED: use logEvent()
  // remains here for backwards compatibility
  // --------------------------------------------------------------------------
  // [Lua] logStandardEvent(eventType, options)
  // --------------------------------------------------------------------------
  private class LogStandardEvent implements NamedJavaFunction {
    /**
     * Gets the name of the Lua function as it would appear in the Lua script.
     * @return Returns the name of the custom Lua function.
     */
    @Override
    public String getName() {
      return "logStandardEvent";
    }

    /**
     * This method is called when the Lua function is called.
     * <p>
     * Warning! This method is not called on the main UI thread.
     * @param luaState Reference to the Lua state.
     *                 Needed to retrieve the Lua function's parameters and to return values back to Lua.
     * @return Returns the number of values to be returned by the Lua function.
     */
    @Override
    public int invoke( LuaState luaState ) {
      Log.i(CORONA_TAG, "logStandardEvent() is deprecated, use logEvent() instead");
      LogEvent event = new LogEvent();
      return event.invoke(luaState);
    }
  }

  // DEPRECATED: use logEvent()
  // remains here for backwards compatibility
  // --------------------------------------------------------------------------
  // [Lua] logCustomEvent(event [, details, receipt, receiptDataSignature])
  // --------------------------------------------------------------------------
  private class LogCustomEvent implements NamedJavaFunction {
    /**
     * Gets the name of the Lua function as it would appear in the Lua script.
     * @return Returns the name of the custom Lua function.
     */
    @Override
    public String getName() {
      return "logCustomEvent";
    }

    /**
     * This method is called when the Lua function is called.
     * <p>
     * Warning! This method is not called on the main UI thread.
     * @param luaState Reference to the Lua state.
     *                 Needed to retrieve the Lua function's parameters and to return values back to Lua.
     * @return Returns the number of values to be returned by the Lua function.
     */
    @Override
    public int invoke( LuaState luaState ) {
      Log.i(CORONA_TAG, "logCustomEvent() is deprecated, use logEvent() instead");

      final String eventName;
      final String eventDetails;
      final String receipt;
      final String receiptDataSignature;

      functionSignature = "kochava.logCustomEvent(event [, details, receipt, receiptDataSignature])";

      if (!isSDKInitialized()) {
        return 0;
      }

      // check number or args
      int nargs = luaState.getTop();
      if ((nargs < 1) || (nargs > 4)) {
        logMsg(ERROR_MSG, "Expected 1-4 arguments, got " + nargs);
        return 0;
      }

      // get event name
      if (luaState.type(1) == LuaType.STRING) {
        eventName = luaState.toString(1);
      }
      else {
        logMsg(ERROR_MSG, "eventName (string) expected, got " + luaState.typeName(1));
        return 0;
      }

      // get event details
      if (! luaState.isNoneOrNil(2)) {
        if (luaState.type(2) == LuaType.STRING) {
          eventDetails = luaState.toString(2);
        }
        else {
          logMsg(ERROR_MSG, "eventDetails (string) expected, got " + luaState.typeName(2));
          return 0;
        }
      }
      else {
        eventDetails = "";
      }

      // get app store receipt
      if (! luaState.isNoneOrNil(3)) {
        if (luaState.type(3) == LuaType.STRING) {
          receipt = luaState.toString(3);
        }
        else {
          logMsg(ERROR_MSG, "receipt (string) expected, got " + luaState.typeName(3));
          return 0;
        }
      }
      else {
        receipt = null;
      }

      // get receipt data signature (only Google Play Store is currently supported)
      if (! luaState.isNoneOrNil(4)) {
        if (luaState.type(4) == LuaType.STRING) {
          receiptDataSignature = luaState.toString(4);
        }
        else {
          logMsg(ERROR_MSG, "receiptDataSignature (string) expected, got " + luaState.typeName(4));
          return 0;
        }
      }
      else {
        receiptDataSignature = null;
      }

      // validate event name. Make sure event names do not begin with '_' (reserved for Kochava system)
      if (eventName.startsWith("_")) {
        logMsg(ERROR_MSG, "eventName must not start with '_' (reserved for Kochava system)");
        return 0;
      }

      // validate receipt
      if (receipt != null) {
        if (receiptDataSignature == null) {
          logMsg(ERROR_MSG, "receiptDataSignature must also be specified when providing receipt data");
          return 0;
        }
      }

      final CoronaActivity coronaActivity = CoronaEnvironment.getCoronaActivity();

      if (coronaActivity != null) {
        coronaActivity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            // send tracking event to Kochava
            if (receipt != null) {
              Event receiptEvent = new Event(eventName)
                .addCustom("purchaseDetails", eventDetails)
                .setGooglePlayReceipt(receipt, receiptDataSignature);

              Tracker.sendEvent(receiptEvent);
            }
            else {
              Tracker.sendEvent(eventName, eventDetails);
            }

            // send Corona Lua event
            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_RECORDED);
            coronaEvent.put(EVENT_TYPE_KEY, TYPE_CUSTOM);
            dispatchLuaEvent(coronaEvent);
          }
        });
      }

      return 0;
    }
  }

  private class LogEvent implements NamedJavaFunction {
    /**
     * Gets the name of the Lua function as it would appear in the Lua script.
     * @return Returns the name of the custom Lua function.
     */
    @Override
    public String getName() {
      return "logEvent";
    }

    /**
     * This method is called when the Lua function is called.
     * <p>
     * Warning! This method is not called on the main UI thread.
     * @param luaState Reference to the Lua state.
     *                 Needed to retrieve the Lua function's parameters and to return values back to Lua.
     * @return Returns the number of values to be returned by the Lua function.
     */
    @Override
    public int invoke( LuaState luaState ) {
      functionSignature = "kochava.logEvent(eventType, options)";

      if (!isSDKInitialized()) {
        return 0;
      }

      // check number or args
      int nargs = luaState.getTop();
      if ((nargs < 1) || (nargs > 2)){
        logMsg(ERROR_MSG, "Expected 1 or 2 arguments, got " + nargs);
        return 0;
      }

      final String eventParamType;
      final Map<String, Object> standardParams = new HashMap<>();
      String receiptData = null;
      String dataSignature = null;
      boolean isCustomEvent = false;

      // get event param type
      if (luaState.type(1) == LuaType.STRING) {
        eventParamType = luaState.toString(1);
      }
      else {
        logMsg(ERROR_MSG, "eventType (string) expected, got " + luaState.typeName(1));
        return 0;
      }

      // get event param properties
      if (! luaState.isNoneOrNil(2)) {
        if (luaState.type(2) == LuaType.STRING) {
          // backwards compatibility
          LogCustomEvent event = new LogCustomEvent();
          return event.invoke(luaState);
        }
        else if (luaState.type(2) == LuaType.TABLE) {
          // traverse and validate all the properties
          for (luaState.pushNil(); luaState.next(2); luaState.pop(1)) {
            String key = luaState.toString(-2);

            if (validStandardParamProperties.get(key) != null) {
              // check variable type
              if (validStandardParamProperties.get(key).equals("String")) {
                if (luaState.type(-1) == LuaType.STRING) {
                  standardParams.put(key, luaState.toString(-1));
                }
                else {
                  logMsg(ERROR_MSG, "options." + key + " (string) expected, got "+ luaState.typeName(-1));
                  return 0;
                }
              }
              else if (validStandardParamProperties.get(key).equals("Boolean")) {
                if (luaState.type(-1) == LuaType.BOOLEAN) {
                  standardParams.put(key, luaState.toBoolean(-1));
                }
                else {
                  logMsg(ERROR_MSG, "options." + key + " (boolean) expected, got "+ luaState.typeName(-1));
                  return 0;
                }
              }
              else if (validStandardParamProperties.get(key).equals("Interval")) {
                if (luaState.type(-1) == LuaType.NUMBER) {
                  standardParams.put(key, luaState.toNumber(-1));
                }
                else {
                  logMsg(ERROR_MSG, "options." + key + " (number) expected, got "+ luaState.typeName(-1));
                  return 0;
                }
              }
              else if (validStandardParamProperties.get(key).equals("Double")) {
                if (luaState.type(-1) == LuaType.NUMBER) {
                  standardParams.put(key, luaState.toNumber(-1));
                }
                else {
                  logMsg(ERROR_MSG, "options." + key + " (number) expected, got "+ luaState.typeName(-1));
                  return 0;
                }
              }
              else if (validStandardParamProperties.get(key).equals("Date")) {
                if (luaState.type(-1) == LuaType.STRING) {
                  standardParams.put(key, luaState.toString(-1));
                }
                else {
                  logMsg(ERROR_MSG, "options." + key + " (string) expected, got "+ luaState.typeName(-1));
                  return 0;
                }
              }
            }
            else { // custom properties
              isCustomEvent = true;

              if (luaState.type(-1) == LuaType.STRING) {
                standardParams.put(key, luaState.toString(-1));
              }
              else if (luaState.type(-1) == LuaType.BOOLEAN) {
                standardParams.put(key, luaState.toBoolean(-1));
              }
              else if (luaState.type(-1) == LuaType.NUMBER) {
                standardParams.put(key, luaState.toNumber(-1));
              }
              else {
                logMsg(ERROR_MSG, "options." + key + " unhandled type ("+ luaState.typeName(-1) + ")");
                return 0;
              }
            }
          }
        }
        else {
          logMsg(ERROR_MSG, "options table expected, got " + luaState.typeName(2));
          return 0;
        }
      }

      // configure Kochava params
      Event eventParameters = null;

      if (eventParamType.equals(STANDARD_TYPE_ACHIEVEMENT)) {
        eventParameters = new Event(Tracker.EVENT_TYPE_ACHIEVEMENT);
      }
      else if (eventParamType.equals(STANDARD_TYPE_ADVIEW)) {
        eventParameters = new Event(Tracker.EVENT_TYPE_AD_VIEW);
      }
      else if (eventParamType.equals(STANDARD_TYPE_ADDTOCART)) {
        eventParameters = new Event(Tracker.EVENT_TYPE_ADD_TO_CART);
      }
      else if (eventParamType.equals(STANDARD_TYPE_ADDTOWISHLIST)) {
        eventParameters = new Event(Tracker.EVENT_TYPE_ADD_TO_WISH_LIST);
      }
      else if (eventParamType.equals(STANDARD_TYPE_CHECKOUTSTART)) {
        eventParameters = new Event(Tracker.EVENT_TYPE_CHECKOUT_START);
      }
      else if (eventParamType.equals(STANDARD_TYPE_LEVELCOMPLETE)) {
        eventParameters = new Event(Tracker.EVENT_TYPE_LEVEL_COMPLETE);
      }
      else if (eventParamType.equals(STANDARD_TYPE_PURCHASE)) {
        eventParameters = new Event(Tracker.EVENT_TYPE_PURCHASE);
      }
      else if (eventParamType.equals(STANDARD_TYPE_PUSH_OPENED)) {
        eventParameters = new Event(Tracker.EVENT_TYPE_PUSH_OPENED);
      }
      else if (eventParamType.equals(STANDARD_TYPE_PUSH_RECEIVED)) {
        eventParameters = new Event(Tracker.EVENT_TYPE_PUSH_RECEIVED);
      }
      else if (eventParamType.equals(STANDARD_TYPE_RATING)) {
        eventParameters = new Event(Tracker.EVENT_TYPE_RATING);
      }
      else if (eventParamType.equals(STANDARD_TYPE_REGISTRATIONCOMPLETE)) {
        eventParameters = new Event(Tracker.EVENT_TYPE_REGISTRATION_COMPLETE);
      }
      else if (eventParamType.equals(STANDARD_TYPE_SEARCH)) {
        eventParameters = new Event(Tracker.EVENT_TYPE_SEARCH);
      }
      else if (eventParamType.equals(STANDARD_TYPE_TUTORIALCOMPLETE)) {
        eventParameters = new Event(Tracker.EVENT_TYPE_TUTORIAL_COMPLETE);
      }
      else if (eventParamType.equals(STANDARD_TYPE_VIEW)) {
        eventParameters = new Event(Tracker.EVENT_TYPE_VIEW);
      }
      else { // custom type
        isCustomEvent = true;
        eventParameters = new Event(eventParamType);
      }

      // standard events must have properties
      if ((! isCustomEvent) && (standardParams.size() == 0)) {
        logMsg(ERROR_MSG, "Standard events must have properties set");
        return 0;
      }

      for (String key: standardParams.keySet()) {
        if (validStandardParamProperties.get(key) != null) {
          if (validStandardParamProperties.get(key).equals("Date")) {
            // validate date
            String dateCheck = (String)standardParams.get(key);

            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            format.setLenient(false);
            try {
              format.parse(dateCheck);
            }
            catch (ParseException e) {
              logMsg(ERROR_MSG, "options." + key + " Invalid format '" + dateCheck + "'. Valid format: yyyy-mm-dd");
              return 0;
            }

            if (key.equals(STANDARD_PROPERTY_DATE)) {
              eventParameters = eventParameters.setDate(dateCheck);
            }
            else if (key.equals(STANDARD_PROPERTY_ENDDATE)) {
              eventParameters = eventParameters.setEndDate(dateCheck);
            }
            else if (key.equals(STANDARD_PROPERTY_STARTDATE)) {
              eventParameters = eventParameters.setStartDate(dateCheck);
            }
          }
          else {
            if (key.equals(STANDARD_PROPERTY_ACTION)) {
              eventParameters = eventParameters.setAction((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_ADCAMPAIGNID)) {
              eventParameters = eventParameters.setAdCampaignId((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_ADCAMPAIGNNAME)) {
              eventParameters = eventParameters.setAdCampaignName((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_ADDEVICETYPE)) {
              eventParameters = eventParameters.setAdDeviceType((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_ADGROUPID)) {
              eventParameters = eventParameters.setAdGroupId((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_ADGROUPNAME)) {
              eventParameters = eventParameters.setAdGroupName((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_ADMEDIATIONNAME)) {
              eventParameters = eventParameters.setAdMediationName((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_ADNETWORKNAME)) {
              eventParameters = eventParameters.setAdNetworkName((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_ADPLACEMENT)) {
              eventParameters = eventParameters.setAdPlacement((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_ADSIZE)) {
              eventParameters = eventParameters.setAdSize((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_ADTYPE)) {
              eventParameters = eventParameters.setAdType((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_BACKGROUND)) {
              eventParameters = eventParameters.setBackground((boolean)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_CHECKOUTASGUEST)) {
              eventParameters = eventParameters.setCheckoutAsGuest((boolean)standardParams.get(key) ? "true" : "false");
            }
            else if (key.equals(STANDARD_PROPERTY_COMPLETED)) {
              eventParameters = eventParameters.setCompleted((boolean)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_CONTENTID)) {
              eventParameters = eventParameters.setContentId((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_CONTENTTYPE)) {
              eventParameters = eventParameters.setContentType((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_CURRENCY)) {
              eventParameters = eventParameters.setCurrency((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_DESCRIPTION)) {
              eventParameters = eventParameters.setDescription((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_DESTINATION)) {
              eventParameters = eventParameters.setDestination((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_DURATIONTIMEINTERVAL)) {
              eventParameters = eventParameters.setDuration((double)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_ITEMADDEDFROM)) {
              eventParameters = eventParameters.setItemAddedFrom((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_LEVEL)) {
              eventParameters = eventParameters.setLevel((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_MAXRATING)) {
              eventParameters = eventParameters.setMaxRatingValue((double)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_NAME)) {
              eventParameters = eventParameters.setName((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_ORDERID)) {
              eventParameters = eventParameters.setOrderId((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_ORIGIN)) {
              eventParameters = eventParameters.setOrigin((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_PAYLOAD)) {
              eventParameters = eventParameters.setPayload((Bundle)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_PRICE)) {
              eventParameters = eventParameters.setPrice((double)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_QUANTITY)) {
              eventParameters = eventParameters.setQuantity((double)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_RATINGVALUE)) {
              eventParameters = eventParameters.setRatingValue((double)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_RECEIPTID)) {
              eventParameters = eventParameters.setReceiptId((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_RECEIPTDATA)) {
              receiptData = (String)standardParams.get(key);
            }
            else if (key.equals(STANDARD_PROPERTY_RECEIPTDATASIGNATURE)) {
              dataSignature = (String)standardParams.get(key);
            }
            else if (key.equals(STANDARD_PROPERTY_REFERRALFROM)) {
              eventParameters = eventParameters.setReferralFrom((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_REGISTRATIONMETHOD)) {
              eventParameters = eventParameters.setRegistrationMethod((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_RESULTS)) {
              eventParameters = eventParameters.setResults((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_SCORE)) {
              eventParameters = eventParameters.setScore((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_SEARCHTERM)) {
              eventParameters = eventParameters.setSearchTerm((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_SPATIAL_X)) {
              eventParameters = eventParameters.setSpatialX((double)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_SPATIAL_Y)) {
              eventParameters = eventParameters.setSpatialY((double)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_SPATIAL_Z)) {
              eventParameters = eventParameters.setSpatialZ((double)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_SUCCESS)) {
              eventParameters = eventParameters.setSuccess((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_USERID)) {
              eventParameters = eventParameters.setUserId((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_USERNAME)) {
              eventParameters = eventParameters.setUserName((String)standardParams.get(key));
            }
            else if (key.equals(STANDARD_PROPERTY_VALIDATED)) {
              eventParameters = eventParameters.setValidated((String)standardParams.get(key));
            }
          }
        }
        else { // custom property
          Object value = standardParams.get(key);

          if (value instanceof Double) {
            eventParameters = eventParameters.addCustom(key, (double)standardParams.get(key));
          }
          else if (value instanceof String) {
            eventParameters = eventParameters.addCustom(key, (String)standardParams.get(key));
          }
          else if (value instanceof Boolean) {
            eventParameters = eventParameters.addCustom(key, (boolean)standardParams.get(key));
          }
          else {
            Log.i(ERROR_MSG, "Invalid data type for custom parameter with key '" + key + "'");
            return 0;
          }
        }
      }

      // validation
      if (receiptData != null) {
        if (dataSignature == null) {
          logMsg(ERROR_MSG, "receiptDataSignature must also be specified when providing receipt data");
          return 0;
        }

        eventParameters = eventParameters.setGooglePlayReceipt(receiptData, dataSignature);
      }

      // declare final values for inner loop
      final CoronaActivity coronaActivity = CoronaEnvironment.getCoronaActivity();
      final Event fEventParameters = eventParameters;
      final boolean fIsCustomEvent = isCustomEvent;

      if (coronaActivity != null) {
        coronaActivity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            // send parameters to Kochava
            Tracker.sendEvent(fEventParameters);

            // send Corona Lua event
            Map<String, Object> coronaEvent = new HashMap<>();
            coronaEvent.put(EVENT_PHASE_KEY, PHASE_RECORDED);
            coronaEvent.put(EVENT_TYPE_KEY, fIsCustomEvent ? TYPE_CUSTOM : TYPE_STANDARD);
            dispatchLuaEvent(coronaEvent);
          }
        });
      }

      return 0;
    }
  }

  // [Lua] setIdentityLink(table)
  private class SetIdentityLink implements NamedJavaFunction {
    /**
     * Gets the name of the Lua function as it would appear in the Lua script.
     * @return Returns the name of the custom Lua function.
     */
    @Override
    public String getName() {
      return "setIdentityLink";
    }

    /**
     * This method is called when the Lua function is called.
     * <p>
     * Warning! This method is not called on the main UI thread.
     * @param luaState Reference to the Lua state.
     *                 Needed to retrieve the Lua function's parameters and to return values back to Lua.
     * @return Returns the number of values to be returned by the Lua function.
     */
    @Override
    public int invoke( LuaState luaState ) {
      functionSignature = "kochava.setIdentityLink(table)";

      if (!isSDKInitialized()) {
        return 0;
      }

      // check number or args
      int nargs = luaState.getTop();
      if (nargs != 1) {
        logMsg(ERROR_MSG, "Expected 1 argument, got " + nargs);
        return 0;
      }

      IdentityLink link = null;

      // check for key/value table (required)
      if (luaState.type(1) == LuaType.TABLE) {
        link = makeIdentityLink(CoronaLua.toHashtable(luaState, 1));
      }
      else {
        logMsg(ERROR_MSG, "key/value table expected, got " + luaState.typeName(1));
        return 0;
      }

      // validation
      if (link == null) {
        logMsg(ERROR_MSG, "Invalid data privided to setIdentityLink()");
        return 0;
      }

      final CoronaActivity coronaActivity = CoronaEnvironment.getCoronaActivity();
      final IdentityLink fLink = link;

      if (coronaActivity != null) {
        coronaActivity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            // send link dictionary to Kochava
            Tracker.setIdentityLink(fLink);
          }
        });
      }

      return 0;
    }
  }

  // [Lua] setHasUserConsent(bool)
  private class SetHasUserConsent implements NamedJavaFunction {
    /**
     * Gets the name of the Lua function as it would appear in the Lua script.
     * @return Returns the name of the custom Lua function.
     */
    @Override
    public String getName() {
      return "setHasUserConsent";
    }

    /**
     * This method is called when the Lua function is called.
     * <p>
     * Warning! This method is not called on the main UI thread.
     * @param luaState Reference to the Lua state.
     *                 Needed to retrieve the Lua function's parameters and to return values back to Lua.
     * @return Returns the number of values to be returned by the Lua function.
     */
    @Override
    public int invoke( LuaState luaState ) {
      functionSignature = "kochava.setHasUserConsent(boolean)";

      if (!isSDKInitialized()) {
        return 0;
      }

      // check number or args
      int nargs = luaState.getTop();
      if (nargs != 1) {
        logMsg(ERROR_MSG, "Expected 1 argument, got " + nargs);
        return 0;
      }

      Boolean localHasUserConsent;

      // check for consent boolean (required)
      if (luaState.type(1) == LuaType.BOOLEAN) {
        localHasUserConsent = luaState.toBoolean(-1);
        final CoronaActivity coronaActivity = CoronaEnvironment.getCoronaActivity();
        final Boolean fLocalHasUserConsent = localHasUserConsent;
        if (coronaActivity != null) {
          coronaActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              // send consent to Kochava
              Tracker.setConsentGranted(fLocalHasUserConsent);
              Tracker.clearConsentShouldPrompt();
            }
          });
        }
        return 0;
      }
      else {
        logMsg(ERROR_MSG, "Boolean expected, got " + luaState.typeName(1));
        return 0;
      }
    }
  }

  // -------------------------------------------------------
  // delegate implementation
  // -------------------------------------------------------

  private class KochavaDelegate implements AttributionUpdateListener {
    @Override
    public void onAttributionUpdated(String msg) {
      // send Corona Lua event
      Map<String, Object> coronaEvent = new HashMap<>();
      coronaEvent.put(EVENT_PHASE_KEY, PHASE_RECEIVED);
      coronaEvent.put(EVENT_TYPE_KEY, TYPE_ATTRIBUTION);
      coronaEvent.put(EVENT_DATA_KEY, msg);
      dispatchLuaEvent(coronaEvent);
    }
  }

  private class KochavaConsentDelegate implements ConsentStatusChangeListener {
    @Override
    public void onConsentStatusChange() {
      // Check if we know our consent status from a previous launch.
      if(!Tracker.isConsentGrantedOrNotRequired()) {
        if(Tracker.isConsentShouldPrompt()) {
          if (!intelligentConsentManagement) {
            Tracker.setConsentGranted(hasUserConsent);
            Tracker.clearConsentShouldPrompt();
          } else {
            // send Corona Lua event
            Map<String, Object> coronaConsentEvent = new HashMap<>();
            coronaConsentEvent.put(EVENT_PHASE_KEY, PHASE_RECEIVED);
            coronaConsentEvent.put(EVENT_TYPE_KEY, TYPE_CONSENT);
            coronaConsentEvent.put(EVENT_DATA_KEY, "Should prompt for user consent");
            dispatchLuaEvent(coronaConsentEvent);
          }
        }
      }
    }
  }
}
