-- Kochava plugin

local Library = require "CoronaLibrary"

-- Create library
local lib = Library:new{ name='plugin.kochava', publisherId='com.coronalabs', version = 6 }

-------------------------------------------------------------------------------
-- BEGIN
-------------------------------------------------------------------------------

-- This sample implements the following Lua:
--
--    local PLUGIN_NAME = require "plugin_PLUGIN_NAME"
--    PLUGIN_NAME:showPopup()
--

local function showWarning(functionName)
    print( functionName .. "WARNING: The Kochava plugin is only supported on Android, iOS and tvOS devices. Please build for device" );
end

function lib.getAttributionData()
    showWarning("kochava.getAttributionData()")
end

function lib.init()
    showWarning("kochava.init()")
end

function lib.limitAdTracking()
    showWarning("kochava.limitAdTracking()")
end

function lib.logDeeplinkEvent()
    showWarning("kochava.logDeeplinkEvent()")
end

function lib.logEvent()
    showWarning("kochava.logEvent()")
end

function lib.logCustomEvent() -- for backwards compatibility only (use logEvent)
    showWarning("kochava.logEvent()")
end

function lib.logStandardEvent() -- for backwards compatibility only (use logEvent)
    showWarning("kochava.logEvent()")
end

function lib.setIdentityLink()
    showWarning("kochava.setIdentityLink()")
end

-------------------------------------------------------------------------------
-- END
-------------------------------------------------------------------------------

-- Return an instance
return lib
