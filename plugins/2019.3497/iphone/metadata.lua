local metadata =
{
	plugin =
	{
		format = "staticLibrary",

		-- This is the name without the 'lib' prefix.
		staticLibs = { "plugin_kochava", "KochavaTrackeriOS", "KochavaCoreiOS" },

		frameworks = { "WebKit" },
		frameworksOptional = {},
	}
}

return metadata
