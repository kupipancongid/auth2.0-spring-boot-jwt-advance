package id.kupipancong.userregistration.service;

import id.kupipancong.userregistration.entity.User;
import id.kupipancong.userregistration.enums.UserType;
import id.kupipancong.userregistration.model.request.UserRegisterRequest;
import id.kupipancong.userregistration.repository.UserRepository;
import id.kupipancong.userregistration.security.BCrypt;
import id.kupipancong.userregistration.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    ValidationService validationService;

    public static final long ACCESS_TOKEN_EXPIRED_TIMEMILIS = 3_600_000L;
    public static final long REFRESH_TOKEN_EXPIRED_TIMEMILIS = 10_000_000L;

    @Transactional
    public void userRegister(UserRegisterRequest request){
        validationService.validate(request);

        if (!userRepository.existsByEmail(request.getEmail())){
            if (!request.getPassword().equals(request.getPasswordConfirmation())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password do not match");
            }
            User user = new User();
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setUserType(UserType.User);
            user.setEmail(request.getEmail());
            user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
            userRepository.save(user);
        }else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already taken.");
        }
    }


}
