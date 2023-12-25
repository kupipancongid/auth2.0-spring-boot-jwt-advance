package id.kupipancong.userregistration.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum UserType{
    Admin("Administrator"),
    User("User");
    private final String description;
}