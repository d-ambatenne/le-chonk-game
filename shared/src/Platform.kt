package dev.dambatenne.lechonk

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
