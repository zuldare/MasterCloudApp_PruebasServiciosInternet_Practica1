package es.urjc.code.daw.library.book;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.CoreMatchers.equalTo;

@DisplayName("Tests for BookController")
@SpringBootTest
@AutoConfigureMockMvc
public class BookControllerTest {

    private static final int UNAUTHORIZED_STATUS = 401;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Get all books. No logged user is needed.")
    void getBooksWithoutLoggedUser() throws Exception {
        mockMvc.perform(get("/api/books/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$[0].id", equalTo(1)))
                .andExpect(jsonPath("$[0].title", equalTo("SUEÑOS DE ACERO Y NEON")))

                .andExpect(jsonPath("$[1].id", equalTo(2)))
                .andExpect(jsonPath("$[1].title", equalTo("LA VIDA SECRETA DE LA MENTE")))

                .andExpect(jsonPath("$[2].id", equalTo(3)))
                .andExpect(jsonPath("$[2].title", equalTo("CASI SIN QUERER")))

                .andExpect(jsonPath("$[3].id", equalTo(4)))
                .andExpect(jsonPath("$[3].title", equalTo("TERMINAMOS Y OTROS POEMAS SIN TERMINAR")))

                .andExpect(jsonPath("$[4].id", equalTo(5)))
                .andExpect(jsonPath("$[4].title", equalTo("LA LEGIÓN PERDIDA")));
    }

    @Test
    @DisplayName("Get all books with a logged user.")
    @WithMockUser(username = "user", password = "pass", roles = "USER")
    void getBooksWithLoggedUser() throws Exception {
        mockMvc.perform(get("/api/books/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$[0].id", equalTo(1)))
                .andExpect(jsonPath("$[0].title", equalTo("SUEÑOS DE ACERO Y NEON")))

                .andExpect(jsonPath("$[1].id", equalTo(2)))
                .andExpect(jsonPath("$[1].title", equalTo("LA VIDA SECRETA DE LA MENTE")))

                .andExpect(jsonPath("$[2].id", equalTo(3)))
                .andExpect(jsonPath("$[2].title", equalTo("CASI SIN QUERER")))

                .andExpect(jsonPath("$[3].id", equalTo(4)))
                .andExpect(jsonPath("$[3].title", equalTo("TERMINAMOS Y OTROS POEMAS SIN TERMINAR")))

                .andExpect(jsonPath("$[4].id", equalTo(5)))
                .andExpect(jsonPath("$[4].title", equalTo("LA LEGIÓN PERDIDA")));
    }

    @Test
    @DisplayName("Create new book with no logged user must fail")
    void createBookWithoutLoggedUser() throws Exception {
        mockMvc.perform( MockMvcRequestBuilders
                .post("/api/books")
                .content(asJsonString(new Book("NewBook Title", "NewBook description")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(UNAUTHORIZED_STATUS));
    }

    @Test
    @DisplayName("Create new book with logged user must create user")
    @WithMockUser(username = "user", password = "pass", roles = "USER")
    void createBookWithLoggedUser() throws Exception {
        mockMvc.perform( MockMvcRequestBuilders
                .post("/api/books/")
                .content(asJsonString(new Book("NewBook Title", "NewBook description")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", equalTo("NewBook Title")))
                .andExpect(jsonPath("$.description", equalTo("NewBook description")));
    }

    @Test
    @DisplayName("Delete book with USER role must fail")
    void deleteBookWithUserRolMustFail(){

    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
