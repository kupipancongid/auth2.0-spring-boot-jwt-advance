package id.kupipancong.userregistration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.kupipancong.userregistration.entity.User;
import id.kupipancong.userregistration.entity.UserVerificationToken;
import id.kupipancong.userregistration.model.request.UserRegisterRequest;
import id.kupipancong.userregistration.model.response.WebResponse;
import id.kupipancong.userregistration.repository.UserRepository;
import id.kupipancong.userregistration.repository.UserVerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserAuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserVerificationTokenRepository userVerificationTokenRepository;

    @BeforeEach
    void setUp(){
        userVerificationTokenRepository.deleteAll();
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
                post("/api/users")
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
                post("/api/users")
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
                post("/api/users")
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
                post("/api/users")
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
    void testRegisterSuccessTokenValid() throws Exception{
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("kupipancong");
        request.setLastName("ID");
        request.setUsername("kupipancongid");
        request.setEmail("idkupipancong@gmail.com");
        request.setPassword("secret");
        request.setPasswordConfirmation("secret");

        mockMvc.perform(
                post("/api/users")
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
        Optional<User> optUser = userRepository.findByEmailOrUsername(null,"kupipancongid");
        User user = optUser.get();
        assertEquals(null, user.getEmailVerifiedAt());
        Optional<UserVerificationToken> optUserVerificationToken = userVerificationTokenRepository.findByUser(user);
        UserVerificationToken userVerificationToken = optUserVerificationToken.get();
        assertEquals(null, userVerificationToken.getTokenTakenAt());
        assertNotEquals(null, userVerificationToken.getToken());

        String verificationToken = userVerificationToken.getToken();

        mockMvc.perform(
                get("/api/auth?verification-token="+verificationToken)
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
        optUser = userRepository.findByEmailOrUsername(null,"kupipancongid");
        user = optUser.get();
        assertNotEquals(null, user.getEmailVerifiedAt());
        optUserVerificationToken = userVerificationTokenRepository.findByUser(user);
        userVerificationToken = optUserVerificationToken.get();
        assertNotEquals(null, userVerificationToken.getTokenTakenAt());
    }

    @Test
    void testRegisterFailedUserRegistered() throws Exception{
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("kupipancong");
        request.setLastName("ID");
        request.setUsername("kupipancongid");
        request.setEmail("idkupipancong@gmail.com");
        request.setPassword("secret");
        request.setPasswordConfirmation("secret");

        mockMvc.perform(
                post("/api/users")
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
        Optional<User> optUser = userRepository.findByEmailOrUsername(null,"kupipancongid");
        User user = optUser.get();
        assertEquals(null, user.getEmailVerifiedAt());
        Optional<UserVerificationToken> optUserVerificationToken = userVerificationTokenRepository.findByUser(user);
        UserVerificationToken userVerificationToken = optUserVerificationToken.get();
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);
        userVerificationToken.setTokenTakenAt(LocalDateTime.now());
        userVerificationTokenRepository.save(userVerificationToken);

        String verificationToken = userVerificationToken.getToken();

        mockMvc.perform(
                get("/api/auth?verification-token="+verificationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isNotFound()
        ).andDo(
                result -> {
                    WebResponse<String> response= objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
                    });
                    assertEquals("User verified", response.getErrors());
                }
        );
    }

    @Test
    void testRegisterFailedInvalidToken() throws Exception{
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("kupipancong");
        request.setLastName("ID");
        request.setUsername("kupipancongid");
        request.setEmail("idkupipancong@gmail.com");
        request.setPassword("secret");
        request.setPasswordConfirmation("secret");

        mockMvc.perform(
                post("/api/users")
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

        String verificationToken = "wrong-token";

        mockMvc.perform(
                get("/api/auth?verification-token="+verificationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isNotFound()
        ).andDo(
                result -> {
                    WebResponse<String> response= objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
                    });
                    assertEquals("Token not found", response.getErrors());
                }
        );
    }

}