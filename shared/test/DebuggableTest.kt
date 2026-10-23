package dev.dambatenne.lechonk

import kotlin.test.Test
import kotlin.test.assertFalse

class DebuggableTest {
    @Test
    fun `internal state stays hidden until the app reports a debuggable build`() {
        assertFalse(isDebuggable())
    }
}
