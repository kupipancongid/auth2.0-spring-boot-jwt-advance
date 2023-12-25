package id.kupipancong.userregistration.controller;

import id.kupipancong.userregistration.model.response.WebResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {
    @GetMapping(
            path = {"/api", "/"},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> index(HttpServletRequest request){
        return WebResponse.<String>builder()
                .data("Hello, guest")
                .build();
    }
}
