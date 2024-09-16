package org.example.payservice.HttpRequest;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class RequestBody {
    private String type;
    private String idClient;
    private double usdt;
}
