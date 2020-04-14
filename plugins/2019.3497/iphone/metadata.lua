local metadata =
{
	plugin =
	{
		format = "staticLibrary",

		-- This is the name without the 'lib' prefix.
		staticLibs = { "KochavaPlugin", "KochavaTrackeriOS" }, 

		frameworks = { "iAd", "AdSupport", "WebKit" },
		frameworksOptional = {},
	}
}

return metadata
