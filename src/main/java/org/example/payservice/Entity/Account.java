package org.example.payservice.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Table(name = "accounts")
@Entity()
public class Account {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "address")
    private String address;
    @Column(name = "secret_phrase")
    private String privateKey;

    public Account(String address, String privateKey) {
        this.address = address;
        this.privateKey = privateKey;
    }
}
