package id.kupipancong.userregistration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.kupipancong.userregistration.entity.User;
import id.kupipancong.userregistration.model.request.UserRegisterRequest;
import id.kupipancong.userregistration.model.response.WebResponse;
import id.kupipancong.userregistration.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.MockMvcBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp(){
        userRepository.deleteAll();
    }

    @Test
    void testRegisterSuccess() throws Exception{
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("kupipancong");
        request.setLastName("ID");
        request.setUsername("kupipancongid");
        request.setEmail("idkupipancong@gmail.com");
        request.setPassword("secret");
        request.setPasswordConfirmation("secret");

        mockMvc.perform(
                post("/api/register")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isOk()
        ).andDo(
                result -> {
                    WebResponse<String> response= objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
                    });

                    assertEquals("OK", response.getData());
                }
        );
    }

    @Test
    void testRegisterBadRequest() throws Exception{
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("");
        request.setFirstName("");
        request.setUsername("");
        request.setPassword("");
        request.setPasswordConfirmation("");

        mockMvc.perform(
                post("/api/register")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(
                result -> {
                    WebResponse<String> response= objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
                    });

                    assertNotNull(response.getErrors());
                }
        );
    }

    @Test
    void testRegisterEmailExist() throws Exception{
        User user = new User();
        user.setFirstName("Kupipancong");
        user.setLastName("ID");
        user.setEmail("idkupipancong@gmail.com");
        user.setUsername("kupipancongid1");
        userRepository.save(user);

        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("idkupipancong@gmail.com");
        request.setFirstName("kupipancong");
        request.setUsername("kupipancongid2");
        request.setPassword("secret");
        request.setPasswordConfirmation("secret");

        mockMvc.perform(
                post("/api/register")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isConflict()
        ).andDo(
                result -> {
                    WebResponse<String> response= objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
                    });

                    assertNotNull(response.getErrors());
                }
        );
    }

    @Test
    void testRegisterUsernameExist() throws Exception{
        User user = new User();
        user.setFirstName("Kupipancong");
        user.setLastName("ID");
        user.setEmail("idkupipancong@gmail.com");
        user.setUsername("kupipancongid");
        userRepository.save(user);

        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("idkupipancong2@gmail.com");
        request.setFirstName("kupipancong");
        request.setUsername("kupipancongid");
        request.setPassword("secret");
        request.setPasswordConfirmation("secret");

        mockMvc.perform(
                post("/api/register")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isConflict()
        ).andDo(
                result -> {
                    WebResponse<String> response= objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
                    });

                    assertNotNull(response.getErrors());
                }
        );
    }

}