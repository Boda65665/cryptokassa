package org.example.payservice.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "transactions_pending")
@AllArgsConstructor
@Data
@ToString
@NoArgsConstructor
public class Transaction {
    @Column(name = "token")
    private String token;
    @Column(name = "to_address")
    private String to;
    @Column(name = "from_address")
    private String from;
    @Column(name = "value")
    private Double value;
    @Column(name = "chain")
    private String chain;
    @Column(name = "hash")
    private String hash;
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "client_id")
    private String clientId;

    public Transaction(String to, String from, String token, Double value, String chain, String hash, String clientId) {
        this.to = to;
        this.from = from;
        this.token = token;
        this.chain = chain;
        this.value = value;
        this.hash = hash;
        this.clientId = clientId;
    }


}
