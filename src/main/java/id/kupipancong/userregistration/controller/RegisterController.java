package id.kupipancong.userregistration.controller;

import id.kupipancong.userregistration.model.request.UserRegisterRequest;
import id.kupipancong.userregistration.model.response.WebResponse;
import id.kupipancong.userregistration.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegisterController {

    @Autowired
    UserService userService;

    @PostMapping(
            path = "/api/register",
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
}
