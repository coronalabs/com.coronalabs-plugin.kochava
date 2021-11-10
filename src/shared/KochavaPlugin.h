//
//  KochavaPlugin.h
//  Kochava Plugin
//
//  Copyright (c) 2016 Corona Labs Inc. All rights reserved.
//

#ifndef KochavaPlugin_H
#define KochavaPlugin_H

#import "CoronaLua.h"
#import "CoronaMacros.h"

// This corresponds to the name of the library, e.g. [Lua] require "plugin.library"
// where the '.' is replaced with '_'
CORONA_EXPORT int luaopen_plugin_kochava( lua_State *L );

#endif // KochavaPlugin_H
