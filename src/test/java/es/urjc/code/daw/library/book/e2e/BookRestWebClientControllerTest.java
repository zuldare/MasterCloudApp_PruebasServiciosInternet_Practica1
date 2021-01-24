package es.urjc.code.daw.library.book.e2e;

import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.book.BookRepository;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.util.List;

import static es.urjc.code.daw.library.book.TestConstants.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("E2E-Tests")
@DisplayName("Tests for BookController with WebClient E2E")
public class BookRestWebClientControllerTest {

    private static final String USER_USER = "user";
    private static final String USER_PASSWORD = "pass";
    private static final String USER_ADMIN = "admin";

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    public void setup() throws SSLException {
        // Connector needed in order to avoid this message
        // org.springframework.web.reactive.function.client.WebClientRequestException: finishConnect(..) failed: Conexión rehusada: /0:0:0:0:0:0:0:1:80; nested exception is io.netty.channel.AbstractChannel$AnnotatedConnectException: finishConnect(..) failed: Conexión rehusada: /0:0:0:0:0:0:0:1:80
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        HttpClient httpClient = HttpClient.create()
                .secure(sslSpec -> sslSpec.sslContext(sslContext))
                .baseUrl("https://localhost:" + port);


        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        this.webTestClient = WebTestClient
                .bindToServer(connector)
                .build();
    }

    @Nested
    @DisplayName("Get books ")
    class gettingBooksForUser {
        @Test
        @DisplayName("GET all books. No logged user is needed. OK(200)")
        void getBooksWithoutLoggedUser(){
            List<Book> booksFromRepo = bookRepository.findAll();

            List<Book> booksRetrieved = webTestClient.get()
                    .uri(BOOKS_URL)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(Book.class)
                    .returnResult()
                    .getResponseBody();

            assertNotNull(booksRetrieved);
            assertEqualsBooks(booksFromRepo, booksRetrieved);
        }

        @Test
        @DisplayName("GET all books with a logged user. OK(200)")
        void getBooksWithLoggedUser(){
            List<Book> booksFromRepo = bookRepository.findAll();

            List<Book> booksRetrieved = webTestClient
                    .mutate()
                    .filter(basicAuthentication(USER_USER, USER_PASSWORD)).build()
                    .get()
                    .uri(BOOKS_URL)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(Book.class)
                    .returnResult()
                    .getResponseBody();

            assertNotNull(booksRetrieved);
            assertEqualsBooks(booksFromRepo, booksRetrieved);

        }

        private void assertEqualsBooks(List<Book> booksFromRepo, List<Book> booksRetrieved) {
            assertThat(booksRetrieved.size(), equalTo(booksFromRepo.size()));

            for (int i = 0; i < booksFromRepo.size(); i++){
                Book bookFromRepo = booksFromRepo.get(i);
                Book bookRetrieved = booksRetrieved.get(i);

                assertThat(bookFromRepo.getId(), is(bookRetrieved.getId()));
                assertThat(bookFromRepo.getDescription(), is(bookRetrieved.getDescription()));
                assertThat(bookFromRepo.getTitle(), is(bookRetrieved.getTitle()));
            }
        }
    }

    @Nested
    @DisplayName("Create new books ")
    class creatingNewBooks{

        @Test
        @DisplayName("POST new book with no logged user must fail. UNAUTHORIZED(401)")
        void createBookWithoutLoggedUser() {
           webTestClient
                    .post()
                    .uri(BOOKS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new Book(NEW_BOOK_TITLE, NEW_BOOK_DESCRIPTION))
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("POST new book with logged user must create user. CREATED(201)")
        void createBookWithLoggedUser() {
            Book bookCreated = webTestClient
                    .mutate().filter(basicAuthentication(USER_USER, USER_PASSWORD)).build()
                    .post()
                    .uri(BOOKS_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new Book(NEW_BOOK_TITLE, NEW_BOOK_DESCRIPTION))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(Book.class)
                    .returnResult()
                    .getResponseBody();

            assertNotNull(bookCreated);
            assertNotNull(bookCreated.getId());
            assertThat(bookCreated.getDescription(), is(bookCreated.getDescription()));
            assertThat(bookCreated.getTitle(), is(bookCreated.getTitle()));

            // Restore database
            bookRepository.deleteById(bookCreated.getId());
        }
    }

    @Nested
    @DisplayName("Delete books ")
    class deletingBooks {
        @Test
        @DisplayName("DELETE book with unauthorized user must fail. UNAUTHORIZED(401)")
        void deleteBookWithUnauthorizedUserMustFail()  {
            webTestClient.delete()
                    .uri(BOOKS_URL + "{id}", 1)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }

        @Test
        @DisplayName("DELETE book with authorized user ROLE USER must fail. FORBIDDEN(403)")
        @WithMockUser(username = "user", password = "pass", roles = "USER")
        void deleteBookWithUserRolMustFail() {
            webTestClient
                    .mutate().filter(basicAuthentication(USER_USER, USER_PASSWORD)).build()
                    .delete()
                    .uri(BOOKS_URL + "{id}", 1)
                    .exchange()
                    .expectStatus().isForbidden();
        }

        @Test
        @DisplayName("DELETE book with authorized user ADMIN but no book found must fail. NOT_FOUND(404)")
        @WithMockUser(username = "user", password = "pass", roles = "ADMIN")
        void deleteBookWithUserOkBookNotExists() {
            webTestClient
                    .mutate().filter(basicAuthentication(USER_ADMIN, USER_PASSWORD)).build()
                    .delete()
                    .uri(BOOKS_URL + "{id}", 9999999)
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        @DisplayName("DELETE book with authorized user ADMIN valid book. OK(200)")
        @WithMockUser(username = "user", password = "pass", roles = "ADMIN")
        void deleteBookWithUserOkAdminRole()  {

            List<Book> books = bookRepository.findAll();

            webTestClient
                    .mutate().filter(basicAuthentication(USER_ADMIN, USER_PASSWORD)).build()
                    .delete()
                    .uri(BOOKS_URL + "{id}", books.get(0).getId())
                    .exchange()
                    .expectStatus().isOk();

            assertThat(books.size(), is(bookRepository.findAll().size() +1 ));

            // Restore database
            bookRepository.save(books.get(0));
        }
    }
}
