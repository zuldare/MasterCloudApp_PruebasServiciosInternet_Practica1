package es.urjc.code.daw.library.book.unit;

import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.book.BookService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

import java.util.Arrays;
import java.util.List;

import static es.urjc.code.daw.library.book.TestConstants.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Tests for BookController with WebClient")
@SpringBootTest
@AutoConfigureMockMvc
@Tag("UnitTests")
public class BookRestWebClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    private WebTestClient webTestClient;

    @BeforeEach
    public void setup(){
        this.webTestClient = MockMvcWebTestClient
                .bindTo(mockMvc)
                .build();
    }

    @Nested
    @DisplayName("Get books ")
    class gettingBooksForUser {

        @Test
        @DisplayName("GET all books. No logged user is needed. OK(200)")
        void getBooksWithoutLoggedUser(){
            when(bookService.findAll())
                    .thenReturn(generateFullBooksList());

            webTestClient.get()
                    .uri(BOOKS_URL)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()

                    .jsonPath("$").isArray()
                    .jsonPath("$").isNotEmpty()

                    .jsonPath("$[0].id").value(equalTo(ID_1.intValue()))
                    .jsonPath("$[0].title").value(equalTo(SUENIOS_DE_ACERO_Y_NEON_TITLE))
                    .jsonPath("$[0].description").value(equalTo(SUENIOS_DE_ACERO_Y_NEON_DESCRIPTION))

                    .jsonPath("$[1].id").value(equalTo(ID_2.intValue()))
                    .jsonPath("$[1].title").value(equalTo(LA_VIDA_SECRETA_DE_LA_MENTE_TITLE))
                    .jsonPath("$[1].description").value(equalTo(LA_VIDA_SECRETA_DE_LA_MENTE_DESCRIPTION));
        }

        @Test
        @DisplayName("GET all books with a logged user. OK(200)")
        @WithMockUser(username = "user", password = "pass", roles = "USER")
        void getBooksWithLoggedUser(){

            when(bookService.findAll())
                    .thenReturn(generateFullBooksList());

            webTestClient.get()
                    .uri(BOOKS_URL)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()

                    .jsonPath("$").isArray()
                    .jsonPath("$").isNotEmpty()

                    .jsonPath("$[0].id").value(equalTo(ID_1.intValue()))
                    .jsonPath("$[0].title").value(equalTo(SUENIOS_DE_ACERO_Y_NEON_TITLE))
                    .jsonPath("$[0].description").value(equalTo(SUENIOS_DE_ACERO_Y_NEON_DESCRIPTION))

                    .jsonPath("$[1].id").value(equalTo(ID_2.intValue()))
                    .jsonPath("$[1].title").value(equalTo(LA_VIDA_SECRETA_DE_LA_MENTE_TITLE))
                    .jsonPath("$[1].description").value(equalTo(LA_VIDA_SECRETA_DE_LA_MENTE_DESCRIPTION));
        }
    }

    @Nested
    @DisplayName("Create new books ")
    class creatingNewBooks{

        @Test
        @DisplayName("POST new book with no logged user must fail. UNAUTHORIZED(401)")
        void createBookWithoutLoggedUser() {
            webTestClient.post()
                    .uri(BOOKS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new Book(NEW_BOOK_TITLE, NEW_BOOK_DESCRIPTION))
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
        @Test
        @DisplayName("POST new book with logged user must create user. CREATED(201)")
        @WithMockUser(username = "user", password = "pass", roles = "USER")
        void createBookWithLoggedUser() {
            when(bookService.save(any(Book.class)))
                    .thenReturn(new Book(NEW_BOOK_TITLE, NEW_BOOK_DESCRIPTION));

            webTestClient.post()
                    .uri(BOOKS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new Book(NEW_BOOK_TITLE, NEW_BOOK_DESCRIPTION))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                            .jsonPath("$.id").value(is(nullValue()))
                            .jsonPath("$.title").value(equalTo(NEW_BOOK_TITLE))
                            .jsonPath("$.description").value(equalTo(NEW_BOOK_DESCRIPTION)
                    );
        }
    }

    @Nested
    @DisplayName("Delete books ")
    class deletingBooks {
        @Test
        @DisplayName("DELETE book with unauthorized user must fail. UNAUTHORIZED(401)")
        void deleteBookWithUnauthorizedUserMustFail() throws Exception {
            webTestClient.delete()
                    .uri(BOOKS_URL + "{id}", 1)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("DELETE book with authorized user ROLE USER must fail. FORBIDDEN(403)")
        @WithMockUser(username = "user", password = "pass", roles = "USER")
        void deleteBookWithUserRolMustFail() {
            webTestClient.delete()
                    .uri(BOOKS_URL + "{id}", 1)
                    .exchange()
                    .expectStatus().isForbidden();
        }

        @Test
        @DisplayName("DELETE book with authorized user ADMIN but no book found must fail. NOT_FOUND(404)")
        @WithMockUser(username = "user", password = "pass", roles = "ADMIN")
        void deleteBookWithUserOkBookNotExists() throws Exception {

            doThrow(EmptyResultDataAccessException.class)
                    .when(bookService).delete(1);

            webTestClient.delete()
                    .uri(BOOKS_URL + "{id}", 1)
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        @DisplayName("DELETE book with authorized user ADMIN valid book. OK(200)")
        @WithMockUser(username = "user", password = "pass", roles = "ADMIN")
        void deleteBookWithUserOkAdminRole()  {

            doNothing().when(bookService).delete(1L);

            webTestClient.delete()
                    .uri(BOOKS_URL + "{id}", 1)
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    private static List<Book> generateFullBooksList(){
        Book book1 = new Book(SUENIOS_DE_ACERO_Y_NEON_TITLE, SUENIOS_DE_ACERO_Y_NEON_DESCRIPTION);
        book1.setId(ID_1);

        Book book2 = new Book(LA_VIDA_SECRETA_DE_LA_MENTE_TITLE, LA_VIDA_SECRETA_DE_LA_MENTE_DESCRIPTION);
        book2.setId(ID_2);

        return Arrays.asList( book1, book2);
    }
}
