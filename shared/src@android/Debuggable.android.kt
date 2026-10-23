package dev.dambatenne.lechonk

import android.content.Context
import android.content.pm.ApplicationInfo

private var debuggableApp: Boolean = false

/**
 * Records whether the app runs as a debuggable build, checked at run time
 * against [ApplicationInfo.FLAG_DEBUGGABLE]. Called once at startup, before
 * any UI is composed.
 */
fun setAppDebuggable(context: Context) {
    debuggableApp = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
}

actual fun isDebuggable(): Boolean = debuggableApp
