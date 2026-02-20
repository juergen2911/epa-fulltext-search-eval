package de.epa.fulltext.model

enum class SupportedMimeType(val mimeType: String) {
    PDF("application/pdf"),
    TEXT("text/plain"),
    XML("application/xml"),
    JSON("application/json"),
    FHIR_XML("application/fhir+xml"),
    FHIR_JSON("application/fhir+json"),
    HL7_V3("application/hl7-v3");

    companion object {
        fun isSupported(mimeType: String): Boolean {
            return values().any { it.mimeType.equals(mimeType, ignoreCase = true) }
        }

        fun fromString(mimeType: String): SupportedMimeType? {
            return values().find { it.mimeType.equals(mimeType, ignoreCase = true) }
        }
    }
}
