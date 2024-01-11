package id.kupipancong.userregistration.service;

import id.kupipancong.userregistration.entity.Session;
import id.kupipancong.userregistration.entity.SessionToken;
import id.kupipancong.userregistration.entity.User;
import id.kupipancong.userregistration.entity.UserVerificationToken;
import id.kupipancong.userregistration.enums.UserType;
import id.kupipancong.userregistration.format.JwtToken;
import id.kupipancong.userregistration.model.request.UserLoginRequest;
import id.kupipancong.userregistration.model.request.UserRegisterRequest;
import id.kupipancong.userregistration.model.response.TokenResponse;
import id.kupipancong.userregistration.repository.SessionRepository;
import id.kupipancong.userregistration.repository.SessionTokenRepository;
import id.kupipancong.userregistration.repository.UserRepository;
import id.kupipancong.userregistration.repository.UserVerificationTokenRepository;
import id.kupipancong.userregistration.security.BCrypt;
import id.kupipancong.userregistration.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    UserVerificationTokenRepository userVerificationTokenRepository;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    SessionTokenRepository sessionTokenRepository;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    ValidationService validationService;

    public static final long ACCESS_TOKEN_EXPIRED_TIMEMILIS = 3_600_000L;
    public static final long REFRESH_TOKEN_EXPIRED_TIMEMILIS = 10_000_000L;

    @Transactional
    public void userRegister(UserRegisterRequest request) {
        validationService.validate(request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already taken.");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken.");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setUserType(UserType.User);
        user.setEmail(request.getEmail());
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setReferral(request.getReferral());
        userRepository.save(user);

        UserVerificationToken userVerificationToken = new UserVerificationToken();
        userVerificationToken.setToken(UUID.randomUUID().toString());
        userVerificationToken.setUser(user);
        userVerificationTokenRepository.save(userVerificationToken);
    }

    @Transactional
    public void verifyUser(String token){
        UserVerificationToken userVerificationToken = userVerificationTokenRepository.findUserVerificationTokenByToken(token).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token not found")
        );

        if (userVerificationToken.getTokenTakenAt() != null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User verified");
        }

        User user = userRepository.findById(userVerificationToken.getUser().getId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token")
        );

        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        userVerificationToken.setTokenTakenAt(LocalDateTime.now());
        userVerificationTokenRepository.save(userVerificationToken);
    }

    @Transactional
    public TokenResponse login(UserLoginRequest request, HttpServletResponse response) {
        validationService.validate(request);
        removeCookie(response, "access_token");
        removeCookie(response, "refresh_token");

        User user = userRepository.findByEmailOrUsername(request.getUsernameOrEmail(), request.getUsernameOrEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login failed. wrong credentials."));

        if (BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            Session session = new Session();
            session.setUser(user);
            session.setLoggedOutAt(null);
            sessionRepository.save(session);

            String sessionTokenHistoryId = UUID.randomUUID().toString();
            Date accessTokenIssuedAt = new Date(System.currentTimeMillis());
            Date accessTokenExpiredAt = new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRED_TIMEMILIS);
            Date refreshTokenIssuedAt = accessTokenIssuedAt;
            Date refreshTokenExpiredAt = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRED_TIMEMILIS);
            SessionToken auth = generateSessionToken(sessionTokenHistoryId, session, user, accessTokenIssuedAt, accessTokenExpiredAt, refreshTokenIssuedAt, refreshTokenExpiredAt);

            setCookie(response, "access_token", auth.getAccessToken(), 1000 * 60 * 60 * 1);
            setCookie(response, "refresh_token", auth.getRefreshToken(), 1000 * 60 * 60 * 5);

            return TokenResponse.builder()
                    .accessToken(auth.getAccessToken())
                    .refreshToken(auth.getRefreshToken())
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login failed. wrong credentials.");
        }
    }

    @Transactional
    public SessionToken generateSessionToken(
            String authId,
            Session session,
            User user,
            Date accessTokenIssuedAt,
            Date accessTokenExpiredAt,
            Date refreshTokenIssuedAt,
            Date refreshTokenExpiredAt
    ) {
        JwtToken jwtAccessToken = new JwtToken(session.getId(), authId, accessTokenIssuedAt, accessTokenExpiredAt);
        String accessToken = jwtUtil.generateTokenString(jwtAccessToken);
        JwtToken jwtRefreshToken = new JwtToken(session.getId(), authId, refreshTokenIssuedAt, refreshTokenExpiredAt);
        String refreshToken = jwtUtil.generateTokenString(jwtRefreshToken);

        SessionToken sessionToken = new SessionToken();
        sessionToken.setId(authId);
        sessionToken.setUser(user);
        sessionToken.setSession(session);
        sessionToken.setAccessToken(accessToken);
        sessionToken.setRefreshToken(refreshToken);
        sessionToken.setRefreshTokenUsed(Boolean.FALSE);
        sessionTokenRepository.save(sessionToken);

        return sessionToken;
    }

    private void setCookie(HttpServletResponse response, String key, String value, Integer maxAge) {
        Cookie tokenCookie = new Cookie(key, value);
        tokenCookie.setMaxAge(maxAge);
        tokenCookie.setSecure(false);
        tokenCookie.setHttpOnly(true);
        tokenCookie.setPath("/");
        response.addCookie(tokenCookie);
    }

    private void removeCookie(HttpServletResponse response, String key) {
        Cookie tokenCookie = new Cookie(key, key);
        tokenCookie.setMaxAge(0);
        response.addCookie(tokenCookie);
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
