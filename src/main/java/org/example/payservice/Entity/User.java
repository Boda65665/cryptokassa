package org.example.payservice.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pay_service_users")
@Data
@NoArgsConstructor
public class User {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "post_back_url")
    private String postBackUrl;
    @Column(name = "email")
    private String email;
    @Column(name = "password")
    private String password;

    public User(String postBackUrl) {
        this.postBackUrl = postBackUrl;
    }
}
