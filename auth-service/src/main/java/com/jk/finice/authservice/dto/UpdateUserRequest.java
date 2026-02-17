package com.jk.finice.authservice.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserRequest {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username can only contain letters, numbers, underscores, and hyphens")
    private String username;

    @Size(min = 1, max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(min = 1, max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format (E.164)")
    private String phoneNumber;

    @AssertTrue(message = "At least one field must be provided for update")
    public boolean isAtLeastOneFieldProvided() {
        return username != null || firstName != null ||
                lastName != null || phoneNumber != null;
    }
}
