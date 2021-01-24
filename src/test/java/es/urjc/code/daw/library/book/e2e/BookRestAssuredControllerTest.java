package es.urjc.code.daw.library.book.e2e;

import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.book.BookService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static es.urjc.code.daw.library.book.TestConstants.*;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("E2E-Tests")
public class BookRestAssuredControllerTest {


    @LocalServerPort
    int port;

    @Autowired
    private BookService bookService;

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
        void getBooksWithoutLoggedUser() {
            given()
                    .relaxedHTTPSValidation()
            .when()
                    .get(BOOKS_URL)
            .then()
                    .statusCode(200)
                    .body("title", containsInAnyOrder(SUENIOS_DE_ACERO_Y_NEON_TITLE, LA_VIDA_SECRETA_DE_LA_MENTE_TITLE, CASI_SIN_QUERER_TITLE, TERMINAMOS_POEMAS_TITLE, LEGION_PERDIDA_TITLE));
// **************************
// **     IMPORTANT        **
// **************************
/*
    THIS KIND OF TESTS CAN FAIL BECAUSE OF OTHER TESTS
        .body(
                "size()", is(5),
                //"$.size()", is(5)
                "[0].title", is(SUENIOS_DE_ACERO_Y_NEON_TITLE),
                "[1].title", is(LA_VIDA_SECRETA_DE_LA_MENTE_TITLE)
        );
 */
        }

        @Test
        @DisplayName("GET all books. User logged should return full list. OK(200)")
        void getBookWithLoggedUser() {
            given()
                    .auth()
                    .basic(USER_USER, PASSWORD)
            .when()
                    .get(BOOKS_URL)
            .then()
                    .statusCode(200)
                    .body("title", containsInAnyOrder(SUENIOS_DE_ACERO_Y_NEON_TITLE, LA_VIDA_SECRETA_DE_LA_MENTE_TITLE, CASI_SIN_QUERER_TITLE, TERMINAMOS_POEMAS_TITLE, LEGION_PERDIDA_TITLE));
        }
    }

    @Nested
    @DisplayName("Create new books ")
    class creatingNewBooks {
        private static final String NEW_BOOK_TITLE = "NewBook Title";
        private static final String NEW_BOOK_DESCRIPTION = "NewBook description";

        @Test
        @DisplayName("POST new book with no logged user must fail. UNAUTHORIZED(401)")
        void createBookWithoutLoggedUser() {
            when()
                    .post(BOOKS_URL)
                    .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        @DisplayName("POST new book with logged user must create user. CREATED(201)")
        void createBookWithLoggedUser(){

            // Set test conditions/asserts & retrieve element for posterior use
            Book bookPosted = given()
                    .auth()
                    .basic(USER_USER, PASSWORD)
                    .contentType(ContentType.JSON)
                    .body(new Book(NEW_BOOK_TITLE, NEW_BOOK_DESCRIPTION))
                        .when()
                        .post(BOOKS_URL)
                        .then()
                            .statusCode(HttpStatus.CREATED.value())
                            .body("id", notNullValue(),
                                    "title", equalTo(NEW_BOOK_TITLE),
                                    "description", equalTo(NEW_BOOK_DESCRIPTION))
                    .extract()
                    .response().as(Book.class);

            // Assert that book exists in BBDD
            assertNotNull(bookService.findOne(bookPosted.getId()));

            // Delete book in order to "rollback" database for other tests
            bookService.delete(bookPosted.getId());
        }
    }


    @Nested
    @DisplayName("Delete books ")
    class deletingBooks{
        @Test
        @DisplayName("DELETE book with unauthorized user must fail. UNAUTHORIZED(401)")
        void deleteBookWithUnauthorizedUserMustFail(){
            when()
                    .delete(BOOKS_URL + "{id}",1)
            .then()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
        }

        @Test
        @DisplayName("DELETE book with authorized user ROLE USER must fail. FORBIDDEN(403)")
        void deleteBookWithUserRolMustFail(){
            given()
                    .auth()
                    .basic(USER_USER, PASSWORD)
            .when()
                    .delete(BOOKS_URL + "{id}",1)
            .then()
                    .statusCode(HttpStatus.FORBIDDEN.value());
        }

        @Test
        @DisplayName("DELETE book with authorized user ADMIN valid book. OK(200)")
        void deleteBookWithUserOkBookNotExists(){
            Book bookToBeDeleted = bookService.findAll().get(0);

            given()
                    .auth()
                    .basic(USER_ADMIN, PASSWORD)
            .when()
                    .delete(BOOKS_URL + "{id}",1)
            .then()
                    .statusCode(HttpStatus.OK.value());

            assertThat(bookService.exist(bookToBeDeleted.getId()), is(Boolean.FALSE));

            // Restore database
            bookService.save(bookToBeDeleted);
        }
    }
}
