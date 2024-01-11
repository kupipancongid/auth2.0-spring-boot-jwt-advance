package id.kupipancong.userregistration.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginRequest {
    @NotBlank(message = "username or email must not be blank")
    String usernameOrEmail;
    @NotBlank(message = "password must not be blank")
    @Size(max = 32)
    String password;
}
