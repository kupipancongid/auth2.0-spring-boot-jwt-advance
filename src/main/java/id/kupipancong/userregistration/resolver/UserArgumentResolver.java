package id.kupipancong.userregistration.resolver;

import id.kupipancong.userregistration.entity.User;
import id.kupipancong.userregistration.repository.SessionRepository;
import id.kupipancong.userregistration.repository.SessionTokenRepository;
import id.kupipancong.userregistration.repository.UserRepository;
import id.kupipancong.userregistration.service.UserService;
import id.kupipancong.userregistration.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;


@Slf4j
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UserService userService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return User.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest servletRequest = (HttpServletRequest) webRequest.getNativeRequest();
        String accessToken = servletRequest.getHeader("X-API-ACCESS-TOKEN");

        if (accessToken==null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        if (userService.isTokenExpired(accessToken)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Expired");
        }

        if (userService.isTokenInvalid(accessToken)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        User user = userService.getUserByAccessToken(accessToken);

        if (user == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        return user;
    }

}
