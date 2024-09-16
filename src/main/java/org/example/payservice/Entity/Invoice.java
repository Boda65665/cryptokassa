package org.example.payservice.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "invoices")
@Entity()
public class Invoice {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "to_address")
    private String address;
    @Column(name = "id_client")
    private String idClient;
    @Column(name = "time_create")
    private LocalDateTime localDateTime;
}
