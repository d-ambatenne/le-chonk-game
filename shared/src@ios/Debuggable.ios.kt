package dev.dambatenne.lechonk

// iOS has no equivalent run-time flag wired up here, so internal state stays
// hidden in accessibility descriptions on iOS builds.
actual fun isDebuggable(): Boolean = false
