package id.kupipancong.userregistration.controller;

import id.kupipancong.userregistration.model.request.UserLoginRequest;
import id.kupipancong.userregistration.model.request.UserRegisterRequest;
import id.kupipancong.userregistration.model.response.TokenResponse;
import id.kupipancong.userregistration.model.response.WebResponse;
import id.kupipancong.userregistration.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserAuthenticationController {
    @Autowired
    private UserService userService;

    @PostMapping(
            path = "/api/users",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> register(
            @RequestBody UserRegisterRequest request
    ){
        userService.userRegister(request);
        return WebResponse.<String>builder()
                .data("OK")
                .build();
    }

    @PostMapping(
            path = "/api/auth",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<TokenResponse> login(@RequestBody UserLoginRequest request,
                                            HttpServletResponse response
    ){
        TokenResponse tokenResponse = userService.login(request,response);

        return WebResponse.<TokenResponse>builder()
                .data(tokenResponse)
                .build();
    }

    @GetMapping(
            path = "/api/auth",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> userVerification(@RequestParam(value = "verification-token", required = true) String verificationToken){

        userService.verifyUser(verificationToken);
        return WebResponse.<String>builder()
                .data("OK")
                .build();
    }
}
