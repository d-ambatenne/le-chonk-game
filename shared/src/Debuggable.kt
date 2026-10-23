package dev.dambatenne.lechonk

/**
 * Reports whether the running app is a debuggable build.
 *
 * Internal game state (where Le Chonk is along the track, whether he is in
 * the air) is only exposed through accessibility descriptions in debuggable
 * builds.
 */
expect fun isDebuggable(): Boolean
