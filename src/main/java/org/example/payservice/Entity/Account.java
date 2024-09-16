package org.example.payservice.Entity;


import jakarta.persistence.*;
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
    @Column(name = "private_key")
    private String privateKey;
    @Column(name = "is_busy")
    private Boolean isBusy;

    public Account(String address, String privateKey, Boolean isBusy) {
        this.address = address;
        this.privateKey = privateKey;
        this.isBusy = isBusy;
    }

    public Account(String address, String privateKey) {
        this.address = address;
        this.privateKey = privateKey;
    }
}
