local metadata =
{
	plugin =
	{
		format = "framework",

		-- This is the name without the 'lib' prefix.
		staticLibs = {},

		frameworks =
		{
			"Corona_plugin_kochava", "KochavaTracker", "KochavaCore", "AdSupport", "Security", "CFNetwork", "c++", "z", "sqlite3.0", "sqlite3",
			"AudioToolbox", "CoreText", "JavaScriptCore", "TVMLKit", "MobileCoreServices", "CoreMedia", "StoreKit", "SystemConfiguration", "WebKit"
		},

		frameworksOptional = {},
	},
}

return metadata
