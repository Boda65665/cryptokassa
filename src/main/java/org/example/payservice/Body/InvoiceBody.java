package org.example.payservice.Body;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InvoiceBody {
    @NotNull(message = "idClient cannot be null")
    private String idClient;
}
