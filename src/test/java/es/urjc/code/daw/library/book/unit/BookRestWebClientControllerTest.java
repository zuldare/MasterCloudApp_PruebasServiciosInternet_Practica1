package es.urjc.code.daw.library.book.unit;

import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.book.BookService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@DisplayName("Tests for BookController with WebClient")
@SpringBootTest
@AutoConfigureMockMvc
@Tag("UnitTests")
public class BookRestWebClientControllerTest {

    private static final String BOOKS_URL = "/api/books/";
    private static final String SUENIOS_DE_ACERO_Y_NEON_TITLE = "SUEÑOS DE ACERO Y NEON";
    private static final String SUENIOS_DE_ACERO_Y_NEON_DESCRIPTION = "Los personajes que protagonizan este relato sobreviven en una sociedad en decadencia a la que, no obstante, lograrán devolver la posibilidad de un futuro. Año 2484. En un mundo dominado por las grandes corporaciones, solo un hombre, Jordi Thompson, detective privado deslenguado y vividor, pero de gran talento y sentido d...";
    private static final String LA_VIDA_SECRETA_DE_LA_MENTE_TITLE = "LA VIDA SECRETA DE LA MENTE";
    private static final String LA_VIDA_SECRETA_DE_LA_MENTE_DESCRIPTION = "La vida secreta de la mentees un viaje especular que recorre el cerebro y el pensamiento: se trata de descubrir nuestra mente para entendernos hasta en los más pequeños rincones que componen lo que somos, cómo forjamos las ideas en los primeros días de vida, cómo damos forma a las decisiones que nos constituyen, cómo soñamos y cómo imaginamos, por qué sentimos ciertas emociones hacia los demás, cómo los demás influyen en nosotros, y cómo el cerebro se transforma y, con él, lo que somos.";
    private static final Long ID_1 = 1L;
    private static final Long ID_2 = 2L;

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

        private static final String NEW_BOOK_TITLE = "NewBook Title";
        private static final String NEW_BOOK_DESCRIPTION = "NewBook description";

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
                    .uri(BOOKS_URL, 1)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }
//
//        @Test
//        @DisplayName("DELETE book with authorized user ROLE USER must fail. FORBIDDEN(403)")
//        @WithMockUser(username = "user", password = "pass", roles = "USER")
//        void deleteBookWithUserRolMustFail() throws Exception {
//            mockMvc.perform(delete(BOOKS_URL + "{id}", 1)
//                    .contentType(MediaType.APPLICATION_JSON))
//                    .andExpect(status().isForbidden());
//        }
//
//        @Test
//        @DisplayName("DELETE book with authorized user ADMIN but no book found must fail. NOT_FOUND(404)")
//        @WithMockUser(username = "user", password = "pass", roles = "ADMIN")
//        void deleteBookWithUserOkBookNotExists() throws Exception {
//
//            doThrow(EmptyResultDataAccessException.class)
//                    .when(bookService).delete(1);
//
//            mockMvc.perform(delete(BOOKS_URL + "{id}", 1)
//                    .contentType(MediaType.APPLICATION_JSON))
//                    .andExpect(status().isNotFound());
//        }
//
//        @Test
//        @DisplayName("DELETE book with authorized user ADMIN valid book. OK(200)")
//        @WithMockUser(username = "user", password = "pass", roles = "ADMIN")
//        void deleteBookWithUserOkAdminRole() throws Exception {
//
//            mockMvc.perform(delete(BOOKS_URL + "{id}", 1)
//                    .contentType(MediaType.APPLICATION_JSON))
//                    .andExpect(status().isOk());
//        }
//    }
//
    private static List<Book> generateFullBooksList(){
        Book book1 = new Book(SUENIOS_DE_ACERO_Y_NEON_TITLE, SUENIOS_DE_ACERO_Y_NEON_DESCRIPTION);
        book1.setId(ID_1);

        Book book2 = new Book(LA_VIDA_SECRETA_DE_LA_MENTE_TITLE, LA_VIDA_SECRETA_DE_LA_MENTE_DESCRIPTION);
        book2.setId(ID_2);

        return Arrays.asList( book1, book2);
    }
//
//    public static String asJsonString(final Object obj) {
//        try {
//            return new ObjectMapper().writeValueAsString(obj);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}
