package omega_r.com.omegatypesexample

import com.omega_r.libs.omegatypes.Text
import org.junit.Test

import org.junit.Assert.*

class TextUnitTest {
    @Test
    fun text_equals() {
        assertEquals(Text.empty(), Text.empty())
        assertNotEquals(Text.empty(), Text.from(""))
        assertNotEquals(Text.empty(), null)
        assertNotEquals(Text.empty(), "")

        assertEquals(Text.from("string"), Text.from("string"))
        assertNotEquals(Text.from("string"), Text.empty())
        assertNotEquals(Text.from("string"), Text.from(12))
        assertNotEquals(Text.from("string"), Text.from(12, "string", 12))
        assertNotEquals(Text.from("string"), null)
        assertNotEquals(Text.from("string"), "")

        assertEquals(Text.from(12), Text.from(12))
        assertNotEquals(Text.from(12), Text.empty())
        assertNotEquals(Text.from(12), Text.from("12"))
        assertNotEquals(Text.from(12), Text.from(12, "12"))
        assertNotEquals(Text.from(12), null)
        assertNotEquals(Text.from(12), "")

        assertEquals(Text.from(12, 11, Text.empty()), Text.from(12, 11, Text.empty()))
        assertNotEquals(Text.from(12, 11, Text.empty()), Text.empty())
        assertNotEquals(Text.from(12, 11, Text.empty()), Text.from("12"))
        assertNotEquals(Text.from(12, 11, Text.empty()), Text.from(12))
        assertNotEquals(Text.from(12, 11, Text.empty()), null)
        assertNotEquals(Text.from(12, 11, Text.empty()), "")
    }

    @Test
    fun text_hash() {
        assertEquals(Text.empty().hashCode(), Text.empty().hashCode())
        assertEquals(Text.from("hello").hashCode(), Text.from("hello").hashCode())
        assertEquals(Text.from(12).hashCode(), Text.from(12).hashCode())
        assertEquals(Text.from(12, "hello", 11).hashCode(), Text.from(12, "hello", 11).hashCode())
    }
}
