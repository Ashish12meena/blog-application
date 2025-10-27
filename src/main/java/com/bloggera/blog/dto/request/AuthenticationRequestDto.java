package com.bloggera.blog.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuthenticationRequestDto {

    @NotNull
    @Min(3)
    private String username;

    @NotNull
    @Min(6)
    private String password;
}
