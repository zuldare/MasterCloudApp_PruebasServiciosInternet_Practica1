package es.urjc.code.daw.library.book.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.book.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Tests for BookController")
@SpringBootTest
@AutoConfigureMockMvc
public class BookRestControllerTest {

    private static final String BOOKS_URL = "/api/books/";
    private static final String SUENIOS_DE_ACERO_Y_NEON_TITLE = "SUEÑOS DE ACERO Y NEON";
    private static final String SUENIOS_DE_ACERO_Y_NEON_DESCRIPTION = "Los personajes que protagonizan este relato sobreviven en una sociedad en decadencia a la que, no obstante, lograrán devolver la posibilidad de un futuro. Año 2484. En un mundo dominado por las grandes corporaciones, solo un hombre, Jordi Thompson, detective privado deslenguado y vividor, pero de gran talento y sentido d...";
    private static final String LA_VIDA_SECRETA_DE_LA_MENTE_TITLE = "LA VIDA SECRETA DE LA MENTE";
    private static final String LA_VIDA_SECRETA_DE_LA_MENTE_DESCRIPTION = "La vida secreta de la mentees un viaje especular que recorre el cerebro y el pensamiento: se trata de descubrir nuestra mente para entendernos hasta en los más pequeños rincones que componen lo que somos, cómo forjamos las ideas en los primeros días de vida, cómo damos forma a las decisiones que nos constituyen, cómo soñamos y cómo imaginamos, por qué sentimos ciertas emociones hacia los demás, cómo los demás influyen en nosotros, y cómo el cerebro se transforma y, con él, lo que somos.";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;


    private static List<Book> generateFullBooksList(){
        return Arrays.asList( new Book(SUENIOS_DE_ACERO_Y_NEON_TITLE, SUENIOS_DE_ACERO_Y_NEON_DESCRIPTION),
                new Book(LA_VIDA_SECRETA_DE_LA_MENTE_TITLE, LA_VIDA_SECRETA_DE_LA_MENTE_DESCRIPTION));
    }

    @Nested
    @DisplayName("getting books for user ")
    class gettingBooksForUser{

        @Test
        @DisplayName("GET all books. No logged user is needed. OK(200)")
        void getBooksWithoutLoggedUser() throws Exception {

            when(bookService.findAll())
                    .thenReturn(BookRestControllerTest.generateFullBooksList());

            mockMvc.perform(get(BOOKS_URL)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$", hasSize(2)))

                    .andExpect(jsonPath("$[0].title", equalTo(SUENIOS_DE_ACERO_Y_NEON_TITLE)))
                    .andExpect(jsonPath("$[0].description", equalTo(SUENIOS_DE_ACERO_Y_NEON_DESCRIPTION)))

                    .andExpect(jsonPath("$[1].title", equalTo(LA_VIDA_SECRETA_DE_LA_MENTE_TITLE)))
                    .andExpect(jsonPath("$[1].description", equalTo(LA_VIDA_SECRETA_DE_LA_MENTE_DESCRIPTION)));
        }

        @Test
        @DisplayName("GET all books with a logged user. OK(200)")
        @WithMockUser(username = "user", password = "pass", roles = "USER")
        void getBooksWithLoggedUser() throws Exception {

            when(bookService.findAll())
                    .thenReturn(BookRestControllerTest.generateFullBooksList());

            mockMvc.perform(get(BOOKS_URL)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())

                    .andExpect(jsonPath("$", hasSize(2)))

                    .andExpect(jsonPath("$[0].title", equalTo(SUENIOS_DE_ACERO_Y_NEON_TITLE)))
                    .andExpect(jsonPath("$[0].description", equalTo(SUENIOS_DE_ACERO_Y_NEON_DESCRIPTION)))

                    .andExpect(jsonPath("$[1].title", equalTo(LA_VIDA_SECRETA_DE_LA_MENTE_TITLE)))
                    .andExpect(jsonPath("$[1].description", equalTo(LA_VIDA_SECRETA_DE_LA_MENTE_DESCRIPTION)));
        }
    }

    @Nested
    @DisplayName("Create new books ")
    class creatingNewBooks{

        private static final String NEW_BOOK_TITLE = "NewBook Title";
        private static final String NEW_BOOK_DESCRIPTION = "NewBook description";

        @Test
        @DisplayName("POST new book with no logged user must fail. UNAUTHORIZED(401)")
        void createBookWithoutLoggedUser() throws Exception {
            mockMvc.perform( MockMvcRequestBuilders
                    .post(BOOKS_URL)
                    .content(asJsonString(new Book(NEW_BOOK_TITLE, NEW_BOOK_DESCRIPTION)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST new book with logged user must create user. CREATED(201)")
        @WithMockUser(username = "user", password = "pass", roles = "USER")
        void createBookWithLoggedUser() throws Exception {

            when(bookService.save(any(Book.class)))
                    .thenReturn(new Book(NEW_BOOK_TITLE, NEW_BOOK_DESCRIPTION));

            mockMvc.perform( MockMvcRequestBuilders
                    .post(BOOKS_URL)
                    .content(asJsonString(new Book(NEW_BOOK_TITLE, NEW_BOOK_DESCRIPTION)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title", equalTo(NEW_BOOK_TITLE)))
                    .andExpect(jsonPath("$.description", equalTo(NEW_BOOK_DESCRIPTION)));
        }
    }


    @Nested
    @DisplayName("Delete books ")
    class deletingBooks{
        @Test
        @DisplayName("DELETE book with unauthorized user must fail. UNAUTHORIZED(401)")
        void deleteBookWithUnauthorizedUserMustFail() throws Exception {
            mockMvc.perform(delete(BOOKS_URL + "{id}", 1)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE book with authorized user ROLE USER must fail. FORBIDDEN(403)")
        @WithMockUser(username = "user", password = "pass", roles = "USER")
        void deleteBookWithUserRolMustFail() throws Exception {
            mockMvc.perform(delete(BOOKS_URL + "{id}", 1)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE book with authorized user ADMIN but no book found must fail. NOT_FOUND(404)")
        @WithMockUser(username = "user", password = "pass", roles = "ADMIN")
        void deleteBookWithUserOkBookNotExists() throws Exception {

            doThrow(EmptyResultDataAccessException.class)
                    .when(bookService).delete(1);

            mockMvc.perform(delete(BOOKS_URL + "{id}", 1)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("DELETE book with authorized user ADMIN valid book. OK(200)")
        @WithMockUser(username = "user", password = "pass", roles = "ADMIN")
        void deleteBookWithUserOkAdminRole() throws Exception {

            mockMvc.perform(delete(BOOKS_URL + "{id}", 1)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }


    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
