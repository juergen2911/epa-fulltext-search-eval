package de.epa.fulltext

import de.epa.fulltext.model.SupportedMimeType
import io.quarkus.test.junit.QuarkusTest
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Test

@QuarkusTest
class HealthResourceTest {

    @Test
    fun testHealthEndpoint() {
        io.restassured.RestAssured.given()
            .`when`().get("/api/health")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("status", `is`("UP"))
            .body("service", `is`("fulltext-search-service"))
    }
}
