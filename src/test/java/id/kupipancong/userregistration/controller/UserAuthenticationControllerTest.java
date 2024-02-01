package id.kupipancong.userregistration.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.kupipancong.userregistration.entity.SessionToken;
import id.kupipancong.userregistration.entity.User;
import id.kupipancong.userregistration.entity.UserVerificationToken;
import id.kupipancong.userregistration.model.request.UserLoginRequest;
import id.kupipancong.userregistration.model.request.UserRegisterRequest;
import id.kupipancong.userregistration.model.response.TokenResponse;
import id.kupipancong.userregistration.model.response.UserResponse;
import id.kupipancong.userregistration.model.response.WebResponse;
import id.kupipancong.userregistration.repository.SessionRepository;
import id.kupipancong.userregistration.repository.SessionTokenRepository;
import id.kupipancong.userregistration.repository.UserRepository;
import id.kupipancong.userregistration.repository.UserVerificationTokenRepository;
import id.kupipancong.userregistration.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
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
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private SessionTokenRepository sessionTokenRepository;
    @Autowired
    private UserService userService;

    @AfterEach
    void afterEach(){
        truncateAuthenticationRelatedData();
    }

    void truncateAuthenticationRelatedData(){
        sessionTokenRepository.deleteAll();
        sessionRepository.deleteAll();
        userVerificationTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    void registerUser(UserRegisterRequest request){
        userService.userRegister(request);
    }

    TokenResponse login(UserLoginRequest request){
        return userService.login(request);
    }

    @Test
    void testRegisterSuccess() throws Exception{
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("kupi");
        request.setLastName("pancongid");
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
        user.setFirstName("kupi");
        user.setLastName("pancongid");
        user.setEmail("idkupipancong@gmail.com");
        user.setUsername("kupipancongid");
        userRepository.save(user);

        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("kupi");
        request.setLastName("pancongid");
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
        user.setFirstName("kupi");
        user.setLastName("pancongid");
        user.setEmail("idkupipancong@gmail.com");
        user.setUsername("kupipancongid");
        userRepository.save(user);

        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("kupi");
        request.setLastName("pancongid");
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
    void testVerificationTokenValidEmailVerificationSuccess() throws Exception{
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("kupi");
        request.setLastName("pancongid");
        request.setUsername("kupipancongid");
        request.setEmail("idkupipancong@gmail.com");
        request.setPassword("secret");
        request.setPasswordConfirmation("secret");
        registerUser(request);

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
    void testVerificationTokenInvalidEmailVerificationFailed() throws Exception{
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("kupi");
        request.setLastName("pancongid");
        request.setUsername("kupipancongid");
        request.setEmail("idkupipancong@gmail.com");
        request.setPassword("secret");
        request.setPasswordConfirmation("secret");
        registerUser(request);

        Optional<User> optUser = userRepository.findByEmailOrUsername(null,"kupipancongid");
        User user = optUser.get();

        assertEquals(null, user.getEmailVerifiedAt());

        Optional<UserVerificationToken> optUserVerificationToken = userVerificationTokenRepository.findByUser(user);
        UserVerificationToken userVerificationToken = optUserVerificationToken.get();

        assertEquals(null, userVerificationToken.getTokenTakenAt());
        assertNotEquals(null, userVerificationToken.getToken());

        String verificationToken = "wrong-token";

        mockMvc.perform(
                get("/api/auth?verification-token="+verificationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(
                result -> {
                    WebResponse<String> response= objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
                    });
                    assertEquals("Token not found", response.getErrors());
                }
        );
        optUser = userRepository.findByEmailOrUsername(null,"kupipancongid");
        user = optUser.get();

        assertEquals(null, user.getEmailVerifiedAt());

        optUserVerificationToken = userVerificationTokenRepository.findByUser(user);
        userVerificationToken = optUserVerificationToken.get();

        assertEquals(null, userVerificationToken.getTokenTakenAt());
    }

    @Test
    void testRegisterFailedUserRegistered() throws Exception{
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("kupi");
        request.setLastName("pancongid");
        request.setUsername("kupipancongid");
        request.setEmail("idkupipancong@gmail.com");
        request.setPassword("secret");
        request.setPasswordConfirmation("secret");
        registerUser(request);

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
                status().isUnauthorized()
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
        request.setFirstName("kupi");
        request.setLastName("pancongid");
        request.setUsername("kupipancongid");
        request.setEmail("idkupipancong@gmail.com");
        request.setPassword("secret");
        request.setPasswordConfirmation("secret");
        registerUser(request);

        String verificationToken = "wrong-token";

        mockMvc.perform(
                get("/api/auth?verification-token="+verificationToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(
                result -> {
                    WebResponse<String> response= objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
                    });

                    assertEquals("Token not found", response.getErrors());
                }
        );
    }

    @Test
    void testLoginSuccessTokenReceived() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("kupi");
        request.setLastName("pancongid");
        request.setUsername("kupipancongid");
        request.setEmail("idkupipancong@gmail.com");
        request.setPassword("secret");
        request.setPasswordConfirmation("secret");
        registerUser(request);

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("idkupipancong@gmail.com");
        loginRequest.setPassword("secret");

        mockMvc.perform(
                post("/api/auth")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
        ).andExpectAll(
                status().isOk()
        ).andDo(
                result -> {
                    WebResponse<TokenResponse> response= objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<TokenResponse>>() {
                    });

                    assertNotNull(response.getData().getAccessToken());
                    assertNotNull(response.getData().getRefreshToken());
                    assertNull(response.getErrors());
                }
        );


    }

    @Test
    void testTokenValidAccessToResourcesValid() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("kupi");
        request.setLastName("pancongid");
        request.setUsername("kupipancongid");
        request.setEmail("idkupipancong@gmail.com");
        request.setPassword("secret");
        request.setPasswordConfirmation("secret");
        registerUser(request);

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("idkupipancong@gmail.com");
        loginRequest.setPassword("secret");

        TokenResponse token = login(loginRequest);
        log.info(token.getAccessToken());

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-ACCESS-TOKEN", token.getAccessToken());

        mockMvc.perform(
                get("/api/auth/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk()
        ).andDo(
                result -> {
                    WebResponse<UserResponse> response= objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<UserResponse>>() {
                    });

                    assertNotNull(response.getData().getId());
                }
        );
    }
    @Test
    void testTokenExpiredAccessToResourcesInvalid() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("kupi");
        request.setLastName("pancongid");
        request.setUsername("kupipancongid");
        request.setEmail("idkupipancong@gmail.com");
        request.setPassword("secret");
        request.setPasswordConfirmation("secret");
        registerUser(request);

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("idkupipancong@gmail.com");
        loginRequest.setPassword("secret");

        TokenResponse token = login(loginRequest);

        Optional<SessionToken> optionalSessionToken = sessionTokenRepository.findSessionTokenByAccessToken(token.getAccessToken());
        SessionToken sessionToken = optionalSessionToken.get();

        User user = sessionToken.getUser();

        String sessionTokenId = UUID.randomUUID().toString();
        Date accessTokenIssuedAt = new Date(System.currentTimeMillis());
        Date accessTokenExpiredAt = new Date(System.currentTimeMillis() - 2L);
        Date refreshTokenIssuedAt = accessTokenIssuedAt;
        Date refreshTokenExpiredAt = new Date(System.currentTimeMillis() - 1L);

        sessionToken = userService.generateSessionToken(sessionTokenId, sessionToken.getSession(), user, accessTokenIssuedAt, accessTokenExpiredAt, refreshTokenIssuedAt, refreshTokenExpiredAt);

        sessionTokenRepository.save(sessionToken);

        String expiredAccessToken = sessionToken.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-ACCESS-TOKEN", sessionToken.getAccessToken());

        mockMvc.perform(
                get("/api/auth/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(
                result -> {
                    WebResponse<UserResponse> response= objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<UserResponse>>() {
                    });

                    Optional<SessionToken> optionalSessionToken1 = sessionTokenRepository.findSessionTokenByAccessToken(expiredAccessToken);
                    SessionToken sessionToken1 = optionalSessionToken1.get();
                    assertNotNull(sessionToken1.getAccessToken());
                    assertEquals(sessionToken1.getAccessToken(), expiredAccessToken);
                    assertEquals(response.getErrors(), "Expired");
                }
        );
    }

    @Test
    void testTokenInvalidAccessToResourcesInvalid() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("kupi");
        request.setLastName("pancongid");
        request.setUsername("kupipancongid");
        request.setEmail("idkupipancong@gmail.com");
        request.setPassword("secret");
        request.setPasswordConfirmation("secret");
        registerUser(request);

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("idkupipancong@gmail.com");
        loginRequest.setPassword("kupipancongid");

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-ACCESS-TOKEN", "wrong-token");

        mockMvc.perform(
                get("/api/auth/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(
                result -> {
                    WebResponse<UserResponse> response= objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<UserResponse>>() {
                    });
                }
        );
    }

    @Test
    void testTokenInvalidLogoutFailed() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("kupi");
        request.setLastName("pancongid");
        request.setUsername("kupipancongid");
        request.setEmail("idkupipancong@gmail.com");
        request.setPassword("secret");
        request.setPasswordConfirmation("secret");
        registerUser(request);

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("idkupipancong@gmail.com");
        loginRequest.setPassword("secret");

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-ACCESS-TOKEN", "wrong-token");

        mockMvc.perform(
                delete("/api/auth")
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(
                result -> {
                    WebResponse<UserResponse> response= objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<UserResponse>>() {
                    });
                }
        );
    }

    @Test
    void testTokenExpiredLogoutFailed() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("kupi");
        request.setLastName("pancongid");
        request.setUsername("kupipancongid");
        request.setEmail("idkupipancong@gmail.com");
        request.setPassword("secret");
        request.setPasswordConfirmation("secret");
        registerUser(request);

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("idkupipancong@gmail.com");
        loginRequest.setPassword("secret");

        TokenResponse token = login(loginRequest);

        Optional<SessionToken> optionalSessionToken = sessionTokenRepository.findSessionTokenByAccessToken(token.getAccessToken());
        SessionToken sessionToken = optionalSessionToken.get();

        User user = sessionToken.getUser();

        String sessionTokenId = UUID.randomUUID().toString();
        Date accessTokenIssuedAt = new Date(System.currentTimeMillis());
        Date accessTokenExpiredAt = new Date(System.currentTimeMillis() - 2L);
        Date refreshTokenIssuedAt = accessTokenIssuedAt;
        Date refreshTokenExpiredAt = new Date(System.currentTimeMillis() - 1L);
        sessionToken = userService.generateSessionToken(sessionTokenId, sessionToken.getSession(), user, accessTokenIssuedAt, accessTokenExpiredAt, refreshTokenIssuedAt, refreshTokenExpiredAt);

        sessionTokenRepository.save(sessionToken);

        String expiredAccessToken = sessionToken.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-ACCESS-TOKEN", sessionToken.getAccessToken());

        mockMvc.perform(
                delete("/api/auth")
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(
                result -> {
                    WebResponse<UserResponse> response= objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<UserResponse>>() {
                    });

                    Optional<SessionToken> optionalSessionToken1 = sessionTokenRepository.findSessionTokenByAccessToken(expiredAccessToken);
                    SessionToken sessionToken1 = optionalSessionToken1.get();
                    assertNotNull(sessionToken1.getAccessToken());
                    assertEquals(sessionToken1.getAccessToken(), expiredAccessToken);
                    assertEquals(response.getErrors(), "Expired");
                }
        );
    }

    @Test
    void testLogoutSuccessAccessToResourcesInvalid() throws Exception{
        UserRegisterRequest request = new UserRegisterRequest();
        request.setFirstName("kupi");
        request.setLastName("pancongid");
        request.setUsername("kupipancongid");
        request.setEmail("idkupipancong@gmail.com");
        request.setPassword("secret");
        request.setPasswordConfirmation("secret");
        registerUser(request);

        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("idkupipancong@gmail.com");
        loginRequest.setPassword("secret");

        TokenResponse token = login(loginRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-API-ACCESS-TOKEN", token.getAccessToken());

        mockMvc.perform(
                delete("/api/auth")
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
        ).andExpectAll(
                status().isOk()
        ).andDo(
                result -> {
                    WebResponse<String> response= objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<String>>() {
                    });
                }
        );

        mockMvc.perform(
                get("/api/auth/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(
                result -> {
                    WebResponse<UserResponse> response= objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<WebResponse<UserResponse>>() {
                    });
                    assertEquals("Unauthorized", response.getErrors());
                }
        );
    }

    //TODO: testRefreshTokenValidNewAccessTokenGiven
    //TODO: testRefreshTokenInvalid
    //TODO: testRefreshTokenExpired
}