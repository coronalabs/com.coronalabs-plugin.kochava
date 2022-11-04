local metadata =
{
    plugin =
    {
        format = 'jar',
        manifest =
        {
            permissions = {},
            usesPermissions =
            {
                "android.permission.INTERNET",
                "android.permission.ACCESS_NETWORK_STATE",
                "android.permission.ACCESS_WIFI_STATE",
                "com.google.android.gms.permission.AD_ID"
            },
            usesFeatures =
            {
            },

        }
    },

    coronaManifest = {
        dependencies = {

        }
    }
}

return metadata
