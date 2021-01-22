package es.urjc.code.daw.library.book.rest;

import es.urjc.code.daw.library.book.Book;
import io.restassured.RestAssured;
import io.restassured.config.SSLConfig;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.*;
import static io.restassured.config.RestAssuredConfig.newConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookRestAssuredControllerTest {

    private static final String BOOKS_URL = "/api/books/";
    private static final String SUENIOS_DE_ACERO_Y_NEON_TITLE = "SUEÑOS DE ACERO Y NEON";
    private static final String LA_VIDA_SECRETA_DE_LA_MENTE_TITLE = "LA VIDA SECRETA DE LA MENTE";

    @LocalServerPort
    int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.baseURI = "https://localhost:" + port;
    }

    @Nested
    @DisplayName("Get books ")
    class gettingBooksForUser {

        @Test
        @DisplayName("GET all books. No logged user is needed. OK(200)")
        void getBooksWithoutLoggedUser() throws Exception {
            given()
                    .relaxedHTTPSValidation()
                    .when()
                    .get(BOOKS_URL)
                    .then()
                    .statusCode(200)
                    .body(
                            "size()", is(5),
                            //"$.size()", is(5)
                            "[0].title", is(SUENIOS_DE_ACERO_Y_NEON_TITLE),
                            "[1].title", is(LA_VIDA_SECRETA_DE_LA_MENTE_TITLE)
                            );

        }
    }
}
