package de.epa.fulltext

import de.epa.fulltext.model.SupportedMimeType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class SupportedMimeTypeTest {

    @Test
    fun testSupportedMimeTypes() {
        assertTrue(SupportedMimeType.isSupported("application/pdf"))
        assertTrue(SupportedMimeType.isSupported("text/plain"))
        assertTrue(SupportedMimeType.isSupported("application/xml"))
        assertTrue(SupportedMimeType.isSupported("application/json"))
        assertTrue(SupportedMimeType.isSupported("application/fhir+xml"))
        assertTrue(SupportedMimeType.isSupported("application/fhir+json"))
        assertTrue(SupportedMimeType.isSupported("application/hl7-v3"))
    }

    @Test
    fun testUnsupportedMimeType() {
        assertFalse(SupportedMimeType.isSupported("application/unsupported"))
        assertFalse(SupportedMimeType.isSupported("text/html"))
    }

    @Test
    fun testFromString() {
        assertEquals(SupportedMimeType.PDF, SupportedMimeType.fromString("application/pdf"))
        assertEquals(SupportedMimeType.TEXT, SupportedMimeType.fromString("text/plain"))
        assertNull(SupportedMimeType.fromString("application/unsupported"))
    }

    @Test
    fun testCaseInsensitive() {
        assertTrue(SupportedMimeType.isSupported("APPLICATION/PDF"))
        assertTrue(SupportedMimeType.isSupported("Text/Plain"))
        assertEquals(SupportedMimeType.JSON, SupportedMimeType.fromString("APPLICATION/JSON"))
    }
}
