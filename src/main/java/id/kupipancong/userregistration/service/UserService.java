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
import id.kupipancong.userregistration.model.response.UserResponse;
import id.kupipancong.userregistration.repository.SessionRepository;
import id.kupipancong.userregistration.repository.SessionTokenRepository;
import id.kupipancong.userregistration.repository.UserRepository;
import id.kupipancong.userregistration.repository.UserVerificationTokenRepository;
import id.kupipancong.userregistration.security.BCrypt;
import id.kupipancong.userregistration.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
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

        if (!request.getPassword().equals(request.getPasswordConfirmation())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Password doesnot match.");
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
    public TokenResponse login(UserLoginRequest request) {
        validationService.validate(request);

        User user = userRepository.findByEmailOrUsername(request.getEmail(), request.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login failed. wrong credentials."));

        if (BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            Session session = new Session();
            session.setUser(user);
            session.setLoggedOutAt(null);

            sessionRepository.save(session);

            String sessionTokenId = UUID.randomUUID().toString();
            Date accessTokenIssuedAt = new Date(System.currentTimeMillis());
            Date accessTokenExpiredAt = new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRED_TIMEMILIS);
            Date refreshTokenIssuedAt = accessTokenIssuedAt;
            Date refreshTokenExpiredAt = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRED_TIMEMILIS);
            SessionToken sessionToken = generateSessionToken(sessionTokenId, session, user, accessTokenIssuedAt, accessTokenExpiredAt, refreshTokenIssuedAt, refreshTokenExpiredAt);

            return TokenResponse.builder()
                    .accessToken(sessionToken.getAccessToken())
                    .refreshToken(sessionToken.getRefreshToken())
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login failed. wrong credentials.");
        }
    }

    @Transactional
    public TokenResponse refresh(HttpServletRequest request){
        String refreshToken = request.getHeader("X-API-REFRESH-TOKEN");

        if (refreshToken==null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        if (isTokenExpired(refreshToken)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Expired");
        }

        if (isTokenInvalid(refreshToken)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Optional<SessionToken> optionalSessionToken = sessionTokenRepository.findSessionTokenByRefreshToken(refreshToken);

        if (optionalSessionToken.isEmpty()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        SessionToken sessionToken = optionalSessionToken.get();

        if (sessionToken == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        if (sessionToken.getRefreshTokenUsed()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        if (sessionToken.getLoggedOut()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Optional<Session> optionalSession = sessionRepository.findById(sessionToken.getSession().getId());

        if (optionalSession.isEmpty()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        Session session = optionalSession.get();

        if (session == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        if (session.getLoggedOutAt() != null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        User user = userRepository.findById(session.getUser().getId()).orElseThrow();

        if (user == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        sessionToken.setRefreshTokenUsed(true);
        sessionTokenRepository.save(sessionToken);

        String sessionTokenId = UUID.randomUUID().toString();
        Date accessTokenIssuedAt = new Date(System.currentTimeMillis());
        Date accessTokenExpiredAt = new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRED_TIMEMILIS);
        Date refreshTokenIssuedAt = accessTokenIssuedAt;
        Date refreshTokenExpiredAt = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRED_TIMEMILIS);
        SessionToken refreshedSessionToken = generateSessionToken(sessionTokenId, session, user, accessTokenIssuedAt, accessTokenExpiredAt, refreshTokenIssuedAt, refreshTokenExpiredAt);

        return TokenResponse.builder()
                .accessToken(refreshedSessionToken.getAccessToken())
                .refreshToken(refreshedSessionToken.getRefreshToken())
                .build();
    }

    public Boolean isTokenInvalid(String accessToken){
        return !jwtUtil.isTokenValid(accessToken);
    }

    public Boolean isTokenExpired(String accessToken){
        return jwtUtil.isTokenExpired(accessToken);
    }

    public User getUserByAccessToken(String accessToken){
        try {
            SessionToken sessionToken = sessionTokenRepository.findSessionTokenByAccessToken(accessToken).orElseThrow();
            Session session = sessionRepository.findById(sessionToken.getSession().getId()).orElseThrow();
            User user = userRepository.findById(session.getUser().getId()).orElseThrow();

            if (session.getLoggedOutAt() != null){
                return null;
            }

            return user;

        }catch (Exception e){
            return null;
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
        sessionToken.setLoggedOut(Boolean.FALSE);
        sessionTokenRepository.save(sessionToken);

        return sessionToken;
    }

    @Transactional
    public void logout(HttpServletRequest request){
        String accessToken = request.getHeader("X-API-ACCESS-TOKEN");

        User user = getUserByAccessToken(accessToken);

        if (user == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        SessionToken sessionToken = sessionTokenRepository.findSessionTokenByAccessToken(accessToken).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized")
        );

        Session session = sessionRepository.findById(sessionToken.getSession().getId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized")
        );

        sessionTokenRepository.updateLoggedOutTrueBySession(session);

        session.setLoggedOutAt(LocalDateTime.now());

        sessionRepository.save(session);
    }

    public UserResponse toUserResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .userType(user.getUserType())
                .emailVerifiedAt(user.getEmailVerifiedAt())
                .build();
    }
}
