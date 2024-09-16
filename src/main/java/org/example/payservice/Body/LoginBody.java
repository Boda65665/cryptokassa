package org.example.payservice.Body;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
public class LoginBody {
    @NotNull("cannot password be null")
    private String password;
    @NotNull("cannot email be null")
    private String email;
}
