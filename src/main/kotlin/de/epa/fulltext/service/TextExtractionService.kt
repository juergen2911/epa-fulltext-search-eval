package de.epa.fulltext.service

import de.epa.fulltext.exception.DocumentProcessingException
import de.epa.fulltext.model.SupportedMimeType
import jakarta.enterprise.context.ApplicationScoped
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.tika.Tika
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.tesseract.TessBaseAPI
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@ApplicationScoped
class TextExtractionService {

    private val logger = Logger.getLogger(TextExtractionService::class.java)
    private val tika = Tika()

    @ConfigProperty(name = "tesseract.language", defaultValue = "eng")
    lateinit var tesseractLanguage: String

    fun extractText(fileData: ByteArray, mimeType: String): String {
        logger.debug("Extracting text from document with mime type: $mimeType")
        
        return try {
            when (SupportedMimeType.fromString(mimeType)) {
                SupportedMimeType.PDF -> extractTextFromPdf(fileData)
                SupportedMimeType.TEXT -> String(fileData, Charsets.UTF_8)
                SupportedMimeType.XML, 
                SupportedMimeType.FHIR_XML, 
                SupportedMimeType.HL7_V3 -> extractTextWithTika(fileData)
                SupportedMimeType.JSON, 
                SupportedMimeType.FHIR_JSON -> String(fileData, Charsets.UTF_8)
                null -> throw DocumentProcessingException("Unsupported mime type: $mimeType")
            }
        } catch (e: Exception) {
            logger.error("Error extracting text from document", e)
            throw DocumentProcessingException("Failed to extract text from document", e)
        }
    }

    private fun extractTextFromPdf(fileData: ByteArray): String {
        val document = Loader.loadPDF(fileData)
        return try {
            val textStripper = PDFTextStripper()
            val extractedText = textStripper.getText(document)
            
            // Check if PDF has meaningful text content
            val hasTextContent = extractedText.trim().length > 50
            
            if (!hasTextContent) {
                logger.info("PDF appears to be image-based, applying OCR")
                extractTextWithOcr(document)
            } else {
                extractedText
            }
        } finally {
            document.close()
        }
    }

    private fun extractTextWithOcr(document: PDDocument): String {
        logger.debug("Performing OCR on PDF document")
        val renderer = PDFRenderer(document)
        val textBuilder = StringBuilder()

        val api = TessBaseAPI()
        try {
            // Initialize Tesseract
            if (api.Init(null, tesseractLanguage) != 0) {
                logger.warn("Could not initialize tesseract, falling back to empty text")
                return ""
            }

            // Process each page
            for (pageIndex in 0 until document.numberOfPages) {
                logger.debug("Processing page ${pageIndex + 1} of ${document.numberOfPages}")
                val image = renderer.renderImageWithDPI(pageIndex, 300f)
                val ocrText = performOcrOnImage(api, image)
                textBuilder.append(ocrText)
                textBuilder.append("\n\n")
            }
        } finally {
            api.End()
        }

        return textBuilder.toString()
    }

    private fun performOcrOnImage(api: TessBaseAPI, image: BufferedImage): String {
        // Convert BufferedImage to byte array
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "png", baos)
        val imageBytes = baos.toByteArray()

        // Perform OCR
        api.SetImage(BytePointer(*imageBytes), image.width, image.height, 3, image.width * 3)
        val result = api.GetUTF8Text()
        val text = result.string
        result.deallocate()
        
        return text
    }

    private fun extractTextWithTika(fileData: ByteArray): String {
        return tika.parseToString(ByteArrayInputStream(fileData))
    }
}
